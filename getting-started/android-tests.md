---
description: setting up an Android Test
---

# Android Tests



## Configure Android app

* resources -&gt; properties.property
* ```text
  # Android
  # Espresso or UiAutomator2
  androidTechnology=Espresso
  androidApp = "selendroid.apk"
  android_app_dir = "resources/"
  androidMobile = "Pixel_2_XL_API_25"
  androidTablet = "Pixel_2_XL_API_25"
  ```
* * Example project: ⁨automation-client⁩ ▸ ⁨automation⁩ ▸ ⁨src⁩ ▸ ⁨main⁩ ▸ ⁨java⁩ ▸ ⁨modules⁩ ▸ androidApp⁩

  * Setup locators

    androidApp ▸ MainPanel.java

* {% code-tabs %}
  {% code-tabs-item title="Main.java" %}
  ```text
		
  	// Locators
  	//--------------------------------------------------------------------------------------------------------	
  	public static class elements {
  		public static EnhancedBy REGISTER_PANEL = Element.byAccessibility("startUserRegistrationCD", "registration button");
  		public static EnhancedBy POPUP_BUTTON = Element.byAccessibility("showPopupWindowButtonCD", "popup button");

  	}
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}

## Define actions

*  androidApp ▸ MainPanel.java
* {% code-tabs %}
  {% code-tabs-item title="Main.java" %}
  ```text
  	// Actions
  	//--------------------------------------------------------------------------------------------------------	
  	public void selectRegisterPanel() {
  		Helper.clickAndExpect(elements.REGISTER_PANEL, RegistrationPanel.elements.USERNAME_FIELD);
  	}
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}

## Write Test

* ⁨automation-client⁩ ▸ ⁨automation⁩ ▸ ⁨src⁩ ▸ ⁨Test ▸ ⁨java⁩ ▸ ⁨module ▸ ⁨android -&gt; tests
* setup the android driver
* {% code-tabs %}
  {% code-tabs-item title="RegisterUserTest.java" %}
  ```text
  	public void beforeMethod() throws Exception {
  		setupWebDriver(app.androidApp.getAndroidMobileDriver());
  	}
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}

* Add Test
* {% code-tabs %}
  {% code-tabs-item title="RegisterUserTest.java" %}
  ```text
  public class RegisterUserTest extends TestBase {


  	@BeforeMethod
  	public void beforeMethod() throws Exception {
  		setupWebDriver(app.androidApp.getAndroidMobileDriver());
  	}

  	@Test
  	public void registerUser() {
  		UserObject user = UserObject.user().withDefaultUser();

  		TestLog.When("I select the registration panel");
  		app.androidApp.main.selectRegisterPanel();
		
  		TestLog.When("I register a user");
  		app.androidApp.registration.registerUser(user);
  	}
  }
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}



