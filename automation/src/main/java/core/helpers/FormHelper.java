package core.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.Select;

import core.apiCore.helpers.DataHelper;
import core.support.logger.TestLog;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

public class FormHelper {

	/**
	 * clears and set field
	 * 
	 * @param field
	 * @param value
	 */
	public void clearAndSetField(EnhancedBy field, CharSequence... value) {
		clearField(field, 0);
		setField(field, 0, value);
	}

	/**
	 * clear and sets field
	 * 
	 * @param field
	 * @param index
	 * @param value
	 */
	public void clearAndSetField(EnhancedBy field, int index, CharSequence... value) {
		if (value.length == 0)
			return;

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
	 * sets and clears field before setting the value
	 * 
	 * @param field
	 * @param index
	 * @parampvalue
	 */
	public void setField(EnhancedBy field, int index, CharSequence... value) {
		if (value.length == 0)
			return;

		TestLog.logPass("I set field '" + field.name + "' with value '" + Arrays.toString(value) + "'");

		if (!StringUtils.isBlank(value.toString())) {
			EnhancedWebElement fieldElement = Element.findElements(field);

			// attempt to hide keyboard if element is not visible
			Helper.mobile.smartHideKeyboard(field);

			Helper.wait.waitForElementToLoad(field);
			fieldElement.sendKeys(index, value);
		}
	}

	/**
	 * sets and clears field before setting the value by actions
	 *
	 * @param field
	 * @param index
	 * @param value
	 */
	public void setFieldByAction(EnhancedBy field, CharSequence... value) {
		setFieldByAction(field, 0, value);
	}

	/**
	 * sets and clears field before setting the value by actions
	 *
	 * @param field
	 * @param index
	 * @param value
	 */
	public void setFieldByAction(EnhancedBy field, int index, CharSequence... value) {
		if (value.length == 0)
			return;

		TestLog.logPass("I set field '" + field.name + "' with value '" + Arrays.toString(value) + "'");

		if (!StringUtils.isBlank(value.toString())) {
			EnhancedWebElement fieldElement = Element.findElements(field);
			Helper.wait.waitForElementToLoad(field);

			fieldElement.sendKeysByAction(index, value);

			// hides keyboard if on mobile device (ios/android)
			Helper.mobile.hideKeyboard();
		}
	}

	/**
	 * set field through javascript
	 * 
	 * @param field
	 * @param value
	 */
	public void setFieldByJs(EnhancedBy field, CharSequence... value) {
		setFieldByJs(field, 0, value);
	}

	/**
	 * set field through javascript
	 * 
	 * @param field
	 * @param index
	 * @param value
	 */
	public void setFieldByJs(EnhancedBy field, int index, CharSequence... value) {
		if (value.length == 0)
			return;

		TestLog.logPass("I set field '" + field.name + "' with value '" + Arrays.toString(value) + "'");

		if (!StringUtils.isBlank(value.toString())) {
			EnhancedWebElement fieldElement = Element.findElements(field);
			Helper.wait.waitForElementToLoad(field);

			fieldElement.sendKeyByJs(index, value);

			// hides keyboard if on mobile device (ios/android)
			Helper.mobile.hideKeyboard();
		}
	}

	/**
	 * use multiple strategies to clear the filed 1. element.clear() 2. send escape
	 * key 3. press backspace to delete the value
	 * 
	 * @param field
	 * @param index
	 */
	public void clearField(EnhancedBy field, int index) {
		EnhancedWebElement fieldElement = Element.findElements(field);
		Helper.waitForElementToLoad(field);

		String value = fieldElement.getText(index);
		if (value.isEmpty())
			return;

		Helper.clickAndWait(field, 0);
		fieldElement.clear(index);

		value = fieldElement.getText(index);
		if (!value.isEmpty()) {
			for (int i = 0; i < value.length(); i++)
				fieldElement.sendKeys(Keys.BACK_SPACE);
		}
		Helper.wait.waitForSeconds(0.1);
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

		if (value.length == 0)
			return;

		TestLog.logPass("I set field '" + child.name + "' with value '" + Arrays.toString(value) + "'");

		if (value != null && value.length != 0) {
			EnhancedWebElement childElement = Element.findElements(parent, parentIndex, child);
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
		if (value.length == 0)
			return;

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
	 * select submit button and wait for expected element to load
	 * 
	 * @param button
	 * @param expected
	 * 
	 */
	public void formSubmit(EnhancedBy button, EnhancedBy expected) {
		Helper.click.clickAndExpect(button, expected, false);
	}

	/**
	 * submit form with retrying selecting the element
	 * 
	 * @param button
	 * @param expected
	 */
	public void formSubmitNoRetry(EnhancedBy button, EnhancedBy expected) {
		// attempt to hide keyboard if element is not visible
		Helper.mobile.smartHideKeyboard(button);

		Helper.click.clickAndExpectNoRetry(button, 0, expected);
	}

	/**
	 * submit form with retrying selecting the element
	 * 
	 * @param button
	 * @param index
	 * @param expected
	 */
	public void formSubmitNoRetry(EnhancedBy button, int index, EnhancedBy expected) {
		Helper.click.clickAndExpectNoRetry(button, index, expected);
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
		// attempt to hide keyboard if element is not visible
		Helper.mobile.smartHideKeyboard(button);

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
		if (StringUtils.isBlank(option))
			return;

		TestLog.logPass("I select drop down option '" + option + "'");

		Helper.click.clickAndWait(field, index, 0.1);
		Helper.click.clickAndExpect(field, index, list, true);
		Helper.list.selectListItemEqualsByName(list, option);
	}
	
	/**
	 * selects drop down
	 * 
	 * @param option : list option we want to select
	 * @param field  : the drop down field
	 */
	public void selectDropDown(EnhancedBy field, String... options) {
		if(DataHelper.removeEmptyElements(options).length == 0)
			return;
		
		
		TestLog.logPass("I select drop down option(s) '" + Arrays.toString(options) + "'");


		boolean isOptionFound = false;
		int targetWaitTimeInSeconds = 1;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;
		
		do {
			retry--;
			for(String option : options) {
				try {
					EnhancedWebElement targetElement = Element.findElements(field);
					Select dropdown = targetElement.getSelect(0);
					dropdown.selectByVisibleText(option);
					isOptionFound = true;
				}catch(NoSuchElementException e) {
					e.getMessage();
					Helper.waitForSeconds(targetWaitTimeInSeconds);
				}catch(Exception ex) {
					ex.getMessage();
					Helper.waitForSeconds(targetWaitTimeInSeconds);
				}
			}
		} while (!isOptionFound && retry > 0);

		Helper.assertTrue("drop down option not found: " + options, isOptionFound);
	}
	
	/**
	 * selects drop down
	 * 
	 * @param index : index number for the option
	 * @param field  : the drop down field
	 */
	public void selectDropDown(EnhancedBy field, int index) {
		if (index == -1) 
			return;
		
		TestLog.logPass("I select drop down option at index '" + index + "'");

		boolean isOptionFound = false;
		int targetWaitTimeInSeconds = 1;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;
		
		do {
			retry--;
				try {
					EnhancedWebElement targetElement = Element.findElements(field);
					Select dropdown = targetElement.getSelect(0);
					dropdown.selectByIndex(index);
					isOptionFound = true;
				}catch(NoSuchElementException e) {
					e.getMessage();
					Helper.waitForSeconds(targetWaitTimeInSeconds);
				}catch(Exception ex) {
					ex.getMessage();
					Helper.waitForSeconds(targetWaitTimeInSeconds);
				}
		
		} while (!isOptionFound && retry > 0);

		Helper.assertTrue("drop down option not found at index: " + index, isOptionFound);
	}

	
	/**
	 * selects drop down
	 * 
	 * @param option : list option we want to select
	 * @param field  : the drop down field
	 * @param list   : the list items in the drop down list
	 */
	public void selectDropDown(EnhancedBy field, EnhancedBy list, String... options) {
		if(DataHelper.removeEmptyElements(options).length == 0)
			return;

		TestLog.logPass("I select drop down option(s) '" + Arrays.toString(options) + "'");

		StopWatchHelper watch = StopWatchHelper.start();
		long passedTimeInSeconds = 0;
		int index = 0;
		do {
			passedTimeInSeconds = watch.time(TimeUnit.SECONDS);

			Helper.click.clickAndExpect(field, list);
			for(String option : options)
				index = Helper.list.getElementIndexEqualsByTextWithoutRetry(list, option);

		} while (index < 0 && passedTimeInSeconds < AbstractDriver.TIMEOUT_SECONDS);
		
		if(index == -1)
			Helper.assertFalse("option: " + Arrays.toString(options) + " not found in list: " + Helper.getTextList(list));

		for(String option : options)
			Helper.list.selectListItemEqualsByName(list, option);
	}
	
	/**
	 * @deprecated replaced by selectDropDown(field, list, options)
	 * selects drop down
	 * 
	 * @param option : list option we want to select
	 * @param field  : the drop down field
	 * @param list   : the list items in the drop down list
	 */
	@Deprecated  
	public void selectDropDown(String option, EnhancedBy field, EnhancedBy list) {
		selectDropDown(field, list, option);
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

		if (StringUtils.isBlank(option))
			return;
		
		StopWatchHelper watch = StopWatchHelper.start();
		long passedTimeInSeconds = 0;
		int index = 0;
		do {
			passedTimeInSeconds = watch.time(TimeUnit.SECONDS);

			Helper.list.selectListItemContainsByName(field, field_Identifier);
			index = Helper.list.getElementIndexEqualsByTextWithoutRetry(list, option);

		} while (index < 0 && passedTimeInSeconds < AbstractDriver.TIMEOUT_SECONDS);
		
		if(index == -1)
			Helper.assertFalse("option: " + option + " not found in list: " + Helper.getTextList(list));
			Helper.list.selectListItemEqualsByName(list, option);

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
	 * select drop down field based on index select option field based on index
	 * 
	 * @param field
	 * @param index
	 * @param list
	 * @param optionIndex
	 */
	public void selectDropDown(EnhancedBy field, int index, EnhancedBy list, int optionIndex) {
		if (index != -1) {
			Helper.click.clickAndExpect(field, index, list);
			Helper.list.selectListItemByIndex(list, index);
		}
	}

	/**
	 * select drop down field based on index select option field based on text
	 * 
	 * @param field
	 * @param index
	 * @param list
	 * @param text
	 */
	public void selectDropDown(EnhancedBy field, int index, EnhancedBy list, String option) {
		if (index != -1) {
			
			StopWatchHelper watch = StopWatchHelper.start();
			long passedTimeInSeconds = 0;
			int optionIndex = 0;
			do {
				passedTimeInSeconds = watch.time(TimeUnit.SECONDS);

				Helper.click.clickAndExpect(field, index, list);
				optionIndex = Helper.list.getElementIndexEqualsByTextWithoutRetry(list, option);

			} while (index < 0 && passedTimeInSeconds < AbstractDriver.TIMEOUT_SECONDS);
			
			if(optionIndex == -1)
				Helper.assertFalse("option: " + option + " not found in list: " + Helper.getTextList(list));
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
		if (StringUtils.isBlank(option))
			return;

		Helper.click.clickAndExpect(field, index, list, true);
		Helper.list.selectListItemEqualsByName(list, option);
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
		if (StringUtils.isBlank(option))
			return;

		Helper.click.clickAndExpect(field, list);
		EnhancedWebElement fieldElement = Element.findElements(list);

		fieldElement.sendKeys(listIndex, option);
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
	public void selectDropDown(String option, EnhancedBy field, int index, EnhancedBy list, int listIndex) {
		if (StringUtils.isBlank(option))
			return;

		Helper.click.clickAndExpect(field, index, list);
		EnhancedWebElement fieldElement = Element.findElements(list);

		fieldElement.sendKeys(listIndex, option);
	}

	/**
	 * selects radio button by radio button description
	 * 
	 * @param option
	 * @param buttons
	 */
	public void selectRadioButton(String option, EnhancedBy buttons) {
		if (StringUtils.isBlank(option))
			return;

		Helper.list.selectListItemEqualsByName(buttons, option);
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
		if (isOn)
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
	public void uploadFile(String location, EnhancedBy fileTypeElement) {
		TestLog.logPass("I upload file at location '" + location + "'");

		String path = Helper.getFullPath(location);
		setField(fileTypeElement, path);
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
	 * @param images      : uploaded image
	 */
	public void uploadImage(String location, EnhancedBy imageButton, EnhancedBy images) {
		TestLog.logPass("uploaded file: " + location);

		int imageCount = Helper.list.getListCount(images);
		String path = Helper.getFullPath(location);
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
		Helper.waitForElementToLoad(element);
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
		Helper.waitForElementToLoad(element);
		EnhancedWebElement targetElement = Element.findElements(element);
		return targetElement.getText(index);
	}
	
	/**
	 * attempts to set text value on element, if not successful, then element is not editable
	 * @param element
	 * @param index
	 * @return
	 */
	public boolean isElementEditable(EnhancedBy element) {
		return isElementEditable(element, 0);
	}
	
	/**
	 * attempts to set text value on element, if not successful, then element is not editable
	 * @param element
	 * @param index
	 * @return
	 */
	public boolean isElementEditable(EnhancedBy element, int index) {
		Helper.setField(element, "test");
		String value = Helper.getTextValue(element);
		if(value.equals("test"))
			return true;
		return false;
	}
}