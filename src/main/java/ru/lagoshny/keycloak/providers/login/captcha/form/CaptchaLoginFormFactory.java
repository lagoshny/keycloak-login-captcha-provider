package ru.lagoshny.keycloak.providers.login.captcha.form;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

/**
 * Factory to create custom {@link CaptchaLoginForm} provider.
 * <p>
 * More details about the factory see in the <a href="https://www.keycloak.org/docs/latest/server_development/#implementing-an-authenticatorfactory">documentation</a>.
 */
public class CaptchaLoginFormFactory implements AuthenticatorFactory {

    public static final String CONFIG_KEY_CAPTCHA_ENABLED = "captchaEnabled";

    public static final String CONFIG_KEY_CAPTCHA_PROVIDER = "captchaProvider";

    public static final String CONFIG_KEY_CAPTCHA_SCRIPT_URL = "captchaScriptUrl";

    public static final String CONFIG_KEY_CAPTCHA_SERVER_KEY = "captchaServerKey";

    public static final String CONFIG_KEY_CLIENT_KEY = "captchaSiteKey";

    public static final String CAPTCHA_HTML_BLOCK = "captchaHtmlBlock";

    public static final String PROVIDER_ID = "captcha-login-form";

    public static final CaptchaLoginForm SINGLETON = new CaptchaLoginForm();

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    protected final List<ProviderConfigProperty> configMetadata;

    public CaptchaLoginFormFactory() {
        this.configMetadata = ProviderConfigurationBuilder.create()
                .property()
                .name(CONFIG_KEY_CAPTCHA_ENABLED)
                .label("Enable captcha")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue(Boolean.FALSE)
                .helpText("When value is true then a captcha will be shown on the auth form")
                .add()

                .property()
                .name(CONFIG_KEY_CAPTCHA_PROVIDER)
                .label("Captcha provider")
                .helpText("Choose one of captcha providers")
                .type(ProviderConfigProperty.LIST_TYPE)
                .options(CaptchaProvider.getCaptchaProviderNames())
                .add()

                .property()
                .name(CONFIG_KEY_CLIENT_KEY)
                .label("Site key")
                .type(ProviderConfigProperty.PASSWORD)
                .helpText("Site key received from captcha provider. For YandexSmartCaptcha it is client secret.")
                .secret(true)
                .add()

                .property()
                .name(CONFIG_KEY_CAPTCHA_SERVER_KEY)
                .label("Server key")
                .type(ProviderConfigProperty.PASSWORD)
                .helpText("Server key received from captcha provider")
                .secret(true)
                .add()
                .build();
    }

    @Override
    public String getDisplayType() {
        return "Captcha Login Form";
    }

    @Override
    public String getReferenceCategory() {
        return PasswordCredentialModel.TYPE;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public String getHelpText() {
        return "Custom Keycloak auth form with captcha";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
