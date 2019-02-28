# Web Tests



## Configure Web app

* resources -&gt; properties.property
* ```text
  # Web
  webApp="http://45.76.245.149:1337/admin/"
  ```
* Example project: ⁨automation-client⁩ ▸ ⁨automation⁩ ▸ ⁨src⁩ ▸ ⁨main⁩ ▸ ⁨java⁩ ▸ ⁨modules⁩ ▸ ⁨webApp⁩
  * Setup locators

    webApp ▸ LoginPanel.java

{% code-tabs %}
{% code-tabs-item title="LoginPanel.java" %}
```java
        public static class elements {
            public static EnhancedBy USER_NAME_FIELD = Element.byCss("[placeholder='John Doe']", "username field");
            public static EnhancedBy PASSWORD_FIELD = Element.byCss("#password", "password field");
            public static EnhancedBy LOGIN_SUBMIT = Element.byCss("[type='submit']", "submit button");
            public static EnhancedBy LOGOUT_BUTTON = Element.byCss("[href*='logout']", "logout button");
            public static EnhancedBy MAIN_SITE = Element.byCss(".main-site", "main site button");
            public static EnhancedBy ERROR_MESSAGE = Element.byCss("[class*='InputErrors']", "input errors");

            public static EnhancedBy LOADING_INDICATOR = Element.byCss("[class*='Loading']", "loading indicator");

        }
```
{% endcode-tabs-item %}
{% endcode-tabs %}

## Define actions

*  webApp ▸ LoginPanel.java

  {% code-tabs %}
  {% code-tabs-item title="LoginPanel.java" %}
  ```java
        /**
         * enter login info and click login button
         * 
         * @param user
         */
        public void login(UserObject user) {
            setLoginFields(user);
            Helper.form.formSubmit(elements.LOGIN_SUBMIT, MainPanel.elements.ADMIN_LOGO, elements.LOADING_INDICATOR);

        }

        public void loginError(UserObject user) {
            setLoginFields(user);
            Helper.form.formSubmit(elements.LOGIN_SUBMIT, elements.ERROR_MESSAGE);
        }

        public void relogin(UserObject user) {
            manager.main.logout();
            login(user);
        }

        public void setLoginFields(UserObject user) {
            Helper.form.setField(elements.USER_NAME_FIELD, user.username().get());
            Helper.form.setField(elements.PASSWORD_FIELD, user.password().get());
        }
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}

## Define objects

* ⁨automation-client⁩ ▸ ⁨automation⁩ ▸ ⁨src⁩ ▸ ⁨main⁩ ▸ ⁨java⁩ ▸ ⁨common⁩ ▸ ⁨objects⁩

  {% code-tabs %}
  {% code-tabs-item title="userObject.java" %}
  ```java
  object
  */
  public abstract Optional name();
  public abstract Optional username();
  public abstract Optional password();
  public abstract Optional email();
  /**
  Predefined objects

  @return
  */
  public UserObject withAdminLogin() {
  return new UserObject.Builder().username(ADMIN_USER).password(ADMIN_PASSWORD).buildPartial();
  }
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}

## Write Test

{% code-tabs %}
{% code-tabs-item title="Verify\_Login\_Test.java" %}
```text
* setup test


    @BeforeMethod
 	public void beforeMethod() throws Exception {
		setupWebDriver(app.webApp.getWebDriver());
	}

    @Test
    public void validate_user_Login() {
        UserObject user = UserObject.user().withAdminLogin();
        TestLog.When("I login with admin user");
        app.strapi.login.login(user);

        TestLog.Then("I verify admin logo is displayed");
        Helper.verifyElementIsDisplayed(MainPanel.elements.ADMIN_LOGO);

        TestLog.When("I logout");
        app.strapi.main.logout();

        TestLog.Then("I should see the login panel");
        Helper.verifyElementIsDisplayed(LoginPanel.elements.LOGIN_SUBMIT);
    }
```
{% endcode-tabs-item %}
{% endcode-tabs %}

