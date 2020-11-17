/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates.
 * 
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://opensource.org/licenses/MIT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.fortify.plugin.jenkins.steps;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.fortify.plugin.jenkins.Messages;
import com.google.common.collect.ImmutableSet;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;

public class FortifyUpdate extends FortifyStep {
	private String updateServerURL;
	private String locale;
	private transient String proxyURL;
	private transient String proxyUsername;
	private transient String proxyPassword;
	private transient boolean useProxy;

	@DataBoundConstructor
	public FortifyUpdate(String updateServerURL, String locale) {
		this.updateServerURL = updateServerURL;
		this.locale = locale;
	}

	@Deprecated
	public FortifyUpdate(String updateServerURL, String proxyURL, String proxyUsername, String proxyPassword,
			boolean useProxy) {
		this.updateServerURL = updateServerURL;
		this.proxyURL = proxyURL;
		this.proxyUsername = proxyUsername;
		this.proxyPassword = proxyPassword;
		this.useProxy = useProxy;
	}

	public String getUpdateServerURL() {
		return updateServerURL;
	}

	public String getLocale() { return locale; }

	@Deprecated
	public String getProxyURL() {
		return proxyURL;
	}

	@Deprecated
	public String getProxyUsername() {
		return proxyUsername;
	}

	@Deprecated
	public String getProxyPassword() {
		return proxyPassword;
	}

	@Deprecated
	public boolean getUseProxy() {
		return useProxy;
	}

	@DataBoundSetter
	public void setUpdateServerURL(String updateServerURL) {
		this.updateServerURL = updateServerURL;
	}

	@DataBoundSetter
	public void setLocale(String locale) { this.locale = locale; }

	@DataBoundSetter @Deprecated
	public void setProxyURL(String proxyURL) {
		this.proxyURL = proxyURL;
	}

	@DataBoundSetter @Deprecated
	public void setProxyUsername(String proxyUsername) {
		this.proxyUsername = proxyUsername;
	}

	@DataBoundSetter @Deprecated
	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	@DataBoundSetter @Deprecated
	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}

	public String getResolvedUpdateServerURL(TaskListener listener) {
		return resolve(getUpdateServerURL(), listener);
	}

	public String getResolvedLocale(TaskListener listener) {
		return resolve(getLocale(), listener);
	}

	@Deprecated
	public String getResolvedUpdateProxyURL(TaskListener listener) {
		return resolve(getProxyURL(), listener);
	}

	@Deprecated
	public String getResolvedUpdateProxyUsername(TaskListener listener) {
		return resolve(getProxyUsername(), listener);
	}

	@Deprecated
	public String getResolvedUpdateProxyPassword(TaskListener listener) {
		return resolve(getProxyPassword(), listener);
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new Execution(this, context);
	}

	@Override
	public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
		PrintStream log = listener.getLogger();
		log.println("Fortify Jenkins plugin v " + VERSION);
		log.println("Launching fortifyupdate command");
		ArrayList<String> args = new ArrayList<String>();
		String fortifyUpdate = getFortifyUpdateExecutable(build, workspace, launcher, listener);
		args.add(fortifyUpdate);
		String updateServerUrl = getResolvedUpdateServerURL(listener);
		if (!"".equals(updateServerUrl)) {
			try {
				URL url = new URL(updateServerUrl);
				if ("http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol())) {
					args.add("-url");
					args.add(updateServerUrl);
				} else {
					log.println(Messages.ForitfyUpdate_URL_Protocol_Warning(updateServerUrl));
				}
			} catch (MalformedURLException mue) {
				log.println(Messages.FortifyUpdate_URL_Invalid(updateServerUrl));
			}
		}

		String localeStr = getResolvedLocale(listener);
		if (!"".equals(localeStr)) {
			args.add("-locale");
			args.add(localeStr);
		}

		EnvVars vars = build.getEnvironment(listener);
		ProcStarter ps = launcher.decorateByEnv(vars).launch().pwd(workspace).cmds(args).envs(vars)
				.stdout(listener.getLogger()).stderr(listener.getLogger());
		int exitcode = ps.join();
		log.println(Messages.FortifyUpdate_Result(exitcode));
		if (exitcode != 0) {
			build.setResult(Result.FAILURE);
			throw new AbortException(Messages.FortifyUpdate_Error());
		}

	}

	private String getFortifyUpdateExecutable(Run<?, ?> build, FilePath workspace, Launcher launcher,
			TaskListener listener) throws InterruptedException, IOException {
		return getExecutable("fortifyupdate" + (launcher.isUnix() ? "" : ".cmd"), build, workspace,
				listener, "FORTIFY_HOME");
	}

	@Extension
	public static class DescriptorImpl extends StepDescriptor {
		private static final String DEFAULT_URL = "https://update.fortify.com";

		public String getDefaultURL() {
			return DEFAULT_URL;
		}

		@Override
		public String getFunctionName() {
			return "fortifyUpdate";
		}

		@Override
		public String getDisplayName() {
			return Messages.FortifyUpdate_DisplayName();
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
		}

		public ListBoxModel doFillLocaleItems(String value) {
			ListBoxModel items = new ListBoxModel();
			items.add("English", "en");
			items.add("Chinese Simplified", "zh_CN");
			items.add("Chinese Traditional", "zh_TW");
			items.add("Japanese", "ja");
			items.add("Korean", "ko");
			items.add("Portuguese (Brazil)", "pt_BR");
			items.add("Spanish", "es");

			if ((null == value) || (0 == value.length())) {
				items.get(0).selected = true; // default to en_US
			}

			return items;
		}
	}

	private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
		private transient FortifyUpdate fu;

		protected Execution(FortifyUpdate fu, StepContext context) {
			super(context);
			this.fu = fu;
		}

		@Override
		protected Void run() throws Exception {
			getContext().get(TaskListener.class).getLogger().println("Running FortifyUpdate step");
			if (!getContext().get(FilePath.class).exists()) {
				getContext().get(FilePath.class).mkdirs();
			}
			fu.perform(getContext().get(Run.class), getContext().get(FilePath.class), getContext().get(Launcher.class),
					getContext().get(TaskListener.class));

			return null;
		}

		private static final long serialVersionUID = 1L;

	}

	public static class Builder {
		private String updateServerURL;
		private String locale;

		public Builder() {
		}

		public Builder updateServerURL(String updateServerURL) {
			if (StringUtils.isNotBlank(updateServerURL)) {
				this.updateServerURL = updateServerURL;
			}
			return this;
		}

		public Builder locale(String localeString) {
			if (StringUtils.isNotBlank(localeString)) {
				this.locale = localeString;
			}
			return this;
		}

		public FortifyUpdate build() {
			return new FortifyUpdate(updateServerURL, locale);
		}

	}

}
