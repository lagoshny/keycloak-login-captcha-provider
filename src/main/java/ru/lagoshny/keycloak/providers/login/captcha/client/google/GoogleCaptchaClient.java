package ru.lagoshny.keycloak.providers.login.captcha.client.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.services.ServicesLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lagoshny.keycloak.providers.login.captcha.client.CaptchaClient;

import static ru.lagoshny.keycloak.providers.login.captcha.form.CaptchaLoginFormFactory.CONFIG_KEY_CAPTCHA_SERVER_KEY;
import static ru.lagoshny.keycloak.providers.login.captcha.util.ConfigUtils.getConfigVal;

public interface GoogleCaptchaClient<T> extends CaptchaClient {

    Logger logger = LoggerFactory.getLogger(GoogleCaptchaClient.class);

    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Common code to perform captcha validation request.
     *
     * @return response object that depends on passed <T> type
     */
    default T verifyGoogleCaptchaRequest(AuthenticationFlowContext context,
                                         String captchaToken,
                                         Class<T> responseClass) {
        try {
            HttpUriRequest request = RequestBuilder.post(getCaptchaValidateUrl())
                    .addParameter("secret", getConfigVal(context, CONFIG_KEY_CAPTCHA_SERVER_KEY))
                    .addParameter("response", captchaToken)
                    .addParameter("remoteip", context.getConnection().getRemoteAddr())
                    .build();

            logger.debug("Captcha validation request: {}", request);

            CloseableHttpClient httpClient = context.getSession()
                    .getProvider(HttpClientProvider.class)
                    .getHttpClient();
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                try {
                    T captchaResponse = objectMapper.readValue(EntityUtils.toString(response.getEntity()),
                            responseClass);
                    logger.debug("Captcha validation result: {}", captchaResponse);

                    return captchaResponse;
                } finally {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
            }
        } catch (Exception e) {
            logger.error("Captcha validation exception {}", e.getMessage(), e);
            ServicesLogger.LOGGER.failedAuthentication(e);
            return null;
        }
    }

    @Override
    default String getCaptchaValidateUrl() {
        return "https://www.google.com/recaptcha/api/siteverify";
    }
}
