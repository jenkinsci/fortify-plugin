package com.fortify.plugin.jenkins.steps;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;

public abstract class FortifyCloudScanStep extends FortifyStep {
    protected String scanOptions;

    public String getScanOptions() {
        return scanOptions;
    }

    @DataBoundSetter
    public void setScanOptions(String scanOptions) {
        this.scanOptions = scanOptions;
    }

    public String getResolvedScanArgs(TaskListener listener) {
        return resolve(getScanOptions(), listener);
    }

    protected String getCloudScanExecutable(Run<?, ?> build, FilePath workspace, Launcher launcher,
                                         TaskListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Checking for cloudscan executable");
        return getExecutable("cloudscan" + (launcher.isUnix() ? "" : ".bat"), build, workspace,
                listener, "FORTIFY_HOME");
    }

    protected String getScancentralExecutable(Run<?, ?> build, FilePath workspace, Launcher launcher,
                                            TaskListener listener) throws InterruptedException, IOException {
        return getExecutable("scancentral" + (launcher.isUnix() ? "" : ".bat"), build, workspace,
                listener, "FORTIFY_HOME");
    }
}
