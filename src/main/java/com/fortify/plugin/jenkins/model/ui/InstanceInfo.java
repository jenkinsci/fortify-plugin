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

public class InstanceInfo {

	protected String sinkFunction;
	protected String sourceFunction;
	protected IssueLocation issueLocation;
	protected double severity;
	protected double confidence;

	/**
	 * Gets the value of the sinkFunction property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSinkFunction() {
		return sinkFunction;
	}

	/**
	 * Sets the value of the sinkFunction property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSinkFunction(String value) {
		this.sinkFunction = value;
	}

	/**
	 * Gets the value of the sourceFunction property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSourceFunction() {
		return sourceFunction;
	}

	/**
	 * Sets the value of the sourceFunction property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSourceFunction(String value) {
		this.sourceFunction = value;
	}

	/**
	 * Gets the value of the issueLocation property.
	 * 
	 * @return possible object is {@link IssueLocation }
	 * 
	 */
	public IssueLocation getIssueLocation() {
		return issueLocation;
	}

	/**
	 * Sets the value of the issueLocation property.
	 * 
	 * @param value
	 *            allowed object is {@link IssueLocation }
	 * 
	 */
	public void setIssueLocation(IssueLocation value) {
		this.issueLocation = value;
	}

	/**
	 * Gets the value of the severity property.
	 * 
	 */
	public double getSeverity() {
		return severity;
	}

	/**
	 * Sets the value of the severity property.
	 * 
	 */
	public void setSeverity(double value) {
		this.severity = value;
	}

	/**
	 * Gets the value of the confidence property.
	 * 
	 */
	public double getConfidence() {
		return confidence;
	}

	/**
	 * Sets the value of the confidence property.
	 * 
	 */
	public void setConfidence(double value) {
		this.confidence = value;
	}

}
