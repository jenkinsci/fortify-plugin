package com.fortify.plugin.jenkins;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;

import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

public class ProxyConfig extends AbstractDescribableImpl<ProxyConfig> {
	private static final Logger LOGGER = Logger.getLogger(FortifyPlugin.class.getName());

	private String proxyUrl;
	private String proxyCredentialsId;

	/** SSC Authentication Token */
	/** @deprecated use {@link #proxyCredentialsId} */
	private Secret proxyUsername;
	/** SSC Authentication Token */
	/** @deprecated use {@link #proxyCredentialsId} */
	private Secret proxyPassword;

	private String proxyHost;
	private int proxyPort;

	@DataBoundConstructor
	public ProxyConfig(String proxyUrl, Secret proxyUsername, Secret proxyPassword) {
		try {
			proxyUrl = proxyUrl == null ? null : proxyUrl.trim();
			checkProxyUrlValue(proxyUrl);
			this.proxyUrl = proxyUrl;
			setProxyHostAndPort(proxyUrl);
		} catch (FortifyException e) {
			LOGGER.log(Level.WARNING, "Fortify proxy server configuration error: " + e.getMessage());
			this.proxyUrl = null;
		}
		setProxyUsernameAndPassword(proxyUsername, proxyPassword);
		trySettingJenkinsProxy(proxyHost, proxyPort, proxyUsername, proxyPassword); //during initial settings migration
	}

	private void trySettingJenkinsProxy(String proxyHost, int proxyPort, Secret proxyUsername, Secret proxyPassword) {
		Jenkins jenkins = Jenkins.get();
		if (jenkins != null) {
			ProxyConfiguration proxy = jenkins.getProxy();
			if (proxy == null || StringUtils.isBlank(proxy.getName())) {
				proxy = new ProxyConfiguration(proxyHost, proxyPort, Secret.toString(proxyUsername), Secret.toString(proxyPassword));
				jenkins.setProxy(proxy);
				try {
					proxy.save();
				} catch (IOException e) {
				}
			}
		}
	}

