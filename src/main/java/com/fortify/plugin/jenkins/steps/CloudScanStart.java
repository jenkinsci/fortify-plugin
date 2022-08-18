/*******************************************************************************
 * (c) Copyright 2022 Micro Focus or one of its affiliates. 
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.fortify.plugin.jenkins.FortifyPlugin;
import com.fortify.plugin.jenkins.steps.remote.GradleProjectType;
import com.fortify.plugin.jenkins.steps.remote.MSBuildProjectType;
import com.fortify.plugin.jenkins.steps.remote.MavenProjectType;
import com.fortify.plugin.jenkins.steps.remote.PhpProjectType;
import com.fortify.plugin.jenkins.steps.remote.PythonProjectType;
import com.fortify.plugin.jenkins.steps.remote.RemoteAnalysisProjectType;
import com.google.common.collect.ImmutableSet;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ComboBoxModel;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;

public class CloudScanStart extends FortifyCloudScanStep implements SimpleBuildStep {
    private RemoteAnalysisProjectType remoteAnalysisProjectType;
    private FortifyPlugin.RemoteOptionalConfigBlock remoteOptionalConfig;
    private FortifyPlugin.UploadSSCBlock uploadSSC;

    private String buildID;

    @DataBoundConstructor
    public CloudScanStart(RemoteAnalysisProjectType remoteAnalysisProjectType) { this.remoteAnalysisProjectType = remoteAnalysisProjectType; }

    public RemoteAnalysisProjectType getRemoteAnalysisProjectType() { return remoteAnalysisProjectType; }

    public FortifyPlugin.RemoteOptionalConfigBlock getRemoteOptionalConfig() {
        return remoteOptionalConfig;
    }
    @DataBoundSetter
    public void setRemoteOptionalConfig(FortifyPlugin.RemoteOptionalConfigBlock remoteOptionalConfig) {
        this.remoteOptionalConfig = remoteOptionalConfig;
    }

    public FortifyPlugin.UploadSSCBlock getUploadSSC() {
        return uploadSSC;
    }
    @DataBoundSetter
    public void setUploadSSC(FortifyPlugin.UploadSSCBlock uploadSSC) {
        this.uploadSSC = uploadSSC;
    }

    public String getBuildTool() {
        if (remoteAnalysisProjectType == null) {
            return null;
        }
        if (getRemoteAnalysisProjectType() instanceof GradleProjectType) {
            return "gradle";
        } else if (getRemoteAnalysisProjectType() instanceof MavenProjectType) {
            return "mvn";
        } else if (getRemoteAnalysisProjectType() instanceof MSBuildProjectType) {
            return "msbuild";
        } else {
            return "none";
        }
    }

    public String getBuildFile() {
        if (getRemoteAnalysisProjectType() instanceof GradleProjectType) {
            return ((GradleProjectType)remoteAnalysisProjectType).getBuildFile();
        } else if (getRemoteAnalysisProjectType() instanceof MavenProjectType) {
            return ((MavenProjectType)remoteAnalysisProjectType).getBuildFile();
        } else if (getRemoteAnalysisProjectType() instanceof MSBuildProjectType) {
            return ((MSBuildProjectType)remoteAnalysisProjectType).getDotnetProject();
        } else {
            return "";
        }
    }

    public boolean isIncludeTests() {
        if (getRemoteAnalysisProjectType() instanceof GradleProjectType) {
            return ((GradleProjectType)remoteAnalysisProjectType).getIncludeTests();
        } else if (getRemoteAnalysisProjectType() instanceof MavenProjectType) {
            return ((MavenProjectType)remoteAnalysisProjectType).getIncludeTests();
        } else {
            return false;
        }
    }

    private boolean isExcludeDisabledProjects() {
        if (getRemoteAnalysisProjectType() instanceof MSBuildProjectType) {
            return ((MSBuildProjectType)remoteAnalysisProjectType).isExcludeDisabledProjects();
        }
        return false;
    }

    public boolean isSkipBuild() {
        if (getRemoteAnalysisProjectType() instanceof GradleProjectType) {
            return ((GradleProjectType)remoteAnalysisProjectType).getSkipBuild();
        } else if (getRemoteAnalysisProjectType() instanceof MavenProjectType) {
            return ((MavenProjectType)remoteAnalysisProjectType).getSkipBuild();
        }
        return false;
    }

    public String getSensorPoolName() {
        return getRemoteOptionalConfig() == null ? "" : getRemoteOptionalConfig().getSensorPoolUUID();
    }

    public String getEmailAddr() {
        return getRemoteOptionalConfig() == null ? "" : getRemoteOptionalConfig().getNotifyEmail();
    }

    public String getRulepacks() {
        return getRemoteOptionalConfig() == null ? "" : getRemoteOptionalConfig().getCustomRulepacks();
    }

    public String getFilterFile() {
        return getRemoteOptionalConfig() == null ? "" : getRemoteOptionalConfig().getFilterFile();
    }

    public String getApplicationName() {
        return getUploadSSC() == null ? "" : getUploadSSC().getAppName();
    }

    public String getApplicationVersion() {
        return getUploadSSC() == null ? "" : getUploadSSC().getAppVersion();
    }

    public String getPythonRequirementsFile() {
        return getRemoteAnalysisProjectType() instanceof PythonProjectType ? ((PythonProjectType)remoteAnalysisProjectType).getPythonRequirementsFile() : "";
    }

    public String getPythonVersion() {
        return getRemoteAnalysisProjectType() instanceof PythonProjectType ? ((PythonProjectType)remoteAnalysisProjectType).getPythonVersion() : "";
    }

    public String getPythonVirtualEnv() {
        return getRemoteAnalysisProjectType() instanceof PythonProjectType ? ((PythonProjectType)remoteAnalysisProjectType).getPythonVirtualEnv() : "";
    }

    public String getPhpVersion() {
        return getRemoteAnalysisProjectType() instanceof PhpProjectType ? ((PhpProjectType)remoteAnalysisProjectType).getPhpVersion() : "";
    }

    public String getBuildID() { return buildID; }

    public void setBuildID(String buildID) { this.buildID = buildID; }

    // resolved variables
    public String getResolvedBuildTool(TaskListener listener) {
        return resolve(getBuildTool(), listener);
    }

    public String getResolvedBuildFile(TaskListener listener) {
        return resolve(getBuildFile(), listener);
    }

    public String getResolvedSensorPoolName(TaskListener listener) {
        return resolve(getSensorPoolName(), listener);
    }

    public String getResolvedEmailAddr(TaskListener listener) {
        return resolve(getEmailAddr(), listener);
    }

    public String getResolvedRulepacks(TaskListener listener) {
        return resolve(getRulepacks(), listener);
    }

    public String getResolvedFilterFile(TaskListener listener) {
        return resolve(getFilterFile(), listener);
    }

    public String getResolvedBuildID(TaskListener listener) { return resolve(getBuildID(), listener); }

    public String getResolvedApplicationName(TaskListener listener) { return resolve(getApplicationName(), listener); }

    public String getResolvedApplicationVersion(TaskListener listener) { return resolve(getApplicationVersion(), listener); }

    public String getResolvedPythonRequirementsFile(TaskListener listener) { return resolve(getPythonRequirementsFile(), listener); }

    public String getResolvedPythonVersion(TaskListener listener) { return resolve(getPythonVersion(), listener); }

    public String getResolvedPythonVirtualEnv(TaskListener listener) { return resolve(getPythonVirtualEnv(), listener); }

    public String getResolvedPhpVersion(TaskListener listener) { return resolve(getPhpVersion(), listener); }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(this, context);
    }

    @Override
    public void perform(Run<?, ?> run, FilePath filePath, EnvVars vars, Launcher launcher, TaskListener taskListener) throws InterruptedException, IOException {
        setLastBuild(run);
        PrintStream log = taskListener.getLogger();
        log.println("Fortify Jenkins plugin v " + VERSION);
        log.println("Performing Fortify remote analysis");
        String projectRoot = filePath.child(".fortify").getRemote();
        String cloudscanExec;
        try {
            cloudscanExec = getScancentralExecutable(run, filePath, launcher, taskListener, vars);
        } catch (FileNotFoundException ex) {
            log.println("WARNING: Cannot find scancentral executable");
            try {
                cloudscanExec = getCloudScanExecutable(run, filePath, launcher, taskListener, vars);
            } catch (FileNotFoundException exception) {
                throw new RuntimeException("Cannot find cloudscan executable");
            }
        }

        ArrayList<Pair<String, Boolean>> args = new ArrayList<Pair<String, Boolean>>(30);
        args.add(Pair.of(cloudscanExec, Boolean.FALSE));

        /*
            if SSC is configured, use SSC's configuration to find the Controller
         */
        if (StringUtils.isNotBlank(FortifyPlugin.DESCRIPTOR.getUrl())) {
            args.add(Pair.of("-sscurl", Boolean.FALSE));
            args.add(Pair.of(FortifyPlugin.DESCRIPTOR.getUrl(), Boolean.FALSE));
            args.add(Pair.of("-ssctoken", Boolean.FALSE));
            args.add(Pair.of(FortifyPlugin.DESCRIPTOR.getToken(), Boolean.TRUE));
        } else if (StringUtils.isNotBlank(FortifyPlugin.DESCRIPTOR.getCtrlUrl())) {
            args.add(Pair.of("-url", Boolean.FALSE));
            args.add(Pair.of(FortifyPlugin.DESCRIPTOR.getCtrlUrl(), Boolean.FALSE));
        } else {
            throw new AbortException("Fortify remote analysis execution failed: No SSC or Controller URL found");
        }
        args.add(Pair.of("start", Boolean.FALSE));
        if (StringUtils.isNotEmpty(getResolvedBuildTool(taskListener))) {
            args.add(Pair.of("-bt", Boolean.FALSE));
            args.add(Pair.of(getResolvedBuildTool(taskListener), Boolean.FALSE));
            if (getResolvedBuildTool(taskListener).equals("none")) {
                if (StringUtils.isNotEmpty(getResolvedPhpVersion(taskListener))) {
                    args.add(Pair.of("-hv", Boolean.FALSE));
                    args.add(Pair.of(getResolvedPhpVersion(taskListener), Boolean.FALSE));
                }
                if (StringUtils.isNotEmpty(getResolvedPythonRequirementsFile(taskListener))) {
                    args.add(Pair.of("-pyr", Boolean.FALSE));
                    args.add(Pair.of(getResolvedPythonRequirementsFile(taskListener), Boolean.FALSE));
                }
                if (StringUtils.isNotEmpty(getResolvedPythonVersion(taskListener))) {
                    args.add(Pair.of("-yv", Boolean.FALSE));
                    args.add(Pair.of(getResolvedPythonVersion(taskListener), Boolean.FALSE));
                }
                if (StringUtils.isNotEmpty(getResolvedPythonVirtualEnv(taskListener))) {
                    args.add(Pair.of("-pyv", Boolean.FALSE));
                    args.add(Pair.of(getResolvedPythonVirtualEnv(taskListener), Boolean.FALSE));
                }
            } else {
                if (StringUtils.isNotEmpty(getResolvedBuildFile(taskListener))) {
                    args.add(Pair.of("-bf", Boolean.FALSE));
                    args.add(Pair.of(getResolvedBuildFile(taskListener), Boolean.FALSE));
                }
                if (isIncludeTests()) {
                    args.add(Pair.of("-t", Boolean.FALSE));
                }
                if (isExcludeDisabledProjects()) {
                    args.add(Pair.of("-exclude-disabled-projects", Boolean.FALSE));
                }
                if (isSkipBuild()) {
                    args.add(Pair.of("-skipBuild", Boolean.FALSE));
                }
            }
        } else {
            args.add(Pair.of("-b", Boolean.FALSE));
            args.add(Pair.of(getResolvedBuildID(taskListener), Boolean.FALSE));
            args.add(Pair.of("-project-root", Boolean.FALSE));
            args.add(Pair.of(projectRoot, Boolean.FALSE));
        }
        if (StringUtils.isNotEmpty(getResolvedEmailAddr(taskListener))) {
            args.add(Pair.of("-email", Boolean.FALSE));
            args.add(Pair.of(getResolvedEmailAddr(taskListener), Boolean.TRUE));
        }
        if (StringUtils.isNotEmpty(getResolvedSensorPoolName(taskListener))) {
            args.add(Pair.of("-pool", Boolean.FALSE));
            args.add(Pair.of(getResolvedSensorPoolName(taskListener), Boolean.FALSE));
        }
        if (StringUtils.isNotEmpty(getResolvedApplicationName(taskListener))) {
            args.add(Pair.of("-upload", Boolean.FALSE));
            args.add(Pair.of("-application", Boolean.FALSE));
            args.add(Pair.of(getResolvedApplicationName(taskListener), Boolean.FALSE));
            args.add(Pair.of("-version", Boolean.FALSE));
            args.add(Pair.of(getResolvedApplicationVersion(taskListener), Boolean.FALSE));
            args.add(Pair.of("-uptoken", Boolean.FALSE));
            args.add(Pair.of(FortifyPlugin.DESCRIPTOR.getCtrlToken(), Boolean.TRUE));
        }
        if (StringUtils.isNotEmpty(getResolvedRulepacks(taskListener))) {
            addAllArgumentsWithNoMasks(args, getResolvedRulepacks(taskListener), "-rules");
        }
        if (StringUtils.isNotEmpty(getResolvedFilterFile(taskListener))) {
            addAllArgumentsWithNoMasks(args, getResolvedFilterFile(taskListener), "-filter");
        }

        if (StringUtils.isEmpty(getResolvedBuildTool(taskListener))) {
            args.add(Pair.of("-scan", Boolean.FALSE));
            // additional SCA arguments come after the -scan option
            if (StringUtils.isNotEmpty(getResolvedScanArgs(taskListener))) {
                args.add(Pair.of(getResolvedScanArgs(taskListener), Boolean.FALSE));
            }
        }

		List<String> cmds = new ArrayList<String>(args.size());
		boolean[] masks = new boolean[args.size()];
		args.stream().forEach(p -> {
			cmds.add(p.getLeft());
			masks[args.indexOf(p)] = p.getRight();
		});
		Launcher.ProcStarter p = launcher.launch().cmds(cmds).masks(masks).envs(vars).stdout(log).stderr(log).pwd(filePath);
		int exitcode = p.start().join();
		log.println("Fortify remote analysis completed with exit code: " + exitcode);

        if (exitcode != 0) {
            run.setResult(Result.FAILURE);
            throw new AbortException("Fortify remote analysis execution failed.");
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getFunctionName() {
            return "fortifyRemoteAnalysis";
        }

        @Override
        public String getDisplayName() {
            return "Upload a project for remote Fortify SCA analysis";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, EnvVars.class, Launcher.class, TaskListener.class);
        }

        public void doRefreshProjects(StaplerRequest req, StaplerResponse rsp, @QueryParameter String value)
                throws Exception {
            FortifyPlugin.DESCRIPTOR.doRefreshProjects(req, rsp, value);
        }

        public void doRefreshVersions(StaplerRequest req, StaplerResponse rsp, @QueryParameter String value)
                throws Exception {
            FortifyPlugin.DESCRIPTOR.doRefreshVersions(req, rsp, value);
        }

        public ComboBoxModel doFillAppNameItems() {
            return FortifyPlugin.DESCRIPTOR.doFillAppNameItems();
        }

        public ComboBoxModel doFillAppVersionItems(@QueryParameter String appName) {
            return FortifyPlugin.DESCRIPTOR.doFillAppVersionItems(appName);
        }

        public ListBoxModel doFillSensorPoolUUIDItems() {
            return FortifyPlugin.DESCRIPTOR.doFillSensorPoolUUIDItems();
        }

    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private transient CloudScanStart csStart;

        protected Execution(CloudScanStart csStart, StepContext context) {
            super(context);
            this.csStart = csStart;
        }

        @Override
        protected Void run() throws Exception {
            StepContext context = getContext();
            context.get(TaskListener.class).getLogger().println("Running Fortify remote analysis step");
            if (!context.get(FilePath.class).exists()) {
                context.get(FilePath.class).mkdirs();
            }
            csStart.perform(context.get(Run.class), context.get(FilePath.class), context.get(EnvVars.class),
                    context.get(Launcher.class), context.get(TaskListener.class));
            return null;
        }

        private static final long serialVersionUID = 1L;
    }

}
