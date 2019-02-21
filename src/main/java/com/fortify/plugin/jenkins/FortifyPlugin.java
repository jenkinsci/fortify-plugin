/*******************************************************************************
 * (c) Copyright 2019 Micro Focus or one of its affiliates. 
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
package com.fortify.plugin.jenkins;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

import com.fortify.plugin.jenkins.bean.ProjectTemplateBean;
import com.fortify.plugin.jenkins.fortifyclient.FortifyClient;
import com.fortify.plugin.jenkins.fortifyclient.FortifyClient.NoReturn;
import com.fortify.plugin.jenkins.steps.FortifyClean;
import com.fortify.plugin.jenkins.steps.FortifyScan;
import com.fortify.plugin.jenkins.steps.FortifyTranslate;
import com.fortify.plugin.jenkins.steps.FortifyUpdate;
import com.fortify.plugin.jenkins.steps.FortifyUpload;
import com.fortify.plugin.jenkins.steps.types.AdvancedScanType;
import com.fortify.plugin.jenkins.steps.types.DevenvScanType;
import com.fortify.plugin.jenkins.steps.types.DotnetSourceScanType;
import com.fortify.plugin.jenkins.steps.types.GradleScanType;
import com.fortify.plugin.jenkins.steps.types.JavaScanType;
import com.fortify.plugin.jenkins.steps.types.MavenScanType;
import com.fortify.plugin.jenkins.steps.types.MsbuildScanType;
import com.fortify.plugin.jenkins.steps.types.OtherScanType;
import com.fortify.plugin.jenkins.steps.types.ProjectScanType;
import com.fortify.ssc.restclient.ApiException;

import hudson.Extension;
import hudson.Launcher;
import hudson.Plugin;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Fortify Jenkins plugin to work with Fortify Software Security Center and
 * Fortify Static Code Analyzer
 * 
 * <p>
 * Main plugin functionality:
 * <ul>
 * <li>Provide pipeline and other means to launch Fortify Static Code Analysis
 * (SCA) as part of the build</li>
 * <li>Upload the resulting FPR analysis file to Fortify Software Security
 * Center (SSC) server</li>
 * <li>Calculate NVS from the results collected from SSC and plot graph on the
 * project main page</li>
 * <li>Make a build to be UNSTABLE if some critical vulnerabilities are found
 * (or based on other info from SSC)</li>
 * <li>Display detailed list of vulnerabilities collected from SSC and provide
 * remediation links</li>
 * </ul>
 *
 */
public class FortifyPlugin extends Recorder {
	private static String pluginVersion;

	public static String getPluginVersion() {
		if (pluginVersion == null) {
			Plugin plugin = Jenkins.get().getPlugin("fortify");
			if (plugin != null) {
				pluginVersion = plugin.getWrapper().getVersion();
			}
		}
		return pluginVersion;
	}

	private static Object syncObj = new Object();

	public static final int DEFAULT_PAGE_SIZE = 50;

	private UploadSSCBlock uploadSSC;
	private RunTranslationBlock runTranslation;
	private RunScanBlock runScan;
	private UpdateContentBlock updateContent;
	private boolean runSCAClean;
	private String buildId;
	private String scanFile;
	private String maxHeap;
	private String addJVMOptions;

	@DataBoundConstructor
	public FortifyPlugin(String buildId, String scanFile, String maxHeap, String addJVMOptions,
			UpdateContentBlock updateContent, boolean runSCAClean, RunTranslationBlock runTranslation,
			RunScanBlock runScan, UploadSSCBlock uploadSSC) {
		this.buildId = buildId;
		this.scanFile = scanFile;
		this.maxHeap = maxHeap;
		this.addJVMOptions = addJVMOptions;
		this.updateContent = updateContent;
		this.runSCAClean = runSCAClean;
		this.runTranslation = runTranslation;
		this.runScan = runScan;
		this.uploadSSC = uploadSSC;
	}

	public String getBuildId() {
		return buildId;
	}

	public String getScanFile() {
		return scanFile;
	}

	public String getMaxHeap() {
		return maxHeap;
	}

	public String getAddJVMOptions() {
		return addJVMOptions;
	}

	public boolean getUpdateContent() {
		return updateContent != null;
	}

	public boolean getRunTranslation() {
		return runTranslation != null;
	}

	public boolean getRunScan() {
		return runScan != null;
	}

	public boolean getUploadSSC() {
		return uploadSSC != null;
	}

	public String getUpdateServerUrl() {
		return getUpdateContent() ? updateContent.getUpdateServerUrl() : "";
	}

	public boolean getUpdateUseProxy() {
		return getUpdateContent() && updateContent.getUpdateUseProxy();
	}

	public String getUpdateProxyUrl() {
		return getUpdateUseProxy() ? updateContent.getUpdateProxyUrl() : "";
	}

	public String getUpdateProxyUsername() {
		return getUpdateUseProxy() ? updateContent.getUpdateProxyUsername() : "";
	}

	public String getUpdateProxyPassword() {
		return getUpdateUseProxy() ? updateContent.getUpdateProxyPassword() : "";
	}

	public boolean getRunSCAClean() {
		return runSCAClean;
	}

	public String getTranslationType() {
		return getRunTranslation() ? runTranslation.getTranslationType() : "";
	}

	public boolean getIsBasicTranslationType() {
		return getRunTranslation() && runTranslation.isBasicTranslationType();
	}

	public boolean getIsAdvancedTranslationType() {
		return getRunTranslation() && runTranslation.isAdvancedTranslationType();
	}

	public boolean getIsBasicJavaTranslationType() {
		return getRunTranslation() && runTranslation.isBasicJavaTranslationType();
	}

	public boolean getIsBasicDotNetTranslationType() {
		return getRunTranslation() && runTranslation.isBasicDotNetTranslationType();
	}

	public boolean getIsBasicMaven3TranslationType() {
		return getRunTranslation() && runTranslation.isBasicMaven3TranslationType();
	}

	public boolean getIsBasicGradleTranslationType() {
		return getRunTranslation() && runTranslation.isBasicGradleTranslationType();
	}

	public boolean getIsBasicOtherTranslationType() {
		return getRunTranslation() && runTranslation.isBasicOtherTranslationType();
	}

	public String getTranslationJavaVersion() {
		return getRunTranslation() ? runTranslation.getTranslationJavaVersion() : "";
	}

	public String getTranslationJavaClasspath() {
		return getRunTranslation() ? runTranslation.getTranslationClasspath() : "";
	}

	public String getTranslationJavaSourceFiles() {
		return getRunTranslation() ? runTranslation.getTranslationSourceFiles() : "";
	}

	public String getTranslationJavaAddOptions() {
		return getRunTranslation() ? runTranslation.getTranslationAddOptions() : "";
	}

	public String getTranslationExcludeList() {
		return getRunTranslation() ? runTranslation.getTranslationExcludeList() : "";
	}

	public String getTranslationOptions() {
		return getRunTranslation() ? runTranslation.getTranslationOptions() : "";
	}

	public boolean getTranslationDebug() {
		return getRunTranslation() && runTranslation.getTranslationDebug();
	}

	public boolean getTranslationVerbose() {
		return getRunTranslation() && runTranslation.getTranslationVerbose();
	}

	public String getTranslationLogFile() {
		return getRunTranslation() ? runTranslation.getTranslationLogFile() : "";
	}

	public boolean getIsBasicDotNetProjectSolutionScanType() {
		return getRunTranslation() && runTranslation.isBasicDotNetProjectSolutionScanType();
	}

	public boolean getIsBasicDotNetSourceCodeScanType() {
		return getRunTranslation() && runTranslation.isBasicDotNetSourceCodeScanType();
	}

