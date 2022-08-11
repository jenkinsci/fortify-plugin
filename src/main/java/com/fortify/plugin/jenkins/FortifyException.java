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
package com.fortify.plugin.jenkins;

public class FortifyException extends RuntimeException {
	private static final long serialVersionUID = 3038297165891293730L;
	protected Throwable cause;
	protected Message message;

	public FortifyException(Message msg) {
		super();
		this.message = msg;
	}

	public FortifyException(Message msg, Throwable cause) {
		super();
		this.cause = cause;
		this.message = msg;
	}

	@Override
	public Throwable getCause() {
		return cause;
	}

	@Override
	public String getMessage() {
		return message.getMessage();
	}

	public void setMessage(Message msg) {
		this.message = msg;
	}

}
