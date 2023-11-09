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
package com.fortify.plugin.jenkins.bean;

import java.util.ArrayList;
import java.util.List;

public class MetaDataDefinition {
	protected String name;
	protected String description;
	protected List<MetaDataValue> value;
	protected Boolean freeFormValue;
	protected Boolean freeFormSensitiveValue;
	protected Boolean freeFormLongValue;
	protected Boolean booleanValue;
	protected Boolean integerValue;
	protected Boolean fileValue;
	protected Boolean dateValue;
	protected String id;
	protected ProjectMetaDataCategory type;
	protected Boolean multiple;
	protected Boolean hidden;
	protected Boolean required;
	protected String appEntityType;
	protected SystemUsageType systemUsage;

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

	public List<MetaDataValue> getValue() {
		if (value == null) {
			value = new ArrayList<MetaDataValue>();
		}
		return this.value;
	}

	public Boolean isFreeFormValue() {
		return freeFormValue;
	}

	public void setFreeFormValue(Boolean value) {
		this.freeFormValue = value;
	}

	public Boolean isFreeFormSensitiveValue() {
		return freeFormSensitiveValue;
	}

	public void setFreeFormSensitiveValue(Boolean value) {
		this.freeFormSensitiveValue = value;
	}

	public Boolean isFreeFormLongValue() {
		return freeFormLongValue;
	}

	public void setFreeFormLongValue(Boolean value) {
		this.freeFormLongValue = value;
	}

	public Boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean value) {
		this.booleanValue = value;
	}

	public Boolean isIntegerValue() {
		return integerValue;
	}

	public void setIntegerValue(Boolean value) {
		this.integerValue = value;
	}

	public Boolean isFileValue() {
		return fileValue;
	}

	public void setFileValue(Boolean value) {
		this.fileValue = value;
	}

	public Boolean isDateValue() {
		return dateValue;
	}

	public void setDateValue(Boolean value) {
		this.dateValue = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String value) {
		this.id = value;
	}

	public ProjectMetaDataCategory getType() {
		return type;
	}

	public void setType(ProjectMetaDataCategory value) {
		this.type = value;
	}

	public Boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean value) {
		this.multiple = value;
	}

	public Boolean isHidden() {
		return hidden;
	}

	public void setHidden(Boolean value) {
		this.hidden = value;
	}

	public Boolean isRequired() {
		return required;
	}

	public void setRequired(Boolean value) {
		this.required = value;
	}

	public String getAppEntityType() {
		return appEntityType;
	}

	public void setAppEntityType(String value) {
		this.appEntityType = value;
	}

	public SystemUsageType getSystemUsage() {
		return systemUsage;
	}

	public void setSystemUsage(SystemUsageType value) {
		this.systemUsage = value;
	}

}
