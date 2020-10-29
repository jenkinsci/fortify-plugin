/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates.
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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import hudson.Util;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

import com.fortify.plugin.jenkins.FindExecutableRemoteService;
import com.fortify.plugin.jenkins.FortifyPlugin;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.StreamBuildListener;
import hudson.model.TaskListener;
import jenkins.tasks.SimpleBuildStep;

public abstract class FortifyStep extends Step implements SimpleBuildStep {
	public static final String VERSION = FortifyPlugin.getPluginVersion();

	protected Run<?, ?> lastBuild;

	protected void setLastBuild(Run<?, ?> lastBuild) {
		this.lastBuild = lastBuild;
	}

	/**
	 * Search for the executable filename in executable home directory or on PATH environment
	 * variable or in workspace
	 *
	 * @param filename
	 * @param build
	 * @param workspace
	 * @param listener
	 * @param targetEnvVarName
	 * @return found executable
	 * @throws InterruptedException
	 * @throws IOException
	 */
	protected String getExecutable(String filename, Run<?, ?> build, FilePath workspace,
								   TaskListener listener, String targetEnvVarName) throws InterruptedException, IOException {
		PrintStream logger = listener.getLogger();
		EnvVars env = build.getEnvironment(listener);

		String home = null;
		String path = null;
		boolean isEnvVarSetProperly = true;
		// check env variables defined in Jenkins master
		for (Map.Entry<String, String> entry : env.entrySet()) {
			String envVarName = entry.getKey();
			String envVarValue = entry.getValue();
			if (targetEnvVarName != null && targetEnvVarName.equals(envVarName)) {
				if ("PATH".equalsIgnoreCase(targetEnvVarName)) {
					path = envVarValue;
				} else {
					home = envVarValue;
					if (endsWithBin(envVarValue)) {
						logger.println("WARNING: Environment variable " + envVarName + " should not point to bin directory");
						isEnvVarSetProperly = false;
					}
				}
			} else if ("PATH".equalsIgnoreCase(envVarName)) {
				path = envVarValue;
			}
		}

		String errorMsg = "make sure that either ";
		if (targetEnvVarName != null) {
			errorMsg += targetEnvVarName + " environment variable is set" + (!isEnvVarSetProperly ? " properly" : "") + " or ";
		}
		errorMsg += filename + " is on the PATH or in workspace";
		return findExecutablePath(filename, home, path, workspace, logger, errorMsg);
	}

	private static boolean endsWithBin(String str) {
		return str.endsWith("bin") || str.endsWith("bin/") || str.endsWith("bin\\");
	}

	private String findExecutablePath(String filename, String home, String path, FilePath workspace, PrintStream logger, String errorMsg)
			throws IOException, InterruptedException {
		String executablePath = workspace.act(new FindExecutableRemoteService(filename, home, path, workspace));
		if (executablePath == null) {
			throw new FileNotFoundException("ERROR: executable not found: " + filename + "; " + errorMsg);
		} else {
			logger.printf("Found executable: %s%n", executablePath);
			return executablePath;
		}
	}

	protected String resolve(String param, TaskListener listener) {
		if (param == null) {
			return "";
		}
		if (lastBuild == null) {
			return param;
		}
		listener = listener == null ? new StreamBuildListener(System.out, Charset.defaultCharset()) : listener;
		try {
			// TODO: see at lastBuild.getBuildVariableResolver()
			final EnvVars vars = lastBuild.getEnvironment(listener);
			return vars.expand(param);
		} catch (IOException e) {
			// do nothing
		} catch (InterruptedException e) {
			// do nothing
		}
		return param;
	}

	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
		return false;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		if (build != null && launcher != null && listener != null && build.getWorkspace() != null) {
			perform(build, build.getWorkspace(), launcher, listener);
		}
		return true;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return null;
	}

	@Override
	public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
		return Collections.emptyList();
	}

	@Override
	public StepExecution start(StepContext arg0) throws Exception {
		return null;
	}

	// breaks down argsToAdd into individual arguments before adding to args List.
	protected void addAllArguments(List<String> args, String argsToAdd) {
		for (String s : Util.tokenize(argsToAdd)) {
			args.add(s);
		}
	}

	// breaks down argsToAdd into individual arguments before adding to args List.
	// Adds a flag argument before each individual argument.
	protected void addAllArguments(List<String> args, String argsToAdd, String flag) {
		for (String s : Util.tokenize(argsToAdd)) {
			args.add(flag);
			args.add(s);
		}
	}
}
