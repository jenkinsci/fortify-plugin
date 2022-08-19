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
package com.fortify.plugin.jenkins.steps;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.fortify.plugin.jenkins.FPRSummary;
import com.fortify.plugin.jenkins.FortifyPlugin;
import com.fortify.plugin.jenkins.FortifyUploadBuildAction;
import com.fortify.plugin.jenkins.Messages;
import com.fortify.plugin.jenkins.PathUtils;
import com.fortify.plugin.jenkins.RemoteService;
import com.fortify.plugin.jenkins.TableAction;
import com.fortify.plugin.jenkins.bean.GroupingProfile;
import com.fortify.plugin.jenkins.bean.GroupingValueBean;
import com.fortify.plugin.jenkins.bean.IssueBean;
import com.fortify.plugin.jenkins.bean.IssueFolderBean;
import com.fortify.plugin.jenkins.fortifyclient.FortifyClient;
import com.fortify.ssc.restclient.model.Artifact;
import com.google.common.collect.ImmutableSet;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.StreamBuildListener;
import hudson.model.TaskListener;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

public class FortifyUpload extends FortifyStep implements Serializable {
	private static final long serialVersionUID = -8308672776705963290L;

	private boolean accessToProject = true;
	private int pageSize = 0;

	private boolean isPipeline = true;

	private String resultsFile;
	private String filterSet;
	private String failureCriteria;
	private String appName;
	private String appVersion;
	private String timeout;
	private String pollingInterval;

	public FortifyUpload(boolean isPipeline, String appName, String appVersion) {
		this(appName, appVersion);
		this.isPipeline = isPipeline;
	}

	@DataBoundConstructor
	public FortifyUpload(String appName, String appVersion) {
		super();
		this.appName = appName != null ? appName.trim() : "";
		this.appVersion = appName != null ? appVersion.trim() : "";
	}

