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

import java.util.logging.Level;

public class Message {

	public static final int NONE = 0;
	public static final int INFORMATIONAL = 1;
	public static final int INFO_VERBOSE = 2;
	public static final int WARNING = 3;
	public static final int ERROR = 4;
	public static final int LOG = 5;
	public static final int INTERNAL_WARNING = 6;
	public static final int EXCEPTION = 7;
	public static final int BUG = 8;
	public static final int LOG_VERBOSE = 9;
	public static final int LOG_STATUS = 10;

	public static final int DEFAULT_ERROR_CODE = -1;

	private Level level;
	private Throwable throwable;
	private String message;
	private Object[] parameters;
	private int severity;
	private final int errorCode;

	public Message(int errorCode, Level level, int severity, Throwable throwable, String message, Object[] parameters) {
		this.level = level;
		this.throwable = throwable;
		this.message = message;
		this.parameters = parameters;
		this.severity = severity;
		this.errorCode = errorCode;
	}

	public Message(int severity, String message) {
		this(DEFAULT_ERROR_CODE, Level.OFF, severity, null, message, null);
	}

	public Message(int severity, int errorCode, Object... parameters) {
		this(errorCode, Level.OFF, severity, null, null, parameters);
	}

	public Message(int severity, String message, Object... parameters) {
		this(DEFAULT_ERROR_CODE, Level.OFF, severity, null, message, parameters);
	}

	public String getMessage() {
		return message;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public Level getLogLevel() {
		if (level != Level.OFF) {
			return level;
		}
		switch (severity) {
		case NONE:
			return Level.ALL;
		case INFORMATIONAL:
			return Level.INFO;
		case INFO_VERBOSE:
			return Level.INFO;
		case WARNING:
			return Level.WARNING;
		case ERROR:
			return Level.SEVERE;
		case LOG:
			return Level.FINE;
		case INTERNAL_WARNING:
			return Level.WARNING;
		case EXCEPTION:
			return Level.SEVERE;
		case BUG:
			return Level.SEVERE;
		case LOG_VERBOSE:
			return Level.FINE;
		case LOG_STATUS:
			return Level.INFO;
		default:
			return Level.WARNING;
		}
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public boolean hasErrorCode() {
		return errorCode != DEFAULT_ERROR_CODE;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public static Message mk(int ec) {
		return new Message(NONE, ec, (Object[]) null);
	}

	public static Message mk(int ec, Object... params) {
		return new Message(NONE, ec, params);
	}
}
