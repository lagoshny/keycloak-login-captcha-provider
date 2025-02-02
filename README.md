# Keycloak login captcha provider

An example of the keycloak provider, how to add a captcha to an authorization page.

Supports the next captcha providers:

- [Google ReCaptchaV2](https://developers.google.com/recaptcha/docs/versions#recaptcha_v2_im_not_a_robot_checkbox)
- [Google ReCaptchaV3](https://developers.google.com/recaptcha/docs/v3)
- [Yandex Smart Captcha](https://cloud.yandex.ru/services/smartcaptcha)

Before using any of captcha provider, you should to get server and site keys to your Google or Yandex account.

> You will need these keys later to setting provider.

## How to use provider

To use this provider, you need an instance of keycloak server (this manual uses docker-compose to start keycloak server)
then build provider's sources to `jar` file and copy it with custom keycloak theme (that supports captcha) to the keycloak sever.

All these steps are described in more detail below.

### Run keycloak using docker-compose
The project has the simple `docker-compose` configuration to start keycloak with prepared configuration.

> Configuration adds new keycloak realm and realm client to test captcha provider

Run it in the project root directory command:

```bash
docker-compose up -d
```

After start docker container, you can open `http://localhost:8080` and login to the admin panel with credentials: `admin`/`admin`.

### Build provider

Build the provider using `Maven builder`:

```bash
mvn clean install
```

After build check out the `target` folder. Look there the `keycloak-login-captcha-provider-{VERSION}.jar` file.

> After any provider changes you need to rebuild it and copy to keycloak server with keycloak restart after that.

### Copy provider and theme to keycloak

Because keycloak server started at the docker container we can use `docker cp` command:

```bash
docker cp ./src/main/resources/themes/captcha-form mykeycloak:/opt/keycloak/themes
docker cp ./target/keycloak-login-captcha-provider-1.0.jar mykeycloak:/opt/keycloak/providers
```

Restart keycloak server to see the changes.

```bash
docker-compose restart
```

### Settings keycloak to using the provider

Started keycloak has *predefined* settings:
- created new realm `test-realm` and client `capthca-test`
- to show captcha form added realm Security defences settings:
```
frame-src 'self' https://www.google.com https://captcha-api.yandex.ru; frame-ancestors  'self' https://www.google.com https://captcha-api.yandex.ru; object-src 'none';
```
- added new Authentication flow: `Copy of browser` that uses the captcha provider and its setup as default flow for `Browser flow`

To check out the provider, *you need*:
1. Open `capthca-test` client settings and choose `captcha-form` theme.
2. Open `Authentication` -> `Copy of browser` flow and put your captcha provider site and server keys to `Captcha Login Form` settings.
3. (Optional) Create some User in `capthca-test` realm to test login page.

Now you can open [login page](http://localhost:8080/realms/test-realm/protocol/openid-connect/auth?client_id=capthca-test&response_type=code&redirect_uri=http://localhost:8080/realms/test-realm/.well-known/openid-configuration) and check captcha.

### Describe changes of login theme

To demonstrate how to work with captcha provider the project has custom keycloak login theme.

Find theme files here `./src/main/resources/themes/captcha-form`

It is a simple copy of base keycloak theme with captcha support:

1. Login page has new block:

```js
<#if captchaEnabled?? && captchaEnabled == "true" && captchaProvider != "ReCaptchaV3">
    ${captchaHtmlBlock?no_esc}
</#if>
```

That checks if provider enabled and add captcha html block to the page.

And `template.ftl` changes:

```js
<#if captchaEnabled?? && captchaEnabled == "true">
    <script src="${captchaScriptUrl}" type="text/javascript" defer async></script>
    <#if captchaProvider == "ReCaptchaV3">
        <script>
            function onSubmit(token) {
                document.getElementById('kc-form-login').submit();
            }
        </script>
    </#if>
</#if>
```

All these changes are simple instruction from captcha providers documentations, only generalized to supply 3 providers at once.

### Debug the provider

The keycloak docker container opens `8787` port that allows you to perform remote debug JVM process.

Use it when you want to debug any issues with captcha provider.
