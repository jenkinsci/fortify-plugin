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

import java.util.Objects;

import org.threeten.bp.OffsetDateTime;

import com.google.gson.annotations.SerializedName;

/**
 * A project or application on the server
 */

public class Project {
	@SerializedName("createdBy")
	private String createdBy = null;

	@SerializedName("creationDate")
	private OffsetDateTime creationDate = null;

	@SerializedName("description")
	private String description = null;

	@SerializedName("id")
	private Long id = null;

	@SerializedName("issueTemplateId")
	private String issueTemplateId = null;

	@SerializedName("name")
	private String name = null;

	public Project createdBy(String createdBy) {
		this.createdBy = createdBy;
		return this;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public OffsetDateTime getCreationDate() {
		return creationDate;
	}

	public Project description(String description) {
		this.description = description;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Project issueTemplateId(String issueTemplateId) {
		this.issueTemplateId = issueTemplateId;
		return this;
	}

	public String getIssueTemplateId() {
		return issueTemplateId;
	}

	public void setIssueTemplateId(String issueTemplateId) {
		this.issueTemplateId = issueTemplateId;
	}

	public Project name(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Project project = (Project) o;
		return Objects.equals(this.createdBy, project.createdBy)
				&& Objects.equals(this.creationDate, project.creationDate)
				&& Objects.equals(this.description, project.description) && Objects.equals(this.id, project.id)
				&& Objects.equals(this.issueTemplateId, project.issueTemplateId)
				&& Objects.equals(this.name, project.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(createdBy, creationDate, description, id, issueTemplateId, name);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Project {\n");

		sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
		sb.append("    creationDate: ").append(toIndentedString(creationDate)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    issueTemplateId: ").append(toIndentedString(issueTemplateId)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

}
