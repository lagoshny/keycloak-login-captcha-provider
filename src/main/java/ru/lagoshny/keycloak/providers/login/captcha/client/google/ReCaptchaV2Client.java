package ru.lagoshny.keycloak.providers.login.captcha.client.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.http.client.HttpClient;
import org.keycloak.authentication.AuthenticationFlowContext;

import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

/**
 * Google ReCaptchaV2 client that uses {@link HttpClient} to perform request for captcha validation.
 * <p>
 * Instance of {@link HttpClient} obtains from current {@link AuthenticationFlowContext} session.
 * <p>
 * See more about ReCaptchaV2 validation in <a href="https://developers.google.com/recaptcha/docs/verify">documentation</a>.
 */
public class ReCaptchaV2Client implements GoogleCaptchaClient<ReCaptchaV2Client.CaptchaResponse> {

    @Override
    public boolean validateCaptchaToken(AuthenticationFlowContext context, String captchaToken) {
        CaptchaResponse result = verifyGoogleCaptchaRequest(context, captchaToken, CaptchaResponse.class);
        return result != null && BooleanUtils.isTrue(result.getSuccess());
    }

    /**
     * ReCaptchaV2 <a href="https://developers.google.com/recaptcha/docs/verify#api-response">response object</a> that contains captcha validation result.
     */
    static class CaptchaResponse {
        @JsonProperty("success")
        Boolean success;
        @JsonProperty("challenge_ts")
        Date challengeTs;
        @JsonProperty("hostname")
        String hostname;
        @JsonProperty("error-codes")
        List<String> errorCodes;

        public CaptchaResponse() {
        }

        public Boolean getSuccess() {
            return success;
        }

        public Date getChallengeTs() {
            return challengeTs;
        }

        public String getHostname() {
            return hostname;
        }

        public List<String> getErrorCodes() {
            return errorCodes;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", CaptchaResponse.class.getSimpleName() + "[", "]")
                    .add("success=" + success)
                    .add("challengeTs=" + challengeTs)
                    .add("hostname='" + hostname + "'")
                    .add("errorCodes=" + errorCodes)
                    .toString();
        }
    }

}
