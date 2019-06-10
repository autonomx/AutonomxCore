package core.helpers;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.Keys;

import core.support.logger.TestLog;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

public class FormHelper {

	/**
	 * clears and set field
	 * @param field
	 * @param value
	 */
	public void clearAndSetField(EnhancedBy field, CharSequence... value) {
		clearField(field, 0);
		setField(field, 0, value);
	}
	
	/**
	 * clear and sets field
	 * @param field
	 * @param index
	 * @param value
	 */
	public void clearAndSetField(EnhancedBy field, int index, CharSequence... value) {
		clearField(field, index);
		setField(field, index, value);
	}
	/**
	 * sets field clears field before setting the value
	 * 
	 * @param field
	 * @param value
	 */
	public void setField(EnhancedBy field, CharSequence... value) {
		setField(field, 0, value);
	}

	/**
	 * sets field clears field before setting the value
	 * 
	 * @param field
	 * @param index
	 * @param value
	 */
	public void setField(EnhancedBy field, int index, CharSequence... value) {
		TestLog.logPass("I set field '" + field.name + "' with value '" + Arrays.toString(value) + "'");

		if (value != null && value.length != 0) {
			EnhancedWebElement fieldElement = Element.findElements(field);
			Helper.wait.waitForElementToLoad(field);
			
			// clear field is slow on android And ios
			clearField(field, index);
			Helper.wait.waitForSeconds(0.5);
			fieldElement.sendKeys(index, value);

			// hides keyboard if on mobile device (ios/android)
			Helper.mobile.hideKeyboard();
		}
	}
	
	/**
	 * use multiple strategies to clear the filed
	 * 1. element.clear()
	 * 2. send escape key
	 * 3. press backspace to delete the value
	 * @param field
	 * @param index
	 */
	private void clearField(EnhancedBy field, int index) {
		EnhancedWebElement fieldElement = Element.findElements(field);
	
		String value = fieldElement.getText(index);
		if(value.isEmpty()) return;
		
		Helper.clickAndWait(field, 0);
		fieldElement.clear(index);
		fieldElement.get(index).sendKeys(Keys.ESCAPE);
		
		 value = fieldElement.getText(index);
		if(!value.isEmpty())
		{
			for(int i = 0; i< value.length(); i++)
				fieldElement.sendKeys(Keys.BACK_SPACE);
		}	
	}
	
	/**
	 * sets field text value by index hides keyboard if on ios device
	 * 
	 * @param value
	 * @param field
	 * @param index
	 */
	public void setField(String value, EnhancedBy field, int index) {
		setField(field, index, value);
	}

	/**
	 * sets key based on nested text field
	 * 
	 * @param parent
	 * @param parentIndex
	 * @param child
	 * @param childIndex
	 * @param value
	 */
	public void setKeyChildField(EnhancedBy parent, int parentIndex, EnhancedBy child, int childIndex,
			CharSequence... value) {
		TestLog.logPass("I set field '" + child.name + "' with value '" + Arrays.toString(value) + "'");

		if (value != null && value.length != 0) {
			EnhancedWebElement parentElement = Element.findElements(parent);
			EnhancedWebElement childElement = Element.findElements(child, parentElement.get(parentIndex));
			// clear field is slow on android And ios
			childElement.clear(childIndex);
			childElement.sendKeys(childIndex, value);

			// hides keyboard if on mobile device (ios/android)
			Helper.mobile.hideKeyboard();
		}
	}

	/**
	 * sets field And presses the enter key
	 * 
	 * @param field
	 * @param value
	 */
	public void setFieldAndEnter(EnhancedBy field, CharSequence... value) {
		setField(field, value);

		pressEnterOnWeb(field);
		Helper.mobile.pressEnterOnAndroid();
	}

	/**
	 * send
	 * 
	 * @param key
	 * @param field
	 */
	public void setKey(Keys key, EnhancedBy field) {
		EnhancedWebElement fieldElement = Element.findElements(field);
		fieldElement.sendKeys(key);
	}

