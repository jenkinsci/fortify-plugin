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

import javax.xml.datatype.XMLGregorianCalendar;

public class CustomIssue extends IssueBase {

	protected String category;
	protected String file;
	protected Integer line;
	protected XMLGregorianCalendar creationDate;
	protected String ruleId;

	/**
	 * Gets the value of the category property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Sets the value of the category property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCategory(String value) {
		this.category = value;
	}

	/**
	 * Gets the value of the file property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Sets the value of the file property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setFile(String value) {
		this.file = value;
	}

	/**
	 * Gets the value of the line property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getLine() {
		return line;
	}

	/**
	 * Sets the value of the line property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setLine(Integer value) {
		this.line = value;
	}

	/**
	 * Gets the value of the creationDate property.
	 * 
	 * @return possible object is {@link XMLGregorianCalendar }
	 * 
	 */
	public XMLGregorianCalendar getCreationDate() {
		return creationDate;
	}

	/**
	 * Sets the value of the creationDate property.
	 * 
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 * 
	 */
	public void setCreationDate(XMLGregorianCalendar value) {
		this.creationDate = value;
	}

	/**
	 * Gets the value of the ruleId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getRuleId() {
		return ruleId;
	}

	/**
	 * Sets the value of the ruleId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setRuleId(String value) {
		this.ruleId = value;
	}

}
