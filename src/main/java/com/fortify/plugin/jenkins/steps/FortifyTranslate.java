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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.fortify.plugin.jenkins.Messages;
import com.fortify.plugin.jenkins.steps.types.AdvancedScanType;
import com.fortify.plugin.jenkins.steps.types.DevenvScanType;
import com.fortify.plugin.jenkins.steps.types.DotnetSourceScanType;
import com.fortify.plugin.jenkins.steps.types.GradleScanType;
import com.fortify.plugin.jenkins.steps.types.JavaScanType;
import com.fortify.plugin.jenkins.steps.types.MavenScanType;
import com.fortify.plugin.jenkins.steps.types.MsbuildScanType;
import com.fortify.plugin.jenkins.steps.types.OtherScanType;
import com.fortify.plugin.jenkins.steps.types.ProjectScanType;
import com.google.common.collect.ImmutableSet;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.Util;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;

public class FortifyTranslate extends FortifySCAStep {
	private ProjectScanType projectScanType;
	private String excludeList;
	private Node currentNode;

	@DataBoundConstructor
	public FortifyTranslate(String buildID, ProjectScanType projectScanType) {
		this.buildID = buildID;
		this.projectScanType = projectScanType;
	}

	// this is required for GlobalTooling accessing the node during the build
	public void setCurrentNode(Node node) {
		this.currentNode = node;
	}

	public ProjectScanType getProjectScanType() {
		return projectScanType;
	}

	public void setJavaVersion(String javaVersion) {
		((JavaScanType) projectScanType).setJavaVersion(javaVersion);
	}

	public void setJavaClasspath(String javaClasspath) {
		((JavaScanType) projectScanType).setJavaClasspath(javaClasspath);
	}

	public void setJavaSrcFiles(String javaSrcFiles) {
		((JavaScanType) projectScanType).setJavaSrcFiles(javaSrcFiles);
	}

	public void setJavaAddOptions(String javaAddOptions) {
		((JavaScanType) projectScanType).setJavaAddOptions(javaAddOptions);
	}

	public void setDotnetProject(String dotnetProject) {
		if (projectScanType instanceof DevenvScanType) {
			((DevenvScanType) projectScanType).setDotnetProject(dotnetProject);
		} else {
			((MsbuildScanType) projectScanType).setDotnetProject(dotnetProject);
		}
	}

	public void setDotnetAddOptions(String dotnetAddOptions) {
		if (projectScanType instanceof DevenvScanType) {
			((DevenvScanType) projectScanType).setDotnetAddOptions(dotnetAddOptions);
		} else if (projectScanType instanceof MsbuildScanType) {
			((MsbuildScanType) projectScanType).setDotnetAddOptions(dotnetAddOptions);
		} else {
			((DotnetSourceScanType) projectScanType).setDotnetAddOptions(dotnetAddOptions);
		}
	}

	public void setDotnetFrameworkVersion(String dotnetFrameworkVersion) {
		((DotnetSourceScanType) projectScanType).setDotnetFrameworkVersion(dotnetFrameworkVersion);
	}

	public void setDotnetLibdirs(String dotnetLibdirs) {
		((DotnetSourceScanType) projectScanType).setDotnetLibdirs(dotnetLibdirs);
	}

	public void setDotnetSrcFiles(String dotnetSrcFiles) {
		((DotnetSourceScanType) projectScanType).setDotnetSrcFiles(dotnetSrcFiles);
	}

	public void setMavenOptions(String mavenOptions) {
		((MavenScanType) projectScanType).setMavenOptions(mavenOptions);
	}

	public void setMavenName(String mName) {
		((MavenScanType) projectScanType).setMavenInstallationName(mName);
	}

	public void setUseWrapper(boolean useWrapper) {
		((GradleScanType) projectScanType).setUseWrapper(useWrapper);
	}

	public void setGradleTasks(String gradleTasks) {
		((GradleScanType) projectScanType).setGradleTasks(gradleTasks);
	}

	public void setGradleOptions(String gradleOptions) {
		((GradleScanType) projectScanType).setGradleOptions(gradleOptions);
	}

	public void setOtherOptions(String otherOptions) {
		((OtherScanType) projectScanType).setOtherOptions(otherOptions);
	}

	public void setOtherIncludesList(String otherIncludesList) {
		((OtherScanType) projectScanType).setOtherIncludesList(otherIncludesList);
	}

	public void setAdvOptions(String advOptions) {
		((AdvancedScanType) projectScanType).setAdvOptions(advOptions);
	}

	public String getExcludeList() {
		return excludeList;
	}

	@DataBoundSetter
	public void setExcludeList(String excludeList) {
		this.excludeList = excludeList;
	}

	public String getResolvedJavaClasspath(JavaScanType javaType, TaskListener listener) {
		return resolve(javaType.getJavaClasspath(), listener);
	}

