/*******************************************************************************
 * Copyright 2025 Open Text.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TokenUtilTest {

	@Test
	public void testValidUuid() {
		assertTrue(TokenUtil.isUuid("550e8400-e29b-41d4-a716-446655440000"));
	}

	@Test
	public void testUpperCaseUuid() {
		assertTrue(TokenUtil.isUuid("550E8400-E29B-41D4-A716-446655440000"));
	}

	@Test
	public void testNullInput() {
		assertFalse(TokenUtil.isUuid(null));
	}

	@Test
	public void testEmptyString() {
		assertFalse(TokenUtil.isUuid(""));
	}

	@Test
	public void testNonUuidString() {
		assertFalse(TokenUtil.isUuid("not-a-uuid"));
	}

	@Test
	public void testBase64Token() {
		assertFalse(TokenUtil.isUuid("YWJjZGVmZzEyMzQ1Njc4OQ=="));
	}
}