	public boolean getIsBasicDotNetDevenvBuildType() {
		return getRunTranslation() && runTranslation.isBasicDotNetDevenvBuildType();
	}

	public boolean getIsBasicDotNetMSBuildBuildType() {
		return getRunTranslation() && runTranslation.isBasicDotNetMSBuildBuildType();
	}

	public String getDotNetDevenvProjects() {
		return getRunTranslation() ? runTranslation.getDotNetDevenvProjects() : "";
	}

	public String getDotNetDevenvAddOptions() {
		return getRunTranslation() ? runTranslation.getDotNetDevenvAddOptions() : "";
	}

	public String getDotNetMSBuildProjects() {
		return getRunTranslation() ? runTranslation.getDotNetMSBuildProjects() : "";
	}

	public String getDotNetMSBuildAddOptions() {
		return getRunTranslation() ? runTranslation.getDotNetMSBuildAddOptions() : "";
	}

	public String getDotNetSourceCodeFrameworkVersion() {
		return getRunTranslation() ? runTranslation.getDotNetSourceCodeFrameworkVersion() : "";
	}

	public String getDotNetSourceCodeLibdirs() {
		return getRunTranslation() ? runTranslation.getDotNetSourceCodeLibdirs() : "";
	}

	public String getDotNetSourceCodeAddOptions() {
		return getRunTranslation() ? runTranslation.getDotNetSourceCodeAddOptions() : "";
	}

	public String getDotNetSourceCodeSrcFiles() {
		return getRunTranslation() ? runTranslation.getDotNetSourceCodeSrcFiles() : "";
	}

	public String getMaven3Options() {
		return getRunTranslation() ? runTranslation.getMaven3Options() : "";
	}

	public boolean getGradleUseWrapper() {
		return getRunTranslation() && runTranslation.getGradleUseWrapper();
	}

	public String getGradleTasks() {
		return getRunTranslation() ? runTranslation.getGradleTasks() : "";
	}

	public String getGradleOptions() {
		return getRunTranslation() ? runTranslation.getGradleOptions() : "";
	}

	public String getOtherOptions() {
		return getRunTranslation() ? runTranslation.getOtherOptions() : "";
	}

	public String getOtherIncludesList() {
		return getRunTranslation() ? runTranslation.getOtherIncludesList() : "";
	}

	public String getScanCustomRulepacks() {
		return getRunScan() ? runScan.getScanCustomRulepacks() : "";
	}

	public String getScanAddOptions() {
		return getRunScan() ? runScan.getScanAddOptions() : "";
	}

	public boolean getScanDebug() {
		return getRunScan() && runScan.getScanDebug();
	}

	public boolean getScanVerbose() {
		return getRunScan() && runScan.getScanVerbose();
	}

	public String getScanLogFile() {
		return getRunScan() ? runScan.getScanLogFile() : "";
	}

	// these are the original fields for uploading to ssc - don't feel like renaming
	// them...
	public String getFilterSet() {
		return uploadSSC == null ? "" : uploadSSC.getFilterSet();
	}

	public String getSearchCondition() {
		return uploadSSC == null ? "" : uploadSSC.getSearchCondition();
	}

	public String getProjectName() {
		return uploadSSC == null ? "" : uploadSSC.getProjectName();
	}

	public String getProjectVersion() {
		return uploadSSC == null ? "" : uploadSSC.getProjectVersion();
	}

	public String getUploadWaitTime() {
		return uploadSSC == null ? null : uploadSSC.getPollingInterval();
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	/*
	 * https://bugzilla.fortify.swinfra.net/bugzilla/show_bug.cgi?id=49956 It is may
	 * be some bad practice to get current opened Jenkins configuration from that
	 * method, but it is not very easy to get that information from other place. The
	 * main problem is that we should store the build (or project) info between
	 * Jenkins starts. If you know how to correctly get project in Publisher without
	 * some manual configuration saving please change it. We should make Fortify
	 * Assessment action as build action not project.
	 */
	@Override
	public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
		return Collections.emptyList();
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		PrintStream log = listener.getLogger();
		log.println("Fortify Jenkins plugin v " + getPluginVersion());

		if (updateContent != null) {
			FortifyUpdate fu = new FortifyUpdate.Builder().updateServerURL(getUpdateServerUrl())
					.useProxy(getUpdateUseProxy()).proxyURL(getUpdateProxyUrl()).proxyUsername(getUpdateProxyUsername())
					.proxyPassword(getUpdateProxyPassword()).build();
			fu.perform(build, launcher, listener);
		}

		if (runSCAClean) {
			FortifyClean fc = new FortifyClean(getBuildId());
			fc.perform(build, launcher, listener);
		}

		if (runTranslation != null) {
			final ProjectScanType projectScanType = calculateProjectScanType();
			FortifyTranslate ft = new FortifyTranslate(getBuildId(), projectScanType);
			ft.setMaxHeap(getMaxHeap());
			ft.setAddJVMOptions(getAddJVMOptions());
			ft.setDebug(getTranslationDebug());
			ft.setVerbose(getTranslationVerbose());
			ft.setLogFile(getTranslationLogFile());
			ft.setExcludeList(getTranslationExcludeList());

			if (projectScanType instanceof JavaScanType) {
				ft.setJavaVersion(getTranslationJavaVersion());
				ft.setJavaClasspath(getTranslationJavaClasspath());
				ft.setJavaSrcFiles(getTranslationJavaSourceFiles());
				ft.setJavaAddOptions(getTranslationJavaAddOptions());
			} else if (projectScanType instanceof DevenvScanType) {
				ft.setDotnetProject(getDotNetDevenvProjects());
				ft.setDotnetAddOptions(getDotNetDevenvAddOptions());
			} else if (projectScanType instanceof MsbuildScanType) {
				ft.setDotnetProject(getDotNetMSBuildProjects());
				ft.setDotnetAddOptions(getDotNetMSBuildAddOptions());
			} else if (projectScanType instanceof DotnetSourceScanType) {
				ft.setDotnetFrameworkVersion(getDotNetSourceCodeFrameworkVersion());
				ft.setDotnetLibdirs(getDotNetSourceCodeLibdirs());
				ft.setDotnetAddOptions(getDotNetSourceCodeAddOptions());
				ft.setDotnetSrcFiles(getDotNetSourceCodeSrcFiles());
			} else if (projectScanType instanceof MavenScanType) {
				ft.setMavenOptions(getMaven3Options());
			} else if (projectScanType instanceof GradleScanType) {
				ft.setUseWrapper(getGradleUseWrapper());
				ft.setGradleTasks(getGradleTasks());
				ft.setGradleOptions(getGradleOptions());
			} else if (projectScanType instanceof OtherScanType) {
				ft.setOtherIncludesList(getOtherIncludesList());
				ft.setOtherOptions(getOtherOptions());
			} else if (projectScanType instanceof AdvancedScanType) {
				ft.setAdvOptions(getTranslationOptions());
			}

			ft.perform(build, launcher, listener);
		}

		if (runScan != null) {
			FortifyScan fs = new FortifyScan(getBuildId());
			fs.setAddJVMOptions(getAddJVMOptions());
			fs.setMaxHeap(getMaxHeap());
			fs.setDebug(getScanDebug());
			fs.setVerbose(getScanVerbose());
			fs.setLogFile(getScanLogFile());
			fs.setResultsFile(getScanFile());
			fs.setCustomRulepacks(getScanCustomRulepacks());
			fs.setAddOptions(getScanAddOptions());
			fs.perform(build, launcher, listener);
		}

		if (uploadSSC != null) {
			FortifyUpload upload = new FortifyUpload(false, getProjectName(), getProjectVersion());
			upload.setFailureCriteria(getSearchCondition());
			upload.setFilterSet(getFilterSet());
			upload.setResultsFile(getScanFile());
			upload.setPollingInterval(getUploadWaitTime());
			upload.perform(build, launcher, listener);

		}

		return true;
	}

