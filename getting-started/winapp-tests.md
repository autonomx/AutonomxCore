# WinApp Tests

## Configure WebApp app

* resources -&gt; properties.property
* ```text
  #win
  winApp = "Microsoft.WindowsCalculator_8wekyb3d8bbwe!App"
  ```
* * Example project: ⁨automation-client⁩ ▸ ⁨automation⁩ ▸ ⁨src⁩ ▸ ⁨main⁩ ▸ ⁨java⁩ ▸ ⁨modules⁩ ▸ windowsApp⁩

  * Setup locators

    windowsApp ▸ CalculatorPanel.java

* {% code-tabs %}
  {% code-tabs-item title="calculatePanel.java" %}
  ```text
		
  	// Locators
  	//--------------------------------------------------------------------------------------------------------	
  	public static class elements {
  	    public static EnhancedBy ONE = Element.byName("One", "one button");
  	    public static EnhancedBy TWO = Element.byName("Two", "two button");
  	    public static EnhancedBy PLUS = Element.byName("Plus", "plus button");
  	    public static EnhancedBy EQUALS = Element.byName("Equals", "equal button");
  	    public static EnhancedBy RESULTS = Element.byAccessibility("CalculatorResults", "calculator results");
  	}
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}

## Define actions

*  windowsApp ▸ CalculatorPanel.java
* {% code-tabs %}
  {% code-tabs-item title="CalculatorPanel.java" %}
  ```text
  	// Actions
  	//--------------------------------------------------------------------------------------------------------	
  	/**
  	 * 
  	 */
  	public void calculate() {
  		Helper.click.clickAndExpect(elements.ONE, elements.TWO);
  		Helper.click.clickAndExpect(elements.PLUS, elements.TWO);
  		Helper.click.clickAndExpect(elements.TWO, elements.EQUALS);
  		Helper.click.clickAndExpect(elements.EQUALS, elements.RESULTS);
  		verifyResults("3");
  	}
	
      protected void verifyResults(String val)
      {
          // trim extra text and whitespace off of the display value
    	
         String result =  Helper.form.getTextValue(elements.RESULTS).replace("Display is", "").trim();
         Helper.assertEquals(val, result);
      }
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}

## Write Test

* ⁨automation-client⁩ ▸ ⁨automation⁩ ▸ ⁨src⁩ ▸ ⁨test ▸ ⁨java⁩ ▸ ⁨module ▸ ⁨win -&gt; tests
* setup the windows driver
* {% code-tabs %}
  {% code-tabs-item title="VerifyCalculatorTest.java" %}
  ```text
  	@BeforeMethod
  	public void beforeMethod() throws Exception {
  		setupWebDriver(app.windowsApp.getWinAppDriver());
  	}
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}

* Add Test
* {% code-tabs %}
  {% code-tabs-item title="VerifyCalculatorTest.java" %}
  ```text
  public class VerifyCalculatorTest extends TestBase {

  	@BeforeMethod
  	public void beforeMethod() throws Exception {
  		setupWebDriver(app.windowsApp.getWinAppDriver());
  	}
	
	
	
  	@Test(enabled=true) 
  	public void validateCalculator() {

  		TestLog.When("I calculate 2 numbers");
  		app.windowsApp.calculate.calculate();
  	}
  }
  ```
  {% endcode-tabs-item %}
  {% endcode-tabs %}



