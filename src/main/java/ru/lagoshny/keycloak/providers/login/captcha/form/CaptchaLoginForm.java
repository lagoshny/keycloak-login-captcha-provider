package ru.lagoshny.keycloak.providers.login.captcha.form;

import org.apache.commons.lang3.BooleanUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import ru.lagoshny.keycloak.providers.login.captcha.client.CaptchaClient;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.keycloak.utils.StringUtil.isBlank;
import static ru.lagoshny.keycloak.providers.login.captcha.form.CaptchaLoginFormFactory.*;
import static ru.lagoshny.keycloak.providers.login.captcha.util.ConfigUtils.getConfigVal;

/**
 * Custom {@link UsernamePasswordForm} provider that adds the captcha validation step.
 * You need to change the standard browser flow to use this provider instead default one.
 * <p>
 * See more about browser flow in <a href="https://www.keycloak.org/docs/latest/server_development/#algorithm-overview">documentation</a>.
 */
public class CaptchaLoginForm extends UsernamePasswordForm {

    public static final String CAPTCHA_FORM_NOT_PASSED_MSG_KEY = "captchaFormNotPassed";

    public static final String CAPTCHA_INVALID_TOKEN_MSG_KEY = "captchaInvalidToken";

    private final Map<String, CaptchaProvider> captchaProviders = Arrays.stream(CaptchaProvider.values())
            .collect(Collectors.toMap(CaptchaProvider::getName, value -> value));


    @Override
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        // add additional form attributes with values from provider config to manage captcha on auth form
        fillCustomFormAttributes(context);
        return super.challenge(context, formData);
    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        // add additional form attributes with values from provider config to manage captcha on auth form
        fillCustomFormAttributes(context);
        return super.challenge(context, error, field);
    }

    private void fillCustomFormAttributes(AuthenticationFlowContext context) {
        if ((context != null) && (context.form() != null) &&
                BooleanUtils.toBoolean(getConfigVal(context, CONFIG_KEY_CAPTCHA_ENABLED))) {
            CaptchaProvider captchaProvider = captchaProviders.get(getConfigVal(context, CONFIG_KEY_CAPTCHA_PROVIDER));
            context.form().setAttribute(CONFIG_KEY_CAPTCHA_ENABLED, getConfigVal(context, CONFIG_KEY_CAPTCHA_ENABLED));
            context.form().setAttribute(CONFIG_KEY_CAPTCHA_SCRIPT_URL, captchaProvider.getCaptchaScriptUrl());
            context.form().setAttribute(CAPTCHA_HTML_BLOCK, captchaProvider.getCaptchaHtmlBlock(getConfigVal(context, CONFIG_KEY_CLIENT_KEY)));
            context.form().setAttribute(CONFIG_KEY_CAPTCHA_PROVIDER, captchaProvider.getName());
        }
    }

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return validateCaptcha(context, formData)
                && super.validateForm(context, formData);
    }

    private boolean validateCaptcha(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        if (!BooleanUtils.toBoolean(getConfigVal(context, CONFIG_KEY_CAPTCHA_ENABLED))) {
            return true;
        }

        CaptchaProvider captchaProvider = captchaProviders.get(getConfigVal(context, CONFIG_KEY_CAPTCHA_PROVIDER));
        String captchaToken = formData.getFirst(captchaProvider.getCaptchaTokenField());
        if (isBlank(captchaToken)) {
            Response failure = challenge(context, CAPTCHA_FORM_NOT_PASSED_MSG_KEY, null);
            context.failureChallenge(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR, failure);
            return false;
        }


        CaptchaClient captchaClient = captchaProvider.getCaptchaClient();
        if (!captchaClient.validateCaptchaToken(context, captchaToken)) {
            Response failure = challenge(context, CAPTCHA_INVALID_TOKEN_MSG_KEY, null);
            context.failureChallenge(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR, failure);
            return false;
        }

        return true;
    }

}