	public void pressEnterOnWeb(EnhancedBy field) {
		if (Helper.mobile.isWebDriver()) {
			EnhancedWebElement targetElement = Element.findElements(field);
			targetElement.sendKeys(Keys.ENTER);
		}
	}

	/**
	 * select submit button And wait for expected element to load
	 * 
	 * @param button
	 * @param expected
	 * 
	 */
	public void formSubmit(EnhancedBy button, EnhancedBy expected) {
		Helper.click.clickAndExpect(button, expected, false);
		// TestLog.logPass("Then I select button '" + button.name + "'");

	}

	/**
	 * clicks submit button, wait for element to appear And loading spinner to be
	 * removed
	 * 
	 * @param button
	 * @param expected
	 * @param spinner
	 */
	public void formSubmit(EnhancedBy button, EnhancedBy expected, EnhancedBy spinner) {
		Helper.click.clickAndExpect(button, expected, spinner);
	}

	/**
	 * selects dropdown by double clicking on the field
	 * 
	 * @param option
	 * @param field
	 * @param list
	 */
	public void selectDropDownWithDoubleClick(String option, EnhancedBy field, EnhancedBy list) {
		selectDropDownWithDoubleClick(option, field, 0, list);
	}

	/**
	 * selects dropdown by double clicking on the field
	 * 
	 * @param option
	 * @param field
	 * @param list
	 */
	public void selectDropDownWithDoubleClick(String option, EnhancedBy field, int index, EnhancedBy list) {
		TestLog.logPass("I select drop down option '" + option + "'");

		if (option != null && !option.isEmpty()) {
			Helper.click.clickAndWait(field, index, 0.1);
			Helper.click.clickAndExpect(field, index, list, true);
			Helper.list.selectListItemEqualsByName(list, option);
		}
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
	public void selectDropDown(String option, EnhancedBy field, EnhancedBy list) {
		TestLog.logPass("I select drop down option '" + option + "'");

		if (option != null && !option.isEmpty()) {
			Helper.click.clickAndExpect(field, list);
			Helper.list.selectListItemEqualsByName(list, option);
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
	public void selectDropDown(String option, EnhancedBy field, String field_Identifier, EnhancedBy list) {
		if (option != null && !option.isEmpty()) {
			Helper.list.selectListItemContainsByName(field, field_Identifier);
			Helper.list.selectListItemEqualsByName(list, option);

		}
	}

	/**
	 * select drop down by index from the drop down list
	 * 
	 * @param index
	 * @param field
	 * @param list
	 */
	public void selectDropDown(int index, EnhancedBy field, EnhancedBy list) {
		if (index != -1) {
			Helper.click.clickAndExpect(field, list);
			Helper.list.selectListItemByIndex(list, index);
		}
	}
	
	/**
	 * select drop down field based on index
	 * select option field based on index
	 * @param field
	 * @param index
	 * @param list
	 * @param optionIndex
	 */
	public void selectDropDown(EnhancedBy field,int index, EnhancedBy list, int optionIndex) {
		if (index != -1) {
			Helper.click.clickAndExpect(field, index, list);
			Helper.list.selectListItemByIndex(list, index);
		}
	}
	
	/**
	 * select drop down field based on index
	 * select option field based on text
	 * @param field
	 * @param index
	 * @param list
	 * @param text
	 */
	public void selectDropDown(EnhancedBy field,int index, EnhancedBy list, String text) {
		if (index != -1) {
			Helper.click.clickAndExpect(field, index, list);
			Helper.list.selectListItemEqualsByName(list, text);
		}
	}

	/**
	 * select drop down by index from the drop down list
	 * 
	 * @param index
	 * @param field
	 * @param list
	 */
	public void selectDropDown(EnhancedBy field, EnhancedBy list) {
		selectDropDown(0, field, list);
	}

	/**
	 * selects drop down based on index of the drop down field
	 * 
	 * @param option
	 * @param field
	 * @param index
	 * @param list
	 */
	public void selectDropDown(String option, EnhancedBy field, int index, EnhancedBy list) {

		if (option != null && !option.isEmpty()) {
			Helper.click.clickAndExpect(field, index, list, true);
			Helper.list.selectListItemEqualsByName(list, option);
		}
	}

	/**
	 * select drop down based on index of the drop down list eg. used for date
	 * selection where each date value: day, month, year, is separate list send key
	 * is used to select the value from the list
	 * 
	 * @param option
	 * @param field
	 * @param list
	 * @param listIndex
	 */
	public void selectDropDown(String option, EnhancedBy field, EnhancedBy list, int listIndex) {

		if (option != null && !option.isEmpty()) {
			Helper.click.clickAndExpect(field, list);
			EnhancedWebElement fieldElement = Element.findElements(list);

			fieldElement.sendKeys(listIndex, option);
		}
	}
	/**
	 * select drop down based on index of the drop down list eg. used for date
	 * selection where each date value: day, month, year, is separate list send key
	 * is used to select the value from the list
	 * 
	 * @param option
	 * @param field
	 * @param index
	 * @param list
	 * @param listIndex
	 */
	public void selectDropDown(String option, EnhancedBy field, int index,  EnhancedBy list, int listIndex) {

		if (option != null && !option.isEmpty()) {
			Helper.click.clickAndExpect(field, index, list);
			EnhancedWebElement fieldElement = Element.findElements(list);

			fieldElement.sendKeys(listIndex, option);
		}
	}

	/**
	 * selects radio button by radio button description
	 * 
	 * @param option
	 * @param buttons
	 */
	public void selectRadioButton(String option, EnhancedBy buttons) {
		if (option != null && !option.isEmpty()) {
			Helper.list.selectListItemEqualsByName(buttons, option);
		}
	}

	/**
	 * selects checkbox based on by value
	 * 
	 * @param button
	 * @param isSelect
	 */
	public void selectCheckBox(EnhancedBy button, boolean isSelect) {
		if (isSelect) {
			Helper.click.clickAndWait(button, 0.1);
		}
	}

	/**
	 * selects a button
	 * 
	 * @param button
	 */
	public void selectRadioButton(EnhancedBy button) {
		Helper.click.clickAndExpect(button, button);
	}
	
	/**
	 * select toggle button, on or off
	 */
	public void selectToggle(EnhancedBy on, EnhancedBy off, boolean isOn) {
		if(isOn) 
			Helper.clickAndWait(on, 0);
		else
			Helper.clickAndWait(off, 0);
	}

	/**
	 * selects multiple checkbox options
	 * 
	 * @param selections
	 * @param checkboxes
	 */
	public void selectMultipleCheckboxOptions(List<String> selections, EnhancedBy checkboxes) {
		for (String selection : selections) {
			TestLog.logPass("I select '" + selection + "'");
			Helper.list.selectListItemEqualsByName(checkboxes, selection);
		}
	}

	/**
	 * uploads file by specifying file location relative to main path
	 * 
	 * @param location
	 * @param imageButton
	 */
	public void uploadFile(String location, EnhancedBy imageButton) {
		TestLog.logPass("I upload file at location '" + location + "'");

		File file = new File("");
		String path = file.getAbsolutePath() + location;
		setField(imageButton, path);
	}

	/**
	 * * sets the image based on list of image path
	 * 
	 * @param locations
	 * @param imageButton
	 * @param images
	 */
	public void uploadImages(List<String> locations, EnhancedBy imageButton, EnhancedBy images) {
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
	public void uploadImage(String location, EnhancedBy imageButton, EnhancedBy images) {
		TestLog.logPass("uploaded file: " + location);

		int imageCount = Helper.list.getListCount(images);
		File file = new File("");
		String path = file.getAbsolutePath() + location;
		setField(imageButton, path);
		Helper.wait.waitForAdditionalElementsToLoad(images, imageCount);
	}

	/**
	 * gets the text value from an element
	 * 
	 * @param element
	 * @return
	 */
	public String getTextValue(EnhancedBy element) {
		EnhancedWebElement targetElement = Element.findElements(element);
		return targetElement.getText();
	}

	/**
	 * gets the text value from an element
	 * 
	 * @param element
	 * @return
	 */
	public String getTextValue(EnhancedBy element, int index) {
		EnhancedWebElement targetElement = Element.findElements(element);
		return targetElement.getText(index);
	}
}