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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Set;

import com.fortify.plugin.jenkins.FortifyPlugin;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.fortify.plugin.jenkins.Messages;
import com.fortify.plugin.jenkins.PathUtils;
import com.google.common.collect.ImmutableSet;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;

public class FortifyScan extends FortifySCAStep {

	private String resultsFile;
	private String customRulepacks;
	private String addOptions;

	@DataBoundConstructor
	public FortifyScan(String buildID) {
		this.buildID = buildID;
	}

	public String getResolvedResultsFile(TaskListener listener) {
		String s = resolve(getResultsFile(), listener);
		return PathUtils.appendExtentionIfNotEmpty(s, ".fpr");
	}

	public String getResolvedCustomRulepacks(TaskListener listener) {
		return resolve(getCustomRulepacks(), listener);
	}

	public String getResolvedAddOptions(TaskListener listener) {
		return resolve(getAddOptions(), listener);
	}

	public String getResultsFile() {
		return resultsFile;
	}

	@DataBoundSetter
	public void setResultsFile(String resultsFile) {
		this.resultsFile = resultsFile;
	}

	public String getCustomRulepacks() {
		return customRulepacks;
	}

	@DataBoundSetter
	public void setCustomRulepacks(String customRulepacks) {
		this.customRulepacks = customRulepacks;
	}

	public String getAddOptions() {
		return addOptions;
	}

	@DataBoundSetter
	public void setAddOptions(String addOptions) {
		this.addOptions = addOptions;
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new Execution(this, context);
	}

	@Override
	public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		setLastBuild(build);
		PrintStream log = listener.getLogger();
		log.println("Fortify Jenkins plugin v " + VERSION);
		log.println("Launching Fortify SCA scan command");
		String projectRoot = workspace.getRemote() + File.separator + ".fortify";
		String sourceanalyzer = null;

		if (sourceanalyzer == null) {
			sourceanalyzer = getSourceAnalyzerExecutable(build, workspace, launcher, listener);
		}
		EnvVars vars = build.getEnvironment(listener);
		ArrayList<String> args = new ArrayList<String>();
		args.add(sourceanalyzer);
		args.add("-Dcom.fortify.sca.ProjectRoot=" + projectRoot);
		args.add("-b");
		args.add(getResolvedBuildID(listener));
		Integer intOption = getResolvedMaxHeap(listener);
		if (intOption != null) {
			args.add("-Xmx" + intOption + "M");
		}
		String option;
		option = getResolvedAddJVMOptions(listener);
		if (StringUtils.isNotEmpty(option)) {
			addAllArguments(args, option);
		}
		option = getResolvedLogFile(listener);
		if (StringUtils.isNotEmpty(option)) {
			args.add("-logfile");
			args.add(option);
		}
		if (getDebug()) {
			args.add("-debug");
		}
		if (getVerbose()) {
			args.add("-verbose");
		}
		args.add("-scan");
		args.add("-f");
		option = getResolvedResultsFile(listener);
		if (StringUtils.isNotEmpty(option)) {
			args.add(option);
		} else {
			args.add("scan.fpr");
		}
		option = getResolvedCustomRulepacks(listener);
		if (StringUtils.isNotEmpty(option)) {
			args.add("-rules");
			args.add(option);
		}
		option = getResolvedAddOptions(listener);
		if (StringUtils.isNotEmpty(option)) {
			addAllArguments(args, option);
		}
		ProcStarter ps = launcher.decorateByEnv(vars).launch().pwd(workspace).cmds(args).envs(vars)
				.stdout(listener.getLogger()).stderr(listener.getLogger());
		int exitcode = ps.join();
		log.println(Messages.FortifyScan_Result(exitcode));
		if (exitcode != 0) {
			build.setResult(Result.FAILURE);
			throw new AbortException(Messages.FortifyScan_Error());
		}

	}

	@Extension
	public static class DescriptorImpl extends StepDescriptor {

		@Override
		public String getFunctionName() {
			return "fortifyScan";
		}

		@Override
		public String getDisplayName() {
			return Messages.FortifyScan_DisplayName();
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
		}

		public FormValidation doCheckBuildID(@QueryParameter String value) {
			return Validators.checkFieldNotEmpty(value);
		}

		public FormValidation doCheckMaxHeap(@QueryParameter String value) {
			return Validators.checkValidInteger(value);
		}

	}

	private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
		private transient FortifyScan fs;

		protected Execution(FortifyScan fs, StepContext context) {
			super(context);
			this.fs = fs;
		}

		@Override
		protected Void run() throws Exception {
			if (FortifyPlugin.DESCRIPTOR.isPreventLocalScans()) {
				throw new AbortException(Messages.FortifyScan_Local_NotSupported());
			}
			getContext().get(TaskListener.class).getLogger().println("Running FortifyScan step");
			if (!getContext().get(FilePath.class).exists()) {
				getContext().get(FilePath.class).mkdirs();
			}
			fs.perform(getContext().get(Run.class), getContext().get(FilePath.class), getContext().get(Launcher.class),
					getContext().get(TaskListener.class));

			return null;
		}

		private static final long serialVersionUID = 1L;

	}

}
