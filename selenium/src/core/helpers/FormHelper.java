package core.helpers;

import java.io.File;
import java.util.List;

import org.openqa.selenium.Keys;

import core.logger.TestLog;
import core.webElement.EnhancedBy;
import core.webElement.EnhancedWebElement;

public class FormHelper {

	/**
	 * sets field text value by index
	 * hides keyboard if on ios device
	 * @param value
	 * @param field
	 * @param index
	 */
	public static void setField(String value, EnhancedBy field, int index) {

		if (value != null && !value.isEmpty()) {
			EnhancedWebElement fieldElement = Element.findElements(field);
		//	WaitHelper.waitForElementToLoad(field);
			
			// clear field is slow on android and ios
			fieldElement.sendKeys(index, value);

			// hides keyboard if on mobile device (ios/android)
			MobileHelper.hideKeyboard();

			TestLog.logPass("I set field '" + field.name + "' with value '" + value + "'");
		}
	}

	/**
	 * set field value if value is not empty
	 * 
	 * @param value
	 * @param field
	 */
	public static void setField(String value, EnhancedBy field) {
		setField(value, field, 0);
	}
	
	/**
	 * sets field
	 * clears field before setting the value
	 * @param value
	 * @param field
	 */
	public static void clearAndSetField(String value, EnhancedBy field) {
		clearAndSetField(value, field, 0);
	}
	
	/**
	 * sets field
	 * clears field before setting the value
	 * @param value
	 * @param field
	 * @param index
	 */
	public static void clearAndSetField(String value, EnhancedBy field, int index) {

		if (value != null && !value.isEmpty()) {
			EnhancedWebElement fieldElement = Element.findElements(field);
			WaitHelper.waitForElementToLoad(field);
			
			// clear field is slow on android and ios
			fieldElement.clear(index);
			fieldElement.sendKeys(index, value);

			// hides keyboard if on mobile device (ios/android)
			MobileHelper.hideKeyboard();

			TestLog.logPass("I set field '" + field.name + "' with value '" + value + "'");
		}
	}

	/**
	 * sets key based on nested text field
	 * @param value
	 * @param parent
	 * @param parentIndex
	 * @param child
	 * @param childIndex
	 */
	public static void setKeyChildField(String value, EnhancedBy parent, int parentIndex,  EnhancedBy child, int childIndex) {

		if (value != null && !value.isEmpty()) {
			EnhancedWebElement parentElement = Element.findElements(parent);
			EnhancedWebElement childElement = Element.findElements(child, parentElement.get(parentIndex));
			// clear field is slow on android and ios
			childElement.clear(childIndex);
			childElement.sendKeys(childIndex, value);

			// hides keyboard if on mobile device (ios/android)
			MobileHelper.hideKeyboard();

			TestLog.logPass("I set field '" + child.name + "' with value '" + value + "'");
		}
	}
	
	/**
	 * sets field and presses the enter key
	 * @param value
	 * @param field
	 */
	public static void setFieldAndEnter(String value, EnhancedBy field) {
		setField(value, field);
		
		pressEnterOnWeb(field);
		Helper.pressEnterOnAndroid();
	}
	
	public static void pressEnterOnWeb(EnhancedBy field) {
		if(MobileHelper.isWeb()) {
			EnhancedWebElement targetElement = Element.findElements(field);	
			targetElement.sendKeys(Keys.ENTER);
		}
	}

	/**
	 * select submit button and wait for expected element to load
	 * 
	 * @param button
	 * @param expected
	 * 
	 */
	public static void formSubmit(EnhancedBy button, EnhancedBy expected) {
		ClickHelper.clickAndExpect(button, expected);
		//TestLog.logPass("Then I select button '" + button.name + "'");

	}

	/**
	 * clicks submit button, wait for element to appear and loading spinner to be
	 * removed
	 * 
	 * @param button
	 * @param expected
	 * @param spinner
	 */
	public static void formSubmit(EnhancedBy button, EnhancedBy expected, EnhancedBy spinner) {
		ClickHelper.clickAndExpect(button, expected);
		WaitHelper.waitForElementToBeRemoved(spinner);
	}

	/**
	 * selects drop down
	 * 
	 * @param option
	 *            : list option we want to select
	 * @param field
	 *            : the drop down field
	 * @param list
	 *            : the list items in the drop down list
	 */
	public static void selectDropDown(String option, EnhancedBy field, EnhancedBy list) {
		if (option != null && !option.isEmpty()) {
			ClickHelper.clickAndExpect(field, list);
			ListHelper.selectListItemEqualsByName(list, option);
			TestLog.logPass("I select drop down option '" + option + "'");
		}
	}

