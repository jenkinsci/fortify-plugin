/*******************************************************************************
 * Copyright 2019 - 2023 Open Text.
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
package com.fortify.plugin.jenkins.credentials;

import java.util.regex.Pattern;

/**
 * Utility class for token format validation.
 */
public class TokenUtil {
	private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
	private static final Pattern UUID_REGEX_PATTERN = Pattern.compile(UUID_REGEX);

	/**
	 * Check whether the given string is in UUID format.
	 *
	 * @param str Input string to be checked for UUID format
	 * @return {@code true} if the input is a UUID format string, {@code false} otherwise
	 */
	public static boolean isUuid(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
		return UUID_REGEX_PATTERN.matcher(str).matches();
	}
}
