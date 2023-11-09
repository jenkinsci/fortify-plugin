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

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardCredentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.util.Secret;

import java.io.IOException;

@NameWith(value = FortifyApiToken.NameProvider.class, priority = 1)
public interface FortifyApiToken extends StandardCredentials {

    @NonNull
    Secret getToken() throws IOException, InterruptedException;

    class NameProvider extends CredentialsNameProvider<FortifyApiToken> {

        @NonNull
        @Override
        public String getName(@NonNull final FortifyApiToken credentials) {
            final String description = Util.fixEmptyAndTrim(credentials.getDescription());
            return credentials.getId() + "/*fortify*" + (description != null ? " (" + description + ")" : "");
        }
    }
}