	/**
	 * selects drop down from list defined by another list defined by text
	 * 
	 * @param option
	 * @param field
	 * @param field_Identifier
	 * @param list
	 */
	public static void selectDropDown(String option, EnhancedBy field, String field_Identifier, EnhancedBy list) {
		if (option != null && !option.isEmpty()) {
			ListHelper.selectListItemContainsByName(field, field_Identifier);
			ListHelper.selectListItemEqualsByName(list, option);
			TestLog.logPass("I select drop down option '" + option + "'");
		}
	}
	
   /**
    * select drop down by index from the drop down list
    * @param index
    * @param field
    * @param list
    */
	public static void selectDropDown(int index, EnhancedBy field, EnhancedBy list) {
		if (index != -1) {
			ClickHelper.clickAndExpect(field, list);
			ListHelper.selectListItemByIndex(list, index);
			TestLog.logPass("I select drop down option at index '" + index + "'");
		}
	}
	
	/**
	 * selects drop down based on index of the drop down field
	 * @param option
	 * @param field
	 * @param index
	 * @param list
	 */
	public static void selectDropDown(String option, EnhancedBy field, int index, EnhancedBy list) {
		if (option != null && !option.isEmpty()) {
			ClickHelper.clickAndExpect(field, index, list);
			ListHelper.selectListItemEqualsByName(list, option);
			TestLog.logPass("I select drop down option at index '" + index + "'");
		}
	}
	
	
	/**
	 * select drop down based on index of the drop down list
	 * eg. used for date selection where each date value: day, month, year, is separate list
	 * send key is used to select the value from the list
	 * @param option
	 * @param field
	 * @param list
	 * @param listIndex
	 */
	public static void selectDropDown(String option, EnhancedBy field, EnhancedBy list, int listIndex) {
			if (option != null && !option.isEmpty()) {
				ClickHelper.clickAndExpect(field, list);
				EnhancedWebElement fieldElement = Element.findElements(list);

			    fieldElement.sendKeys(listIndex, option);
				TestLog.logPass("I select drop down option '" + option + "'");
			}
		}

	/**
	 * selects radio button by radio button description
	 * 
	 * @param option
	 * @param buttons
	 */
	public static void selectRadioButton(String option, EnhancedBy buttons) {
		if (option != null && !option.isEmpty()) {
			ListHelper.selectListItemEqualsByName(buttons, option);
		}
	}
	
	/**
	 * selects a button
	 * @param button
	 */
	public static void selectRadioButton(EnhancedBy button) {
		ClickHelper.clickAndExpect(button, button);
	}

	/**
	 * selects multiple checkbox options
	 * 
	 * @param selections
	 * @param checkboxes
	 */
	public static void selectMultipleCheckboxOptions(List<String> selections, EnhancedBy checkboxes) {
		for (String selection : selections) {
			TestLog.logPass("I select '" + selection + "'");
			ListHelper.selectListItemEqualsByName(checkboxes, selection);
		}
	}

	/**
	 * uploads file by specifying file location relative to main path
	 * 
	 * @param location
	 * @param imageButton
	 */
	public static void uploadFile(String location, EnhancedBy imageButton) {
		File file = new File("");
		String path = file.getAbsolutePath() + location;
		setField(path, imageButton);
		TestLog.logPass("I upload file at location '" + location + "'");
	}
    
	/**
	 *  * sets the image based on list of image path
	 * @param locations
	 * @param imageButton
	 * @param images
	 */
	public static void uploadImages(List<String> locations, EnhancedBy imageButton, EnhancedBy images) {
		for (String location : locations) {
			uploadImage(location, imageButton, images);
		}
	}

	/**
	 * sets the image based on image path
	 * 
	 * @param location
	 * @param imageButton
	 * @param images
	 *            : uploaded image
	 */
	public static void uploadImage(String location, EnhancedBy imageButton, EnhancedBy images) {
		int imageCount = ListHelper.getListCount(images);
		File file = new File("");
		String path = file.getAbsolutePath() + location;
		setField(path, imageButton);
		WaitHelper.waitForAdditionalElementsToLoad(images, imageCount);
		TestLog.logPass("uploaded file: " + location);
	}
	
	/**
	 * gets the text value from an element
	 * @param element
	 * @return
	 */
	public static String getTextValue(EnhancedBy element) {
		EnhancedWebElement targetElement = Element.findElements(element);
		return targetElement.getText();
	}
}