	public String getAppName() {
		return appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	@DataBoundSetter
	public void setResultsFile(String resultsFile) {
		this.resultsFile = resultsFile;
	}

	public String getResultsFile() {
		return resultsFile;
	}

	@DataBoundSetter
	public void setFilterSet(String filterSet) {
		this.filterSet = filterSet != null ? filterSet.trim() : "";
	}

	public String getFilterSet() {
		return filterSet;
	}

	@DataBoundSetter
	public void setFailureCriteria(String failureCriteria) {
		this.failureCriteria = failureCriteria != null ? failureCriteria.trim() : "";
	}

	public String getFailureCriteria() {
		return failureCriteria;
	}

	public String getTimeout() {
		return timeout;
	}

	@DataBoundSetter
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	@DataBoundSetter
	public void setPollingInterval(String pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	public String getPollingInterval() {
		return pollingInterval;
	}

	public boolean isPipeline() {
		return isPipeline;
	}

	public String getResolvedFpr(TaskListener listener) {
		String s = resolve(getResultsFile(), listener);
		return PathUtils.appendExtentionIfNotEmpty(s, ".fpr", new String[] { ".fpr", ".zip" }); // .zip is for third party results
	}

	public String getResolvedAppName(TaskListener listener) {
		return resolve(getAppName(), listener);
	}

	public String getResolvedAppVersion(TaskListener listener) {
		return resolve(getAppVersion(), listener);
	}

	public String getResolvedFilterSet(TaskListener listener) {
		return resolve(getFilterSet(), listener);
	}

	public String getResolvedFailureCriteria(TaskListener listener) {
		return resolve(getFailureCriteria(), listener);
	}

	public Integer getResolvedTimeout(TaskListener listener) {
		if (getTimeout() != null) {
			try {
				return Integer.parseInt(resolve(String.valueOf(getTimeout()), listener));
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	public Integer getResolvedPollingInterval(TaskListener listener) {
		if (getPollingInterval() != null) {
			try {
				return Integer.parseInt(resolve(String.valueOf(getPollingInterval()), listener));
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
		return true;
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new Execution(this, context);
	}

	@Override
	public void perform(Run<?, ?> run, FilePath workspace, EnvVars vars, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
		performAndReturnIssueCount(run, workspace, vars, launcher, listener);
	}

	public Integer performAndReturnIssueCount(Run<?, ?> run, FilePath workspace, EnvVars vars, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
		PrintStream log = listener.getLogger();
		setLastBuild(run);
		RemoteService service = new RemoteService(getResolvedFpr(listener));
		FPRSummary summary = workspace.act(service);
		Long artifactId = uploadToSSC(summary, workspace, listener);
		pollFprProcessing(run, artifactId, listener);

		log.println("Retrieving build statistics from SSC");
		calculateFprStatistics(summary, listener);

		log.printf("Calculated NVS=%f, failedCount=%d%n", summary.getNvs(), summary.getFailedCount());

		// save data under the builds directory, this is always in Jenkins master node
		log.println("Saving build summary");
		if (isPipeline) {
			summary.save(run.getRootDir(), getResolvedAppName(listener), getResolvedAppVersion(listener));
		} else {
			summary.save(run.getRootDir(), null, null);
		}

		// now check if the fail count
		if (summary.getFailedCount() > 0) {
			log.printf(
					"FortifyJenkins plugin: this build is considered unstable because Fail Condition met %d vulnerabilities%n",
					summary.getFailedCount());
			run.setResult(Result.UNSTABLE);
		}

		String appName = getResolvedAppName(listener);
		String appVersion = getResolvedAppVersion(listener);
		FortifyUploadBuildAction buildAction = run.getAction(FortifyUploadBuildAction.class);
		if (buildAction == null) {
			buildAction = new FortifyUploadBuildAction();
			run.addAction(buildAction);
		}
		buildAction.addAppVersion(run.getParent(), this, appName, appVersion);
		return summary.getFailedCount();
	}

	/**
	 * 
	 * @param workspace
	 * @param listener
	 * @return - artifact id of uploaded fpr
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private Long uploadToSSC(FPRSummary summary, FilePath workspace, TaskListener listener) throws InterruptedException, IOException {
		PrintStream log = listener.getLogger();
		log.println("Fortify Jenkins plugin v " + VERSION);
		log.println("Performing Fortify upload process");
		// calling the remote slave to retrieve the FPR
		String logMsg = summary.getLogMessage();
		if (!StringUtils.isBlank(logMsg))
			log.println(logMsg);

		// if FPR is a remote FilePath, copy to local
		File localFPR = null;
		if (summary.getFprFile().isRemote()) {
			localFPR = copyToLocalTmp(summary.getFprFile());
		} else {
			localFPR = new File(summary.getFprFile().toURI());
		}
		log.printf("Using analysis results file: %s%n", summary.getFprFile().toURI());
		// if ( summary.getFprFile().isRemote() )
		//log.printf("Local analysis results file: %s%n", localFPR.getCanonicalFile());

		// if the application ID is not null, then we need to upload the FPR to SSC
		Long artifactId = null;

		if (!StringUtils.isBlank(getResolvedAppName(listener)) && !StringUtils.isBlank(getResolvedAppVersion(listener))
				&& FortifyPlugin.DESCRIPTOR.canUploadToSsc()) {
			// the FPR may be in remote slave, we need to call launcher to do this for me
			log.printf("Uploading analysis results file to SSC at %s to application '%s' and application version '%s'%n",
					FortifyPlugin.DESCRIPTOR.getUrl(), getResolvedAppName(listener), getResolvedAppVersion(listener));
			try {
				final Long projectId = createNewOrGetProject(listener);
				final File fpr = localFPR;
				artifactId = FortifyPlugin.runWithFortifyClient(FortifyPlugin.DESCRIPTOR.getToken(),
						new FortifyClient.Command<Long>() {
							@Override
							public Long runWith(FortifyClient client) throws Exception {
								return client.uploadFPR(fpr, projectId);
							}
						});
				log.printf("Analysis results uploaded successfully. artifact id = %d%n", artifactId);
				return artifactId;
			} catch (Throwable t) {
				log.println("Error uploading to SSC: " + FortifyPlugin.DESCRIPTOR.getUrl());
				String message = t.getMessage();
				log.println(message);
				t.printStackTrace(log);
				throw new AbortException("Error uploading to SSC: " + message);
			} finally {
				// if this is a remote FPR, I need to delete the local temp FPR after use
				if (summary.getFprFile().isRemote()) {
					if (null != localFPR && localFPR.exists()) {
						try {
							boolean deleted = localFPR.delete();
							if (!deleted)
								log.printf("Can't delete local analysis results file: %s%n", localFPR.getCanonicalFile());
						} catch (Exception e) {
							e.printStackTrace(log);
						}
					}
				}
			}
		} else {
			log.printf(
					"Analysis results file uploading was skipped. Some of the required settings are not specified: Application Name='%s', Application Version='%s', serverUrl='%s', authenticationToken='%s'%n",
					getResolvedAppName(listener), getResolvedAppVersion(listener), FortifyPlugin.DESCRIPTOR.getUrl(),
					FortifyPlugin.DESCRIPTOR.getToken());
			throw new AbortException("Analysis results uploading was skipped. Some of the required settings are not specified.");
		}
	}

	private void pollFprProcessing(Run<?, ?> run, Long artifactId, TaskListener listener) throws IOException {
		PrintStream log = listener.getLogger();
		boolean isProcessingComplete = false;

		int timeoutInMinutes = (getResolvedTimeout(listener) != null) ? getResolvedTimeout(listener) : 0;
		int timeoutInMillis = timeoutInMinutes * 60 * 1000;
		long timeoutAfter = System.currentTimeMillis() + timeoutInMillis;

		while (!isProcessingComplete) {
			int sleep = (getResolvedPollingInterval(listener) != null) ? getResolvedPollingInterval(listener) : 1;
			log.printf("Sleep for %d minute(s)%n", sleep);
			sleep = sleep * 60 * 1000; // wait time is in minute(s)
			long sleepUntil = System.currentTimeMillis() + sleep;
			while (true) {
				long diff = sleepUntil - System.currentTimeMillis();
				if (diff > 0) {
					try {
						Thread.sleep(diff);
					} catch (InterruptedException e) {
					}
				} else {
					break;
				}
			}
			try {
				final Long artifactIdFinal = artifactId;
				Artifact.StatusEnum status = FortifyPlugin.runWithFortifyClient(FortifyPlugin.DESCRIPTOR.getToken(),
						new FortifyClient.Command<Artifact.StatusEnum>() {
							@Override
							public Artifact.StatusEnum runWith(FortifyClient client) throws Exception {
								Artifact artifact = client.getArtifactInfo(artifactIdFinal);
								return artifact.getStatus();
							}
						});
				switch (status) {
				case PROCESS_COMPLETE:
					isProcessingComplete = true;
					break;
				case ERROR_PROCESSING:
					throw new AbortException("SSC encountered an error processing the artifact");
				case REQUIRE_AUTH:
					log.println("The artifact needs to be approved for processing in SSC.  Will continue to wait...");
					break;
				default:
					log.println("Unexpected artifact status: " + status.name());
					isProcessingComplete = true;
					break;
				}
				if (isProcessingComplete) {
					break;
				}
			} catch (Throwable t) {
				String message = t.getMessage();
				log.println("Error checking artifact status. " + message);
				t.printStackTrace(log);
				throw new AbortException("Failed to retrieve artifact statistics from SSC. " + message);
			}

			if (timeoutInMinutes != 0) {
				long diff = timeoutAfter - System.currentTimeMillis();
				if (diff <= 0) {
					setBuildUncompleted(run, log, timeoutInMinutes);
				}
			}
		}
	}

	private void setBuildUncompleted(Run<?, ?> run, PrintStream log, int timeoutInMinutes) throws IOException {
		final long projectVersionId = getProjectVersionId(log);
		final String appArtifactsURL = getAppArtifactsURL(projectVersionId);

		run.setResult(Result.NOT_BUILT);
		run.setDescription("A timeout has been reached when checking SSC for status of artifacts, this could happen " +
				"on long running processing jobs and does not mean that the build failed. You can check the status in SSC here: " +
				appArtifactsURL);
		throw new AbortException("Timeout of " + timeoutInMinutes + " minute(s) is reached.");
	}

	private long getProjectVersionId(PrintStream log) throws AbortException {
		long projectVersionId;
		try {
			projectVersionId = FortifyPlugin.runWithFortifyClient(FortifyPlugin.DESCRIPTOR.getToken(),
					new FortifyClient.Command<Long>() {
						@Override
						public Long runWith(FortifyClient client) throws Exception {
							return client.getProjectVersionId(appName, appVersion);
						}
					});
		} catch (Exception e) {
			e.printStackTrace(log);
			throw new AbortException("Error occurred during FPR polling: " + e.getMessage());
		}
		return projectVersionId;
	}

	private String getAppArtifactsURL(Long projectVersionId) {
		return FortifyPlugin.DESCRIPTOR.getUrl() + "/html/ssc/version/" + projectVersionId + "/artifacts";
	}

	private void calculateFprStatistics(FPRSummary summary, TaskListener listener) {
		listener = listener == null ? new StreamBuildListener(System.out, Charset.defaultCharset()) : listener;
		PrintStream log = listener.getLogger();
		// NVS CALCULATIONS
		// NVS = Normalized Vulnerability Score
		// NVS =
		// ((((CFPO*10)+(HFPO*5)+(MFPO*1)+(LFPO*0.1))*.5)+(((P1*2)+(P2*4)+(P3*16)+(PABOVE*64))*.5))/(ExecutableLOC/1000)

		int CFPO = 0; // Number of Critical Vulnerabilities (unless marked as "Not an Issue")
		int HFPO = 0; // Number of High Vulnerabilities (unless marked as "Not an Issue")
		int MFPO = 0; // Number of Medium Vulnerabilities (unless marked as "Not an Issue")
		int LFPO = 0; // Number of Low Vulnerabilities (unless marked as "Not an Issue")

		int PABOVE = 0; // Exploitable
		int P3 = 0; // Suspicious
		int P2 = 0; // Bad Practice
		int P1 = 0; // Reliability Issue

		int failedCount = 0;
		int totalIssues = 0;

		List<IssueFolderBean> folders = getFolders(listener);
		Long versionId = null;
		try {
			versionId = createNewOrGetProject(listener);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (IssueFolderBean folder : folders) {
			log.printf("Processing folder = %s ...%n", folder.getName());

			if (IssueFolderBean.NAME_CRITICAL.equals(folder.getName())
					|| IssueFolderBean.NAME_HOT.equals(folder.getName())) {
				List<GroupingValueBean> groupingValues = getGroupingValues(versionId, folder.getId(), null,
						GroupingValueBean.GROUPING_TYPE_ANALYSIS, listener);
				log.printf("Got %d audit values for folder = %s%n", groupingValues.size(), folder.getName());

				for (GroupingValueBean group : groupingValues) {
					if (GroupingValueBean.ID_NOT_AN_ISSUE.equals(group.getName())) {
						continue;
					}
					CFPO += group.getTotalCount();
				}
			} else if (IssueFolderBean.NAME_HIGH.equals(folder.getName())
					|| IssueFolderBean.NAME_WARNING.equals(folder.getName())) {
				List<GroupingValueBean> groupingValues = getGroupingValues(versionId, folder.getId(), null,
						GroupingValueBean.GROUPING_TYPE_ANALYSIS, listener);
				log.printf("Got %d audit values for folder = %s%n", groupingValues.size(), folder.getName());

				for (GroupingValueBean group : groupingValues) {
					if (GroupingValueBean.ID_NOT_AN_ISSUE.equals(group.getName())) {
						continue;
					}
					HFPO += group.getTotalCount();
				}
			} else if (IssueFolderBean.NAME_MEDIUM.equals(folder.getName())) {
				List<GroupingValueBean> groupingValues = getGroupingValues(versionId, folder.getId(), null,
						GroupingValueBean.GROUPING_TYPE_ANALYSIS, listener);
				log.printf("Got %d audit values for folder = %s%n", groupingValues.size(), folder.getName());

				for (GroupingValueBean group : groupingValues) {
					if (GroupingValueBean.ID_NOT_AN_ISSUE.equals(group.getName())) {
						continue;
					}
					MFPO += group.getTotalCount();
				}
			} else if (IssueFolderBean.NAME_LOW.equals(folder.getName())
					|| IssueFolderBean.NAME_INFO.equals(folder.getName())) {
				List<GroupingValueBean> groupingValues = getGroupingValues(versionId, folder.getId(), null,
						GroupingValueBean.GROUPING_TYPE_ANALYSIS, listener);
				log.printf("Got %d audit values for folder = %s%n", groupingValues.size(), folder.getName());

				for (GroupingValueBean group : groupingValues) {
					if (GroupingValueBean.ID_NOT_AN_ISSUE.equals(group.getName())) {
						continue;
					}
					LFPO += group.getTotalCount();
				}
			} else if (IssueFolderBean.ATTRIBUTE_VALUE_ALL.equals(folder.getName())) {
				List<GroupingValueBean> groupingValues = getGroupingValues(versionId, folder.getId(), null,
						GroupingValueBean.GROUPING_TYPE_ANALYSIS, listener);
				log.printf("Got %d audit values for folder = %s%n", groupingValues.size(), folder.getName());

				totalIssues = folder.getIssueCount();

				for (GroupingValueBean group : groupingValues) {
					if (group.getName().startsWith(GroupingValueBean.ID_EXPLOITABLE)) {
						PABOVE += group.getTotalCount();
					} else if (group.getName().startsWith(GroupingValueBean.ID_SUSPICIOUS)) {
						P3 += group.getTotalCount();
					} else if (group.getName().startsWith(GroupingValueBean.ID_BAD_PRACTICE)) {
						P2 += group.getTotalCount();
					} else if (group.getName().startsWith(GroupingValueBean.ID_RELIABILITY)) {
						P1 += group.getTotalCount();
					}
				}

				if (!StringUtils.isBlank(getResolvedFailureCriteria(listener))) {
					List<GroupingValueBean> groupingValuesByCondition = getGroupingValues(versionId, folder.getId(),
							getFailureCriteria(), GroupingValueBean.GROUPING_TYPE_ANALYSIS, listener);
					log.printf("Got %d grouping values for folder = %s, condition = '%s'%n", groupingValues.size(),
							folder.getName(), getFailureCriteria());

					for (GroupingValueBean group : groupingValuesByCondition) {
						if (GroupingValueBean.ID_NOT_AN_ISSUE.equals(group.getName())) {
							continue;
						}
						failedCount += group.getTotalCount();
					}
				}
			}
		}

		double NVS = ((((CFPO * 10.) + (HFPO * 5) + (MFPO * 1.) + (LFPO * 0.1)) * .5)
				+ (((P1 * 2.) + (P2 * 4.) + (P3 * 16.) + (PABOVE * 64.)) * .5)) / 1.0;// (ExecutableLOC/1000);

		summary.setNvs(NVS);
		summary.setFailedCount(failedCount);
		summary.setTotalIssues(totalIssues);
		summary.setFolderBeans(folders);
	}

	public List<GroupingValueBean> getGroupingValues(final Long versionId, final String folderId,
			final String searchCondition, final String groupingName, final TaskListener taskListener) {
		final TaskListener listener = taskListener != null ? taskListener
				: new StreamBuildListener(System.out, Charset.defaultCharset());
		if (FortifyPlugin.DESCRIPTOR.canUploadToSsc()) {
			PrintStream logger = listener.getLogger();
			try {
				final Writer log = new OutputStreamWriter(logger, "UTF-8");
				Map<String, List<String>> map = FortifyPlugin.runWithFortifyClient(FortifyPlugin.DESCRIPTOR.getToken(),
						new FortifyClient.Command<Map<String, List<String>>>() {
							@Override
							public Map<String, List<String>> runWith(FortifyClient client) throws Exception {
								return client.getGroupingValues(versionId == null ? Long.valueOf(Long.MIN_VALUE) : versionId,
										folderId, getResolvedFilterSet(listener),
										searchCondition == null ? "" : searchCondition, groupingName,
										new PrintWriter(log, true));
							}
						});
				List<GroupingValueBean> list = new ArrayList<GroupingValueBean>(map.size());
				for (Map.Entry<String, List<String>> entry : map.entrySet()) {
					List<String> attributes = entry.getValue();
					if (attributes.size() == 5) {
						GroupingValueBean next = new GroupingValueBean(entry.getKey(), folderId, attributes);
						list.add(next);
					}
				}

				// log.printf("Obtained %d grouping values for folder = %s, search = %s for '%s
				// (%s)'%n", list.size(), folderId, searchCondition,
				// getResolvedAppName(listener), getResolvedAppVersion(listener));

				return list;
			} catch (Throwable e) {
				logger.println(e.getMessage());
				e.printStackTrace(logger);
			}
		}
		return Collections.emptyList();
	}

	public List<IssueFolderBean> getFolders(final TaskListener taskListener) {
		final TaskListener listener = taskListener != null ? taskListener : new StreamBuildListener(System.out, Charset.defaultCharset());
		accessToProject = true;
		getResolvedAppVersion(listener);
		if (FortifyPlugin.DESCRIPTOR.canUploadToSsc()) {
			PrintStream logger = listener.getLogger();
			try {
				final Writer log = new OutputStreamWriter(logger, "UTF-8");
				final Long versionId = createNewOrGetProject(listener);
				Map<String, List<String>> map = FortifyPlugin.runWithFortifyClient(FortifyPlugin.DESCRIPTOR.getToken(),
						new FortifyClient.Command<Map<String, List<String>>>() {
							@Override
							public Map<String, List<String>> runWith(FortifyClient client) throws Exception {
								return client.getFolderIdToAttributesList(
										versionId == null ? Long.valueOf(Long.MIN_VALUE) : versionId, getResolvedFilterSet(listener),
										new PrintWriter(log, true));
							}
						});
				List<IssueFolderBean> list = new ArrayList<IssueFolderBean>(map.size());
				for (Map.Entry<String, List<String>> entry : map.entrySet()) {
					List<String> attributes = entry.getValue();
					if (attributes.size() == 5) {
						list.add(new IssueFolderBean(entry.getKey(), getResolvedAppName(listener), getResolvedAppVersion(listener), attributes));
					}
				}

				return list;
			} catch (Throwable e) {
				String message = e.getMessage();
				if (message.toLowerCase().contains(("access denied"))) {
					accessToProject = false;
				}
				logger.println(message);
				e.printStackTrace(logger);
			}
		}
		return Collections.emptyList();
	}

	public List<GroupingProfile> getGroupingProfiles(final TaskListener taskListener) {
		final TaskListener listener = taskListener != null ? taskListener : new StreamBuildListener(System.out, Charset.defaultCharset());
		accessToProject = true;
		if (FortifyPlugin.DESCRIPTOR.canUploadToSsc()) {
			PrintStream logger = listener.getLogger();
			try {
				final Writer log = new OutputStreamWriter(logger, "UTF-8");
				final Long versionId = createNewOrGetProject(listener);
				List<GroupingProfile> groupingProfiles = FortifyPlugin.runWithFortifyClient(FortifyPlugin.DESCRIPTOR.getToken(),
						new FortifyClient.Command<List<GroupingProfile>>() {
							@Override
							public List<GroupingProfile> runWith(FortifyClient client) throws Exception {
								return client.getGroupingProfiles(versionId == null ? Long.valueOf(Long.MIN_VALUE) : versionId,
										getResolvedFilterSet(listener), new PrintWriter(log, true));
							}
						});
				return groupingProfiles;
			} catch (Throwable e) {
				String message = e.getMessage();
				if (message.toLowerCase().contains(("access denied"))) {
					accessToProject = false;
				}
				logger.println(message);
				e.printStackTrace(logger);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Downloads issues from one of folders on Fortify SSC server
	 * 
	 * @param folderId
	 *            to get available folders use {@link #getFolders(TaskListener)}, to
	 *            get all issues use the folder with 'All' name
	 * @param startPage
	 *            to get refined list of issues, starts with 0, used with pageSize.
	 *            For startPage=3 and pageSize=10 you will get 10 issues from 30 to
	 *            40.
	 * @param pageSize
	 *            to get only this number of issues. Use '-1' to get all issues
	 *            available.
	 * @param sortOrder
	 *            see {@link com.fortify.plugin.jenkins.TableAction.SortOrder} to
	 *            see the list of available sort orders
	 * @param downNotUp
	 *            to reverse sort order if sortOrder is specified
	 * @return list of issues from Fortify SSC server
	 */
	public List<IssueBean> getIssuesByFolder(final String folderId, final int startPage, final int pageSize,
			final TableAction.SortOrder sortOrder, final boolean downNotUp, final boolean showingAllNotNew,
			final String selectedGrouping, final TaskListener taskListener) {
		if (FortifyPlugin.DESCRIPTOR.canUploadToSsc()) {
			final TaskListener listener = taskListener != null ? taskListener
					: new StreamBuildListener(System.out, Charset.defaultCharset());
			PrintStream logger = listener.getLogger();
			try {
				final Writer log = new OutputStreamWriter(logger, "UTF-8");
				final Long versionId = createNewOrGetProject(listener);

				Map<String, IssueBean> map = FortifyPlugin.runWithFortifyClient(FortifyPlugin.DESCRIPTOR.getToken(),
						new FortifyClient.Command<Map<String, IssueBean>>() {
							@Override
							public Map<String, IssueBean> runWith(FortifyClient client) throws Exception {
								return client.getIssuesByFolderId(versionId == null ? Long.valueOf(Long.MIN_VALUE) : versionId,
										folderId, startPage, pageSize, getResolvedFilterSet(listener), selectedGrouping,
										sortOrder.getModelSorting() == null ? "" : sortOrder.getModelSorting(),
										Boolean.valueOf(downNotUp), Boolean.valueOf(showingAllNotNew),
										new PrintWriter(log, true));
							}
						});
				List<IssueBean> list = new ArrayList<IssueBean>(map.size());

				for (Map.Entry<String, IssueBean> issueInstanceEntry : map.entrySet()) {
					IssueBean issueBean = issueInstanceEntry.getValue();
					issueBean.setProjectName(getResolvedAppName(listener));
					issueBean.setProjectVersionName(getResolvedAppVersion(listener));
					list.add(issueBean);
				}

				List<IssueBean> results = list;
				return results;
			} catch (Throwable e) {
				logger.println(e.getMessage());
				e.printStackTrace(logger);
			}
		}
		return Collections.emptyList();
	}

	private Long createNewOrGetProject(final TaskListener taskListener) throws Exception {
		final TaskListener listener = taskListener == null
				? new StreamBuildListener(System.out, Charset.defaultCharset())
				: taskListener;
		final Writer log = new OutputStreamWriter(listener.getLogger(), "UTF-8");
		Long versionId = FortifyPlugin.runWithFortifyClient(FortifyPlugin.DESCRIPTOR.getToken(), new FortifyClient.Command<Long>() {
			@Override
			public Long runWith(FortifyClient client) throws Exception {
				return client.createProject(getResolvedAppName(listener), getResolvedAppVersion(listener),
						FortifyPlugin.DESCRIPTOR.getProjectTemplate(), Collections.<String, String>emptyMap(),
						new PrintWriter(log, true));
			}
		});
		return versionId;
	}

	public boolean getAccessToProject() {
		return accessToProject;
	}

	public boolean isSettingUpdated() {
		return FortifyPlugin.DESCRIPTOR.isSettingUpdated();
	}

	public int getIssuePageSize() {
		Integer breakdownPageSize = FortifyPlugin.DESCRIPTOR.getBreakdownPageSize();
		if (null == breakdownPageSize) {
			breakdownPageSize = FortifyPlugin.DEFAULT_PAGE_SIZE;
		}
		if (pageSize == 0) {
			pageSize = breakdownPageSize;
		}
		return pageSize;
	}

	public void setIssuePageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	@Extension
	public static class DescriptorImpl extends StepDescriptor {
		@Override
		public String getDisplayName() {
			return Messages.FortifyUpload_DisplayName();
		}

		@Override
		public String getFunctionName() {
			return "fortifyUpload";
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return ImmutableSet.of(Run.class, FilePath.class, EnvVars.class, Launcher.class, TaskListener.class);
		}

		public ComboBoxModel getApplicationNameItems() {
			return FortifyPlugin.DESCRIPTOR.getAppNameItems();
		}

		public ComboBoxModel getApplicationVersionItems(@QueryParameter String applicationName) {
			return FortifyPlugin.DESCRIPTOR.getAppVersionItems(applicationName);
		}

		public void doRefreshApplications(StaplerRequest req, StaplerResponse rsp, @QueryParameter String value) throws Exception {
			FortifyPlugin.DESCRIPTOR.doRefreshProjects(req, rsp, value);
		}

		public void doRefreshVersions(StaplerRequest req, StaplerResponse rsp, @QueryParameter String value) throws Exception {
			FortifyPlugin.DESCRIPTOR.doRefreshVersions(req, rsp, value);
		}

		public ListBoxModel doFillFilterSetItems(@QueryParameter String appName, @QueryParameter String appVersion) {
			return FortifyPlugin.DESCRIPTOR.doFillFilterSetItems(appName, appVersion);
		}

		public FormValidation doCheckAppName(@QueryParameter String value) {
			return Validators.checkFieldNotEmpty(value);
		}

		public FormValidation doCheckAppVersion(@QueryParameter String value) {
			return Validators.checkFieldNotEmpty(value);
		}

		public FormValidation doCheckUploadWaitTime(@QueryParameter String value) {
			return Validators.checkValidInteger(value);
		}

	}

	private File copyToLocalTmp(FilePath file) throws IOException, InterruptedException {
		File tmpFile = new File(Util.createTempDir(), file.getName());
		FilePath tmpFP = new FilePath(tmpFile);
		file.copyTo(tmpFP);
		return tmpFile;
	}

	private static class Execution extends SynchronousNonBlockingStepExecution<Integer> {
		private FortifyUpload upload;

		protected Execution(FortifyUpload upload, StepContext context) {
			super(context);
			this.upload = upload;
		}

		@Override
		protected Integer run() throws Exception {
			StepContext context = getContext();
			TaskListener listener = context.get(TaskListener.class);
			listener.getLogger().println("Running FortifyUpload step");
			return upload.performAndReturnIssueCount(context.get(Run.class), context.get(FilePath.class), context.get(EnvVars.class),
					context.get(Launcher.class), listener);
		}

		private static final long serialVersionUID = 1L;
	}
}