	public String getResolvedJavaSrcFiles(JavaScanType javaType, TaskListener listener) {
		return resolve(javaType.getJavaSrcFiles(), listener);
	}

	public String getResolvedJavaAddOptions(JavaScanType javaType, TaskListener listener) {
		return resolve(javaType.getJavaAddOptions(), listener);
	}

	public String getResolvedDotnetProjects(ProjectScanType projectScanType, TaskListener listener) {
		if (projectScanType instanceof DevenvScanType) {
			return resolve(((DevenvScanType) projectScanType).getDotnetProject(), listener);
		} else {
			return resolve(((MsbuildScanType) projectScanType).getDotnetProject(), listener);
		}
	}

	public String getResolvedDotnetAddOptions(ProjectScanType projectScanType, TaskListener listener) {
		if (projectScanType instanceof DevenvScanType) {
			return resolve(((DevenvScanType) projectScanType).getDotnetAddOptions(), listener);
		} else if (projectScanType instanceof MsbuildScanType) {
			return resolve(((MsbuildScanType) projectScanType).getDotnetAddOptions(), listener);
		} else {
			return resolve(((DotnetSourceScanType) projectScanType).getDotnetAddOptions(), listener);
		}
	}

	public String getResolvedDotnetFrameworkVersion(DotnetSourceScanType dotnetSrcType, TaskListener listener) {
		return resolve(dotnetSrcType.getDotnetFrameworkVersion(), listener);
	}

	public String getResolvedDotnetLibdirs(DotnetSourceScanType dotnetSrcType, TaskListener listener) {
		return resolve(dotnetSrcType.getDotnetLibdirs(), listener);
	}

	public String getResolvedDotnetSrcFiles(DotnetSourceScanType dotnetSrcType, TaskListener listener) {
		return resolve(dotnetSrcType.getDotnetSrcFiles(), listener);
	}

	public String getResolvedMavenOptions(MavenScanType mavenType, TaskListener listener) {
		return resolve(mavenType.getMavenOptions(), listener);
	}

	public String getResolvedGradleTasks(GradleScanType gradleType, TaskListener listener) {
		return resolve(gradleType.getGradleTasks(), listener);
	}

	public String getResolvedGradleOptions(GradleScanType gradleType, TaskListener listener) {
		return resolve(gradleType.getGradleOptions(), listener);
	}

	public String getResolvedOtherOptions(OtherScanType otherType, TaskListener listener) {
		return resolve(otherType.getOtherOptions(), listener);
	}

	public String getResolvedOtherIncludesList(OtherScanType otherType, TaskListener listener) {
		return resolve(otherType.getOtherIncludesList(), listener);
	}

	public String getResolvedTranslationExcludeList(TaskListener listener) {
		return resolve(getExcludeList(), listener);
	}

	public String getResolvedAdvOptions(AdvancedScanType advType, TaskListener listener) {
		return resolve(advType.getAdvOptions(), listener);
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new Execution(this, context);
	}

