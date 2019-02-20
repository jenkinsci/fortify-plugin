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
package com.fortify.plugin.jenkins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fortify.plugin.jenkins.steps.FortifyUpload;

import hudson.model.Action;
import hudson.model.Job;
import jenkins.tasks.SimpleBuildStep;

public class FortifyUploadBuildAction implements Action, SimpleBuildStep.LastBuildAction {

	private List<Action> projectActions = null;

	public FortifyUploadBuildAction() {
		super();
	}

	public void addAppVersion(Job<?, ?> project, FortifyUpload upload, String appName, String appVersion) {
		boolean chartExists = false;
		boolean tableExists = false;
		Collection<Action> actions = (Collection<Action>) getProjectActions();
		for (Action existingAction : actions) {
			if (existingAction instanceof ChartAction && appName.equals(((ChartAction) existingAction).getAppName())
					&& appVersion.equals(((ChartAction) existingAction).getAppVersion())) {
				chartExists = true;
			}
			if (existingAction instanceof TableAction && appName.equals(((TableAction) existingAction).getAppName())
					&& appVersion.equals(((TableAction) existingAction).getAppVersion())) {
				tableExists = true;
			}
		}
		if (!chartExists) {
			actions.add(new ChartAction(project, upload.isPipeline(), appName, appVersion));
		}
		if (!tableExists) {
			actions.add(new TableAction(project, upload, appName, appVersion));
		}
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public String getUrlName() {
		return null;
	}

	@Override
	public Collection<? extends Action> getProjectActions() {
		if (projectActions == null) {
			projectActions = new ArrayList<Action>();
		}
		return projectActions;
	}
}
