package com.fortify.plugin.jenkins.steps;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;

public abstract class FortifyCloudScanStep extends FortifyStep {
    protected String scanArgs;

    public String getScanArgs() {
        return scanArgs;
    }

    @DataBoundSetter
    public void setScanArgs(String scanArgs) {
        this.scanArgs = scanArgs;
    }

    public String getResolvedScanArgs(TaskListener listener) {
        return resolve(getScanArgs(), listener);
    }

    protected String getCloudScanExecutable(Run<?, ?> build, FilePath workspace, Launcher launcher,
                                         TaskListener listener) throws InterruptedException, IOException {
        return getExecutable("cloudscan" + (launcher.isUnix() ? "" : ".bat"), true, build, workspace, launcher,
                listener);
    }

}
