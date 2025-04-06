package io.jenkins.plugins.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class CredentialUtils {

    public static String getAPIKey(String key) {
        String APIKey = GlobalEnvVarHelper.getGlobalEnvVar(key);
        // Optional: load from .env in local dev
        if (APIKey == null || APIKey.trim().isEmpty()) {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            APIKey = dotenv.get(key);
        }
        return APIKey;
    }
}
