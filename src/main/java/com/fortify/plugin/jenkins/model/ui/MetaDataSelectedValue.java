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

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

public class MetaDataSelectedValue {

	protected Long projectVersionId;
	protected String metaDataDefinitionId;
	protected List<MetaDataValue> value;
	protected String freeFormValue;
	protected Boolean booleanValue;
	protected Long fileValueDocumentInfoId;
	protected XMLGregorianCalendar dateValue;
	protected Long integerValue;
	protected Integer objectVersion;

	public Long getProjectVersionId() {
		return projectVersionId;
	}

	public void setProjectVersionId(Long value) {
		this.projectVersionId = value;
	}

	public String getMetaDataDefinitionId() {
		return metaDataDefinitionId;
	}

	public void setMetaDataDefinitionId(String value) {
		this.metaDataDefinitionId = value;
	}

	public List<MetaDataValue> getValue() {
		if (value == null) {
			value = new ArrayList<MetaDataValue>();
		}
		return this.value;
	}

	public String getFreeFormValue() {
		return freeFormValue;
	}

	public void setFreeFormValue(String value) {
		this.freeFormValue = value;
	}

	public Boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean value) {
		this.booleanValue = value;
	}

	public Long getFileValueDocumentInfoId() {
		return fileValueDocumentInfoId;
	}

	public void setFileValueDocumentInfoId(Long value) {
		this.fileValueDocumentInfoId = value;
	}

	public XMLGregorianCalendar getDateValue() {
		return dateValue;
	}

	public void setDateValue(XMLGregorianCalendar value) {
		this.dateValue = value;
	}

	public Long getIntegerValue() {
		return integerValue;
	}

	public void setIntegerValue(Long value) {
		this.integerValue = value;
	}

	public Integer getObjectVersion() {
		return objectVersion;
	}

	public void setObjectVersion(Integer value) {
		this.objectVersion = value;
	}

}
