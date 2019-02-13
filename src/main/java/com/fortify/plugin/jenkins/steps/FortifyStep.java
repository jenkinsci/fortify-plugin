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

import java.io.IOException;
import java.nio.charset.Charset;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

import com.fortify.plugin.jenkins.FindExecutableRemoteService;
import com.fortify.plugin.jenkins.FortifyPlugin;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.StreamBuildListener;
import hudson.model.TaskListener;

public abstract class FortifyStep extends Step {
	public static final String VERSION = FortifyPlugin.getPluginVersion();

	protected Run<?, ?> lastBuild;

	protected void setLastBuild(Run<?, ?> lastBuild) {
		this.lastBuild = lastBuild;
	}

	/**
	 * Search for the executable filename in FORTIFY_HOME or PATH environment
	 * variables or workspace
	 * 
	 * @param filename
	 * @param checkFortifyHome
	 * @param build
	 * @param workspace
	 * @param launcher
	 * @param listener
	 * @return found executable or filename if not found
	 * @throws InterruptedException
	 * @throws IOException
	 */
	protected String getExecutable(String filename, boolean checkFortifyHome, Run<?, ?> build, FilePath workspace,
			Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
		EnvVars env = build.getEnvironment(listener);
		String fortifyHome = null;
		String path = null;
		for (String key : env.keySet()) {
			if ("FORTIFY_HOME".equals(key)) {
				if (checkFortifyHome) {
					fortifyHome = env.get(key);
				}
			} else if ("PATH".equalsIgnoreCase(key)) {
				path = env.get(key);
			}
		}
		String s = workspace.act(new FindExecutableRemoteService(filename, fortifyHome, path, workspace));
		if (s == null) {
			listener.getLogger().printf("executable not found: %s\n", filename);
			listener.getLogger().printf("\thome: %s\n", fortifyHome);
			listener.getLogger().printf("\tpath: %s\n", path);
			listener.getLogger().printf("\tworkspace: %s\n", workspace.getRemote());
			return filename;
		} else {
			listener.getLogger().printf("found executable: %s\n", s);
			return s;
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
	public StepExecution start(StepContext arg0) throws Exception {
		return null;
	}

}
