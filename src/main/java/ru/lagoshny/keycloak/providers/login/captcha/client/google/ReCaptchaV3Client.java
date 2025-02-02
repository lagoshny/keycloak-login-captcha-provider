package ru.lagoshny.keycloak.providers.login.captcha.client.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.http.client.HttpClient;
import org.keycloak.authentication.AuthenticationFlowContext;

import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

/**
 * Google ReCaptchaV3 client that uses {@link HttpClient} to perform request for captcha validation.
 * <p>
 * Instance of {@link HttpClient} obtains from current {@link AuthenticationFlowContext} session.
 * <p>
 * IMPORTANT!
 * ReCaptchaV3 does not show to user any verification form, you should analyze response.score value to perform suited verification actions.
 * See more about <a href="https://developers.google.com/recaptcha/docs/v3#interpreting_the_score">here</a>.
 * <p>
 * See more about ReCaptchaV3 validation in <a href="https://developers.google.com/recaptcha/docs/v3#site_verify_response">documentation</a>.
 */
public class ReCaptchaV3Client implements GoogleCaptchaClient<ReCaptchaV3Client.CaptchaResponse> {

    @Override
    public boolean validateCaptchaToken(AuthenticationFlowContext context, String captchaToken) {
        ReCaptchaV3Client.CaptchaResponse result = verifyGoogleCaptchaRequest(context, captchaToken, ReCaptchaV3Client.CaptchaResponse.class);
        logger.debug("Validate captcha token result: {}", result);
        return result != null && BooleanUtils.isTrue(result.getSuccess());
    }

    /**
     * ReCaptchaV3 <a href="https://developers.google.com/recaptcha/docs/v3#site_verify_response">response object</a> that contains captcha validation result.
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
        @JsonProperty("score")
        String score;
        @JsonProperty("action")
        String action;

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

        public String getScore() {
            return score;
        }

        public String getAction() {
            return action;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", CaptchaResponse.class.getSimpleName() + "[", "]")
                    .add("success=" + success)
                    .add("challengeTs=" + challengeTs)
                    .add("hostname='" + hostname + "'")
                    .add("errorCodes=" + errorCodes)
                    .add("score='" + score + "'")
                    .add("action='" + action + "'")
                    .toString();
        }
    }

}
