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

public class RemovedIssue extends IssueBase {

	protected String category;
	protected String product;
	protected String file;
	protected Integer line;
	protected Float confidence;
	protected Float severity;
	protected Float probability;
	protected Float accuracy;
	protected Float impact;
	protected XMLGregorianCalendar removeScanDate;

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
	 * Gets the value of the product property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getProduct() {
		return product;
	}

	/**
	 * Sets the value of the product property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setProduct(String value) {
		this.product = value;
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
	 * Gets the value of the confidence property.
	 * 
	 * @return possible object is {@link Float }
	 * 
	 */
	public Float getConfidence() {
		return confidence;
	}

	/**
	 * Sets the value of the confidence property.
	 * 
	 * @param value
	 *            allowed object is {@link Float }
	 * 
	 */
	public void setConfidence(Float value) {
		this.confidence = value;
	}

	/**
	 * Gets the value of the severity property.
	 * 
	 * @return possible object is {@link Float }
	 * 
	 */
	public Float getSeverity() {
		return severity;
	}

	/**
	 * Sets the value of the severity property.
	 * 
	 * @param value
	 *            allowed object is {@link Float }
	 * 
	 */
	public void setSeverity(Float value) {
		this.severity = value;
	}

	/**
	 * Gets the value of the probability property.
	 * 
	 * @return possible object is {@link Float }
	 * 
	 */
	public Float getProbability() {
		return probability;
	}

	/**
	 * Sets the value of the probability property.
	 * 
	 * @param value
	 *            allowed object is {@link Float }
	 * 
	 */
	public void setProbability(Float value) {
		this.probability = value;
	}

	/**
	 * Gets the value of the accuracy property.
	 * 
	 * @return possible object is {@link Float }
	 * 
	 */
	public Float getAccuracy() {
		return accuracy;
	}

	/**
	 * Sets the value of the accuracy property.
	 * 
	 * @param value
	 *            allowed object is {@link Float }
	 * 
	 */
	public void setAccuracy(Float value) {
		this.accuracy = value;
	}

	/**
	 * Gets the value of the impact property.
	 * 
	 * @return possible object is {@link Float }
	 * 
	 */
	public Float getImpact() {
		return impact;
	}

	/**
	 * Sets the value of the impact property.
	 * 
	 * @param value
	 *            allowed object is {@link Float }
	 * 
	 */
	public void setImpact(Float value) {
		this.impact = value;
	}

	/**
	 * Gets the value of the removeScanDate property.
	 * 
	 * @return possible object is {@link XMLGregorianCalendar }
	 * 
	 */
	public XMLGregorianCalendar getRemoveScanDate() {
		return removeScanDate;
	}

	/**
	 * Sets the value of the removeScanDate property.
	 * 
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 * 
	 */
	public void setRemoveScanDate(XMLGregorianCalendar value) {
		this.removeScanDate = value;
	}

}