	private void setProxyUsernameAndPassword(Secret proxyUsername, Secret proxyPassword) {
		String id = "fortify-proxy-id";
		final Credentials fortifyToken = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
				id, "fortify plugin migration generated proxy credentials",
				Secret.toString(proxyUsername), Secret.toString(proxyPassword));
		try {
			CredentialsProvider.lookupStores(Jenkins.get()).iterator().next().addCredentials(Domain.global(), fortifyToken);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Fortify proxy credentials registration error: " + e.getMessage());
		}
		this.proxyCredentialsId = id;
	}

	public ProxyConfig(String host, int port, String proxyUsername, Secret proxyPassword) {
		try {
			this.proxyHost = host;
			this.proxyPort = port;
			String url = host == null ? null : host.trim() + ":" + (port >= 0 && port < 65536 ?  String.valueOf(port) : "80");
			checkProxyUrlValue(url);
			this.proxyUrl = url;
		} catch (FortifyException e) {
			LOGGER.log(Level.WARNING, "Fortify proxy server configuration error: " + e.getMessage());
			this.proxyUrl = null;
		}
		this.proxyUsername = Secret.fromString(proxyUsername);
		this.proxyPassword = proxyPassword;
	}

	public ProxyConfig(String proxyUrl, String proxyCredentialsId) {
		try {
			proxyUrl = proxyUrl == null ? null : proxyUrl.trim();
			checkProxyUrlValue(proxyUrl);
			this.proxyUrl = proxyUrl;
			setProxyHostAndPort(proxyUrl);
		} catch (FortifyException e) {
			LOGGER.log(Level.WARNING, "Fortify proxy server configuration error: " + e.getMessage());
			this.proxyUrl = null;
		}
		this.proxyCredentialsId = proxyCredentialsId;
	}

	// for backwards compatibility
	private Object readResolve() {
		if (this.proxyUsername != null || this.proxyPassword != null) {
			this.setProxyUsernameAndPassword(this.proxyUsername, this.proxyPassword);
		}
		return this;
	}

	private void setProxyHostAndPort(String proxyUrl) {
		if (proxyUrl == null) {
			return;
		}
		String[] proxyUrlSplit = proxyUrl.split(":");
		this.proxyHost = proxyUrlSplit[0];
		this.proxyPort = 80;
		if (proxyUrlSplit.length > 1) {
			try {
				this.proxyPort = Integer.parseInt(proxyUrlSplit[1]);
			} catch (NumberFormatException nfe) {
			}
		}
	}

	public String getProxyUrl() {
		return proxyUrl;
	}

	public String getProxyCredentialsId() {
		return proxyCredentialsId;
	}

	/** @deprecated use {@link #getProxyCredentialsId()} */
	public Secret getProxyUsername() {
		return proxyUsername;
	}

	String getProxyUsernameValueOrNull() {
		UsernamePasswordCredentials c = getCredentialsFrom(getProxyCredentialsId(), getProxyUrl());
		return c == null ? null : c.getUsername();
	}

	/** @deprecated use {@link #getProxyCredentialsId()} */
	public Secret getProxyPassword() {
		return proxyPassword;
	}

	String getProxyPasswordValueOrNull() {
		UsernamePasswordCredentials c = getCredentialsFrom(getProxyCredentialsId(), getProxyUrl());
		return c == null ? null : Secret.toString(c.getPassword());
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	private static UsernamePasswordCredentials getCredentialsFrom(String tokenId, String url) throws FortifyException {
		UsernamePasswordCredentials c = StringUtils.isBlank(tokenId) ? null : CredentialsMatchers.firstOrNull(CredentialsProvider
				.lookupCredentials(UsernamePasswordCredentials.class, Jenkins.get(), ACL.SYSTEM, 
						StringUtils.isBlank(url) ? Collections.emptyList() : URIRequirementBuilder.fromUri(url).build()),
						CredentialsMatchers.withId(tokenId));
		return c;
	}

	private static void checkProxyUrlValue(String proxyUrl) throws FortifyException {
		if (StringUtils.isNotBlank(proxyUrl)) {
			String[] splits = proxyUrl.split(":");
			if (splits.length > 2) {
				throw new FortifyException(new Message(Message.ERROR, "Invalid proxy url.  Format is <hostname>[:<port>]"));
			}
			Pattern hostPattern = Pattern.compile("([\\w\\-]+\\.)*[\\w\\-]+");
			Matcher hostMatcher = hostPattern.matcher(splits[0]);
			if (!hostMatcher.matches()) {
				throw new FortifyException(new Message(Message.ERROR, "Invalid proxy host"));
			}
			if (splits.length == 2) {
				try {
					Integer.parseInt(splits[1]);
				} catch (NumberFormatException nfe) {
					throw new FortifyException(new Message(Message.ERROR, "Invalid proxy port"));
				}
			}
		}
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<ProxyConfig> {
		@Override
		public String getDisplayName() {
			return "Use proxy";
		}
		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			return super.configure(req, json);
		}

		public ListBoxModel doFillProxyCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String proxyCredentialsId, @QueryParameter String proxyUrl) {
			StandardListBoxModel result = new StandardListBoxModel();
			if (item == null) {
				if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
					return result.includeCurrentValue(proxyCredentialsId);
				}
			} else {
				if (!item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
					return result.includeCurrentValue(proxyCredentialsId);
				}
			}
			return result.includeEmptyValue().includeCurrentValue(proxyCredentialsId)
					.includeMatchingAs(ACL.SYSTEM, item, StandardUsernamePasswordCredentials.class, 
							StringUtils.isBlank(proxyUrl) ? Collections.emptyList() : URIRequirementBuilder.fromUri(proxyUrl).build(), 
							StringUtils.isBlank(proxyCredentialsId) ? CredentialsMatchers.always() : CredentialsMatchers.withId(proxyCredentialsId));
		}

		public FormValidation doCheckProxyCredentialsId(@QueryParameter String value, @QueryParameter String proxyUrl) {
			if (value != null && value.trim().length() > 0) {
				try {
					UsernamePasswordCredentials c = getCredentialsFrom(value, proxyUrl);
					if (c == null) {
						return FormValidation.warning("Cannot get credentials for " + value);
					}
				} catch (FortifyException e) {
					return FormValidation.warning(e.getMessage());
				}
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckProxyUrl(@QueryParameter String value) {
			try {
				checkProxyUrlValue(value.trim());
			} catch (FortifyException e) {
				return FormValidation.warning(e.getMessage());
			}
			return FormValidation.ok();
		}
	}
}
