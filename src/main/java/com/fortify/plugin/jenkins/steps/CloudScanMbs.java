/*******************************************************************************
 * Copyright 2022 - 2023 Open Text. 
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
import hudson.model.Item;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

import com.fortify.plugin.jenkins.FortifyPlugin;
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
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;

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
    public void perform(Run<?, ?> run, FilePath filePath, EnvVars vars, Launcher launcher, TaskListener taskListener) throws InterruptedException, IOException {
        setLastBuild(run);
        PrintStream log = taskListener.getLogger();
        log.println("Fortify Jenkins plugin v " + VERSION);
        log.println("Performing Fortify remote scan");
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

        ArrayList<Pair<String, Boolean>> args = new ArrayList<Pair<String, Boolean>>(20);
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
            throw new AbortException("Fortify remote scan execution failed: No SSC or Controller URL found");
        }
        args.add(Pair.of("start", Boolean.FALSE));
        args.add(Pair.of("-b", Boolean.FALSE));
        args.add(Pair.of(getResolvedBuildID(taskListener), Boolean.FALSE));
        args.add(Pair.of("-project-root", Boolean.FALSE));
        args.add(Pair.of(projectRoot, Boolean.FALSE));

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
        args.add(Pair.of("-scan", Boolean.FALSE)); // must have -scan argument for mbs scans
        if (StringUtils.isNotEmpty(getResolvedScanArgs(taskListener))) {
            args.add(Pair.of(getResolvedScanArgs(taskListener), Boolean.FALSE));
        }

		List<String> cmds = new ArrayList<String>(args.size());
		boolean[] masks = new boolean[args.size()];
		args.stream().forEach(p -> {
			cmds.add(p.getLeft());
			masks[args.indexOf(p)] = p.getRight();
		});
		Launcher.ProcStarter p = launcher.launch().cmds(cmds).masks(masks).envs(vars).stdout(log).stderr(log).pwd(filePath);
		int exitcode = p.start().join();
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
            return ImmutableSet.of(Run.class, FilePath.class, EnvVars.class, Launcher.class, TaskListener.class);
        }

        public FormValidation doCheckBuildID(@QueryParameter String value) {
            return Validators.checkFieldNotEmpty(value);
        }

        @POST
        public void doRefreshProjects(StaplerRequest req, StaplerResponse rsp, @QueryParameter String value, @AncestorInPath Item item)
                throws Exception {
            FortifyPlugin.DESCRIPTOR.doRefreshProjects(req, rsp, value, item);
        }

        @POST
        public void doRefreshVersions(StaplerRequest req, StaplerResponse rsp, @QueryParameter String value, @AncestorInPath Item item)
                throws Exception {
            FortifyPlugin.DESCRIPTOR.doRefreshVersions(req, rsp, value, item);
        }

        @POST
        public ComboBoxModel doFillAppNameItems(@AncestorInPath Item item) {
            return FortifyPlugin.DESCRIPTOR.doFillAppNameItems(item);
        }

        @POST
        public ComboBoxModel doFillAppVersionItems(@QueryParameter String appName, @AncestorInPath Item item) {
            return FortifyPlugin.DESCRIPTOR.doFillAppVersionItems(appName, item);
        }

        @POST
        public ListBoxModel doFillSensorPoolUUIDItems(@AncestorInPath Item item) {
            return FortifyPlugin.DESCRIPTOR.doFillSensorPoolUUIDItems(item);
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
            StepContext context = getContext();
            context.get(TaskListener.class).getLogger().println("Running Fortify remote scan step");
            if (!context.get(FilePath.class).exists()) {
                context.get(FilePath.class).mkdirs();
            }
            csMbs.perform(context.get(Run.class), context.get(FilePath.class), context.get(EnvVars.class), 
                    context.get(Launcher.class), context.get(TaskListener.class));
            return null;
        }

        private static final long serialVersionUID = 1L;
    }

}
