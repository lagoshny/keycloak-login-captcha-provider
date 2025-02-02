package ru.lagoshny.keycloak.providers.login.captcha.client;


import org.keycloak.authentication.AuthenticationFlowContext;

/**
 * Captcha client that uses to perform request for user's captcha validation.
 */
public interface CaptchaClient {

    /**
     * Performing a request to the captcha api provider to verify user's captcha token.
     *
     * @param context      execution {@link AuthenticationFlowContext}
     * @param captchaToken string value of user's captcha token obtained after captcha form passing
     * @return {@code true} if captcha validation is successful, {@code false} otherwise
     */
    boolean validateCaptchaToken(AuthenticationFlowContext context, String captchaToken);

    /**
     * URL to verify user's captcha code
     */
    String getCaptchaValidateUrl();

}
