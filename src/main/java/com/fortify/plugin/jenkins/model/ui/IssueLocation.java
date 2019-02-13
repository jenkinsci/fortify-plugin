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

public class IssueLocation {

	protected String _package;
	protected String className;
	protected String function;
	protected String filePath;
	protected int lineNumber;
	protected String url;
	protected String sourceFilePath;

	/**
	 * Gets the value of the package property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPackage() {
		return _package;
	}

	/**
	 * Sets the value of the package property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPackage(String value) {
		this._package = value;
	}

	/**
	 * Gets the value of the className property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Sets the value of the className property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setClassName(String value) {
		this.className = value;
	}

	/**
	 * Gets the value of the function property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * Sets the value of the function property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setFunction(String value) {
		this.function = value;
	}

	/**
	 * Gets the value of the filePath property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Sets the value of the filePath property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setFilePath(String value) {
		this.filePath = value;
	}

	/**
	 * Gets the value of the lineNumber property.
	 * 
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Sets the value of the lineNumber property.
	 * 
	 */
	public void setLineNumber(int value) {
		this.lineNumber = value;
	}

	/**
	 * Gets the value of the url property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getURL() {
		return url;
	}

	/**
	 * Sets the value of the url property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setURL(String value) {
		this.url = value;
	}

	/**
	 * Gets the value of the sourceFilePath property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSourceFilePath() {
		return sourceFilePath;
	}

	/**
	 * Sets the value of the sourceFilePath property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSourceFilePath(String value) {
		this.sourceFilePath = value;
	}

}
