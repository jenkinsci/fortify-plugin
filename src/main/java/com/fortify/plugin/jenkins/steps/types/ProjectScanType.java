/*******************************************************************************
 * Copyright 2019 - 2023 Open Text. 
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
package com.fortify.plugin.jenkins.steps.types;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public abstract class ProjectScanType implements ExtensionPoint, Describable<ProjectScanType> {

	@Override
	public Descriptor<ProjectScanType> getDescriptor() {
		return Jenkins.get().getDescriptor(this.getClass());
	}

	public DescriptorExtensionList<ProjectScanType, Descriptor<ProjectScanType>> getProjectScanTypes() {
		return Jenkins.get().getDescriptorList(ProjectScanType.class);
	}

	public abstract static class ProjectScanTypeDescriptor extends Descriptor<ProjectScanType> {
		public ProjectScanTypeDescriptor(Class<? extends ProjectScanType> clazz) {
			super(clazz);
		}

		@Override
		abstract public String getDisplayName();
	}

}
