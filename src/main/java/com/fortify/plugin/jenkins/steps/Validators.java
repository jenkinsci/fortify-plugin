/*******************************************************************************
 * Copyright 2019-2023 Open Text.
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
package com.fortify.plugin.jenkins.steps;

import org.apache.commons.lang.StringUtils;

import com.fortify.plugin.jenkins.Messages;

import hudson.util.FormValidation;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class Validators {
	private static final String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

	public static FormValidation checkFieldNotEmpty(String value) {
		value = StringUtils.strip(value);

		if (value == null || value.equals("")) {
			return FormValidation.error(Messages.FortifySCAStep_Check_Required());
		}

		return FormValidation.ok();
	}

	public static FormValidation checkValidInteger(String value) {
		if (StringUtils.isBlank(value)) {
			return FormValidation.ok();
		}

		int testInt = 0;
		try {
			testInt = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return FormValidation.error(Messages.FortifySCAStep_Check_Integer());
		}
		if (testInt < 1) {
			return FormValidation.error(Messages.FortifySCAStep_Check_Integer());
		}
		return FormValidation.ok();
	}

	public static FormValidation checkValidVersionNumber(String value) {
		if (StringUtils.isBlank(value)) {
			return FormValidation.ok();
		}
		StringTokenizer tz = new StringTokenizer(value, ".");
		int testNum = 0;
		while (tz.hasMoreTokens()) {
			try {
				testNum = Integer.parseInt(tz.nextToken());
			} catch (NumberFormatException e) {
				return FormValidation.error(Messages.FortifySCAStep_Check_Version_Number());
			}
			if (testNum < 0 || testNum > 1000) {
				return FormValidation.error(Messages.FortifySCAStep_Check_Version_Number());
			}
		}
		return FormValidation.ok();
	}

	public static FormValidation checkValidEmail(String value) {
		if (StringUtils.isBlank(value)) {
			return FormValidation.ok();
		}

		Pattern pat = Pattern.compile(emailPattern);
		try {
			InternetAddress emailAddr = new InternetAddress(value);
			emailAddr.validate();
			if (pat.matcher(value).matches()) {
				return FormValidation.ok();
			} else {
				return FormValidation.error("Invalid email address");
			}
		} catch (AddressException ex) {
			return FormValidation.error("Invalid email address");
		}
	}
}
