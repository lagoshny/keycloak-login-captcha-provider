package ru.lagoshny.keycloak.providers.login.captcha.client.yandex;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.services.ServicesLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lagoshny.keycloak.providers.login.captcha.client.CaptchaClient;

import java.net.URI;
import java.util.StringJoiner;

import static ru.lagoshny.keycloak.providers.login.captcha.form.CaptchaLoginFormFactory.CONFIG_KEY_CAPTCHA_SERVER_KEY;
import static ru.lagoshny.keycloak.providers.login.captcha.util.ConfigUtils.getConfigVal;

/**
 * Yandex Smart Captcha client that uses {@link HttpClient} to perform request for captcha validation.
 * <p>
 * Instance of {@link HttpClient} obtains from current {@link AuthenticationFlowContext} session.
 * <p>
 * See more about yandex captcha validation in <a href="https://cloud.yandex.ru/docs/smartcaptcha/concepts/validation#validation-result">documentation</a>.
 */
public class YandexSmartCaptchaClient implements CaptchaClient {

    private final static Logger logger = LoggerFactory.getLogger(YandexSmartCaptchaClient.class);

    private final static String SUCCESS_RESPONSE_STATUS = "ok";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean validateCaptchaToken(AuthenticationFlowContext context, String captchaToken) {
        try {
            var uriBuilder = new URIBuilder(getConfigVal(context, getCaptchaValidateUrl()));
            uriBuilder.addParameter("secret", getConfigVal(context, CONFIG_KEY_CAPTCHA_SERVER_KEY));
            uriBuilder.addParameter("token", captchaToken);
            uriBuilder.addParameter("ip", context.getConnection().getRemoteAddr());
            URI uri = uriBuilder.build();
            logger.debug("Captcha validation URL: {}", uri);

            CloseableHttpClient httpClient = context.getSession()
                    .getProvider(HttpClientProvider.class)
                    .getHttpClient();
            try (CloseableHttpResponse response = httpClient.execute(new HttpGet(uri))) {
                try {
                    CaptchaResponse captchaResponse = objectMapper.readValue(EntityUtils.toString(response.getEntity()),
                            CaptchaResponse.class);
                    logger.debug("Captcha validation result: {}", captchaResponse);

                    return StringUtils.equalsIgnoreCase(SUCCESS_RESPONSE_STATUS, captchaResponse.getStatus());
                } finally {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
            }
        } catch (Exception e) {
            logger.error("Captcha validation exception {}", e.getMessage(), e);
            ServicesLogger.LOGGER.failedAuthentication(e);
        }

        return false;
    }

    @Override
    public String getCaptchaValidateUrl() {
        return "https://captcha-api.yandex.ru/validate";
    }

    /**
     * Yandex captcha <a href="https://yandex.cloud/en-ru/docs/smartcaptcha/operations/validate-captcha#service-response">response object</a> that contains captcha validation result.
     */
    static class CaptchaResponse {
        @JsonProperty("status")
        String status;
        @JsonProperty("self")
        String self;
        @JsonProperty("message")
        String message;

        public CaptchaResponse() {
        }

        public String getStatus() {
            return status;
        }

        public String getSelf() {
            return self;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", CaptchaResponse.class.getSimpleName() + "[", "]")
                    .add("status='" + status + "'")
                    .add("self='" + self + "'")
                    .add("message='" + message + "'")
                    .toString();
        }
    }

}
