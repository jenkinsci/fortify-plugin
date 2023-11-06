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
