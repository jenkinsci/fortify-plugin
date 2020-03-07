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
        return getExecutable("cloudscan" + (launcher.isUnix() ? "" : ".bat"), true, build, workspace, launcher,
                listener, null);
    }

    /* Look for scancentral executable in Jenkins environment, if not found, get the old cloudscan executable. It's considered not found
     * if the getExecutable() returns just the filename rather than the full path.*/
    protected String getScancentralExecutable(Run<?, ?> build, FilePath workspace, Launcher launcher,
                                            TaskListener listener) throws InterruptedException, IOException {
        String filename = "scancentral" + (launcher.isUnix() ? "" : ".bat");
        String msg = "Checking for cloudscan executable";
        String exec = getExecutable(filename, true, build, workspace, launcher,
                listener, msg);
        if (exec.equals(filename)) {
            return getCloudScanExecutable(build, workspace, launcher, listener);
        } else {
            return exec;
        }
    }
}
