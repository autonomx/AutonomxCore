# iOS Tests



## Configure iOS app

* resources -&gt; properties.property
* ```text
  #iOS
  iosApp = "eurika.app"
  ios_app_dir = "resources/"
  iosMobile = "iPhone 7"
  iosTablet = "iPad Air 2"
  iosDeviceVersion = "12.1"
  ```
* Example project: ⁨automation-client⁩ ▸ ⁨automation⁩ ▸ ⁨src⁩ ▸ ⁨main⁩ ▸ ⁨java⁩ ▸ ⁨modules⁩ ▸ iosApp⁩

  * Setup locators

    iosApp ▸ MainPanel.java

* {% code-tabs %}
  {% code-tabs-item title="Main.java" %}
  ```text
		
  	// Locators
  	//--------------------------------------------------------------------------------------------------------	
  	public static class elements {
  		public static EnhancedBy PLAIN_TABLE_VIEW_STYLE = Element.byAccessibility("Plain Table View Style", "plain table view style");
  		public static EnhancedBy LIST_SECTIONS = Element.byAccessibility("List Sections", "list sections");
  		public static EnhancedBy EUREKA = Element.byAccessibility("Eureka", "eureka logo");

  	}
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}

## Define actions

*  iosApp ▸ MainPanel.java
* {% code-tabs %}
  {% code-tabs-item title="Main.java" %}
  ```text
  	// Actions
  	//--------------------------------------------------------------------------------------------------------	
  	public void selectPanel(options panel) {
  		switch (panel) {
  		case PLAIN_TABLE_VIEW_STYLE:
  			Helper.mobile.mobile_scrollToElement(elements.PLAIN_TABLE_VIEW_STYLE);
  			Helper.click.clickAndExpect(elements.PLAIN_TABLE_VIEW_STYLE, PlainTableViewPanel.elements.NAME);
  			break;
  		case LIST_SECTIONS:
  			Helper.click.clickAndExpect(elements.LIST_SECTIONS, ListSections.elements.BACK);
  			break;
  		default:
  			throw new IllegalStateException("Unsupported panels " + panel);
  		}
  	}
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}

## Define objects

* Objects contain test data used for the tests
* In this example, they contain values for the form
* ⁨automation-client⁩ ▸ ⁨automation⁩ ▸ ⁨src⁩ ▸ ⁨Test ▸ ⁨java⁩ ▸ ⁨module ▸ ⁨ios -&gt; tests
* {% code-tabs %}
  {% code-tabs-item title="PlainTableViewObject.java" %}
  ```text
  public static final String NAME = "auto";
  	public static final String USER_NAME = "auto user";
  	public static final String EMAIL_ADDRESS = "test123@email.com";
  	public static final String PASSWORD = "password123";
	
  	public abstract Optional<String> name();
  	public abstract Optional<String> username();
  	public abstract Optional<String> emailAddress();
  	public abstract Optional<String> password();
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}
* ⁨define default values
* {% code-tabs %}
  {% code-tabs-item title="PlainTableViewObject.java" %}
  ```text
  	public PlainTableViewObject withDefaultValues() {
  		return new PlainTableViewObject.Builder()
  				.name(NAME).username(USER_NAME).emailAddress(EMAIL_ADDRESS).password(PASSWORD)		
  				.buildPartial();
  	}
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}



## Write Test

* ⁨automation-client⁩ ▸ ⁨automation⁩ ▸ ⁨src⁩ ▸ ⁨Test ▸ ⁨java⁩ ▸ ⁨module ▸ ⁨ios -&gt; tests
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



