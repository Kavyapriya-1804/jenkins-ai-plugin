package io.jenkins.plugins.utils;

import hudson.slaves.EnvironmentVariablesNodeProperty;
import java.util.Map;
import jenkins.model.Jenkins;

public class GlobalEnvVarHelper {
    public static String getGlobalEnvVar(String key) {
        String envVar = null;
        Jenkins jenkins = null;
        try {
            jenkins = Jenkins.getInstance();

            if (jenkins == null) {
                return envVar;
            }

            // Iterate over all global node properties to find environment variables
            for (EnvironmentVariablesNodeProperty envProp :
                    jenkins.getGlobalNodeProperties().getAll(EnvironmentVariablesNodeProperty.class)) {
                Map<String, String> envVars = envProp.getEnvVars();

                if (envVars.containsKey(key)) {
                    envVar = envVars.get(key);
                    return envVar;
                }
            }
        } catch (Exception e) {
            return envVar;
        }
        return envVar;
    }
}
