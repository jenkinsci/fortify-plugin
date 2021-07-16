package com.fortify.plugin.jenkins;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.Secret;
import net.sf.json.JSONObject;

public class ProxyConfig extends AbstractDescribableImpl<ProxyConfig> {
	private static final Logger LOGGER = Logger.getLogger(FortifyPlugin.class.getName());

	private String proxyUrl;
	private Secret proxyUsername;
	private Secret proxyPassword;

	@DataBoundConstructor
	public ProxyConfig(String proxyUrl, Secret proxyUsername, Secret proxyPassword) {
		try {
			proxyUrl = proxyUrl == null ? null : proxyUrl.trim();
			checkProxyUrlValue(proxyUrl);
			this.proxyUrl = proxyUrl;
		} catch (FortifyException e) {
			LOGGER.log(Level.WARNING, "Fortify proxy server configuration error: " + e.getMessage());
			this.proxyUrl = null;
		}
		this.proxyUsername = proxyUsername;
		this.proxyPassword = proxyPassword;
	}

	public String getProxyUrl() {
		return proxyUrl;
	}

	public Secret getProxyUsername() {
		return proxyUsername;
	}

	String getProxyUsernameValueOrNull() {
		return proxyUsername == null ? null : proxyUsername.getEncryptedValue();
	}

	public Secret getProxyPassword() {
		return proxyPassword;
	}

	String getProxyPasswordValueOrNull() {
		return proxyPassword == null ? null : proxyPassword.getEncryptedValue();
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
	}

	public FormValidation doCheckProxyUrl(@QueryParameter String value) {
		try {
			checkProxyUrlValue(value.trim());
		} catch (FortifyException e) {
			return FormValidation.warning(e.getMessage());
		}
		return FormValidation.ok();
	}

	private void checkProxyUrlValue(String proxyUrl) throws FortifyException {
		if (StringUtils.isNotBlank(proxyUrl)) {
			String[] splits = proxyUrl.split(":");
			if (splits.length > 2) {
				throw new FortifyException(
						new Message(Message.ERROR, "Invalid proxy url.  Format is <hostname>[:<port>]"));
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

	@POST
	public FormValidation doCheckProxyUsername(@QueryParameter Secret value) {
		try {
			checkProxyUsernameValue(value);
		} catch (FortifyException e) {
			return FormValidation.warning(e.getMessage());
		}
		return FormValidation.ok();
	}

	@POST
	public FormValidation doCheckProxyPassword(@QueryParameter Secret value) {
		try {
			checkProxyPasswordValue(value);
		} catch (FortifyException e) {
			return FormValidation.warning(e.getMessage());
		}
		return FormValidation.ok();
	}

	private void checkProxyUsernameValue(Secret proxyUsername) throws FortifyException {
		// accept anything
	}

	private void checkProxyPasswordValue(Secret proxyPassword) throws FortifyException {
		// accept anything
	}
}