	/**
	 * Determines the {@link ProjectScanType} based on the configuration.
	 */
	private ProjectScanType calculateProjectScanType() {
		if (getIsAdvancedTranslationType()) {
			return new AdvancedScanType();
		} else {
			if (getIsBasicJavaTranslationType()) {
				return new JavaScanType();
			} else if (getIsBasicDotNetTranslationType()) {
				if (getIsBasicDotNetProjectSolutionScanType()) {
					if (getIsBasicDotNetMSBuildBuildType()) {
						return new MsbuildScanType();
					} else {
						return new DevenvScanType();
					}
				} else {
					return new DotnetSourceScanType();
				}
			} else if (getIsBasicMaven3TranslationType()) {
				return new MavenScanType();
			} else if (getIsBasicGradleTranslationType()) {
				return new GradleScanType();
			} else {
				return new OtherScanType();
			}
		}
	}

	private static <T> T runWithFortifyClient(String token, FortifyClient.Command<T> cmd) throws Exception {
		if (cmd != null) {
			String url = DESCRIPTOR.getUrl();
			ClassLoader contextClassLoader = null;
			try {
				FortifyClient client = null;
				synchronized (syncObj) {
					contextClassLoader = Thread.currentThread().getContextClassLoader();
					Thread.currentThread().setContextClassLoader(FortifyPlugin.class.getClassLoader());
					client = new FortifyClient();
					boolean useProxy = DESCRIPTOR.getUseProxy();
					String proxyUrl = DESCRIPTOR.getProxyUrl();
					if (!useProxy || StringUtils.isEmpty(proxyUrl)) {
						client.init(url, token);
					} else {
						String[] proxyUrlSplit = proxyUrl.split(":");
						String proxyHost = proxyUrlSplit[0];
						int proxyPort = 80;
						if (proxyUrlSplit.length > 1) {
							try {
								proxyPort = Integer.parseInt(proxyUrlSplit[1]);
							} catch (NumberFormatException nfe) {
							}
						}
						client.init(url, token, proxyHost, proxyPort, DESCRIPTOR.getProxyUsername(),
								DESCRIPTOR.getProxyPassword());
					}
				}
				if (client != null) {
					return cmd.runWith(client);
				}
			} finally {
				if (contextClassLoader != null) {
					Thread.currentThread().setContextClassLoader(contextClassLoader);
				}
			}
		}
		return null;
	}

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		/** SSC URL, e.g. http://localhost:8080/ssc */
		private String url;

		/** SSC proxy **/
		private boolean useProxy;
		private String proxyUrl; // host:port
		private Secret proxyUsername;
		private Secret proxyPassword;

		/** SSC Authentication Token */
		private Secret token;

		/** SSC issue template name (used during creation of new application version) */
		private String projectTemplate;

		/** Number of issues to be displayed per page in breakdown table */
		private Integer breakdownPageSize;

		/** List of Issue Templates obtained from SSC */
		private List<ProjectTemplateBean> projTemplateList = Collections.emptyList();

		/** List of all Projects (including versions info) obtained from SSC */
		private Map<String, Map<String, Long>> allProjects = Collections.emptyMap();

		public DescriptorImpl() {
			super(FortifyPlugin.class);
			load();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true; // applicable to all application type
		}

		@Override
		public String getDisplayName() {
			return "Fortify Assessment";
		}

		public String getUrl() {
			return url;
		}

		public boolean getUseProxy() {
			return useProxy;
		}

		public String getProxyUrl() {
			return proxyUrl;
		}

		public String getProxyUsername() {
			return proxyUsername == null ? "" : proxyUsername.getPlainText();
		}

		public String getProxyPassword() {
			return proxyPassword == null ? "" : proxyPassword.getPlainText();
		}

		public String getToken() {
			return token == null ? "" : token.getPlainText();
		}

		public boolean canUploadToSsc() {
			return (!StringUtils.isBlank(getUrl()) && !StringUtils.isBlank(getToken()));
		}

		public String getProjectTemplate() {
			return projectTemplate;
		}

		public Integer getBreakdownPageSize() {
			return breakdownPageSize;
		}