	@Override
	public void perform(Run<?, ?> build, FilePath workspace, EnvVars vars, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
		setLastBuild(build);
		PrintStream log = listener.getLogger();
		log.println("Fortify Jenkins plugin v " + VERSION);
		log.println("Launching Fortify SCA translate command");
		String projectRoot = workspace.child(".fortify").getRemote();
		String sourceanalyzer = getSourceAnalyzerExecutable(build, workspace, launcher, listener, vars);
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
		if (projectScanType instanceof AdvancedScanType) {
			log.println("Running Advanced translation");
			option = getResolvedAdvOptions((AdvancedScanType) projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option);
			}
		} else if (projectScanType instanceof JavaScanType) {
			log.println("Running Java translation");
			option = getResolvedTranslationExcludeList(listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option, "-exclude");
			}
			option = ((JavaScanType) projectScanType).getJavaVersion();
			if (StringUtils.isNotEmpty(option)) {
				args.add("-source");
				args.add(option);
			}
			option = getResolvedJavaClasspath((JavaScanType) projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				args.add("-cp");
				args.add(option);
			}
			option = getResolvedJavaAddOptions((JavaScanType) projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option);
			}
			option = getResolvedJavaSrcFiles((JavaScanType) projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option);
			}
		} else if (projectScanType instanceof DevenvScanType) {
			log.println("Running .NET devenv translation");
			option = getResolvedTranslationExcludeList(listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option, "-exclude");
			}
			args.add(getDevenvExecutable(build, workspace, launcher, listener, vars));
			option = getResolvedDotnetProjects(projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				args.add(option);
			}
			option = getResolvedDotnetAddOptions(projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option);
			}
		} else if (projectScanType instanceof MsbuildScanType) {
			log.println("Running .NET MSBuild translation");
			option = getResolvedTranslationExcludeList(listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option, "-exclude");
			}
			args.add(getMSBuildExecutable(build, workspace, launcher, listener, vars));
			option = getResolvedDotnetProjects(projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				args.add(option);
			}
			option = getResolvedDotnetAddOptions(projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option);
			}
		} else if (projectScanType instanceof DotnetSourceScanType) {
			log.println("Running .NET source code translation");
			option = getResolvedTranslationExcludeList(listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option, "-exclude");
			}
			option = getResolvedDotnetFrameworkVersion((DotnetSourceScanType) projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				args.add("-dotnet-version");
				args.add(option);
			}
			option = getResolvedDotnetLibdirs((DotnetSourceScanType) projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				args.add("-libdirs");
				args.add(option);
			}
			option = getResolvedDotnetAddOptions(projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option);
			}
			option = getResolvedDotnetSrcFiles((DotnetSourceScanType) projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option);
			}
		} else if (projectScanType instanceof MavenScanType) {
			log.println("Running Maven 3 translation");
			String mavenInstallationName = ((MavenScanType) projectScanType).getMavenInstallationName();
			args.add(getMavenExecutable(build, workspace, launcher, listener, mavenInstallationName, currentNode, vars));
			option = getResolvedTranslationExcludeList(listener);
			if (StringUtils.isNotEmpty(option)) {
				addMavenExcludes(args, option, launcher.isUnix());
			}
			option = getResolvedMavenOptions((MavenScanType) projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option);
			}
		} else if (projectScanType instanceof GradleScanType) {
			log.println("Running Gradle translation");
			if (getVerbose()) {
				args.add("-gradle");
			}
			String gradleInstallationName = ((GradleScanType) projectScanType).getGradleInstallationName();
			option = getResolvedTranslationExcludeList(listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option, "-exclude");
			}
			args.add(getGradleExecutable(((GradleScanType) projectScanType).getUseWrapper(), build, workspace, launcher, listener, gradleInstallationName, currentNode, vars));
			option = getResolvedGradleOptions((GradleScanType) projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option);
			}
			option = getResolvedGradleTasks((GradleScanType) projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option);
			}
		} else if (projectScanType instanceof OtherScanType) {
			log.println("Running Other translation");
			option = getResolvedTranslationExcludeList(listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option, "-exclude");
			}
			option = getResolvedOtherOptions((OtherScanType) projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option);
			}
			option = getResolvedOtherIncludesList((OtherScanType) projectScanType, listener);
			if (StringUtils.isNotEmpty(option)) {
				addAllArguments(args, option);
			}
		}

		ProcStarter ps = launcher.decorateByEnv(vars).launch().pwd(workspace).cmds(args).envs(vars)
				.stdout(listener.getLogger()).stderr(listener.getLogger());
		int exitcode = ps.join();
		log.println(Messages.FortifyTranslate_Result(exitcode));
		if (exitcode != 0) {
			build.setResult(Result.FAILURE);
			throw new AbortException(Messages.FortifyTranslate_Error());
		}

	}

	private void addMavenExcludes(List<String> args, String argsToAdd, boolean isUnix) {
		StringBuilder excludeParam = new StringBuilder();
		for (Iterator<String> itr = Arrays.asList(Util.tokenize(argsToAdd)).iterator(); itr.hasNext();) {
			excludeParam.append(itr.next());
			if (itr.hasNext()) {
				excludeParam.append(isUnix ? ":" : ";");
			}
		}
		args.add("-Dfortify.sca.exclude=" + excludeParam.toString());
	}

	@Extension
	public static class DescriptorImpl extends StepDescriptor {

		public DescriptorImpl() {
			load();
		}

		@Override
		public String getFunctionName() {
			return "fortifyTranslate";
		}

		@Override
		public String getDisplayName() {
			return Messages.FortifyTranslate_DisplayName();
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return ImmutableSet.of(Run.class, FilePath.class, EnvVars.class, Launcher.class, TaskListener.class);
		}

		public FormValidation doCheckBuildID(@QueryParameter String value) {
			return Validators.checkFieldNotEmpty(value);
		}

		public FormValidation doCheckMaxHeap(@QueryParameter String value) {
			return Validators.checkValidInteger(value);
		}

	}

	private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
		private transient FortifyTranslate ft;

		protected Execution(FortifyTranslate ft, StepContext context) {
			super(context);
			this.ft = ft;
		}

		@Override
		protected Void run() throws Exception {
			StepContext context = getContext();
			TaskListener taskListener = context.get(TaskListener.class);
			taskListener.getLogger().println("Running FortifyTranslate step");
			FilePath workspace = context.get(FilePath.class);
			if (!workspace.exists()) {
				workspace.mkdirs();
			}
			ft.setCurrentNode(context.get(Node.class));
			ft.perform(context.get(Run.class), workspace, context.get(EnvVars.class), context.get(Launcher.class), taskListener);
			return null;
		}

		private static final long serialVersionUID = 1L;
	}

}
