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

import org.kohsuke.stapler.DataBoundSetter;

import hudson.DescriptorExtensionList;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.gradle.GradleInstallation;
import hudson.tasks.Maven.MavenInstallation;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;

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

	protected String getMavenExecutable(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, String mavenInstallationName)
			throws InterruptedException, IOException {
		String result = "";
		final EnvVars envVars = build.getEnvironment(listener);
		DescriptorExtensionList<ToolInstallation, ToolDescriptor<?>> tools = ToolInstallation.all();
		ToolDescriptor<?> ti = tools.find(MavenInstallation.class);
		if (ti != null) {
			for (ToolInstallation inst : ti.getInstallations()) {
				String instName = inst.getName();
				if (((instName == null && mavenInstallationName == null) || (instName != null && instName.equalsIgnoreCase(mavenInstallationName))) && build instanceof AbstractBuild) {
					MavenInstallation mvn = (MavenInstallation)inst;
					Node node = ((AbstractBuild)build).getBuiltOn();
					if (node != null) {
						mvn = mvn.forNode(node, listener);
					}
					mvn = mvn.forEnvironment(envVars);
					mvn.buildEnvVars(envVars);
					result = mvn.getExecutable(launcher);
					if (result != null && !result.isEmpty()) {
						break;
					}
				}
			}
		}
		if (result == null || result.isEmpty()) { //fallback to the previous logic
			if (envVars.containsKey("MAVEN_HOME")) {
				result = getExecutableForEnvVar(build, workspace, launcher, listener, ".bat", ".cmd", "MAVEN_HOME");
			} else if (envVars.containsKey("M2_HOME")) {
				result = getExecutableForEnvVar(build, workspace, launcher, listener, ".cmd", ".bat", "M2_HOME");
			}
			if (result == null || result.isEmpty()) {
				result = getExecutableForEnvVar(build, workspace, launcher, listener, ".bat", ".cmd", "PATH");//was null instead of Path
			}
		}
		listener.getLogger().println("Using Maven executable " + result);
		return result;
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

	protected String getGradleExecutable(boolean useWrapper, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, String gradleInstallationName) throws InterruptedException, IOException {
		String result = "";
		final EnvVars envVars = build.getEnvironment(listener);
		DescriptorExtensionList<ToolInstallation, ToolDescriptor<?>> tools = ToolInstallation.all();
		ToolDescriptor<?> ti = tools.find(GradleInstallation.class);
		if (ti != null) {
			for (ToolInstallation inst : ti.getInstallations()) {
				String instName = inst.getName();
				if (((instName == null && gradleInstallationName == null) || (instName != null && instName.equalsIgnoreCase(gradleInstallationName))) && build instanceof AbstractBuild) {
					GradleInstallation gradle = (GradleInstallation)inst;
					Node node = ((AbstractBuild)build).getBuiltOn();
					if (node != null) {
						gradle = gradle.forNode(node, listener);
					}
					gradle = gradle.forEnvironment(envVars);
					gradle.buildEnvVars(envVars);
					result = gradle.getExecutable(launcher);
					if (result != null && !result.isEmpty()) {
						break;
					}
				}
			}
		}
		if (result == null || result.isEmpty()) { //fallback to the previous logic
			result = getExecutable("gradle" + (useWrapper ? "w" : "") + (launcher.isUnix() ? "" : ".bat"), build, workspace, listener, "GRADLE_HOME");
		}
		listener.getLogger().println("Using Gradle executable " + result);
		return result;
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
