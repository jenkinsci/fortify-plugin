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

public class TagHistory {

	protected Tag tag;
	protected XMLGregorianCalendar editTime;
	protected String oldValue;
	protected String username;
	protected Boolean resolve;

	/**
	 * Gets the value of the tag property.
	 * 
	 * @return possible object is {@link Tag }
	 * 
	 */
	public Tag getTag() {
		return tag;
	}

	/**
	 * Sets the value of the tag property.
	 * 
	 * @param value
	 *            allowed object is {@link Tag }
	 * 
	 */
	public void setTag(Tag value) {
		this.tag = value;
	}

	/**
	 * Gets the value of the editTime property.
	 * 
	 * @return possible object is {@link XMLGregorianCalendar }
	 * 
	 */
	public XMLGregorianCalendar getEditTime() {
		return editTime;
	}

	/**
	 * Sets the value of the editTime property.
	 * 
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 * 
	 */
	public void setEditTime(XMLGregorianCalendar value) {
		this.editTime = value;
	}

	/**
	 * Gets the value of the oldValue property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOldValue() {
		return oldValue;
	}

	/**
	 * Sets the value of the oldValue property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setOldValue(String value) {
		this.oldValue = value;
	}

	/**
	 * Gets the value of the username property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the value of the username property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUsername(String value) {
		this.username = value;
	}

	/**
	 * Gets the value of the resolve property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isResolve() {
		return resolve;
	}

	/**
	 * Sets the value of the resolve property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setResolve(Boolean value) {
		this.resolve = value;
	}

}
