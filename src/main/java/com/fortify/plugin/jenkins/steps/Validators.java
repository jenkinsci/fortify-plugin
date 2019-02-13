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
package com.fortify.plugin.jenkins.steps;

import org.apache.commons.lang.StringUtils;

import com.fortify.plugin.jenkins.Messages;

import hudson.util.FormValidation;

public class Validators {

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

	public static FormValidation checkValidNumber(String value) {
		if (StringUtils.isBlank(value)) {
			return FormValidation.ok();
		}

		double testNum = 0;
		try {
			testNum = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return FormValidation.error(Messages.FortifySCAStep_Check_Number());
		}
		if (testNum < 1) {
			return FormValidation.error(Messages.FortifySCAStep_Check_Number());
		}
		return FormValidation.ok();
	}
}
