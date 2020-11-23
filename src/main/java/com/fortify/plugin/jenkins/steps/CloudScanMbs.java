package com.fortify.plugin.jenkins.steps;

import com.fortify.plugin.jenkins.FortifyPlugin;
import com.google.common.collect.ImmutableSet;
import hudson.*;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Set;

public class CloudScanMbs extends FortifyCloudScanStep implements SimpleBuildStep {
    private FortifyPlugin.RemoteOptionalConfigBlock remoteOptionalConfig;
    private FortifyPlugin.UploadSSCBlock uploadSSC;

    private String buildID;

    @DataBoundConstructor
    public CloudScanMbs(String buildID) { this.buildID = buildID; }

    public String getBuildID() { return buildID; }

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

    public String getSensorPoolName() {
        return getRemoteOptionalConfig() == null ? "" : getRemoteOptionalConfig().getSensorPoolUUID();
    }

    public String getEmailAddr() {
        return getRemoteOptionalConfig() == null ? "" : getRemoteOptionalConfig().getNotifyEmail();
    }

    public String getScaScanOptions() {
        return getRemoteOptionalConfig() == null ? "" : getRemoteOptionalConfig().getScanOptions();
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

    // resolved variables
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

    public String getResolvedScanArgs(TaskListener listener) { return resolve(getScaScanOptions(), listener); }

    public String getResolvedApplicationName(TaskListener listener) { return resolve(getApplicationName(), listener); }

    public String getResolvedApplicationVersion(TaskListener listener) { return resolve(getApplicationVersion(), listener); }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(this, context);
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        setLastBuild(run);
        PrintStream log = taskListener.getLogger();
        log.println("Fortify Jenkins plugin v " + VERSION);
        log.println("Performing Fortify remote scan");
        String projectRoot = filePath.getRemote() + File.separator + ".fortify";
        String cloudscanExec;
        try {
            cloudscanExec = getScancentralExecutable(run, filePath, launcher, taskListener);
        } catch (FileNotFoundException ex) {
            log.println("WARNING: Cannot find scancentral executable");
            try {
                cloudscanExec = getCloudScanExecutable(run, filePath, launcher, taskListener);
            } catch (FileNotFoundException exception) {
                throw new RuntimeException("Cannot find cloudscan executable");
            }
        }

        EnvVars vars = run.getEnvironment(taskListener);
        ArrayList<String> args = new ArrayList<String>(2);
        args.add(cloudscanExec);

        /*
            if SSC is configured, use SSC's configuration to find the Controller
         */
        if (FortifyPlugin.DESCRIPTOR.getUrl() != null) {
            args.add("-sscurl");
            args.add(FortifyPlugin.DESCRIPTOR.getUrl());
            args.add("-ssctoken");
            args.add(FortifyPlugin.DESCRIPTOR.getToken());
        } else if (FortifyPlugin.DESCRIPTOR.getCtrlUrl() != null) {
            args.add("-url");
            args.add(FortifyPlugin.DESCRIPTOR.getCtrlUrl());
        } else {
            throw new AbortException("Fortify remote scan execution failed: No SSC or Controller URL found");
        }
        args.add("start");
        args.add("-b");
        args.add(getResolvedBuildID(taskListener));
        args.add("-project-root");
        args.add(projectRoot);

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
        args.add("-scan"); // must have -scan argument for mbs scans
        if (StringUtils.isNotEmpty(getResolvedScanArgs(taskListener))) {
            args.add(getResolvedScanArgs(taskListener));
        }

        Launcher.ProcStarter ps = launcher.decorateByEnv(vars).launch().pwd(filePath).cmds(args).envs(vars)
                .stdout(taskListener.getLogger()).stderr(taskListener.getLogger());
        int exitcode = ps.join();
        log.println("Fortify remote scan completed with exit code: " + exitcode);

        if (exitcode != 0) {
            run.setResult(Result.FAILURE);
            throw new AbortException("Fortify remote scan execution failed.");
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getFunctionName() {
            return "fortifyRemoteScan";
        }

        @Override
        public String getDisplayName() {
            return "Upload a translated project for remote scan";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }

        public FormValidation doCheckBuildID(@QueryParameter String value) {
            return Validators.checkFieldNotEmpty(value);
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
        private transient CloudScanMbs csMbs;

        protected Execution(CloudScanMbs csMbs, StepContext context) {
            super(context);
            this.csMbs = csMbs;
        }

        @Override
        protected Void run() throws Exception {
            getContext().get(TaskListener.class).getLogger().println("Running Fortify remote scan step");
            if (!getContext().get(FilePath.class).exists()) {
                getContext().get(FilePath.class).mkdirs();
            }

            csMbs.perform(getContext().get(Run.class), getContext().get(FilePath.class), getContext().get(Launcher.class),
                    getContext().get(TaskListener.class));

            return null;
        }

        private static final long serialVersionUID = 1L;
    }

}
