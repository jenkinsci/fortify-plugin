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

import java.io.FileNotFoundException;
import java.io.IOException;

import hudson.EnvVars;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

public abstract class FortifySCAStep extends FortifyStep {

	protected String buildID;
	protected Integer maxHeap;
	protected String addJVMOptions;
	protected boolean debug;
	protected boolean verbose;
	protected String logFile;

	public String getBuildID() {
		return buildID;
	}

	public Integer getMaxHeap() {
		return maxHeap;
	}

	@DataBoundSetter
	public void setMaxHeap(Integer maxHeap) {
		this.maxHeap = maxHeap;
	}

	public String getAddJVMOptions() {
		return addJVMOptions;
	}

	@DataBoundSetter
	public void setAddJVMOptions(String addJVMOptions) {
		this.addJVMOptions = addJVMOptions;
	}

	public boolean getDebug() {
		return debug;
	}

	public boolean getVerbose() {
		return verbose;
	}

	public String getLogFile() {
		return logFile;
	}

	@DataBoundSetter
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	@DataBoundSetter
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@DataBoundSetter
	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	protected String getSourceAnalyzerExecutable(Run<?, ?> build, FilePath workspace, Launcher launcher,
			TaskListener listener) throws InterruptedException, IOException {
		return getExecutable("sourceanalyzer" + (launcher.isUnix() ? "" : ".exe"), build, workspace,
			listener, "FORTIFY_HOME");
	}

	protected String getMavenExecutable(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		final EnvVars envVars = build.getEnvironment(listener);
		if (envVars.containsKey("MAVEN_HOME")) {
			return getExecutableForEnvVar(build, workspace, launcher, listener, ".bat", ".cmd", "MAVEN_HOME");
		}
		if (envVars.containsKey("M2_HOME")) {
			return getExecutableForEnvVar(build, workspace, launcher, listener, ".cmd", ".bat", "M2_HOME");
		}
		return getExecutableForEnvVar(build, workspace, launcher, listener, ".bat", ".cmd", null);
	}

	private String getExecutableForEnvVar(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, String ext1, String ext2,
										  String targetEnvVarName) throws InterruptedException, IOException {
		if (launcher.isUnix()) {
			return getExecutable("mvn", build, workspace, listener, targetEnvVarName);
		}
		try {
			return getExecutable("mvn" + ext1, build, workspace, listener, targetEnvVarName);
		} catch (FileNotFoundException ex) {
			return getExecutable("mvn" + ext2, build, workspace, listener, targetEnvVarName);
		}
	}

	protected String getGradleExecutable(boolean useWrapper, Run<?, ?> build, FilePath workspace, Launcher launcher,
			TaskListener listener) throws InterruptedException, IOException {
		return getExecutable("gradle" + (useWrapper ? "w" : "") + (launcher.isUnix() ? "" : ".bat"), build, workspace,
			listener, "GRADLE_HOME");
	}

	protected String getDevenvExecutable(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		if (launcher.isUnix()) {
			throw new RuntimeException("Sorry, devenv is not supported on Unix platform.");
		}
		return getExecutable("devenv.exe", build, workspace, listener, null);
	}

	protected String getMSBuildExecutable(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		if (launcher.isUnix()) {
			throw new RuntimeException("Sorry, msbuild is not supported on Unix platform.");
		}
		return getExecutable("msbuild.exe", build, workspace, listener, null);
	}

	public Integer getResolvedMaxHeap(TaskListener listener) {
		if (getMaxHeap() != null) {
			try {
				return Integer.parseInt(resolve(String.valueOf(getMaxHeap()), listener));
			} catch (NumberFormatException nfe) {
				return null;
			}
		}
		return null;
	}

	public String getResolvedBuildID(TaskListener listener) {
		return resolve(getBuildID(), listener);
	}

	public String getResolvedAddJVMOptions(TaskListener listener) {
		return resolve(getAddJVMOptions(), listener);
	}

	public String getResolvedLogFile(TaskListener listener) {
		return resolve(getLogFile(), listener);
	}

}
