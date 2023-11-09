/*******************************************************************************
 * Copyright 2019-2023 Open Text.
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
package com.fortify.plugin.jenkins.steps.remote;

import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

import java.util.List;

public abstract class RemoteAnalysisProjectType implements ExtensionPoint, Describable<RemoteAnalysisProjectType> {
    @Override
    public Descriptor<RemoteAnalysisProjectType> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(getClass());
    }

    public List<RemoteAnalysisProjectTypeDescriptor> getRemoteAnalysisProjectTypeDescriptors() {
        return Jenkins.get().getDescriptorList(RemoteAnalysisProjectType.class);
    }

    public abstract static class RemoteAnalysisProjectTypeDescriptor extends Descriptor<RemoteAnalysisProjectType> {
        public RemoteAnalysisProjectTypeDescriptor(Class<? extends RemoteAnalysisProjectType> clazz) {
            super(clazz);
        }

        @Override
        abstract public String getDisplayName();
    }
}
