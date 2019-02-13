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
package com.fortify.plugin.jenkins.model.ui;

public class ProjectVersionLite {

	protected long id;
	protected TemplateMode mode;
	protected String name;
	protected String description;
	protected long projectId;
	protected boolean committed;
	protected String projectTemplateId;

	public long getId() {
		return id;
	}

	public void setId(long value) {
		this.id = value;
	}

	public TemplateMode getMode() {
		return mode;
	}

	public void setMode(TemplateMode value) {
		this.mode = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String value) {
		this.description = value;
	}

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long value) {
		this.projectId = value;
	}

	public boolean isCommitted() {
		return committed;
	}

	public void setCommitted(boolean value) {
		this.committed = value;
	}

	public String getProjectTemplateId() {
		return projectTemplateId;
	}

	public void setProjectTemplateId(String value) {
		this.projectTemplateId = value;
	}

}
