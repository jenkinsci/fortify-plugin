package com.fortify.plugin.jenkins.steps;

import com.fortify.plugin.jenkins.FortifyPlugin;
import com.fortify.plugin.jenkins.steps.remote.*;
import com.google.common.collect.ImmutableSet;
import hudson.*;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ComboBoxModel;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Set;

public class CloudScanStart extends FortifyCloudScanStep implements SimpleBuildStep {
    private RemoteAnalysisProjectType remoteAnalysisProjectType;
    private FortifyPlugin.RemoteOptionalConfigBlock remoteOptionalConfig;
    private FortifyPlugin.UploadSSCBlock uploadSSC;

    private String mbsFile;
    private boolean createMbs;
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
        if (getRemoteAnalysisProjectType() instanceof Gradle) {
            return "gradle";
        } else if (getRemoteAnalysisProjectType() instanceof Maven) {
            return "mvn";
        } else {
            return "none";
        }
    }

    public String getBuildFile() {
        if (getRemoteAnalysisProjectType() instanceof Gradle) {
            return ((Gradle)remoteAnalysisProjectType).getBuildFile();
        } else if (getRemoteAnalysisProjectType() instanceof Maven) {
            return ((Maven)remoteAnalysisProjectType).getBuildFile();
        } else {
            return "";
        }
    }

    public boolean isIncludeTests() {
        if (getRemoteAnalysisProjectType() instanceof Gradle) {
            return ((Gradle)remoteAnalysisProjectType).getIncludeTests();
        } else if (getRemoteAnalysisProjectType() instanceof Maven) {
            return ((Maven)remoteAnalysisProjectType).getIncludeTests();
        } else {
            return false;
        }
    }

    public String getMbsFile() {
        return mbsFile;
    }

    public void setMbsFile(String mbsFile) {
        this.mbsFile = mbsFile;
    }

    public boolean isCreateMbs() {
        return createMbs;
    }

    public void setCreateMbs(boolean createMbs) {
        this.createMbs = createMbs;
    }

    public String getSensorPoolName() {
        return getRemoteOptionalConfig() == null ? "" : getRemoteOptionalConfig().getSensorPoolName();
    }

    public String getEmailAddr() {
        return getRemoteOptionalConfig() == null ? "" : getRemoteOptionalConfig().getEmailAddr();
    }

    public String getRulepacks() {
        return getRemoteOptionalConfig() == null ? "" : getRemoteOptionalConfig().getRulepacks();
    }

    public String getFilterFile() {
        return getRemoteOptionalConfig() == null ? "" : getRemoteOptionalConfig().getFilterFile();
    }

    public String getApplicationName() {
        return getUploadSSC() == null ? "" : getUploadSSC().getProjectName();
    }

    public String getApplicationVersion() {
        return getUploadSSC() == null ? "" : getUploadSSC().getProjectVersion();
    }

    public String getPythonRequirementsFile() {
        return getRemoteAnalysisProjectType() instanceof Python ? ((Python)remoteAnalysisProjectType).getPythonRequirementsFile() : "";
    }

    public String getPythonVersion() {
        return getRemoteAnalysisProjectType() instanceof Python ? ((Python)remoteAnalysisProjectType).getPythonVersion() : "";
    }

    public String getPythonVirtualEnv() {
        return getRemoteAnalysisProjectType() instanceof Python ? ((Python)remoteAnalysisProjectType).getPythonVirtualEnv() : "";
    }

    public String getPhpVersion() {
        return getRemoteAnalysisProjectType() instanceof Php ? ((Php)remoteAnalysisProjectType).getPhpVersion() : "";
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

    public String getResolvedMbsFile(TaskListener listener) {
        return resolve(getMbsFile(), listener);
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

    public String getResolvedScanArgs(TaskListener listener) { return resolve(getScanArgs(), listener); }

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
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        setLastBuild(run);
        PrintStream log = taskListener.getLogger();
        log.println("Fortify Jenkins plugin v " + VERSION);
        log.println("Launching Fortify CloudScan start command");
        String projectRoot = filePath.getRemote() + File.separator + ".fortify";
        String cloudscanExec = null;

        if (cloudscanExec == null) {
            cloudscanExec = getCloudScanExecutable(run, filePath, launcher, taskListener);
        }

        EnvVars vars = run.getEnvironment(taskListener);
        ArrayList<String> args = new ArrayList<String>(2);
        args.add(cloudscanExec);
        args.add("-experimental"); // TODO: Remove when -experimental is no longer needed
        args.add("-url");
        args.add(FortifyPlugin.DESCRIPTOR.getCtrlUrl());
        args.add("start");
        if (StringUtils.isNotEmpty(getResolvedBuildTool(taskListener))) {
            args.add("-bt");
            args.add(getResolvedBuildTool(taskListener));
            if (getResolvedBuildTool(taskListener).equals("none")) {
                if (StringUtils.isNotEmpty(getResolvedPhpVersion(taskListener))) {
                    args.add("-hv");
                    args.add(getResolvedPhpVersion(taskListener));
                }
                if (StringUtils.isNotEmpty(getResolvedPythonRequirementsFile(taskListener))) {
                    args.add("-pyr");
                    args.add(getResolvedPythonRequirementsFile(taskListener));
                }
                if (StringUtils.isNotEmpty(getResolvedPythonVersion(taskListener))) {
                    args.add("-yv");
                    args.add(getResolvedPythonVersion(taskListener));
                }
                if (StringUtils.isNotEmpty(getResolvedPythonVirtualEnv(taskListener))) {
                    args.add("-pyv");
                    args.add(getResolvedPythonVirtualEnv(taskListener));
                }
            } else {
                if (isIncludeTests()) {
                    args.add("-t");
                }
            }
        } else {
            if (isCreateMbs() && StringUtils.isNotEmpty(getResolvedBuildID(taskListener))) {
                args.add("-b");
                args.add(getResolvedBuildID(taskListener));
                args.add("-project-root");
                args.add(projectRoot);
            } else if (StringUtils.isNotEmpty(getResolvedMbsFile(taskListener))) {
                args.add("-mbs");
                args.add(getResolvedMbsFile(taskListener));
            }
        }
        if (StringUtils.isNotEmpty(getResolvedEmailAddr(taskListener))) {
            args.add("-email");
            args.add(getResolvedEmailAddr(taskListener));
        }
        if (StringUtils.isNotEmpty(getResolvedSensorPoolName(taskListener))) {
            args.add("-pool");
            args.add(getResolvedSensorPoolName(taskListener));
        }
        if (StringUtils.isNotEmpty(getResolvedApplicationName(taskListener))) {
            args.add("-upload");
            args.add("-application");
            args.add(getResolvedApplicationName(taskListener));
            args.add("-version");
            args.add(getResolvedApplicationVersion(taskListener));
            args.add("-uptoken");
            args.add(FortifyPlugin.DESCRIPTOR.getCtrlToken());
        }
        if (StringUtils.isNotEmpty(getResolvedRulepacks(taskListener))) {
            addAllArguments(args, getResolvedRulepacks(taskListener), "-rules");
        }
        if (StringUtils.isNotEmpty(getResolvedFilterFile(taskListener))) {
            addAllArguments(args, getResolvedFilterFile(taskListener), "-filter");
        }
        if (StringUtils.isNotEmpty(getResolvedScanArgs(taskListener))) {
            args.add("-scan");
            args.add(getResolvedScanArgs(taskListener));
        }

        Launcher.ProcStarter ps = launcher.decorateByEnv(vars).launch().pwd(filePath).cmds(args).envs(vars)
                .stdout(taskListener.getLogger()).stderr(taskListener.getLogger());
        int exitcode = ps.join();
        log.println("Fortify CloudScan start command completed with exit code: " + exitcode);

        if (exitcode != 0) {
            run.setResult(Result.FAILURE);
            throw new AbortException("Fortify CloudScan start command execution failed.");
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getFunctionName() {
            return "fortifyRemoteStart";
        }

        @Override
        public String getDisplayName() {
            return "Fortify Remote Start";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }

        public void doRefreshProjects(StaplerRequest req, StaplerResponse rsp, @QueryParameter String value)
                throws Exception {
            FortifyPlugin.DESCRIPTOR.doRefreshProjects(req, rsp, value);
        }

        public void doRefreshVersions(StaplerRequest req, StaplerResponse rsp, @QueryParameter String value)
                throws Exception {
            FortifyPlugin.DESCRIPTOR.doRefreshVersions(req, rsp, value);
        }

        public ComboBoxModel doFillProjectNameItems() {
            return FortifyPlugin.DESCRIPTOR.doFillProjectNameItems();
        }

        public ComboBoxModel doFillProjectVersionItems(@QueryParameter String projectName) {
            return FortifyPlugin.DESCRIPTOR.doFillProjectVersionItems(projectName);
        }

        public ListBoxModel doFillSensorPoolNameItems() {
            return FortifyPlugin.DESCRIPTOR.doFillSensorPoolNameItems();
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
            getContext().get(TaskListener.class).getLogger().println("Running CloudScan start step");
            if (!getContext().get(FilePath.class).exists()) {
                getContext().get(FilePath.class).mkdirs();
            }

            csStart.perform(getContext().get(Run.class), getContext().get(FilePath.class), getContext().get(Launcher.class),
                    getContext().get(TaskListener.class));

            return null;
        }

        private static final long serialVersionUID = 1L;
    }

}
