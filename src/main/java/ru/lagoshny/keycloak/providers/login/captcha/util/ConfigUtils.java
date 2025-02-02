package ru.lagoshny.keycloak.providers.login.captcha.util;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.AuthenticatorConfigModel;


/**
 * Helper utils to work with {@link AuthenticatorConfigModel} obtained from {@link AuthenticationFlowContext}.
 */
public class ConfigUtils {

    /**
     * @param context execution {@link AuthenticationFlowContext}
     * @param key configuration param key
     * @return configuration param value
     */
    public static String getConfigVal(AuthenticationFlowContext context, String key) {
        if (context == null
                || context.getAuthenticatorConfig() == null
                || context.getAuthenticatorConfig().getConfig() == null) {
            throw new IllegalArgumentException("Passed context does not have configuration params, please configure it");
        }

        if (!context.getAuthenticatorConfig().getConfig().containsKey(key)) {
            throw new IllegalArgumentException("Config error: by key=" + key + " not found any value!");
        }

        return context.getAuthenticatorConfig().getConfig().get(key);
    }

}
