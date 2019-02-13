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
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.fortify.plugin.jenkins.Messages;
import com.google.common.collect.ImmutableSet;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;

public class FortifyClean extends FortifySCAStep implements SimpleBuildStep {

	@DataBoundConstructor
	public FortifyClean(String buildID) {
		this.buildID = buildID;
	}

	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
		return false;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		perform(build, build.getWorkspace(), launcher, listener);
		return true;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return null;
	}

	@Override
	public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
		return null;
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
		log.println("Launching Fortify SCA clean command");
		String projectRoot = workspace.getRemote() + File.separator + ".fortify";
		String sourceanalyzer = null;

		if (sourceanalyzer == null) {
			sourceanalyzer = getSourceAnalyzerExecutable(build, workspace, launcher, listener);
		}

		EnvVars vars = build.getEnvironment(listener);
		ArrayList<String> args = new ArrayList<String>(2);
		args.add(sourceanalyzer);
		args.add("-Dcom.fortify.sca.ProjectRoot=" + projectRoot);
		args.add("-clean");
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
		ProcStarter ps = launcher.decorateByEnv(vars).launch().pwd(workspace).cmds(args).envs(vars)
				.stdout(listener.getLogger()).stderr(listener.getLogger());
		int exitcode = ps.join();
		log.println(Messages.FortifyClean_Result(exitcode));

		if (exitcode != 0) {
			build.setResult(Result.FAILURE);
			throw new AbortException(Messages.FortifyClean_Error());
		}

	}

	@Extension
	public static class DescriptorImpl extends StepDescriptor {
		@Override
		public String getFunctionName() {
			return "fortifyClean";
		}

		@Override
		public String getDisplayName() {
			return Messages.FortifyClean_DisplayName();
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
		private transient FortifyClean fc;

		protected Execution(FortifyClean fc, StepContext context) {
			super(context);
			this.fc = fc;
		}

		@Override
		protected Void run() throws Exception {
			getContext().get(TaskListener.class).getLogger().println("Running FortifyClean step");
			if (!getContext().get(FilePath.class).exists()) {
				getContext().get(FilePath.class).mkdirs();
			}
			fc.perform(getContext().get(Run.class), getContext().get(FilePath.class), getContext().get(Launcher.class),
					getContext().get(TaskListener.class));

			return null;
		}

		private static final long serialVersionUID = 1L;

	}

}