		public FormValidation doCheckBreakdownPageSize(@QueryParameter String value) {
			if (StringUtils.isBlank(value)) {
				return FormValidation.ok();
			}

			int pageSize = 0;
			try {
				pageSize = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return FormValidation.warning("Expected an integer value greater than zero.");
			}
			if (pageSize < 1) {
				return FormValidation.warning("Expected an integer value greater than zero.");
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckUrl(@QueryParameter String value) {
			try {
				checkUrlValue(value.trim());
			} catch (FortifyException e) {
				return FormValidation.warning(e.getMessage());
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

		@POST
		public FormValidation doCheckProxyUsername(@QueryParameter String value) {
			try {
				checkProxyUsernameValue(value.trim());
			} catch (FortifyException e) {
				return FormValidation.warning(e.getMessage());
			}
			return FormValidation.ok();
		}

		@POST
		public FormValidation doCheckProxyPassword(@QueryParameter String value) {
			try {
				checkProxyPasswordValue(value);
			} catch (FortifyException e) {
				return FormValidation.warning(e.getMessage());
			}
			return FormValidation.ok();
		}

		@POST
		public FormValidation doCheckToken(@QueryParameter String value) {
			if (StringUtils.isBlank(value)) {
				return FormValidation.warning("SSC Authentication Token can't be empty");
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckFpr(@QueryParameter String value) {
			if (StringUtils.isBlank(value) || value.charAt(0) == '$') { // parameterized values are not checkable
				return FormValidation.ok();
			} else if (value.contains("/") || value.contains("\\")
					|| !FilenameUtils.isExtension(value.toLowerCase(), new String[] { "fpr" })) {
				return FormValidation.error("The FPR filename should be in basename *ONLY*, with extension FPR");
			} else {
				return FormValidation.ok();
			}
		}

		public FormValidation doCheckProjectTemplate(@QueryParameter String value) {
			try {
				checkProjectTemplateName(value.trim());
			} catch (FortifyException e) {
				return FormValidation.error(e.getMessage());
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckProjectName(@QueryParameter String value) {
			return FormValidation.ok();
		}

		public FormValidation doCheckProjectVersion(@QueryParameter String value) {
			return FormValidation.ok();
		}

		public FormValidation doCheckPollingInterval(@QueryParameter String value) {
			if (StringUtils.isBlank(value) || value.charAt(0) == '$') {
				return FormValidation.ok();
			} else {
				int x = -1;
				try {
					x = Integer.parseInt(value);
					if (x >= 0 && x <= 60)
						return FormValidation.ok();
				} catch (NumberFormatException e) {
				}
				return FormValidation.error("The unit is in minutes, and in the range of 0 to 60");
			}
		}

		private FormValidation doTestConnection(String url, String token, String jarsPath) {
			return doTestConnection(url, token, jarsPath, this.useProxy, this.proxyUrl, this.getProxyUsername(),
					this.getProxyPassword());
		}

		public FormValidation doTestConnection(@QueryParameter String url, @QueryParameter String token,
				@QueryParameter String jarsPath, @QueryParameter boolean useProxy, @QueryParameter String proxyUrl,
				@QueryParameter String proxyUsername, @QueryParameter String proxyPassword) {
			String sscUrl = url == null ? "" : url.trim();
			try {
				checkUrlValue(sscUrl);
			} catch (FortifyException e) {
				return FormValidation.error(e.getMessage());
			}
			String userToken = token == null ? "" : token.trim();
			if (StringUtils.isBlank(userToken)) {
				return FormValidation.error("Token can't be empty");
			} else if (userToken.indexOf(' ') != -1) {
				return FormValidation.error("Token should contain no spaces");
			}

			// backup original values
			String orig_url = this.url;
			Secret orig_token = this.token;
			boolean orig_useProxy = this.useProxy;
			String orig_proxyUrl = this.proxyUrl;
			Secret orig_proxyUsername = this.proxyUsername;
			Secret orig_proxyPassword = this.proxyPassword;
			this.url = sscUrl;
			this.token = userToken.isEmpty() ? null : Secret.fromString(userToken);
			this.useProxy = useProxy;
			this.proxyUrl = proxyUrl;
			this.proxyUsername = proxyUsername == null ? null : Secret.fromString(proxyUsername);
			this.proxyPassword = proxyPassword == null ? null : Secret.fromString(proxyPassword);
			try {
				runWithFortifyClient(userToken, new FortifyClient.Command<FortifyClient.NoReturn>() {
					@Override
					public NoReturn runWith(FortifyClient client) throws Exception {
						// as long as no exception, that's ok
						client.getProjectList();
						return FortifyClient.NoReturn.INSTANCE;
					}
				});
				return FormValidation.okWithMarkup("<font color=\"blue\">Connection successful!</font>");
			} catch (Throwable t) {
				if (t.getMessage().contains("Access Denied")) {
					return FormValidation.error(t, "Invalid token");
				}
				return FormValidation.error(t, "Can't connect to SSC server");
			} finally {
				this.url = orig_url;
				this.token = orig_token;
				this.useProxy = orig_useProxy;
				this.proxyUrl = orig_proxyUrl;
				this.proxyUsername = orig_proxyUsername;
				this.proxyPassword = orig_proxyPassword;
			}
		}

		private void checkUrlValue(String sscUrl) throws FortifyException {
			if (StringUtils.isBlank(sscUrl)) {
				throw new FortifyException(new Message(Message.ERROR, "URL can't be empty"));
			} else {
				if (StringUtils.startsWith(sscUrl, "http://") || StringUtils.startsWith(sscUrl, "https://")) {
					if (sscUrl.trim().equalsIgnoreCase("http://") || sscUrl.trim().equalsIgnoreCase("https://")) {
						throw new FortifyException(new Message(Message.ERROR, "URL host is required."));
					}
				} else {
					throw new FortifyException(new Message(Message.ERROR, "Invalid protocol"));
				}
				if (sscUrl.indexOf(' ') != -1) {
					throw new FortifyException(new Message(Message.ERROR, "Please remove spaces from URL adress"));
				}
			}
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
					throw new FortifyException(new Message(Message.ERROR, "Invalid proxy host."));
				}
				if (splits.length == 2) {
					try {
						Integer.parseInt(splits[1]);
					} catch (NumberFormatException nfe) {
						throw new FortifyException(new Message(Message.ERROR, "Invalid proxy port."));
					}
				}
			}
		}

		private void checkProxyUsernameValue(String proxyUsername) throws FortifyException {
			// accept anything
		}

		private void checkProxyPasswordValue(String proxyPassword) throws FortifyException {
			// accept anything
		}

		private void checkProjectTemplateName(String projectTemplateName) throws FortifyException {
			if (!StringUtils.isEmpty(projectTemplateName)) {
				boolean valid = false;
				List<ProjectTemplateBean> projectTemplateList = getProjTemplateListList();
				if (projectTemplateList != null) {
					for (ProjectTemplateBean projectTemplateBean : projectTemplateList) {
						if (projectTemplateBean.getName().equals(projectTemplateName)) {
							valid = true;
						}
					}
					if (!valid) {
						throw new FortifyException(
								new Message(Message.ERROR, "Invalid Issue Template \"" + projectTemplateName + "\"."));
					}
				}
			}
		}

		public void doRefreshProjects(StaplerRequest req, StaplerResponse rsp, @QueryParameter String value)
				throws Exception {
			try {
				// always retrieve data from SSC
				allProjects = getAllProjectsNoCache();
				// and then convert it to JSON
				StringBuilder buf = new StringBuilder();
				List<String> projects = new ArrayList<String>(allProjects.keySet());
				Collections.sort(projects, String.CASE_INSENSITIVE_ORDER);
				for (String prjName : projects) {
					if (buf.length() > 0) {
						buf.append(",");
					}
					buf.append("{ \"name\": \"" + prjName + "\" }\n");
				}
				buf.insert(0, "{ \"list\" : [\n");
				buf.append("]}");
				// send HTML data directly
				rsp.setContentType("text/html;charset=UTF-8");
				rsp.getWriter().print(buf.toString());
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}

		public void doRefreshVersions(StaplerRequest req, StaplerResponse rsp, @QueryParameter String value)
				throws Exception {
			try {
				// always retrieve data from SSC
				allProjects = getAllProjects();
				// and then convert it to JSON
				StringBuilder buf = new StringBuilder();
				for (String prjName : allProjects.keySet()) {
					List<String> versions = new ArrayList<String>(allProjects.get(prjName).keySet());
					Collections.sort(versions, String.CASE_INSENSITIVE_ORDER);
					for (String prjVersion : versions) {
						if (buf.length() > 0) {
							buf.append(",");
						}
						buf.append("{ \"name\": \"" + prjVersion + "\", \"prj\": \"" + prjName + "\" }\n");
					}
				}
				buf.insert(0, "{ \"list\" : [\n");
				buf.append("]}");
				// send HTML data directly
				rsp.setContentType("text/html;charset=UTF-8");
				rsp.getWriter().print(buf.toString());
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}

		public void doRefreshProjectTemplates(StaplerRequest req, StaplerResponse rsp, @QueryParameter String value)
				throws Exception {
			// backup original values
			String orig_url = this.url;
			boolean orig_useProxy = this.useProxy;
			String orig_proxyUrl = this.proxyUrl;
			Secret orig_proxyUsername = this.proxyUsername;
			Secret orig_proxyPassword = this.proxyPassword;
			Secret orig_token = this.token;

			String url = req.getParameter("url");
			boolean useProxy = "true".equals(req.getParameter("useProxy"));
			String proxyUrl = req.getParameter("proxyUrl");
			String proxyUsername = req.getParameter("proxyUsername");
			String proxyPassword = req.getParameter("proxyPassword");
			String token = req.getParameter("token");
			this.url = url != null ? url.trim() : "";
			this.useProxy = useProxy;
			if (useProxy) {
				this.proxyUrl = proxyUrl != null ? proxyUrl.trim() : "";
				this.proxyUsername = proxyUsername != null ? Secret.fromString(proxyUsername.trim()) : null;
				this.proxyPassword = proxyPassword != null ? Secret.fromString(proxyPassword) : null;
			} else {
				this.proxyUrl = "";
				this.proxyUsername = null;
				this.proxyPassword = null;
			}
			this.token = token != null ? Secret.fromString(token.trim()) : null;

			if (!doTestConnection(this.url, this.getToken(), null).kind.equals(FormValidation.Kind.OK)) {
				return; // don't get templates if server is unavailable
			}

			try {
				// always retrieve data from SSC
				projTemplateList = getProjTemplateListNoCache();
				// and then convert it to JSON
				StringBuilder buf = new StringBuilder();
				buf.append("{ \"list\" : [\n");
				for (int i = 0; i < projTemplateList.size(); i++) {
					ProjectTemplateBean b = projTemplateList.get(i);
					buf.append("{ \"name\": \"" + b.getName() + "\", \"id\": \"" + b.getId() + "\" }");
					if (i != projTemplateList.size() - 1) {
						buf.append(",\n");
					} else {
						buf.append("\n");
					}
				}
				buf.append("]}");
				// send HTML data directly
				rsp.setContentType("text/html;charset=UTF-8");
				rsp.getWriter().print(buf.toString());
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			} finally {
				this.url = orig_url;
				this.useProxy = orig_useProxy;
				this.proxyUrl = orig_proxyUrl;
				this.proxyUsername = orig_proxyUsername;
				this.proxyPassword = orig_proxyPassword;
				this.token = orig_token;
			}
		}

		public void doCreateNewProject(final StaplerRequest req, StaplerResponse rsp, @QueryParameter String value) throws Exception {
			try {
				runWithFortifyClient(getToken(), new FortifyClient.Command<FortifyClient.NoReturn>() {
					@Override
					public NoReturn runWith(FortifyClient client) throws Exception {
						client.createProject(req.getParameter("newprojName"), req.getParameter("newprojVersion"),
								req.getParameter("newprojTemplate"), Collections.<String, String>emptyMap(),
								new PrintWriter(System.out));
						return FortifyClient.NoReturn.INSTANCE;
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}

			doRefreshProjects(req, rsp, value);
		}

		private boolean isSettingUpdated = false;

		@Override
		public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
			// to persist global configuration information,
			// set that to properties and call save().
			try {
				url = o.getString("url").trim();
				checkUrlValue(url);
			} catch (JSONException e) {
				System.out.println("Can't restore 'URL' property. Will use default (empty) values.");
				url = null;
			} catch (FortifyException e) {
				System.out.println(e.getMessage());
				url = null;
			}
			JSONObject useProxy = null;
			try {
				useProxy = o.getJSONObject("useProxy");
			} catch (JSONException e) {
			}
			if (useProxy == null || useProxy.isNullObject()) {
				this.useProxy = false;
			} else {
				this.useProxy = true;
				try {
					proxyUrl = useProxy.getString("proxyUrl").trim();
					checkProxyUrlValue(proxyUrl);
				} catch (JSONException e) {
					e.printStackTrace();
					System.out.println("Can't restore 'proxyUrl' property.  Will use default (empty) values.");
					proxyUrl = null;
				} catch (FortifyException e) {
					System.out.println(e.getMessage());
					proxyUrl = null;
				}
				try {
					String usernameParam = useProxy.getString("proxyUsername").trim();
					checkProxyUsernameValue(usernameParam);
					proxyUsername = usernameParam.isEmpty() ? null : Secret.fromString(usernameParam);
				} catch (JSONException e) {
					System.out.println("Can't restore 'proxyUsername' property.  Will use default (empty) values.");
					proxyUsername = null;
				} catch (FortifyException e) {
					System.out.println(e.getMessage());
					proxyUsername = null;
				}
				try {
					String pwdParam = useProxy.getString("proxyPassword").trim();
					checkProxyPasswordValue(pwdParam);
					proxyPassword = pwdParam.isEmpty() ? null : Secret.fromString(pwdParam);
				} catch (JSONException e) {
					System.out.println("Can't restore 'proxyPassword' property.  Will use default (empty) values.");
					proxyPassword = null;
				} catch (FortifyException e) {
					System.out.println(e.getMessage());
					proxyPassword = null;
				}
			}
			try {
				String tokenParam = o.getString("token").trim();
				token = tokenParam.isEmpty() ? null : Secret.fromString(tokenParam);
			} catch (JSONException e) {
				System.out.println("Can't restore 'Authentication Token' property. Will use default (empty) values.");
				token = null;
			}

			try {
				projectTemplate = o.getString("projectTemplate").trim();
			} catch (JSONException e) {
				System.out.println("Can't restore 'Issue template' property. Will use default (empty) values.");
				projectTemplate = null;
			}

			try {
				String pageSizeString = o.getString("breakdownPageSize");
				if (pageSizeString != null && pageSizeString.trim().length() > 0) {
					breakdownPageSize = Integer.parseInt(pageSizeString.trim());
				} else {
					breakdownPageSize = DEFAULT_PAGE_SIZE;
				}
			} catch (NumberFormatException | JSONException e) {
				System.out.println("Can't restore 'Issue breakdown page size' property. Will use default (" + DEFAULT_PAGE_SIZE + ") value.");
				breakdownPageSize = DEFAULT_PAGE_SIZE;
			}

			save();
			isSettingUpdated = true;
			return super.configure(req, o);
		}

		public boolean isSettingUpdated() {
			try {
				return isSettingUpdated;
			} finally {
				isSettingUpdated = false;
			}
		}

		public ComboBoxModel doFillProjectNameItems() {
			Map<String, Map<String, Long>> allPrj = getAllProjects();
			return new ComboBoxModel(allPrj.keySet());
		}

		public ComboBoxModel getProjectNameItems() {
			return doFillProjectNameItems();
		}

		public ComboBoxModel doFillProjectVersionItems(@QueryParameter String projectName) {
			Map<String, Long> allPrjVersions = getAllProjects().get(projectName);
			if (null == allPrjVersions) {
				return new ComboBoxModel(Collections.<String>emptyList());
			}
			return new ComboBoxModel(allPrjVersions.keySet());
		}

		public ComboBoxModel getProjectVersionItems(@QueryParameter String projectName) {
			return doFillProjectVersionItems(projectName);
		}

		private Map<String, Map<String, Long>> getAllProjects() {
			if (allProjects.isEmpty()) {
				allProjects = getAllProjectsNoCache();
			}
			return allProjects;
		}

		private Map<String, Map<String, Long>> getAllProjectsNoCache() {
			if (canUploadToSsc()) {
				try {
					Map<String, Map<String, Long>> map = runWithFortifyClient(getToken(),
							new FortifyClient.Command<Map<String, Map<String, Long>>>() {
								@Override
								public Map<String, Map<String, Long>> runWith(FortifyClient client) throws Exception {
									return client.getProjectListEx();
								}
							});
					return map;
					// many strange thing can happen.... need to catch throwable
				} catch (Throwable e) {
					e.printStackTrace();
					return Collections.emptyMap();
				}
			} else {
				return Collections.emptyMap();
			}
		}

		/**
		 * Get Issue template list from SSC via WS <br/>
		 * Basically only for global.jelly pull down menu
		 * 
		 * @return A list of Issue template and ID
		 * @throws ApiException
		 */
		public ComboBoxModel doFillProjectTemplateItems() {
			if (projTemplateList.isEmpty()) {
				projTemplateList = getProjTemplateListNoCache();
			}

			List<String> names = new ArrayList<String>(projTemplateList.size());
			for (ProjectTemplateBean b : projTemplateList) {
				names.add(b.getName());
			}
			return new ComboBoxModel(names);
		}

		public ComboBoxModel getProjectTemplateItems() {
			return doFillProjectTemplateItems();
		}

		public List<ProjectTemplateBean> getProjTemplateListList() {
			if (projTemplateList.isEmpty()) {
				projTemplateList = getProjTemplateListNoCache();
			}
			return projTemplateList;
		}

		private List<ProjectTemplateBean> getProjTemplateListNoCache() {
			if (canUploadToSsc()) {
				try {
					Map<String, String> map = runWithFortifyClient(getToken(),
							new FortifyClient.Command<Map<String, String>>() {
								@Override
								public Map<String, String> runWith(FortifyClient client) throws Exception {
									return client.getProjectTemplateList();
								}
							});
					List<ProjectTemplateBean> list = new ArrayList<ProjectTemplateBean>(map.size());
					for (String name : map.keySet()) {
						ProjectTemplateBean proj = new ProjectTemplateBean(name, map.get(name));
						list.add(proj);
					}
					Collections.sort(list);
					return list;
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			return Collections.emptyList();
		}

		public ListBoxModel doFillTranslationApplicationTypeItems() {
			ListBoxModel options = new ListBoxModel(5);
			options.add("Java", "java");
			options.add(".NET", "dotnet");
			options.add("Maven 3", "maven3");
			options.add("Gradle", "gradle");
			options.add("Other", "other");
			return options;
		}

		public ListBoxModel doFillTranslationJavaVersionItems() {
			ListBoxModel options = new ListBoxModel();
			options.add("1.5", "1.5");
			options.add("1.6", "1.6");
			options.add("1.7", "1.7");
			options.add("1.8", "1.8");
			options.add("1.9", "1.9");
			return options;
		}
	}

	public class UploadSSCBlock {
		private String projectName;
		private String projectVersion;
		private String filterSet;
		private String searchCondition;
		private String pollingInterval;

		@DataBoundConstructor
		public UploadSSCBlock(String projectName, String projectVersion, String filterSet, String searchCondition, String pollingInterval) {
			this.projectName = projectName != null ? projectName.trim() : "";
			this.projectVersion = projectName != null ? projectVersion.trim() : "";
			this.filterSet = filterSet != null ? filterSet.trim() : "";
			this.searchCondition = searchCondition != null ? searchCondition.trim() : "";
			this.pollingInterval = pollingInterval != null ? pollingInterval.trim() : "";
		}

		public String getProjectName() {
			return projectName;
		}

		public String getProjectVersion() {
			return projectVersion;
		}

		public String getFilterSet() {
			return filterSet;
		}

		public String getSearchCondition() {
			return searchCondition;
		}

		public String getPollingInterval() {
			return pollingInterval;
		}
	}

	public class RunTranslationBlock {
		private TranslationTypeBlock translationType;
		private boolean debug;
		private boolean verbose;
		private String logFile;

		@DataBoundConstructor
		public RunTranslationBlock(TranslationTypeBlock translationType, boolean translationDebug,
				boolean translationVerbose, String translationLogFile) {
			this.translationType = translationType;
			this.debug = translationDebug;
			this.verbose = translationVerbose;
			this.logFile = translationLogFile != null ? translationLogFile.trim() : "";
		}

		public boolean isBasicTranslationType() {
			return translationType instanceof BasicTranslationBlock;
		}

		public boolean isAdvancedTranslationType() {
			return translationType instanceof AdvancedTranslationBlock;
		}

		public boolean isBasicJavaTranslationType() {
			return isBasicTranslationType() && ((BasicTranslationBlock) translationType)
					.getTranslationApplicationTypeBlock() instanceof BasicJavaTranslationAppTypeBlock;
		}

		public boolean isBasicDotNetTranslationType() {
			return isBasicTranslationType() && ((BasicTranslationBlock) translationType)
					.getTranslationApplicationTypeBlock() instanceof BasicDotNetTranslationAppTypeBlock;
		}

		public boolean isBasicMaven3TranslationType() {
			return isBasicTranslationType() && ((BasicTranslationBlock) translationType)
					.getTranslationApplicationTypeBlock() instanceof BasicMaven3TranslationAppTypeBlock;
		}

		public boolean isBasicGradleTranslationType() {
			return isBasicTranslationType() && ((BasicTranslationBlock) translationType)
					.getTranslationApplicationTypeBlock() instanceof BasicGradleTranslationAppTypeBlock;
		}

		public boolean isBasicOtherTranslationType() {
			return isBasicTranslationType() && ((BasicTranslationBlock) translationType)
					.getTranslationApplicationTypeBlock() instanceof BasicOtherTranslationAppTypeBlock;
		}

		public boolean isBasicDotNetProjectSolutionScanType() {
			return isBasicDotNetTranslationType()
					&& getBasicDotNetTranslationAppTypeBlock().isProjectSolutionScanType();
		}

		public boolean isBasicDotNetSourceCodeScanType() {
			return isBasicDotNetTranslationType() && getBasicDotNetTranslationAppTypeBlock().isSourceCodeScanType();
		}

		public boolean isBasicDotNetDevenvBuildType() {
			return isBasicDotNetProjectSolutionScanType()
					&& getBasicDotNetTranslationAppTypeBlock().isDevenvBuildType();
		}

		public boolean isBasicDotNetMSBuildBuildType() {
			return isBasicDotNetProjectSolutionScanType()
					&& getBasicDotNetTranslationAppTypeBlock().isMSBuildBuildType();
		}

		private BasicJavaTranslationAppTypeBlock getBasicJavaTranslationAppTypeBlock() {
			return isBasicJavaTranslationType()
					? (BasicJavaTranslationAppTypeBlock) ((BasicTranslationBlock) translationType)
							.getTranslationApplicationTypeBlock()
					: null;
		}

		private BasicDotNetTranslationAppTypeBlock getBasicDotNetTranslationAppTypeBlock() {
			return isBasicDotNetTranslationType()
					? (BasicDotNetTranslationAppTypeBlock) ((BasicTranslationBlock) translationType)
							.getTranslationApplicationTypeBlock()
					: null;
		}

		private BasicMaven3TranslationAppTypeBlock getBasicMaven3TranslationAppTypeBlock() {
			return isBasicMaven3TranslationType()
					? (BasicMaven3TranslationAppTypeBlock) ((BasicTranslationBlock) translationType)
							.getTranslationApplicationTypeBlock()
					: null;
		}

		private BasicGradleTranslationAppTypeBlock getBasicGradleTranslationAppTypeBlock() {
			return isBasicGradleTranslationType()
					? (BasicGradleTranslationAppTypeBlock) ((BasicTranslationBlock) translationType)
							.getTranslationApplicationTypeBlock()
					: null;
		}

		private BasicOtherTranslationAppTypeBlock getBasicOtherTranslationAppTypeBlock() {
			return isBasicOtherTranslationType()
					? (BasicOtherTranslationAppTypeBlock) ((BasicTranslationBlock) translationType)
							.getTranslationApplicationTypeBlock()
					: null;
		}

		public String getTranslationType() {
			return isBasicTranslationType() ? "translationBasic" : "translationAdvanced";
		}

		public String getTranslationOptions() {
			return isAdvancedTranslationType() ? ((AdvancedTranslationBlock) translationType).getTranslationOptions()
					: "";
		}

		public String getTranslationExcludeList() {
			return isBasicTranslationType() ? ((BasicTranslationBlock) translationType).getTranslationExcludeList()
					: "";
		}

		public String getTranslationJavaVersion() {
			return isBasicJavaTranslationType() ? getBasicJavaTranslationAppTypeBlock().getTranslationJavaVersion()
					: "";
		}

		public String getTranslationClasspath() {
			return isBasicJavaTranslationType() ? getBasicJavaTranslationAppTypeBlock().getTranslationClasspath() : "";
		}

		public String getTranslationSourceFiles() {
			return isBasicJavaTranslationType() ? getBasicJavaTranslationAppTypeBlock().getTranslationSourceFiles()
					: "";
		}

		public String getTranslationAddOptions() {
			return isBasicJavaTranslationType() ? getBasicJavaTranslationAppTypeBlock().getTranslationAddOptions() : "";
		}

		public String getDotNetDevenvProjects() {
			return isBasicDotNetTranslationType() ? getBasicDotNetTranslationAppTypeBlock().getDevenvProjects() : "";
		}

		public String getDotNetDevenvAddOptions() {
			return isBasicDotNetTranslationType() ? getBasicDotNetTranslationAppTypeBlock().getDevenvAddOptions() : "";
		}

		public String getDotNetMSBuildProjects() {
			return isBasicDotNetTranslationType() ? getBasicDotNetTranslationAppTypeBlock().getMSBuildProjects() : "";
		}

		public String getDotNetMSBuildAddOptions() {
			return isBasicDotNetTranslationType() ? getBasicDotNetTranslationAppTypeBlock().getMSBuildAddOptions() : "";
		}

		public String getDotNetSourceCodeFrameworkVersion() {
			return isBasicDotNetTranslationType()
					? getBasicDotNetTranslationAppTypeBlock().getSourceCodeFrameworkVersion()
					: "";
		}

		public String getDotNetSourceCodeLibdirs() {
			return isBasicDotNetTranslationType() ? getBasicDotNetTranslationAppTypeBlock().getSourceCodeLibdirs() : "";
		}

		public String getDotNetSourceCodeAddOptions() {
			return isBasicDotNetTranslationType() ? getBasicDotNetTranslationAppTypeBlock().getSourceCodeAddOptions()
					: "";
		}

		public String getDotNetSourceCodeSrcFiles() {
			return isBasicDotNetTranslationType() ? getBasicDotNetTranslationAppTypeBlock().getSourceCodeSrcFiles()
					: "";
		}

		public String getMaven3Options() {
			return isBasicMaven3TranslationType() ? getBasicMaven3TranslationAppTypeBlock().getOptions() : "";
		}

		public boolean getGradleUseWrapper() {
			return isBasicGradleTranslationType() && getBasicGradleTranslationAppTypeBlock().getUseWrapper();
		}

		public String getGradleTasks() {
			return isBasicGradleTranslationType() ? getBasicGradleTranslationAppTypeBlock().getTasks() : "";
		}

		public String getGradleOptions() {
			return isBasicGradleTranslationType() ? getBasicGradleTranslationAppTypeBlock().getOptions() : "";
		}

		public String getOtherOptions() {
			return isBasicOtherTranslationType() ? getBasicOtherTranslationAppTypeBlock().getOptions() : "";
		}

		public String getOtherIncludesList() {
			return isBasicOtherTranslationType() ? getBasicOtherTranslationAppTypeBlock().getIncludesList() : "";
		}

		public boolean getTranslationDebug() {
			return debug;
		}

		public boolean getTranslationVerbose() {
			return verbose;
		}

		public String getTranslationLogFile() {
			return logFile;
		}
	}

	public interface TranslationTypeBlock {
	}

	public class BasicTranslationBlock implements TranslationTypeBlock {
		private BasicTranslationAppTypeBlock appTypeBlock;
		private String excludeList;

		@DataBoundConstructor
		public BasicTranslationBlock(BasicTranslationAppTypeBlock translationAppType, String translationExcludeList) {
			this.appTypeBlock = translationAppType;
			this.excludeList = translationExcludeList != null ? translationExcludeList.trim() : "";
		}

		public BasicTranslationAppTypeBlock getTranslationApplicationTypeBlock() {
			return appTypeBlock;
		}

		public String getTranslationExcludeList() {
			return excludeList;
		}
	}

	public class AdvancedTranslationBlock implements TranslationTypeBlock {
		private String translationOptions;

		@DataBoundConstructor
		public AdvancedTranslationBlock(String translationOptions) {
			this.translationOptions = translationOptions != null ? translationOptions.trim() : "";
		}

		public String getTranslationOptions() {
			return translationOptions;
		}
	}

	public interface BasicTranslationAppTypeBlock {
	}

	public class BasicJavaTranslationAppTypeBlock implements BasicTranslationAppTypeBlock {
		private String javaVersion;
		private String classpath;
		private String sourceFiles;
		private String additionalOptions;

		@DataBoundConstructor
		public BasicJavaTranslationAppTypeBlock(String translationJavaVersion, String translationJavaClasspath,
				String translationJavaSourceFiles, String translationJavaAddOptions) {
			this.javaVersion = translationJavaVersion != null ? translationJavaVersion.trim() : "";
			this.classpath = translationJavaClasspath != null ? translationJavaClasspath.trim() : "";
			this.sourceFiles = translationJavaSourceFiles != null ? translationJavaSourceFiles.trim() : "";
			this.additionalOptions = translationJavaAddOptions != null ? translationJavaAddOptions.trim() : "";
		}

		public String getTranslationJavaVersion() {
			return javaVersion;
		}

		public String getTranslationClasspath() {
			return classpath;
		}

		public String getTranslationSourceFiles() {
			return sourceFiles;
		}

		public String getTranslationAddOptions() {
			return additionalOptions;
		}
	}

	public class BasicDotNetTranslationAppTypeBlock implements BasicTranslationAppTypeBlock {
		private BasicDotNetScanTypeBlock scanType;

		@DataBoundConstructor
		public BasicDotNetTranslationAppTypeBlock(BasicDotNetScanTypeBlock dotNetScanType) {
			scanType = dotNetScanType;
		}

		public boolean isProjectSolutionScanType() {
			return scanType != null && scanType instanceof BasicDotNetProjectSolutionScanTypeBlock;
		}

		public boolean isSourceCodeScanType() {
			return scanType != null && scanType instanceof BasicDotNetSourceCodeScanTypeBlock;
		}

		public BasicDotNetScanTypeBlock getScanTypeBlock() {
			return scanType;
		}

		public boolean isDevenvBuildType() {
			return isProjectSolutionScanType()
					&& ((BasicDotNetProjectSolutionScanTypeBlock) scanType).isDevenvBuildType();
		}

		public boolean isMSBuildBuildType() {
			return isProjectSolutionScanType()
					&& ((BasicDotNetProjectSolutionScanTypeBlock) scanType).isMSBuildBuildType();
		}

		public String getDevenvProjects() {
			return isProjectSolutionScanType()
					? ((BasicDotNetProjectSolutionScanTypeBlock) scanType).getDevenvProjects()
					: "";
		}

		public String getDevenvAddOptions() {
			return isProjectSolutionScanType()
					? ((BasicDotNetProjectSolutionScanTypeBlock) scanType).getDevenvAddOptions()
					: "";
		}

		public String getMSBuildProjects() {
			return isProjectSolutionScanType()
					? ((BasicDotNetProjectSolutionScanTypeBlock) scanType).getMSBuildProjects()
					: "";
		}

		public String getMSBuildAddOptions() {
			return isProjectSolutionScanType()
					? ((BasicDotNetProjectSolutionScanTypeBlock) scanType).getMSBuildAddOptions()
					: "";
		}

		public String getSourceCodeFrameworkVersion() {
			return isSourceCodeScanType() ? ((BasicDotNetSourceCodeScanTypeBlock) scanType).getDotNetVersion() : "";
		}

		public String getSourceCodeLibdirs() {
			return isSourceCodeScanType() ? ((BasicDotNetSourceCodeScanTypeBlock) scanType).getLibdirs() : "";
		}

		public String getSourceCodeAddOptions() {
			return isSourceCodeScanType() ? ((BasicDotNetSourceCodeScanTypeBlock) scanType).getAddOptions() : "";
		}

		public String getSourceCodeSrcFiles() {
			return isSourceCodeScanType() ? ((BasicDotNetSourceCodeScanTypeBlock) scanType).getDotNetSrcFiles() : "";
		}
	}

	public interface BasicDotNetScanTypeBlock {
	}

	public class BasicDotNetProjectSolutionScanTypeBlock implements BasicDotNetScanTypeBlock {
		private BasicDotNetBuildTypeBlock buildType;

		@DataBoundConstructor
		public BasicDotNetProjectSolutionScanTypeBlock(BasicDotNetBuildTypeBlock dotNetBuildType) {
			buildType = dotNetBuildType;
		}

		public boolean isDevenvBuildType() {
			return buildType != null && buildType instanceof BasicDotNetDevenvBuildTypeBlock;
		}

		public boolean isMSBuildBuildType() {
			return buildType != null && buildType instanceof BasicDotNetMSBuildBuildTypeBlock;
		}

		public String getDevenvProjects() {
			return isDevenvBuildType() ? ((BasicDotNetDevenvBuildTypeBlock) buildType).getProjects() : "";
		}

		public String getDevenvAddOptions() {
			return isDevenvBuildType() ? ((BasicDotNetDevenvBuildTypeBlock) buildType).getAddOptions() : "";
		}

		public String getMSBuildProjects() {
			return isMSBuildBuildType() ? ((BasicDotNetMSBuildBuildTypeBlock) buildType).getProjects() : "";
		}

		public String getMSBuildAddOptions() {
			return isMSBuildBuildType() ? ((BasicDotNetMSBuildBuildTypeBlock) buildType).getAddOptions() : "";
		}
	}

	public class BasicDotNetSourceCodeScanTypeBlock implements BasicDotNetScanTypeBlock {
		private String dotNetVersion;
		private String libdirs;
		private String addOptions;
		private String dotNetSrcFiles;

		@DataBoundConstructor
		public BasicDotNetSourceCodeScanTypeBlock(String dotNetSourceCodeFrameworkVersion,
				String dotNetSourceCodeLibdirs, String dotNetSourceCodeAddOptions, String dotNetSourceCodeSrcFiles) {
			dotNetVersion = dotNetSourceCodeFrameworkVersion;
			libdirs = dotNetSourceCodeLibdirs;
			addOptions = dotNetSourceCodeAddOptions;
			dotNetSrcFiles = dotNetSourceCodeSrcFiles;
		}

		public String getDotNetVersion() {
			return dotNetVersion;
		}

		public String getLibdirs() {
			return libdirs;
		}

		public String getAddOptions() {
			return addOptions;
		}

		public String getDotNetSrcFiles() {
			return dotNetSrcFiles;
		}
	}

	public interface BasicDotNetBuildTypeBlock {
	}

	public class BasicDotNetDevenvBuildTypeBlock implements BasicDotNetBuildTypeBlock {
		private String projects;
		private String addOptions;

		@DataBoundConstructor
		public BasicDotNetDevenvBuildTypeBlock(String dotNetDevenvProjects, String dotNetDevenvAddOptions) {
			projects = dotNetDevenvProjects;
			addOptions = dotNetDevenvAddOptions;
		}

		public String getProjects() {
			return projects;
		}

		public String getAddOptions() {
			return addOptions;
		}
	}

	public class BasicDotNetMSBuildBuildTypeBlock implements BasicDotNetBuildTypeBlock {
		private String projects;
		private String addOptions;

		@DataBoundConstructor
		public BasicDotNetMSBuildBuildTypeBlock(String dotNetMSBuildProjects, String dotNetMSBuildAddOptions) {
			projects = dotNetMSBuildProjects;
			addOptions = dotNetMSBuildAddOptions;
		}

		public String getProjects() {
			return projects;
		}

		public String getAddOptions() {
			return addOptions;
		}
	}

	public class BasicMaven3TranslationAppTypeBlock implements BasicTranslationAppTypeBlock {
		private String options;

		@DataBoundConstructor
		public BasicMaven3TranslationAppTypeBlock(String maven3Options) {
			options = maven3Options;
		}

		public String getOptions() {
			return options;
		}
	}

	public class BasicGradleTranslationAppTypeBlock implements BasicTranslationAppTypeBlock {
		private boolean useWrapper;
		private String tasks;
		private String options;

		@DataBoundConstructor
		public BasicGradleTranslationAppTypeBlock(boolean gradleUseWrapper, String gradleTasks, String gradleOptions) {
			useWrapper = gradleUseWrapper;
			tasks = gradleTasks;
			options = gradleOptions;
		}

		public boolean getUseWrapper() {
			return useWrapper;
		}

		public String getTasks() {
			return tasks;
		}

		public String getOptions() {
			return options;
		}
	}

	public class BasicOtherTranslationAppTypeBlock implements BasicTranslationAppTypeBlock {
		private String options;
		private String includesList;

		@DataBoundConstructor
		public BasicOtherTranslationAppTypeBlock(String otherOptions, String otherIncludesList) {
			options = otherOptions;
			includesList = otherIncludesList;
		}

		public String getOptions() {
			return options;
		}

		public String getIncludesList() {
			return includesList;
		}
	}

	public class RunScanBlock {
		private String customRulepacks;
		private String additionalOptions;
		private boolean debug;
		private boolean verbose;
		private String logFile;

		@DataBoundConstructor
		public RunScanBlock(String scanCustomRulepacks, String scanAddOptions, boolean scanDebug, boolean scanVerbose,
				String scanLogFile) {
			this.customRulepacks = scanCustomRulepacks != null ? scanCustomRulepacks.trim() : "";
			this.additionalOptions = scanAddOptions != null ? scanAddOptions.trim() : "";
			this.debug = scanDebug;
			this.verbose = scanVerbose;
			this.logFile = scanLogFile != null ? scanLogFile.trim() : "";
		}

		public String getScanCustomRulepacks() {
			return customRulepacks;
		}

		public String getScanAddOptions() {
			return additionalOptions;
		}

		public boolean getScanDebug() {
			return debug;
		}

		public boolean getScanVerbose() {
			return verbose;
		}

		public String getScanLogFile() {
			return logFile;
		}
	}

	public class UpdateContentBlock {
		private String updateServerUrl;
		private UseProxyBlock useProxy;

		@DataBoundConstructor
		public UpdateContentBlock(String updateServerUrl, UseProxyBlock updateUseProxy) {
			this.updateServerUrl = updateServerUrl != null ? updateServerUrl.trim() : "";
			this.useProxy = updateUseProxy;
		}

		public String getUpdateServerUrl() {
			return updateServerUrl;
		}

		public boolean getUpdateUseProxy() {
			return useProxy != null;
		}

		public String getUpdateProxyUrl() {
			return useProxy == null ? "" : useProxy.getProxyUrl();
		}

		public String getUpdateProxyUsername() {
			return useProxy == null ? "" : useProxy.getProxyUsername();
		}

		public String getUpdateProxyPassword() {
			return useProxy == null ? "" : useProxy.getProxyPassword();
		}
	}

	// possibly re-use for global SSC proxy setting with new constructor? Or just
	// re-use field names?
	public class UseProxyBlock {
		private String proxyUrl;
		private String proxyUsername;
		private String proxyPassword;

		@DataBoundConstructor
		public UseProxyBlock(String updateProxyUrl, String updateProxyUsername, String updateProxyPassword) {
			this.proxyUrl = updateProxyUrl != null ? updateProxyUrl.trim() : "";
			this.proxyUsername = updateProxyUsername != null ? updateProxyUsername.trim() : "";
			this.proxyPassword = updateProxyPassword != null ? updateProxyPassword.trim() : "";
		}

		public String getProxyUrl() {
			return proxyUrl;
		}

		public String getProxyUsername() {
			return proxyUsername;
		}

		public String getProxyPassword() {
			return proxyPassword;
		}
	}
}
