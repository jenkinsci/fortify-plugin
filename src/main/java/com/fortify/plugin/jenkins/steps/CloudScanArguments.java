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

import com.google.common.collect.ImmutableSet;
import hudson.*;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Set;

public class CloudScanArguments extends FortifyCloudScanStep implements SimpleBuildStep {
    protected String transOptions;

    @DataBoundConstructor
    public CloudScanArguments() {}

    public String getTransOptions() {
        return transOptions;
    }

    @DataBoundSetter
    public void setTransOptions(String transOptions) {
        this.transOptions = transOptions;
    }

    public String getResolvedTransArgs(TaskListener listener) {
        return resolve(getTransOptions(), listener);
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(this, context);
    }

    @Override
    public void perform(Run<?, ?> run, FilePath filePath, EnvVars vars, Launcher launcher, TaskListener taskListener) throws InterruptedException, IOException {
        setLastBuild(run);
        PrintStream log = taskListener.getLogger();
        log.println("Fortify Jenkins plugin v " + VERSION);
        log.println("Launching Fortify scancentral arguments command");
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

        ArrayList<String> args = new ArrayList<String>(2);
        args.add(cloudscanExec);
        args.add("arguments");
        args.add("-o");

        String option = getResolvedTransArgs(taskListener);
        if (StringUtils.isNotEmpty(option)) {
            addAllArguments(args, option, "-targs");
        }

        option = getResolvedScanArgs(taskListener);
        if (StringUtils.isNotEmpty(option)) {
            addAllArguments(args, option, "-sargs");
        }

        Launcher.ProcStarter ps = launcher.decorateByEnv(vars).launch().pwd(filePath).cmds(args).envs(vars)
                .stdout(taskListener.getLogger()).stderr(taskListener.getLogger());
        int exitcode = ps.join();
        log.println("Fortify scancentral arguments command completed with exit code: " + exitcode);

        if (exitcode != 0) {
            run.setResult(Result.FAILURE);
            throw new AbortException("Fortify scancentral arguments command execution failed.");
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getFunctionName() {
            return "fortifyRemoteArguments";
        }

        @Override
        public String getDisplayName() {
            return "Set options for remote Fortify SCA analysis";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, EnvVars.class, Launcher.class, TaskListener.class);
        }

    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private transient CloudScanArguments csArguments;

        protected Execution(CloudScanArguments csArguments, StepContext context) {
            super(context);
            this.csArguments = csArguments;
        }

        @Override
        protected Void run() throws Exception {
            StepContext context = getContext();
            context.get(TaskListener.class).getLogger().println("Running ScanCentral arguments step");
            if (!context.get(FilePath.class).exists()) {
                context.get(FilePath.class).mkdirs();
            }
            csArguments.perform(context.get(Run.class), context.get(FilePath.class), context.get(EnvVars.class), 
                    context.get(Launcher.class), context.get(TaskListener.class));
            return null;
        }

        private static final long serialVersionUID = 1L;
    }
}
