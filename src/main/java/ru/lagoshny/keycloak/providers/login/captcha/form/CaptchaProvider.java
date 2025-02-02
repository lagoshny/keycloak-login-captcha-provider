package ru.lagoshny.keycloak.providers.login.captcha.form;

import ru.lagoshny.keycloak.providers.login.captcha.client.CaptchaClient;
import ru.lagoshny.keycloak.providers.login.captcha.client.google.ReCaptchaV2Client;
import ru.lagoshny.keycloak.providers.login.captcha.client.google.ReCaptchaV3Client;
import ru.lagoshny.keycloak.providers.login.captcha.client.yandex.YandexSmartCaptchaClient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.lagoshny.keycloak.providers.login.captcha.form.CaptchaLoginFormFactory.CONFIG_KEY_CLIENT_KEY;

/**
 * List of supported captcha providers.
 */
public enum CaptchaProvider {

    RE_CAPTCHA_V2(
            "ReCaptchaV2",
            new ReCaptchaV2Client(),
            "g-recaptcha-response",
            "https://www.google.com/recaptcha/api.js",
            "<div class=\"g-recaptcha\" data-sitekey=${" + CONFIG_KEY_CLIENT_KEY + "}></div>"
    ),

    RE_CAPTCHA_V3(
            "ReCaptchaV3",
            new ReCaptchaV3Client(),
            "g-recaptcha-response",
            "https://www.google.com/recaptcha/api.js",
            "${" + CONFIG_KEY_CLIENT_KEY + "}"
    ),

    YANDEX_SMART_CAPTCHA(
            "YandexSmartCaptcha",
            new YandexSmartCaptchaClient(),
            "smart-token",
            "https://captcha-api.yandex.ru/captcha.js",
            "<div id=\"captcha-container\" class=\"captcha-style smart-captcha\" data-sitekey=\"${" + CONFIG_KEY_CLIENT_KEY + "}\"></div>"
    );

    /**
     * Captcha provider name.
     */
    private final String name;

    /**
     * Instance of CaptchaClient class to perform captcha validation requests.
     */
    private final CaptchaClient captchaClient;

    /**
     * Captcha token field name, you can get it from captcha provider documentation.
     */
    private final String captchaTokenField;

    /**
     * Script url that captcha uses to put its code to your page. You can get it from captcha provider documentation.
     */
    private final String captchaScriptUrl;

    /**
     * How and where to show captcha block. You can get it from captcha provider documentation.
     */
    private final String captchaHtmlBlock;

    CaptchaProvider(String name, CaptchaClient captchaClient, String captchaTokenField, String captchaScriptUrl, String captchaHtmlBlock) {
        this.name = name;
        this.captchaClient = captchaClient;
        this.captchaTokenField = captchaTokenField;
        this.captchaScriptUrl = captchaScriptUrl;
        this.captchaHtmlBlock = captchaHtmlBlock;
    }

    public CaptchaClient getCaptchaClient() {
        return captchaClient;
    }

    public String getCaptchaTokenField() {
        return captchaTokenField;
    }

    public String getCaptchaScriptUrl() {
        return captchaScriptUrl;
    }

    public String getName() {
        return name;
    }

    public String getCaptchaHtmlBlock(String captchaSiteKey) {
        return captchaHtmlBlock.replace("${" + CONFIG_KEY_CLIENT_KEY + "}", captchaSiteKey);
    }

    public static List<String> getCaptchaProviderNames() {
        return Arrays.stream(values()).map(CaptchaProvider::getName).collect(Collectors.toList());
    }
}
