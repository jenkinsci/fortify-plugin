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

public class IssueBase {

	protected List<Tag> tag;
	protected String assignedUser;
	protected History managerAuditTrail;
	protected History clientAuditTrail;
	protected ThreadedComments threadedComments;
	protected List<Object> any;
	protected String instanceId;
	protected Boolean suppressed;
	protected Boolean hidden;
	protected Boolean removed;
	protected int revision;

	/**
	 * Gets the value of the tag property.
	 * 
	 */
	public List<Tag> getTag() {
		if (tag == null) {
			tag = new ArrayList<Tag>();
		}
		return this.tag;
	}

	/**
	 * Gets the value of the assignedUser property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAssignedUser() {
		return assignedUser;
	}

	/**
	 * Sets the value of the assignedUser property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAssignedUser(String value) {
		this.assignedUser = value;
	}

	/**
	 * Gets the value of the managerAuditTrail property.
	 * 
	 * @return possible object is {@link History }
	 * 
	 */
	public History getManagerAuditTrail() {
		return managerAuditTrail;
	}

	/**
	 * Sets the value of the managerAuditTrail property.
	 * 
	 * @param value
	 *            allowed object is {@link History }
	 * 
	 */
	public void setManagerAuditTrail(History value) {
		this.managerAuditTrail = value;
	}

	/**
	 * Gets the value of the clientAuditTrail property.
	 * 
	 * @return possible object is {@link History }
	 * 
	 */
	public History getClientAuditTrail() {
		return clientAuditTrail;
	}

	/**
	 * Sets the value of the clientAuditTrail property.
	 * 
	 * @param value
	 *            allowed object is {@link History }
	 * 
	 */
	public void setClientAuditTrail(History value) {
		this.clientAuditTrail = value;
	}

	/**
	 * Gets the value of the threadedComments property.
	 * 
	 * @return possible object is {@link ThreadedComments }
	 * 
	 */
	public ThreadedComments getThreadedComments() {
		return threadedComments;
	}

	/**
	 * Sets the value of the threadedComments property.
	 * 
	 * @param value
	 *            allowed object is {@link ThreadedComments }
	 * 
	 */
	public void setThreadedComments(ThreadedComments value) {
		this.threadedComments = value;
	}

	/**
	 * Gets the value of the any property.
	 * 
	 */
	public List<Object> getAny() {
		if (any == null) {
			any = new ArrayList<Object>();
		}
		return this.any;
	}

	/**
	 * Gets the value of the instanceId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * Sets the value of the instanceId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setInstanceId(String value) {
		this.instanceId = value;
	}

	/**
	 * Gets the value of the suppressed property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isSuppressed() {
		return suppressed;
	}

	/**
	 * Sets the value of the suppressed property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setSuppressed(Boolean value) {
		this.suppressed = value;
	}

	/**
	 * Gets the value of the hidden property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isHidden() {
		return hidden;
	}

	/**
	 * Sets the value of the hidden property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setHidden(Boolean value) {
		this.hidden = value;
	}

	/**
	 * Gets the value of the removed property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isRemoved() {
		return removed;
	}

	/**
	 * Sets the value of the removed property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setRemoved(Boolean value) {
		this.removed = value;
	}

	/**
	 * Gets the value of the revision property.
	 * 
	 */
	public int getRevision() {
		return revision;
	}

	/**
	 * Sets the value of the revision property.
	 * 
	 */
	public void setRevision(int value) {
		this.revision = value;
	}

}
