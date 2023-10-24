package com.fortify.plugin.jenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.Util;
import hudson.util.Secret;

import java.io.IOException;

import javax.annotation.Nonnull;

@NameWith(value = FortifyApiToken.NameProvider.class, priority = 1)
public interface FortifyApiToken extends StandardCredentials {

    @Nonnull
    Secret getToken() throws IOException, InterruptedException;

    class NameProvider extends CredentialsNameProvider<FortifyApiToken> {

        @Nonnull
        @Override
        public String getName(@Nonnull final FortifyApiToken credentials) {
            final String description = Util.fixEmptyAndTrim(credentials.getDescription());
            return credentials.getId() + "/*fortify*" + (description != null ? " (" + description + ")" : "");
        }
    }
}
