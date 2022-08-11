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

import hudson.EnvVars;
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
                                         TaskListener listener, EnvVars vars) throws InterruptedException, IOException {
        listener.getLogger().println("Checking for cloudscan executable");
        return getExecutable("cloudscan" + (launcher.isUnix() ? "" : ".bat"), build, workspace,
                listener, "FORTIFY_HOME", vars);
    }

    protected String getScancentralExecutable(Run<?, ?> build, FilePath workspace, Launcher launcher,
                                            TaskListener listener, EnvVars vars) throws InterruptedException, IOException {
        return getExecutable("scancentral" + (launcher.isUnix() ? "" : ".bat"), build, workspace,
                listener, "FORTIFY_HOME", vars);
    }
}
