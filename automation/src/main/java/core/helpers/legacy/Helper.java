package core.helpers.legacy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.json.JSONException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.Location;
import org.testng.asserts.SoftAssert;

import core.helpers.AssertHelper;
import core.helpers.DateHelper;
import core.helpers.Element;
import core.helpers.ElementActionHelper;
import core.helpers.ElementHelper;
import core.helpers.ExternalClassHelper;
import core.helpers.FormHelper;
import core.helpers.ImageProcessingHelper;
import core.helpers.ListHelper;
import core.helpers.LocalizationHelper;
import core.helpers.LoginHelper;
import core.helpers.Loginbuilder;
import core.helpers.MobileHelper;
import core.helpers.PageHelper;
import core.helpers.RestApiHelper;
import core.helpers.StopWatchHelper;
import core.helpers.UtilityHelper;
import core.helpers.VerifyHelper;
import core.helpers.WaitHelper;
import core.helpers.click.ClickHelper;
import core.helpers.click.ClickHelperAction;
import core.helpers.click.ClickHelperJs;
import core.helpers.csvHelper.CsvHelper;
import core.helpers.emailHelper.EmailObject;
import core.helpers.emailHelper.EmailSendHelper;
import core.helpers.excelHelper.ExcelHelper;
import core.helpers.excelHelper.ExcelObject;
import core.support.objects.KeyValue;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class Helper extends core.helpers.Helper {

	public static ClickHelper click = new ClickHelper();
	public static WaitHelper wait = new WaitHelper();
	public static MobileHelper mobile = new MobileHelper();
	public static ListHelper list = new ListHelper();
	public static PageHelper page = new PageHelper();
	public static FormHelper form = new FormHelper();
	public static ImageProcessingHelper image = new ImageProcessingHelper();
	public static DateHelper date = new DateHelper();
	public static CsvHelper csv = new CsvHelper();
	public static Loginbuilder loginbuilder = new Loginbuilder();
	public static VerifyHelper verify = new VerifyHelper();
	public static ClickHelperJs clickJs = new ClickHelperJs();
	public static ClickHelperAction clickAction = new ClickHelperAction();

	// ExcelHelper
	/**
	 * gets the excel file And the work sheet
	 * 
	 * @param Path
	 * @param SheetName
	 * @throws Exception
	 */
	public static ExcelObject excel_setExcelFile(ExcelObject excel) throws Exception {
		return ExcelHelper.setExcelFile(excel);
	}

	/**
	 * returns all the column data as array list of string
	 * 
	 * @param colNum
	 * @return
	 * @throws Exception
	 */
	public static List<String> excel_getColumData(ExcelObject excel) throws Exception {
		return ExcelHelper.getColumData(excel);
	}

	/**
	 * This method is to read the test data from the Excel cell, in this we are
	 * passing parameters as Row num And Col num
	 * 
	 * @param RowNum
	 * @param ColNum
	 * @return
	 * @throws Exception
	 */

	public static String excel_getCellData(ExcelObject excel) throws Exception {
		return ExcelHelper.getCellData(excel);
	}

	/**
	 * This method is to write in the Excel cell, Row num And Col num are the
	 * parameters
	 * 
	 * @param excel - required: excel.row, excel.column, excel.value, excel.file
	 * @throws Exception
	 */
	public static void excel_setCellData(ExcelObject excel) throws Exception {
		ExcelHelper.setCellData(excel);
	}

	/**
	 * 
	 * @param excelObjects - contains data info
	 * @throws Exception
	 */
	public static void excel_setCellData(List<ExcelObject> excelObjects) throws Exception {
		ExcelHelper.setCellData(excelObjects);
	}

	
	// Driver Legacy
	public static void setLegacyDriver(WebDriver driver) {
		 DriverLegacy.setDriver(driver);
	}
	
	public static void setLegacyDriver(WebDriver driver, boolean isPropertyDisabled, int timeoutSec, int implicitSec) {
		DriverLegacy.setDriver(driver, isPropertyDisabled, timeoutSec, implicitSec);
	}

	// Element Helper
	/**
	 * finds element based on parent element
	 * 
	 * @param element
	 * @param parent
	 * @return
	 */
	protected static EnhancedWebElement findElements(WebElement parent, WebElement child) {
		EnhancedBy elementParent = DriverLegacy.getEnhancedElement(parent);
		EnhancedBy elementChild = DriverLegacy.getEnhancedElement(child);
		return core.helpers.Helper.findElements(elementParent, elementChild);
	}

	/**
	 * finds list of elements
	 * 
	 * @param element
	 * @return
	 */
	protected static EnhancedWebElement findElements(WebElement element) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		return core.helpers.Helper.findElements(elementBy);
	}

	/**
	 * finds a list of elements based on parent element
	 * 
	 * @param element
	 * @param parent
	 * @return
	 */
	protected static EnhancedWebElement findElements(WebElement parent, int parentIndex, WebElement child) {
		EnhancedBy parentBy = DriverLegacy.getEnhancedElement(parent);
		EnhancedBy childBy = DriverLegacy.getEnhancedElement(child);

		return core.helpers.Helper.findElements(childBy, parentIndex, parentBy);
	}

	// ClickHelper
	/**
	 * clicks target And waits for expected element to display retries 10 times
	 * 
	 * @param target
	 * @param expected
	 */
	public static void clickAndExpect(WebElement target, WebElement expected) {
		EnhancedBy elementTarget = DriverLegacy.getEnhancedElement(target);
		EnhancedBy elementExpected = DriverLegacy.getEnhancedElement(expected);

		click.clickAndExpect(elementTarget, elementExpected);
	}


	/**
	 * click And expect based on the text value on the element. eg. button with "OK"
	 * text
	 * 
	 * @param target
	 * @param text
	 * @param expected
	 */
	public static void clickAndExpect(WebElement target, String text, WebElement expected) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		EnhancedBy expectedBy = DriverLegacy.getEnhancedElement(expected);
		click.clickAndExpectByText(targetBy, text, expectedBy);
	}

	/**
	 * click And expect based on the text value on the element. eg. button with "OK"
	 * text
	 * 
	 * @param target
	 * @param text
	 * @param expected
	 */
	public void clickAndExpectContainsByText(WebElement target, String text, WebElement expected) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		EnhancedBy expectedBy = DriverLegacy.getEnhancedElement(expected);
		click.clickAndExpectContainsByText(targetBy, text, expectedBy);
	}

	/**
	 * clicks target And waits for expected element to display retries 10 times
	 * 
	 * @param target
	 * @param expected
	 */
	public static void clickAndExpect(WebElement target, WebElement expected, boolean isMobileRefresh) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		EnhancedBy expectedBy = DriverLegacy.getEnhancedElement(expected);
		click.clickAndExpect(targetBy, expectedBy, isMobileRefresh);
	}

	/**
	 * clicks target And wait for one the 2 expected elements to appear
	 * 
	 * @param target
	 * @param index
	 * @param expected1
	 * @param expected2
	 */
	public static void clickAndExpect(WebElement target, int index, WebElement expected1, WebElement expected2) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		EnhancedBy expected1By = DriverLegacy.getEnhancedElement(expected1);
		EnhancedBy expected2By = DriverLegacy.getEnhancedElement(expected2);
		click.clickAndExpect(targetBy, index, expected1By, expected2By);
	}

	/**
	 * clicks element based on index And waits for expected element to be displayed
	 * 
	 * @param target
	 * @param index
	 * @param expected
	 */
	public static void clickAndExpect(WebElement target, int index, WebElement expected) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		EnhancedBy expectedBy = DriverLegacy.getEnhancedElement(expected);
		click.clickAndExpect(targetBy, index, expectedBy, true);
	}

	/**
	 * clicks target And waits for expected element to show up also waits for
	 * spinner element to be removed from display
	 * 
	 * @param target
	 * @param expected
	 * @param spinner
	 */
	public static void clickAndExpect(WebElement target, WebElement expected, WebElement spinner) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		EnhancedBy expectedBy = DriverLegacy.getEnhancedElement(expected);
		EnhancedBy spinnerBy = DriverLegacy.getEnhancedElement(spinner);
		click.clickAndExpect(targetBy, expectedBy, spinnerBy);
	}

	/**
	 * clicks target And waits for expected to not be displayed retries 10 times
	 * 
	 * @param target
	 * @param expected
	 */
	public static void clickAndNotExpect(WebElement target, WebElement expected) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		EnhancedBy expectedBy = DriverLegacy.getEnhancedElement(expected);
		click.clickAndNotExpect(targetBy, expectedBy);
	}

	/**
	 * clicks target And waits for seconds
	 * 
	 * @param target
	 * @param expected
	 */
	public static void clickAndWait(WebElement target, double timeInSeconds) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		click.clickAndWait(targetBy, timeInSeconds);
	}

	/**
	 * clicks target And waits for seconds
	 * 
	 * @param target
	 * @param expected
	 */
	public static void clickAndWait(WebElement target, int index, double timeInSeconds) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		click.clickAndWait(targetBy, index, timeInSeconds);
	}

	public static void clickAndNotExpect(WebElement target, int index, WebElement expected) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		EnhancedBy expectedBy = DriverLegacy.getEnhancedElement(expected);
		click.clickAndNotExpect(targetBy, index, expectedBy);
	}

	/**
	 * Click on an element's specific x,y location
	 * 
	 * @param by by element
	 * @param x  x offset coordinate
	 * @param y  y offset coordinate
	 */
	public static void clickElementLocationBy(WebElement target, int x, int y) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		click.clickElementLocationBy(targetBy, x, y);
	}


	/**
	 * click point at x,y coordinates and expect and element to be present retry
	 * every 5 seconds for duration of explicit timeout
	 * 
	 * @param x
	 * @param y
	 * @param expected
	 */
	public static void clickPointsAndExpect(int x, int y, WebElement expected) {
		EnhancedBy expectedBy = DriverLegacy.getEnhancedElement(expected);
		click.clickPointsAndExpect(x, y, expectedBy);
	}

	/**
	 * double click on element
	 * 
	 * @param target
	 * @param index
	 */
	public static void doubleClick(WebElement target, int index) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		clickAction.doubleClick(targetBy, index);
	}

	/**
	 * right click on element at index
	 * 
	 * @param target
	 * @param index
	 */
	public static void rightClick(WebElement target, int index) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		clickAction.rightClick(targetBy, index);
	}

	/**
	 * click element with text containing
	 * 
	 * @param by
	 * @param text
	 */
	public static void clickElementContinsByText(WebElement target, String text) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		click.clickElementContinsByText(targetBy, text);
	}

	/**
	 * click And hold element
	 * 
	 * @param target
	 * @param seconds
	 */
	public static void clickAndHold(WebElement target, double seconds) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		click.clickAndHold(targetBy, seconds);
	}

	/**
	 * click And hold based on element index
	 * 
	 * @param target
	 * @param index
	 * @param seconds
	 */
	public static void clickAndHold(WebElement target, int index, double seconds) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		click.clickAndHold(targetBy, index, seconds);
	}

	/**
	 * drag And drop from src element to target element
	 * 
	 * @param src
	 * @param target
	 */
	public static void dragAndDrop(WebElement src, WebElement target) {
		EnhancedBy srcBy = DriverLegacy.getEnhancedElement(src);
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);

		click.dragAndDrop(srcBy, targetBy);
	}
	
	/**
	 * drag And drop from src element to target element
	 * 
	 * @param src
	 * @param target
	 */
	public void dragAndDrop(WebElement srcParent, int srcParentIndex, WebElement srcChild, int scrChildIndex, WebElement targetParent, int targeParenttIndex, WebElement targetChild, int targetChildIndex) {
		EnhancedBy srcParentBy = DriverLegacy.getEnhancedElement(srcParent);
		EnhancedBy srcChildBy = DriverLegacy.getEnhancedElement(srcChild);
		EnhancedBy targetParentBy = DriverLegacy.getEnhancedElement(targetParent);
		EnhancedBy targetChildBy = DriverLegacy.getEnhancedElement(targetChild);

		click.dragAndDrop(srcParentBy, srcParentIndex, srcChildBy, scrChildIndex, targetParentBy, targeParenttIndex, targetChildBy, targetChildIndex);
	}

	/**
	 * drag And drop from src element to target element
	 * 
	 * @param src
	 * @param target
	 */
	public static void dragAndDrop(WebElement src, int xOffset, int yOffset) {
		EnhancedBy srcBy = DriverLegacy.getEnhancedElement(src);
		click.dragAndDrop(srcBy, xOffset, yOffset);
	}

	// VerifyHelper
	/**
	 * verifies if element(s) is (are) displayed
	 * 
	 * @param by
	 */
	public static void verifyElementIsDisplayed(WebElement target) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		verify.verifyElementIsDisplayed(targetBy);
	}

	/**
	 * returns true if element is displayed
	 * 
	 * @param element
	 * @return
	 */
	public static boolean isDisplayed(WebElement target) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return verify.isPresent(targetBy);
	}

	/**
	 * returns true if element is displayed
	 * 
	 * @param element
	 * @return
	 */
	public static boolean isPresent(WebElement target) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return verify.isPresent(targetBy);
	}

	/**
	 * returns true if element contains text
	 * 
	 * @param element
	 * @param text
	 * @return
	 */
	public static boolean isElementContainingText(WebElement target, String text) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return verify.isElementContainingText(targetBy, text);
	}

	/**
	 * verify if element contains text
	 * 
	 * @param element
	 * @param text
	 */
	public static void verifyElementContainingText(WebElement target, String text) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		verify.verifyElementContainingText(targetBy, text);
	}

	
	/**
	 * verify tool tip is displayed
	 * @param tooltip
	 * @param index
	 * @param text
	 */
	public void verifyToolTip(WebElement tooltip, int index, String text) {
		EnhancedBy tooltipBy = DriverLegacy.getEnhancedElement(tooltip);
		verify.verifyToolTip(tooltipBy, index, text);
	}

	/**
	 * verifies if element(s) is (are) not displayed
	 * 
	 * @param by
	 */
	public static void verifyElementIsNotDisplayed(WebElement target) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		verify.verifyElementIsNotDisplayed(targetBy);
	}

	/**
	 * verifies element text equals provided value
	 * 
	 * @param by
	 * @param value
	 */
	public static void verifyElementText(WebElement target, String value) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		verify.verifyElementText(targetBy, value);
	}

	/**
	 * verifies element count
	 * 
	 * @param by
	 * @param value
	 */
	public static void verifyElementCount(WebElement target, int value, int... correction) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		verify.verifyElementCount(targetBy, value, correction);
	}

	/**
	 * verifies if text contains any of values in list
	 * 
	 * @param target
	 * @param values
	 */
	public static void verifyAnyTextContaining(WebElement target, String... values) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		verify.verifyAnyTextContaining(targetBy, values);
	}

	/**
	 * verifies if text contains any of values in list
	 * 
	 * @param target
	 * @param values
	 */
	public static void verifyAnyText(WebElement target, String... values) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		verify.verifyAnyText(targetBy, values);
	}

	// FormHelper Class

	/**
	 * sets field text value by index hides keyboard if on ios device
	 * 
	 * @param value
	 * @param field
	 * @param index
	 */
	public static void setField(String value, WebElement field, int index) {
		EnhancedBy element = DriverLegacy.getEnhancedElement(field);
		form.setField(element, index, value);
	}

	/**
	 * set field value if value is not empty
	 * 
	 * @param field
	 * @param value
	 */
	public static void setField(WebElement field, CharSequence... value) {
		EnhancedBy element = DriverLegacy.getEnhancedElement(field);
		form.setField(element, value);
	}
	

	public static void setFieldByAction(WebElement field, int index, CharSequence... value) {
		EnhancedBy element = DriverLegacy.getEnhancedElement(field);
		form.setFieldByAction(element, index, value);
	}

	public static void setFieldByAction(WebElement field, CharSequence... value) {
		EnhancedBy element = DriverLegacy.getEnhancedElement(field);
		form.setFieldByAction(element, 0, value);
	}

	public static void setFieldByJs(WebElement field, int index, CharSequence... value) {
		EnhancedBy element = DriverLegacy.getEnhancedElement(field);
		form.setFieldByJs(element, index, value);
	}

	public static void setFieldByJs(WebElement field, CharSequence... value) {
		EnhancedBy element = DriverLegacy.getEnhancedElement(field);
		form.setFieldByJs(element, 0, value);
	}

	/**
	 * sets field clears field before setting the value
	 * 
	 * @param field
	 * @param value
	 */
	public static void clearAndSetField(WebElement field, CharSequence... value) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		form.clearAndSetField(fieldBy, value);
	}

	/**
	 * sets field clears field before setting the value
	 * 
	 * @param field
	 * @param index
	 * @param value
	 */
	public static void clearAndSetField(WebElement field, int index, CharSequence... value) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		form.clearAndSetField(fieldBy, index, value);
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
	public static void setChildField(WebElement parent, int parentIndex, WebElement child, int childIndex,
			CharSequence... value) {
		EnhancedBy parentBy = DriverLegacy.getEnhancedElement(parent);
		EnhancedBy childBy = DriverLegacy.getEnhancedElement(child);
		form.setKeyChildField(parentBy, parentIndex, childBy, childIndex, value);
	}

	/**
	 * sets field And presses the enter key
	 * 
	 * @param field
	 * @param value
	 */
	public static void setFieldAndEnter(WebElement field, CharSequence... value) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		form.setFieldAndEnter(fieldBy, value);
	}

	/**
	 * send
	 * 
	 * @param key
	 * @param field
	 */
	public static void setKey(Keys key, WebElement field) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		form.setKey(key, fieldBy);
	}

	/**
	 * select submit button And wait for expected element to load
	 * 
	 * @param button
	 * @param expected
	 * 
	 */
	public static void formSubmit(WebElement button, WebElement expected) {
		EnhancedBy buttonBy = DriverLegacy.getEnhancedElement(button);
		EnhancedBy expectedBy = DriverLegacy.getEnhancedElement(expected);
		form.formSubmit(buttonBy, expectedBy);
	}

	/**
	 * clicks submit button, wait for element to appear And loading spinner to be
	 * removed
	 * 
	 * @param button
	 * @param expected
	 * @param spinner
	 */
	public static void formSubmit(WebElement button, WebElement expected, WebElement spinner) {
		EnhancedBy buttonBy = DriverLegacy.getEnhancedElement(button);
		EnhancedBy expectedBy = DriverLegacy.getEnhancedElement(expected);
		EnhancedBy spinnerBy = DriverLegacy.getEnhancedElement(spinner);
		form.formSubmit(buttonBy, expectedBy, spinnerBy);
	}

	/**
	 * selects dropdown by double clicking on the field
	 * 
	 * @param option
	 * @param field
	 * @param list
	 */
	public static void selectDropDownWithDoubleClick(String option, WebElement field, WebElement listValue) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);

		form.selectDropDownWithDoubleClick(option, fieldBy, listValueBy);
	}

	/**
	 * selects dropdown by double clicking on the field
	 * 
	 * @param option
	 * @param field
	 * @param list
	 */
	public static void selectDropDownWithDoubleClick(String option, WebElement field, int index, WebElement listValue) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		form.selectDropDownWithDoubleClick(option, fieldBy, index, listValueBy);
	}
	
	/**
	 * selects drop down
	 * 
	 * @param option : list option we want to select
	 * @param field  : the drop down field
	 */
	public static void selectDropDown(WebElement field,  String... options) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		form.selectDropDown(fieldBy, options);
	}
	
	/**
	 * selects drop down
	 * 
	 * @param option : list option we want to select
	 * @param field  : the drop down field
	 */
	public static void selectDropDown(WebElement field, int index) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		form.selectDropDown(fieldBy, index);
	}
	
	/**
	 * selects drop down
	 * 
	 * @param option : list option we want to select
	 * @param field  : the drop down field
	 * @param list   : the list items in the drop down list
	 */
	public static void selectDropDown(WebElement field, WebElement list, String... options) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		EnhancedBy listBy = DriverLegacy.getEnhancedElement(list);
		form.selectDropDown(fieldBy, listBy, options);
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
	public static void selectDropDown(String option, WebElement field, WebElement listValue) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		form.selectDropDown(option, fieldBy, listValueBy);
	}

	/**
	 * selects drop down from list defined by another list defined by text
	 * 
	 * @param option
	 * @param field
	 * @param field_Identifier
	 * @param list
	 */
	public static void selectDropDown(String option, WebElement field, String field_Identifier, WebElement listValue) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		form.selectDropDown(option, fieldBy, field_Identifier, listValueBy);
	}

	/**
	 * select drop down by index from the drop down list
	 * 
	 * @param index
	 * @param field
	 * @param list
	 */
	public static void selectDropDown(int index, WebElement field, WebElement listValue) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		form.selectDropDown(index, fieldBy, listValueBy);
	}

	/**
	 * select drop down using by value of the list item
	 * 
	 * @param index
	 * @param field
	 * @param list
	 */
	public static void selectDropDown(WebElement field, WebElement item) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		EnhancedBy itemBy = DriverLegacy.getEnhancedElement(item);
		form.selectDropDown(fieldBy, itemBy);
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
	public static void selectDropDown(String option, WebElement field, WebElement listValue, int listIndex) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		form.selectDropDown(option, fieldBy, listValueBy, listIndex);
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
	public static void selectDropDown(String option, WebElement field, int index, WebElement list, int listIndex) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		EnhancedBy listBy = DriverLegacy.getEnhancedElement(list);
		form.selectDropDown(option, fieldBy, index, listBy, listIndex);
	}

	/**
	 * selects drop down based on index of the drop down field
	 * 
	 * @param option
	 * @param field
	 * @param index
	 * @param list
	 */
	public static void selectDropDown(String option, WebElement field, int index, WebElement listValue) {
		EnhancedBy fieldBy = DriverLegacy.getEnhancedElement(field);
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		form.selectDropDown(option, fieldBy, index, listValueBy);
	}

	/**
	 * selects checkbox based on by value
	 * 
	 * @param button
	 * @param isSelect
	 */
	public static void selectCheckBox(WebElement button, boolean isSelect) {
		EnhancedBy buttonBy = DriverLegacy.getEnhancedElement(button);
		form.selectCheckBox(buttonBy, isSelect);
	}

	/**
	 * selects radio button by radio button description
	 * 
	 * @param option
	 * @param buttons
	 */
	public static void selectRadioButton(String option, WebElement buttons) {
		EnhancedBy buttonsBy = DriverLegacy.getEnhancedElement(buttons);
		form.selectRadioButton(option, buttonsBy);
	}

	/**
	 * selects a button
	 * 
	 * @param button
	 */
	public static void selectRadioButton(WebElement button) {
		EnhancedBy buttonBy = DriverLegacy.getEnhancedElement(button);
		form.selectRadioButton(buttonBy);
	}

	/**
	 * selects multiple checkbox options
	 * 
	 * @param selections
	 * @param checkboxes
	 */
	public static void selectMultipleCheckboxOptions(List<String> selections, WebElement checkboxes) {
		EnhancedBy checkboxesBy = DriverLegacy.getEnhancedElement(checkboxes);
		form.selectMultipleCheckboxOptions(selections, checkboxesBy);
	}

	// uploadHelper
	/**
	 * uploads file by specifying file location relative to main path
	 * 
	 * @param location
	 * @param imageButton
	 */
	public static void uploadFile(String location, WebElement imageButton) {
		EnhancedBy imageButtonBy = DriverLegacy.getEnhancedElement(imageButton);
		form.uploadFile(location, imageButtonBy);
	}

	/**
	 * sets the image based on location
	 * 
	 * @param location
	 * @param imageButton
	 * @param images      : uploaded image
	 */
	public static void uploadImages(List<String> locations, WebElement imageButton, WebElement images) {
		EnhancedBy imageButtonBy = DriverLegacy.getEnhancedElement(imageButton);
		EnhancedBy imagesBy = DriverLegacy.getEnhancedElement(images);
		form.uploadImages(locations, imageButtonBy, imagesBy);
	}

	/**
	 * sets the image based on location
	 * 
	 * @param location
	 * @param imageButton
	 * @param images      : uploaded image
	 */
	public static void uploadImage(String location, WebElement imageButton, WebElement images) {
		EnhancedBy imageButtonBy = DriverLegacy.getEnhancedElement(imageButton);
		EnhancedBy imagesBy = DriverLegacy.getEnhancedElement(images);
		form.uploadImage(location, imageButtonBy, imagesBy);
	}

	/**
	 * gets the text value from an element
	 * 
	 * @param element
	 * @return
	 */
	public static String getTextValue(WebElement target) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return form.getTextValue(targetBy);
	}

	/**
	 * gets the text value from an element
	 * 
	 * @param element
	 * @return
	 */
	public static String getTextValue(WebElement target, int index) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return form.getTextValue(targetBy, index);
	}
	
	/**
	 * attempts to set text value on element, if not successful, then element is not editable
	 * @param element
	 * @param index
	 * @return
	 */
	public boolean isElementEditable(WebElement target) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return isElementEditable(targetBy);
	}
	
	/**
	 * attempts to set text value on element, if not successful, then element is not editable
	 * @param element
	 * @param index
	 * @return
	 */
	public boolean isElementEditable(WebElement target, int index) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return isElementEditable(targetBy, index);
	}

	// ListHelper

	/**
	 * selects an element in list by its index value
	 * 
	 * @param list
	 * @param index
	 */
	public static void selectElementInList(WebElement listValue, int index) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		list.selectElementInList(listValueBy, index);
	}

	/**
	 * enters value into the search field And selects enter waits for the loading
	 * spinner to be removed
	 * 
	 * @param searchQuery
	 * @param byTarget
	 * @param spinner
	 */
	public static void searchAndWaitForResults(String searchQuery, WebElement target, WebElement spinner) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		EnhancedBy spinnerBy = DriverLegacy.getEnhancedElement(spinner);
		list.searchAndWaitForResults(searchQuery, targetBy, spinnerBy);
	}

	/**
	 * selects list item by the string option provided
	 * 
	 * @param list
	 * @param option
	 */
	public static void selectListItemEqualsByName(WebElement listValue, String option) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);

		list.selectListItemEqualsByName(listValueBy, option);
	}

	/**
	 * finds target element which is in the same container And has the same index as
	 * the parent eg. delete button in the list of customers, both having index 2.
	 * we find the index by name, And use that to find the target element
	 * 
	 * @param list
	 * @param option
	 * @param target
	 */
	public static void selectListItemEqualsByName(WebElement listValue, String option, WebElement target) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);

		list.selectListItemEqualsByName(listValueBy, option, targetBy);
	}

	/**
	 * selects list item containing string eg. a list of athletes names containing a
	 * delete button
	 * 
	 * @param list
	 * @param option
	 * @param target
	 */
	public static void selectListItemContainsByName(WebElement listValue, String option, WebElement target) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);

		list.selectListItemContainsByName(listValueBy, option, targetBy);
	}

	/**
	 * find the index of the target element in list eg. list A, 5 rows, has element
	 * B in row 2. therefore, index 1 is returned @param list
	 * 
	 * @param list
	 * @param target
	 * @return index of element in list
	 */
	public static int getElementIndexInList(WebElement srcList, WebElement target) {
		EnhancedBy srcListBy = DriverLegacy.getEnhancedElement(srcList);
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);

		return list.getElementIndexInList(srcListBy, targetBy);
	}

	/**
	 * returns the list of string values for a row of elements
	 * 
	 * @param list
	 * @param index
	 * @param rows
	 * @return
	 */
	public List<String> getRowValuesFromList(WebElement srclist, int index, WebElement rows) {
		EnhancedBy listBy = DriverLegacy.getEnhancedElement(srclist);
		EnhancedBy rowsBy = DriverLegacy.getEnhancedElement(rows);

		return list.getRowValuesFromList(listBy, index, rowsBy);
	}

	/**
	 * gets hashmap representation of data column with row values
	 * 
	 * @param columns
	 * @param dataRows
	 * @return
	 */
	public HashMap<String, List<String>> getTableMap(WebElement columns, WebElement dataRows, WebElement dataCells) {
		EnhancedBy columnsBy = DriverLegacy.getEnhancedElement(columns);
		EnhancedBy dataRowsBy = DriverLegacy.getEnhancedElement(dataRows);
		EnhancedBy dataCellsBy = DriverLegacy.getEnhancedElement(dataCells);
		
		return list.getTableMap(columnsBy, dataRowsBy, dataCellsBy);
	}

	/**
	 * gets hashmap representation of data column with row values
	 * 
	 * @param columns
	 * @param dataRows
	 * @param dataCells
	 * @param maxRows
	 * @return
	 */
	public HashMap<String, List<String>> getTableMap(WebElement columns, WebElement dataRows, WebElement dataCells,
			int maxRows) {
		EnhancedBy columnsBy = DriverLegacy.getEnhancedElement(columns);
		EnhancedBy dataRowsBy = DriverLegacy.getEnhancedElement(dataRows);
		EnhancedBy dataCellsBy = DriverLegacy.getEnhancedElement(dataCells);

		return list.getTableMap(columnsBy, dataRowsBy, dataCellsBy, maxRows);
	}

	/**
	 * gets hashmap representation of data column with row values
	 * 
	 * @param columns
	 * @param columnInitialIndex
	 * @param dataRows
	 * @param rowInitialIndex
	 * @param dataCells
	 * @param maxRows
	 * @return
	 */
	public HashMap<String, List<String>> getTableMap(WebElement columns, int columnInitialIndex, WebElement dataRows,
			int rowInitialIndex, WebElement dataCells, int maxRows) {
		EnhancedBy columnsBy = DriverLegacy.getEnhancedElement(columns);
		EnhancedBy dataRowsBy = DriverLegacy.getEnhancedElement(dataRows);
		EnhancedBy dataCellsBy = DriverLegacy.getEnhancedElement(dataCells);
		return list.getTableMap(columnsBy, columnInitialIndex, dataRowsBy, rowInitialIndex, dataCellsBy, maxRows);
	}

	
	/**
	 * Selects list item from a parent container eg. delete button in a list defined
	 * by name find the container containing the name And Then finds the delete
	 * button in that container as target
	 * 
	 * @param list
	 * @param option
	 * @param target
	 */
	public static void selectElementContainedInList(WebElement listValue, String option, WebElement target, int targetIndex) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		list.selectElementContainedInList(listValueBy, option, targetBy, targetIndex);
	}

	/**
	 * Selects list item from a parent container eg. delete button in a list defined
	 * by name find the container containing the name And Then finds the delete
	 * button in that container as target
	 * 
	 * @param list
	 * @param option
	 * @param target
	 */
	public static void selectElementContainedInList(WebElement listValue, String option, WebElement target) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		list.selectElementContainedInList(listValueBy, option, targetBy);
	}

	/**
	 * finds target element which is in the same container And has the same index as
	 * the parent eg. delete button in the list of customers, both having index 2.
	 * we find the index containing name, And use that to find the target element
	 * 
	 * @param list
	 * @param option
	 */
	public static void selectListItemContainsByName(WebElement listValue, String option) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		list.selectListItemContainsByName(listValueBy, option);
	}

	/**
	 * selects list item by the string option provided
	 * 
	 * @param list
	 * @param option
	 */
	public static void selectListItemByIndex(WebElement listValue, int index) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		list.selectListItemByIndex(listValueBy, index);
	}

	/**
	 * returns the number of elements in list
	 * 
	 * @param list
	 * @return
	 */
	public static int getListCount(WebElement listValue) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		return list.getListCount(listValueBy);
	}

	/**
	 * returns the index of text value in a list
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public static int getElementIndexEqualsByText(WebElement listValue, String option) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		return list.getElementIndexEqualsByText(listValueBy, option);
	}

	/**
	 * retuns index of element in list which contains in text
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public static int getElementIndexContainByText(WebElement listValue, String option) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		return list.getElementIndexContainByText(listValueBy, option);
	}

	/**
	 * returns the index of string value in list of strings
	 * 
	 * @param stringList normalized
	 * @param option     normalized
	 * @return
	 */
	public static int getStringIndexContainByText(List<String> stringList, String option) {
		return list.getStringIndexContainByText(stringList, option);
	}


	/**
	 * verifies if option value is in the list index = -1 indicates the value is not
	 * in list
	 * 
	 * @param list
	 * @param option
	 */
	public static void verifyContainsIsInList(WebElement listValue, String option) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		list.verifyContainsIsInList(listValueBy, option);
	}

	/**
	 * verifies if option value is in the list index = -1 indicates the value is not
	 * in list
	 * 
	 * @param list
	 * @param option
	 */
	public static void verifyIsInList(WebElement listValue, String option) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		list.verifyIsInList(listValueBy, option);
	}

	/**
	 * verify text option in list based on key value in the list
	 * 
	 * @param list
	 * @param indicator
	 * @param option
	 */
	public static void verifyIsInList(WebElement listValue, String indicator, String option) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		list.verifyIsInList(listValueBy, indicator, option);
	}

	/**
	 * return if element is contained in list
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public static boolean isContainedInList(WebElement listValue, String option) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		return list.isContainedInList(listValueBy, option);
	}

	/**
	 * return if element is an exact match in list
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public static boolean isExactMatchInList(WebElement listValue, String option) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		return list.isExactMatchInList(listValueBy, option);
	}

	/**
	 * returns the list of values in a list
	 * 
	 * @param list
	 * @return
	 */
	public static List<String> getListValues(WebElement listValue) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		return list.getListValues(listValueBy);
	}

	/**
	 * returns the list of values in a list
	 * 
	 * @param list
	 * @return
	 */
	public static List<String> getTextList(WebElement listValue) {
		EnhancedBy listValueBy = DriverLegacy.getEnhancedElement(listValue);
		return list.getTextList(listValueBy);
	}

	// MobileHelper

	/**
	 * swipe right on the screen based on element position
	 */
	public void mobile_swipeRight(WebElement element, int index, double durationInSeconds) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		mobile.mobile_swipeRight(elementBy, index, durationInSeconds);
	}

	
	/**
	 * swipe left on the screen based on element position
	 */
	public void mobile_swipeLeft(WebElement element, int index, double durationInSeconds) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		mobile.mobile_swipeLeft(elementBy, index, durationInSeconds);
	}

	/**
	 * swipe up on the screen based on element position
	 */
	public void mobile_swipeUp(WebElement element, int index, double durationInSeconds) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		mobile.mobile_swipeUp(elementBy, index, durationInSeconds);
	}

	

	/**
	 * swipe down on the screen based on element position
	 */
	public void mobile_swipeDown(WebElement element, int index, double durationInSeconds) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		mobile.mobile_swipeDown(elementBy, index, durationInSeconds);
	}

	/**
	 * press per retry presses the target element
	 * 
	 * @param target
	 * @param index
	 * @param miliSeconds
	 * @param expected
	 */
	public static void mobile_longPress(WebElement target, long miliSeconds) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		mobile.mobile_longPress(targetBy, miliSeconds);
	}

	/**
	 * long press And expect element
	 * 
	 * @param target
	 * @param miliSeconds
	 * @param expected
	 */
	public static void mobile_longPressAndExpect(WebElement target, long miliSeconds, WebElement expected) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		EnhancedBy expectedBy = DriverLegacy.getEnhancedElement(expected);
		mobile.mobile_longPressAndExpect(targetBy, miliSeconds, expectedBy);
	}

	/**
	 * zooms out And checks if target level indicator has been reached
	 * 
	 * @param zoomLevel
	 * @param indicator
	 */
	public static void mobile_zoomOut(WebElement indicator) {
		EnhancedBy indicatorBy = DriverLegacy.getEnhancedElement(indicator);
		mobile.mobile_zoomOut(indicatorBy);
	}

	/**
	 * zooms in And checks if target level indicator has been reached
	 * 
	 * @param zoomLevel
	 * @param indicator
	 */
	public static void mobile_zoomIn(WebElement indicator) {
		EnhancedBy indicatorBy = DriverLegacy.getEnhancedElement(indicator);
		mobile.mobile_zoomIn(indicatorBy);
	}


	/**
	 * scrolls to mobile element until element is becomes visible
	 * 
	 * @param target
	 */
	public static void mobile_scrollToElement(WebElement target) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		mobile.mobile_scrollToElement(targetBy);
	}

	// PageHelper


	/**
	 * switches frame to frame specified
	 * 
	 * @param frame
	 */
	public static void switchIframe(WebElement frame) {
		EnhancedBy frameBy = DriverLegacy.getEnhancedElement(frame);
		page.switchIframe(frameBy);
	}

	

	/**
	 * returns true if the element is visible in the current page only for web
	 * applications
	 * 
	 * @param by
	 * @return
	 */
	public static Boolean isVisibleInViewport(WebElement element, int index) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		return page.isVisibleInViewport(elementBy, index);
	}

	/**
	 * scroll to web element
	 * @param element
	 */
	public static void scrollToWebElement(WebElement element) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		page.scrollToWebElement(elementBy);
	}

	
	/**
	 * scroll to element
	 * @param element
	 * @param index
	 */
	public static void scrollToWebElement(WebElement element, int index) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		page.scrollToWebElement(elementBy, index);
	}
	

	/**
	 * returns true if the element is visible in the current page only for web
	 * applications
	 * 
	 * @param by
	 * @return
	 */
	public static Boolean isVisibleInViewport(WebElement element) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		return page.isVisibleInViewport(elementBy);
	}

	/**
	 * gets specified attribute of the element
	 * 
	 * @param byValue
	 * @param index
	 * @param attribute
	 */
	public static String getAttribute(WebElement byValue, String attribute) {
		EnhancedBy byValueBy = DriverLegacy.getEnhancedElement(byValue);
		Helper.wait.waitForElementToLoad(byValueBy);
		return ElementHelper.getAttribute(byValueBy, attribute);
	}

	/**
	 * returns true if element contains class value
	 * 
	 * @param by
	 * @param classValue
	 * @return
	 */
	public static boolean isElementContainingClass(WebElement element, String classValue) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		return ElementHelper.isElementContainingClass(elementBy, classValue);
	}

	/**
	 * returns true if element contains attribute value
	 * 
	 * @param by
	 * @param classValue
	 * @return
	 */
	public static boolean isAttributeContaining(WebElement element, String attribute, String value) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		return ElementHelper.isAttributeContaining(elementBy, attribute, value);
	}

	/**
	 * gets specified attribute of the element based on index
	 * 
	 * @param byValue
	 * @param index
	 * @param attribute
	 */
	public static String getAttribute(WebElement element, int index, String attribute) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		return ElementHelper.getAttribute(elementBy, index, attribute);
	}

	/**
	 * sets attribute value of an element
	 * 
	 * @param by
	 * @param attribute
	 * @param value
	 */
	public static void setAttribute(WebElement element, String attribute, String value) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		ElementHelper.setAttribute(elementBy, attribute, value);
	}

	/**
	 * sets attribute value of an element
	 * 
	 * @param by
	 * @param index
	 * @param attribute
	 * @param value
	 */
	public static void setAttribute(WebElement element, int index, String attribute, String value) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		ElementHelper.setAttribute(elementBy, index, attribute, value);
	}

	/**
	 * returns element dimension
	 * 
	 * @param by
	 * @return
	 */
	public static Dimension getElementSize(WebElement element) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		return ElementHelper.getElementSize(elementBy);
	}

	/**
	 * get element position on display
	 * 
	 * @param by
	 * @return
	 */
	public static Point getElementPosition(WebElement element) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		return ElementHelper.getElementPosition(elementBy);
	}

	/**
	 * get element position on display
	 * 
	 * @param by
	 * @param index
	 * @return
	 */
	public static Point getElementPosition(WebElement element, int index) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		return ElementHelper.getElementPosition(elementBy, index);
	}

	/**
	 * returns the center coordinates of the target element
	 * 
	 * @param target
	 * @return
	 */
	public static int[] findElementCoordinates(WebElement target) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return ElementHelper.findMiddleOfElement(targetBy);
	}

	/**
	 * returns the center coordinates of the target element
	 * 
	 * @param target
	 * @return
	 */
	public static int[] findMiddleOfElement(WebElement target) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return ElementHelper.findMiddleOfElement(targetBy);
	}

	// WaitHelper

	/**
	 * waits for element to be displayed for amount of time specified by 60 seconds
	 * 
	 * @param target
	 */
	public static void waitForElementToLoad(final WebElement target) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		wait.waitForElementToLoad(targetBy);
	}
	


	/**
	 * waits for element to load count() checks if the element is displayed Then
	 * gets the count number
	 * 
	 * @param target
	 * @param time
	 */
	public static boolean waitForElementToLoad(final WebElement target, int time) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return wait.waitForElementToLoad(targetBy, time);
	}

	/**
	 * waits for either element to load returns true When first item loads
	 * 
	 * @param element1
	 * @param element2
	 * @param time
	 * @return
	 */
	public static boolean waitForFirstElementToLoad(final WebElement element1, final WebElement element2,
			final WebElement element3) {
		EnhancedBy element1By = DriverLegacy.getEnhancedElement(element1);
		EnhancedBy element2By = DriverLegacy.getEnhancedElement(element2);
		EnhancedBy element3By = DriverLegacy.getEnhancedElement(element3);
		return wait.waitForFirstElementToLoad(element1By, element2By, element3By);
	}

	/**
	 * waits for either element to load returns true When first item loads
	 * 
	 * @param element1
	 * @param element2
	 * @param time
	 * @return
	 */
	public static boolean waitForFirstElementToLoad(final WebElement element1, final WebElement element2,
			final WebElement element3, int time) {
		EnhancedBy element1By = DriverLegacy.getEnhancedElement(element1);
		EnhancedBy element2By = DriverLegacy.getEnhancedElement(element2);
		EnhancedBy element3By = DriverLegacy.getEnhancedElement(element3);
		return wait.waitForFirstElementToLoad(element1By, element2By, element3By, time);
	}

	/**
	 * waits for either element to load returns true When first item loads
	 * 
	 * @param element1
	 * @param element2
	 * @param time
	 * @return
	 */
	public static boolean waitForFirstElementToLoad(final WebElement element1, final WebElement element2, int time) {
		EnhancedBy element1By = DriverLegacy.getEnhancedElement(element1);
		EnhancedBy element2By = DriverLegacy.getEnhancedElement(element2);
		return wait.waitForFirstElementToLoad(element1By, element2By, time);
	}

	/**
	 * waits for either element to load returns true When first item loads
	 * 
	 * @param element1
	 * @param element2
	 * @param time
	 * @return
	 */
	public static boolean waitForFirstElementToLoad(final WebElement element1, final WebElement element2) {
		EnhancedBy element1By = DriverLegacy.getEnhancedElement(element1);
		EnhancedBy element2By = DriverLegacy.getEnhancedElement(element2);
		return wait.waitForFirstElementToLoad(element1By, element2By);
	}

	/**
	 * waits for element to load And refreshes the app each time to renew the dom
	 * 
	 * @param target
	 */
	public static void mobile_waitAndRefreshForElementToLoad(final WebElement target, int time) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		wait.mobile_waitAndRefreshForElementToLoad(targetBy, time);
	}

	/**
	 * waits for element to load And refreshes the app each time to renew the dom
	 * 
	 * @param target
	 */
	public static void mobile_waitAndRefreshForElementToLoad(final WebElement target) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		wait.mobile_waitAndRefreshForElementToLoad(targetBy);
	}

	/**
	 * waits for element to load If mobile device, scrolls down the page until
	 * element is visible
	 * 
	 * @param target: element to wait for
	 * @param time:   max time to wait
	 * @param count:  minimum count of elements to wait for in list
	 * @return
	 */
	public static boolean waitForElementToLoad(final WebElement target, int time, int count) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return wait.waitForElementToLoad(targetBy, time, count);
	}

	/**
	 * waits for element count to increase from the originalCount Usefull When
	 * waiting for a list to expand with additional items
	 * 
	 * @param target
	 * @param originalCount
	 */
	public static void waitForAdditionalElementsToLoad(final WebElement target, final int originalCount) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		wait.waitForAdditionalElementsToLoad(targetBy, originalCount);
	}

	/**
	 * waits for element count to increase from the originalCount Usefull When
	 * waiting for a list to expand with additional items
	 * 
	 * @param target
	 * @param originalCount
	 */
	public static void waitForAdditionalElementsToLoad(final WebElement target, final int originalCount, int time) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		wait.waitForAdditionalElementsToLoad(targetBy, originalCount, time);
	}

	/**
	 * waits for element to not be displayed wait for maximum of 60 seconds
	 * 
	 * @param target
	 * @return 
	 */
	public static boolean waitForElementToBeRemoved(final WebElement target) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return wait.waitForElementToBeRemoved(targetBy);
	}

	/**
	 * waits for element to not be displayed
	 * 
	 * @param target
	 * @param time   : maximum amount of time in seconds to wait
	 */
	public static boolean waitForElementToBeRemoved(final WebElement target, int time) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return wait.waitForElementToBeRemoved(targetBy, time);
	}
	
	/**
	 * waits for element to not be displayed
	 * 
	 * @param target
	 * @param time   : maximum amount of time in seconds to wait. use AbstractDriver.TIMEOUT_SECONDS for default timeout
	 * @param waitForTargetToLoadInSeconds wait for element to load before waiting for element to be removed  
	 */
	public static boolean waitForElementToBeRemoved(final WebElement target, int time, int waitForTargetToLoadInSeconds) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return wait.waitForElementToBeRemoved(targetBy, time, waitForTargetToLoadInSeconds);
	}

	

	/**
	 * waits for item containing in list to load
	 * 
	 * @param list
	 * @param option
	 * @param time
	 */
	public static void waitForListItemToLoad_Contains(final WebElement list, String option) {
		EnhancedBy listBy = DriverLegacy.getEnhancedElement(list);
		wait.waitForListItemToLoad_Contains(listBy, option);
	}

	/**
	 * waits for text to be loaded for amount of time specified by 60 seconds
	 * 
	 * @param target
	 */
	public static void waitForTextToLoad(final WebElement target, String text) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		wait.waitForTextToLoad(targetBy, text);
	}

	/**
	 * make sure only one element And caller needs to take responsibility to have
	 * text in the element
	 * 
	 * @param target
	 * @param time
	 */
	public static void waitForTextToLoad(final WebElement target, int time, String text) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		wait.waitForTextToLoad(targetBy, text);
	}

	/**
	 * wait for element to become clickable causes instability with hybrid mobile
	 * app
	 * 
	 * @param selector
	 * @return
	 */
	public static boolean waitForElementToBeClickable(WebElement selector) {
		EnhancedBy selectorBy = DriverLegacy.getEnhancedElement(selector);
		return wait.waitForElementToBeClickable(selectorBy);
	}

	/**
	 * wait for element to become clickable causes instability with hybrid mobile
	 * app
	 * 
	 * @param selector
	 * @return
	 */
	public static boolean waitForElementToBeClickable(WebElement selector, int timeInSeconds) {
		EnhancedBy selectorBy = DriverLegacy.getEnhancedElement(selector);
		return wait.waitForElementToBeClickable(selectorBy, timeInSeconds);
	}

	/**
	 * wait for class to contain
	 * 
	 * @param target
	 * @param index
	 * @param value
	 * @return
	 */
	public boolean waitForClassContain(final WebElement target, int index, String value) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return wait.waitForClassContain(targetBy, index, value);
	}

	/**
	 * wait for class to contain value
	 * 
	 * @param target
	 * @param index
	 * @param value
	 * @param time
	 * @return
	 */
	public boolean waitForClassContain(final WebElement target, int index, String value, int time) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return wait.waitForClassContain(targetBy, index, value, time);
	}

	/**
	 * wait for any text strings to become available
	 * 
	 * @param target
	 * @param time
	 * @param text
	 * @return
	 */
	public static boolean waitForAnyTextToLoadContaining(final WebElement target, String... text) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return wait.waitForAnyTextToLoad(targetBy, text);
	}

	/**
	 * wait for any text strings to become available
	 * 
	 * @param target
	 * @param time
	 * @param text
	 * @return
	 */
	public static boolean waitForAnyTextToLoadContaining(final WebElement target, int time, String... text) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return wait.waitForAnyTextToLoad(targetBy, time, text);
	}

	/**
	 * wait for any text strings to become available
	 * 
	 * @param target
	 * @param time
	 * @param text
	 * @return
	 */
	public static boolean waitForAnyTextToLoad(final WebElement target, String... text) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return wait.waitForAnyTextToLoad(targetBy, text);
	}

	/**
	 * wait for any text strings to become available
	 * 
	 * @param target
	 * @param time
	 * @param text
	 * @return
	 */
	public static boolean waitForAnyTextToLoad(final WebElement target, int time, String... text) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		return wait.waitForAnyTextToLoad(targetBy, time, text);
	}

	// StopWatchHelper

	
	/**
	 * highlights the web element use on clicks and send key elements
	 * 
	 * @param by
	 * @param index
	 */
	public static void highLightWebElement(WebElement target, int index) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		UtilityHelper.highLightWebElement(targetBy, index);
	}

	// action helper

	/**
	 * hover over element
	 * 
	 * @param by
	 */
	public static void hoverBy(WebElement element) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		ElementActionHelper.hoverBy(elementBy, 0);
	}

	/**
	 * hover over element
	 * 
	 * @param by
	 */
	public static void hoverBy(WebElement element, int index) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		ElementActionHelper.hoverBy(elementBy, index);
	}

	/*
	 * Enter text to an element by action
	 */
	public void inputTextByAction(WebElement element, String text) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		ElementActionHelper.inputTextByAction(elementBy, text);
	}

	/*
	 * Double click an element
	 */
	public static void doubleClickBy(WebElement element) {
		EnhancedBy elementBy = DriverLegacy.getEnhancedElement(element);
		ElementActionHelper.doubleClickBy(elementBy);
	}


	/**
	 * move to element by using action
	 * 
	 * @param target
	 * @param index
	 */
	public static void moveToElement(WebElement target, int index) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		ElementActionHelper.moveToElement(targetBy, index);
	}

	/**
	 * move to element by using action
	 * 
	 * @param target
	 * @param index
	 */
	public static void moveToElement(WebElement target, int index, int xOffset, int yOffset) {
		EnhancedBy targetBy = DriverLegacy.getEnhancedElement(target);
		ElementActionHelper.moveToElement(targetBy, index, xOffset, yOffset);
	}

	//-------------------------------------------------------------------
	//EnhancedBy section
	//Enhanced Drivers
	//----------------------------------------------------------------------------------------

		// AssertHelper

		/**
		 * assert true
		 * 
		 * @param message if fail
		 * @param value
		 */
		public static void assertTrue(String message, boolean value) {
			AssertHelper.assertTrue(message, value);
		}

		public static void assertFalse(String message) {
			AssertHelper.assertFalse(message);
		}

		/**
		 * assert expected equals actual
		 * 
		 * @param expected
		 * @param actual
		 */
		public static void assertEquals(String expected, String actual) {
			AssertHelper.assertEquals(expected, actual);
		}

		/**
		 * assert expected equals actual
		 * 
		 * @param expected
		 * @param actual
		 */
		public static void assertEquals(boolean expected, boolean actual) {
			AssertHelper.assertEquals(expected, actual);
		}

		/**
		 * assert expected equals actual
		 * 
		 * @param expected
		 * @param actual
		 */
		public static void assertEquals(int expected, int actual) {
			AssertHelper.assertEquals(expected, actual);
		}

		/**
		 * assert actual contains expected
		 * 
		 * @param actual
		 * @param expected
		 */
		public static void assertContains(String expected, String actual) {
			AssertHelper.assertContains(expected, actual);
		}

		public static void softAssertTrue(String message, boolean value) {
			AssertHelper.softAssertTrue(message, value);
		}

		public static void softAssertEqual(String expected, String actual) {
			AssertHelper.softAssertEqual(expected, actual);
		}

		public static void softAssertEqual(int expected, int actual) {
			AssertHelper.softAssertEqual(expected, actual);
		}
		
		public static SoftAssert softAssert() {
			return AssertHelper.softAssert();
		}
		
		public static void softAssertAll() {
			AssertHelper.softAssertAll();
		}
		
		public static void logStackTrace(AssertionError e) {
			AssertHelper.logStackTrace(e);
		}
		
		public static void logStackTrace(Exception e) {
			AssertHelper.logStackTrace(e);
		}

		// Element Helper


		/**
		 * gets element by css value
		 * 
		 * @param element
		 * @param name
		 * @return
		 */
		public static EnhancedBy byCss(String element, String name) {
			return Element.byCss(element, name);
		}

		/**
		 * gets element by id
		 * 
		 * @param element
		 * @param name
		 * @return
		 */
		public static EnhancedBy byId(String element, String name) {
			return Element.byId(element, name);
		}

		/**
		 * gets element by xpath
		 * 
		 * @param element
		 * @param name
		 * @return
		 */
		public static EnhancedBy byXpath(String element, String name) {
			return Element.byXpath(element, name);
		}

		/**
		 * gets element by accessibility id
		 * 
		 * @param element
		 * @param name
		 * @return
		 */
		public static EnhancedBy byAccessibility(String element, String name) {
			return Element.byAccessibility(element, name);
		}

		// ClickHelper
		/**
		 * clicks target And waits for expected element to display retries 10 times
		 * 
		 * @param target
		 * @param expected
		 */
		public static void clickAndExpect(EnhancedBy target, EnhancedBy expected) {
			click.clickAndExpect(target, expected);
		}

		/**
		 * click And expect based on the text value on the element. eg. button with "OK"
		 * text
		 * 
		 * @param target
		 * @param text
		 * @param expected
		 */
		public static void clickAndExpect(EnhancedBy target, String text, EnhancedBy expected) {
			click.clickAndExpectByText(target, text, expected);
		}

		/**
		 * click And expect based on the text value on the element. eg. button with "OK"
		 * text
		 * 
		 * @param target
		 * @param text
		 * @param expected
		 */
		public void clickAndExpectContainsByText(EnhancedBy target, String text, EnhancedBy expected) {
			click.clickAndExpectContainsByText(target, text, expected);
		}

		/**
		 * clicks target And waits for expected element to display retries 10 times
		 * 
		 * @param target
		 * @param expected
		 */
		public static void clickAndExpect(EnhancedBy target, EnhancedBy expected, boolean isMobileRefresh) {
			click.clickAndExpect(target, expected, isMobileRefresh);
		}

		/**
		 * clicks target And wait for one the 2 expected elements to appear
		 * 
		 * @param target
		 * @param index
		 * @param expected1
		 * @param expected2
		 */
		public static void clickAndExpect(EnhancedBy target, int index, EnhancedBy expected1, EnhancedBy expected2) {
			click.clickAndExpect(target, index, expected1, expected2);
		}

		/**
		 * clicks element based on index And waits for expected element to be displayed
		 * 
		 * @param target
		 * @param index
		 * @param expected
		 */
		public static void clickAndExpect(EnhancedBy target, int index, EnhancedBy expected) {
			click.clickAndExpect(target, index, expected, true);
		}

		/**
		 * clicks target And waits for expected element to show up also waits for
		 * spinner element to be removed from display
		 * 
		 * @param target
		 * @param expected
		 * @param spinner
		 */
		public static void clickAndExpect(EnhancedBy target, EnhancedBy expected, EnhancedBy spinner) {
			click.clickAndExpect(target, expected, spinner);
		}

		/**
		 * clicks target And waits for expected to not be displayed retries 10 times
		 * 
		 * @param target
		 * @param expected
		 */
		public static void clickAndNotExpect(EnhancedBy target, EnhancedBy expected) {
			click.clickAndNotExpect(target, expected);
		}

		/**
		 * clicks target And waits for seconds
		 * 
		 * @param target
		 * @param expected
		 */
		public static void clickAndWait(EnhancedBy target, double timeInSeconds) {
			click.clickAndWait(target, timeInSeconds);
		}

		/**
		 * clicks target And waits for seconds
		 * 
		 * @param target
		 * @param expected
		 */
		public static void clickAndWait(EnhancedBy target, int index, double timeInSeconds) {
			click.clickAndWait(target, index, timeInSeconds);
		}

		public static void clickAndNotExpect(EnhancedBy target, int index, EnhancedBy expected) {
			click.clickAndNotExpect(target, index, expected);
		}

		/**
		 * Click on an element's specific x,y location
		 * 
		 * @param by by element
		 * @param x  x offset coordinate
		 * @param y  y offset coordinate
		 */
		public static void clickElementLocationBy(EnhancedBy by, int x, int y) {
			click.clickElementLocationBy(by, x, y);
		}

		/**
		 * click at position x, y
		 * 
		 * @param x
		 * @param y
		 */
		public static void clickPoints(int x, int y) {
			click.clickPoints(x, y);
		}

		/**
		 * click point at x,y coordinates and expect and element to be present retry
		 * every 5 seconds for duration of explicit timeout
		 * 
		 * @param x
		 * @param y
		 * @param expected
		 */
		public static void clickPointsAndExpect(int x, int y, EnhancedBy expected) {
			click.clickPointsAndExpect(x, y, expected);
		}

		/**
		 * double click at position
		 * 
		 * @param x
		 * @param y
		 */
		public static void doubleClickPoints(int x, int y) {
			click.doubleClickPoints(x, y);
		}

		/**
		 * double click on element
		 * 
		 * @param target
		 * @param index
		 */
		public static void doubleClick(EnhancedBy target, int index) {
			clickAction.doubleClick(target, index);
		}

		/**
		 * right click on element at index
		 * 
		 * @param target
		 * @param index
		 */
		public static void rightClick(EnhancedBy target, int index) {
			clickAction.rightClick(target, index);
		}

		/**
		 * click element with text containing
		 * 
		 * @param by
		 * @param text
		 */
		public static void clickElementContinsByText(EnhancedBy by, String text) {
			click.clickElementContinsByText(by, text);
		}

		/**
		 * click And hold element
		 * 
		 * @param target
		 * @param seconds
		 */
		public static void clickAndHold(EnhancedBy target, double seconds) {
			click.clickAndHold(target, seconds);
		}

		/**
		 * click And hold based on element index
		 * 
		 * @param target
		 * @param index
		 * @param seconds
		 */
		public static void clickAndHold(EnhancedBy target, int index, double seconds) {
			click.clickAndHold(target, index, seconds);
		}

		/**
		 * drag And drop from src element to target element
		 * 
		 * @param src
		 * @param target
		 */
		public static void dragAndDrop(EnhancedBy src, EnhancedBy target) {
			click.dragAndDrop(src, target);
		}
		
		/**
		 * drag And drop from src element to target element
		 * 
		 * @param src
		 * @param target
		 */
		public void dragAndDrop(EnhancedBy srcParent, int srcParentIndex, EnhancedBy srcChild, int scrChildIndex, EnhancedBy targetParent, int targeParenttIndex, EnhancedBy targetChild, int targetChildIndex) {
			click.dragAndDrop(srcParent, srcParentIndex, srcChild, scrChildIndex, targetParent, targeParenttIndex, targetChild, targetChildIndex);
		}

		/**
		 * drag And drop from src element to target element
		 * 
		 * @param src
		 * @param target
		 */
		public static void dragAndDrop(EnhancedBy src, int xOffset, int yOffset) {
			click.dragAndDrop(src, xOffset, yOffset);
		}

		// VerifyHelper
		/**
		 * verifies if element(s) is (are) displayed
		 * 
		 * @param by
		 */
		public static void verifyElementIsDisplayed(EnhancedBy by) {
			verify.verifyElementIsDisplayed(by);
		}

		/**
		 * returns true if element is displayed
		 * 
		 * @param element
		 * @return
		 */
		public static boolean isDisplayed(EnhancedBy element) {
			return verify.isPresent(element);
		}

		/**
		 * returns true if element is displayed
		 * 
		 * @param element
		 * @return
		 */
		public static boolean isPresent(EnhancedBy element) {
			return verify.isPresent(element);
		}

		/**
		 * returns true if element contains text
		 * 
		 * @param element
		 * @param text
		 * @return
		 */
		public static boolean isElementContainingText(EnhancedBy element, String text) {
			return verify.isElementContainingText(element, text);
		}

		/**
		 * verify if element contains text
		 * 
		 * @param element
		 * @param text
		 */
		public static void verifyElementContainingText(EnhancedBy element, String text) {
			verify.verifyElementContainingText(element, text);
		}

		/**
		 * verify if text is displayed on page
		 * 
		 * @param text
		 */
		public static void verifyTextDisplayed(String text) {
			verify.verifyTextDisplayed(text);
		}

		/**
		 * is text displayed on page
		 * 
		 * @param text
		 * @return
		 */
		public static boolean isTextDisplayed(String text) {
			return verify.isTextDisplayed(text);
		}
		
		/**
		 * verify tool tip is displayed
		 * @param tooltip
		 * @param index
		 * @param text
		 */
		public void verifyToolTip(EnhancedBy tooltip, int index, String text) {
			verify.verifyToolTip(tooltip, index, text);
		}

		/**
		 * verifies if element(s) is (are) not displayed
		 * 
		 * @param by
		 */
		public static void verifyElementIsNotDisplayed(EnhancedBy by) {
			verify.verifyElementIsNotDisplayed(by);
		}

		/**
		 * verifies element text equals provided value
		 * 
		 * @param by
		 * @param value
		 */
		public static void verifyElementText(EnhancedBy by, String value) {
			verify.verifyElementText(by, value);
		}

		/**
		 * verifies element count
		 * 
		 * @param by
		 * @param value
		 */
		public static void verifyElementCount(EnhancedBy by, int value, int... correction) {
			verify.verifyElementCount(by, value, correction);
		}

		/**
		 * verifies if text contains any of values in list
		 * 
		 * @param target
		 * @param values
		 */
		public static void verifyAnyTextContaining(EnhancedBy target, String... values) {
			verify.verifyAnyTextContaining(target, values);
		}

		/**
		 * verifies if text contains any of values in list
		 * 
		 * @param target
		 * @param values
		 */
		public static void verifyAnyText(EnhancedBy target, String... values) {
			verify.verifyAnyText(target, values);
		}

		// FormHelper Class

		/**
		 * sets field text value by index hides keyboard if on ios device
		 * 
		 * @param value
		 * @param field
		 * @param index
		 */
		public static void setField(String value, EnhancedBy field, int index) {
			form.setField(field, index, value);
		}

		/**
		 * set field value if value is not empty
		 * 
		 * @param field
		 * @param value
		 */
		public static void setField(EnhancedBy field, CharSequence... value) {
			form.setField(field, value);
		}

		public static void setFieldByAction(EnhancedBy field, int index, CharSequence... value) {
			form.setFieldByAction(field, index, value);
		}

		public static void setFieldByAction(EnhancedBy field, CharSequence... value) {
			form.setFieldByAction(field, 0, value);
		}

		public static void setFieldByJs(EnhancedBy field, int index, CharSequence... value) {
			form.setFieldByJs(field, index, value);
		}

		public static void setFieldByJs(EnhancedBy field, CharSequence... value) {
			form.setFieldByJs(field, 0, value);
		}

		/**
		 * sets field clears field before setting the value
		 * 
		 * @param field
		 * @param value
		 */
		public static void clearAndSetField(EnhancedBy field, CharSequence... value) {
			form.clearAndSetField(field, value);
		}

		/**
		 * sets field clears field before setting the value
		 * 
		 * @param field
		 * @param index
		 * @param value
		 */
		public static void clearAndSetField(EnhancedBy field, int index, CharSequence... value) {
			form.clearAndSetField(field, index, value);
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
		public static void setChildField(EnhancedBy parent, int parentIndex, EnhancedBy child, int childIndex,
				CharSequence... value) {
			form.setKeyChildField(parent, parentIndex, child, childIndex, value);
		}

		/**
		 * sets field And presses the enter key
		 * 
		 * @param field
		 * @param value
		 */
		public static void setFieldAndEnter(EnhancedBy field, CharSequence... value) {
			form.setFieldAndEnter(field, value);
		}

		/**
		 * send
		 * 
		 * @param key
		 * @param field
		 */
		public static void setKey(Keys key, EnhancedBy field) {
			form.setKey(key, field);
		}

		/**
		 * select submit button And wait for expected element to load
		 * 
		 * @param button
		 * @param expected
		 * 
		 */
		public static void formSubmit(EnhancedBy button, EnhancedBy expected) {
			form.formSubmit(button, expected);
		}

		/**
		 * clicks submit button, wait for element to appear And loading spinner to be
		 * removed
		 * 
		 * @param button
		 * @param expected
		 * @param spinner
		 */
		public static void formSubmit(EnhancedBy button, EnhancedBy expected, EnhancedBy spinner) {
			form.formSubmit(button, expected, spinner);
		}

		/**
		 * selects dropdown by double clicking on the field
		 * 
		 * @param option
		 * @param field
		 * @param list
		 */
		public static void selectDropDownWithDoubleClick(String option, EnhancedBy field, EnhancedBy listValue) {
			form.selectDropDownWithDoubleClick(option, field, listValue);
		}

		/**
		 * selects dropdown by double clicking on the field
		 * 
		 * @param option
		 * @param field
		 * @param list
		 */
		public static void selectDropDownWithDoubleClick(String option, EnhancedBy field, int index, EnhancedBy listValue) {
			form.selectDropDownWithDoubleClick(option, field, index, listValue);
		}
		
		
		/**
		 * selects drop down
		 * 
		 * @param option : list option we want to select
		 * @param field  : the drop down field
		 * @param list   : the list items in the drop down list
		 */
		public static void selectDropDown(EnhancedBy field, EnhancedBy list, String... options) {
			form.selectDropDown(field, list, options);
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
		public static void selectDropDown(String option, EnhancedBy field, EnhancedBy listValue) {
			form.selectDropDown(option, field, listValue);
		}

		/**
		 * selects drop down from list defined by another list defined by text
		 * 
		 * @param option
		 * @param field
		 * @param field_Identifier
		 * @param list
		 */
		public static void selectDropDown(String option, EnhancedBy field, String field_Identifier, EnhancedBy listValue) {
			form.selectDropDown(option, field, field_Identifier, listValue);
		}

		/**
		 * select drop down by index from the drop down list
		 * 
		 * @param index
		 * @param field
		 * @param list
		 */
		public static void selectDropDown(int index, EnhancedBy field, EnhancedBy listValue) {
			form.selectDropDown(index, field, listValue);
		}

		/**
		 * select drop down using by value of the list item
		 * 
		 * @param index
		 * @param field
		 * @param list
		 */
		public static void selectDropDown(EnhancedBy field, EnhancedBy item) {
			form.selectDropDown(field, item);
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
		public static void selectDropDown(String option, EnhancedBy field, EnhancedBy listValue, int listIndex) {
			form.selectDropDown(option, field, listValue, listIndex);
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
		public static void selectDropDown(String option, EnhancedBy field, int index, EnhancedBy list, int listIndex) {
			form.selectDropDown(option, field, index, list, listIndex);
		}

		/**
		 * selects drop down based on index of the drop down field
		 * 
		 * @param option
		 * @param field
		 * @param index
		 * @param list
		 */
		public static void selectDropDown(String option, EnhancedBy field, int index, EnhancedBy listValue) {
			form.selectDropDown(option, field, index, listValue);
		}

		/**
		 * selects checkbox based on by value
		 * 
		 * @param button
		 * @param isSelect
		 */
		public static void selectCheckBox(EnhancedBy button, boolean isSelect) {
			form.selectCheckBox(button, isSelect);
		}

		/**
		 * selects radio button by radio button description
		 * 
		 * @param option
		 * @param buttons
		 */
		public static void selectRadioButton(String option, EnhancedBy buttons) {
			form.selectRadioButton(option, buttons);
		}

		/**
		 * selects a button
		 * 
		 * @param button
		 */
		public static void selectRadioButton(EnhancedBy button) {
			form.selectRadioButton(button);
		}

		/**
		 * selects multiple checkbox options
		 * 
		 * @param selections
		 * @param checkboxes
		 */
		public static void selectMultipleCheckboxOptions(List<String> selections, EnhancedBy checkboxes) {
			form.selectMultipleCheckboxOptions(selections, checkboxes);
		}

		// uploadHelper
		/**
		 * uploads file by specifying file location relative to main path
		 * 
		 * @param location
		 * @param imageButton
		 */
		public static void uploadFile(String location, EnhancedBy imageButton) {
			form.uploadFile(location, imageButton);
		}

		/**
		 * sets the image based on location
		 * 
		 * @param location
		 * @param imageButton
		 * @param images      : uploaded image
		 */
		public static void uploadImages(List<String> locations, EnhancedBy imageButton, EnhancedBy images) {
			form.uploadImages(locations, imageButton, images);
		}

		/**
		 * sets the image based on location
		 * 
		 * @param location
		 * @param imageButton
		 * @param images      : uploaded image
		 */
		public static void uploadImage(String location, EnhancedBy imageButton, EnhancedBy images) {
			form.uploadImage(location, imageButton, images);
		}

		/**
		 * gets the text value from an element
		 * 
		 * @param element
		 * @return
		 */
		public static String getTextValue(EnhancedBy element) {
			return form.getTextValue(element);
		}

		/**
		 * gets the text value from an element
		 * 
		 * @param element
		 * @return
		 */
		public static String getTextValue(EnhancedBy element, int index) {
			return form.getTextValue(element, index);
		}
		
		/**
		 * attempts to set text value on element, if not successful, then element is not editable
		 * @param element
		 * @param index
		 * @return
		 */
		public boolean isElementEditable(EnhancedBy element) {
			return form.isElementEditable(element);
		}
		
		/**
		 * attempts to set text value on element, if not successful, then element is not editable
		 * @param element
		 * @param index
		 * @return
		 */
		public boolean isElementEditable(EnhancedBy element, int index) {
			return form.isElementEditable(element, index);
		}

		// ListHelper

		/**
		 * selects an element in list by its index value
		 * 
		 * @param list
		 * @param index
		 */
		public static void selectElementInList(EnhancedBy listValue, int index) {
			list.selectElementInList(listValue, index);
		}

		/**
		 * selects an element in list by its index value And waits for expected element
		 * 
		 * @param list
		 * @param index
		 * @param expected
		 */
		public static void selectElementInList(EnhancedBy listValue, int index, EnhancedBy expected) {
			list.selectElementInList(listValue, index);
		}

		/**
		 * enters value into the search field And selects enter waits for the loading
		 * spinner to be removed
		 * 
		 * @param searchQuery
		 * @param byTarget
		 * @param spinner
		 */
		public static void searchAndWaitForResults(String searchQuery, EnhancedBy byTarget, EnhancedBy spinner) {
			list.searchAndWaitForResults(searchQuery, byTarget, spinner);
		}

		/**
		 * selects list item by the string option provided
		 * 
		 * @param list
		 * @param option
		 */
		public static void selectListItemEqualsByName(EnhancedBy listValue, String option) {
			list.selectListItemEqualsByName(listValue, option);
		}

		/**
		 * finds target element which is in the same container And has the same index as
		 * the parent eg. delete button in the list of customers, both having index 2.
		 * we find the index by name, And use that to find the target element
		 * 
		 * @param list
		 * @param option
		 * @param target
		 */
		public static void selectListItemEqualsByName(EnhancedBy listValue, String option, EnhancedBy target) {
			list.selectListItemEqualsByName(listValue, option, target);
		}

		/**
		 * selects list item containing string eg. a list of athletes names containing a
		 * delete button
		 * 
		 * @param list
		 * @param option
		 * @param target
		 */
		public static void selectListItemContainsByName(EnhancedBy listValue, String option, EnhancedBy target) {
			list.selectListItemContainsByName(listValue, option, target);
		}

		/**
		 * find the index of the target element in list eg. list A, 5 rows, has element
		 * B in row 2. therefore, index 1 is returned @param list
		 * 
		 * @param list
		 * @param target
		 * @return index of element in list
		 */
		public static int getElementIndexInList(EnhancedBy srcList, EnhancedBy target) {
			return list.getElementIndexInList(srcList, target);
		}

		/**
		 * returns the list of string values for a row of elements
		 * 
		 * @param list
		 * @param index
		 * @param rows
		 * @return
		 */
		public List<String> getRowValuesFromList(EnhancedBy srclist, int index, EnhancedBy rows) {
			return list.getRowValuesFromList(srclist, index, rows);
		}

		/**
		 * gets hashmap representation of data column with row values
		 * 
		 * @param columns
		 * @param dataRows
		 * @return
		 */
		public HashMap<String, List<String>> getTableMap(EnhancedBy columns, EnhancedBy dataRows, EnhancedBy dataCells) {
			return list.getTableMap(columns, dataRows, dataCells);
		}

		/**
		 * gets hashmap representation of data column with row values
		 * 
		 * @param columns
		 * @param dataRows
		 * @param dataCells
		 * @param maxRows
		 * @return
		 */
		public HashMap<String, List<String>> getTableMap(EnhancedBy columns, EnhancedBy dataRows, EnhancedBy dataCells,
				int maxRows) {
			return list.getTableMap(columns, dataRows, dataCells, maxRows);
		}

		/**
		 * gets hashmap representation of data column with row values
		 * 
		 * @param columns
		 * @param columnInitialIndex
		 * @param dataRows
		 * @param rowInitialIndex
		 * @param dataCells
		 * @param maxRows
		 * @return
		 */
		public HashMap<String, List<String>> getTableMap(EnhancedBy columns, int columnInitialIndex, EnhancedBy dataRows,
				int rowInitialIndex, EnhancedBy dataCells, int maxRows) {
			return list.getTableMap(columns, columnInitialIndex, dataRows, rowInitialIndex, dataCells, maxRows);
		}
		
		/**
		 * Selects list item from a parent container eg. delete button in a list defined
		 * by name find the container containing the name And Then finds the delete
		 * button in that container as target
		 * 
		 * @param list
		 * @param option
		 * @param target
		 */
		public static void selectElementContainedInList(EnhancedBy listValue, String option, EnhancedBy target, int targetIndex) {
			list.selectElementContainedInList(listValue, option, target, targetIndex);
		}

		/**
		 * Selects list item from a parent container eg. delete button in a list defined
		 * by name find the container containing the name And Then finds the delete
		 * button in that container as target
		 * 
		 * @param list
		 * @param option
		 * @param target
		 */
		public static void selectElementContainedInList(EnhancedBy listValue, String option, EnhancedBy target) {
			list.selectElementContainedInList(listValue, option, target);
		}

		/**
		 * finds target element which is in the same container And has the same index as
		 * the parent eg. delete button in the list of customers, both having index 2.
		 * we find the index containing name, And use that to find the target element
		 * 
		 * @param list
		 * @param option
		 */
		public static void selectListItemContainsByName(EnhancedBy listValue, String option) {
			list.selectListItemContainsByName(listValue, option);
		}

		/**
		 * selects list item by the string option provided
		 * 
		 * @param list
		 * @param option
		 */
		public static void selectListItemByIndex(EnhancedBy listValue, int index) {
			;
			list.selectListItemByIndex(listValue, index);
		}

		/**
		 * returns the number of elements in list
		 * 
		 * @param list
		 * @return
		 */
		public static int getListCount(EnhancedBy listValue) {
			return list.getListCount(listValue);
		}

		/**
		 * returns the index of text value in a list
		 * 
		 * @param list
		 * @param option
		 * @return
		 */
		public static int getElementIndexEqualsByText(EnhancedBy listValue, String option) {
			return list.getElementIndexEqualsByText(listValue, option);
		}

		/**
		 * retuns index of element in list which contains in text
		 * 
		 * @param list
		 * @param option
		 * @return
		 */
		public static int getElementIndexContainByText(EnhancedBy listValue, String option) {
			return list.getElementIndexContainByText(listValue, option);
		}


		/**
		 * returns the index of string value in list of strings
		 * 
		 * @param stringList normalized
		 * @param option     normalized
		 * @return
		 */
		public static int getStringIndexEqualsByText(List<String> stringList, String option) {
			return list.getStringIndexEqualsByText(stringList, option);

		}

		/**
		 * verifies if option value is in the list index = -1 indicates the value is not
		 * in list
		 * 
		 * @param list
		 * @param option
		 */
		public static void verifyContainsIsInList(EnhancedBy listValue, String option) {
			list.verifyContainsIsInList(listValue, option);
		}

		/**
		 * verifies if option value is in the list index = -1 indicates the value is not
		 * in list
		 * 
		 * @param list
		 * @param option
		 */
		public static void verifyIsInList(EnhancedBy listValue, String option) {
			list.verifyIsInList(listValue, option);
		}

		/**
		 * verify text option in list based on key value in the list
		 * 
		 * @param list
		 * @param indicator
		 * @param option
		 */
		public static void verifyIsInList(EnhancedBy listValue, String indicator, String option) {
			list.verifyIsInList(listValue, indicator, option);
		}

		/**
		 * return if element is contained in list
		 * 
		 * @param list
		 * @param option
		 * @return
		 */
		public static boolean isContainedInList(EnhancedBy listValue, String option) {
			return list.isContainedInList(listValue, option);
		}

		/**
		 * return if element is an exact match in list
		 * 
		 * @param list
		 * @param option
		 * @return
		 */
		public static boolean isExactMatchInList(EnhancedBy listValue, String option) {
			return list.isExactMatchInList(listValue, option);
		}

		/**
		 * returns the list of values in a list
		 * 
		 * @param list
		 * @return
		 */
		public static List<String> getListValues(EnhancedBy listValue) {
			return list.getListValues(listValue);
		}

		/**
		 * returns the list of values in a list
		 * 
		 * @param list
		 * @return
		 */
		public static List<String> getTextList(EnhancedBy listValue) {
			return list.getTextList(listValue);
		}

		// MobileHelper

		@SuppressWarnings("rawtypes")
		public static AndroidDriver getAndroidDriver() {
			return mobile.getAndroidDriver();
		}

		@SuppressWarnings("rawtypes")
		public static IOSDriver getiOSDriver() {
			return mobile.getiOSDriver();
		}

		/**
		 * hides ios or android keyboard
		 * 
		 */
		public static void mobile_hideKeyboard() {
			mobile.hideKeyboard();
		}

		/**
		 * select enter on android
		 */
		public static void pressEnterOnAndroid() {
			mobile.pressEnterOnAndroid();
		}

		/**
		 * resets the app
		 */
		public static void mobile_resetApp() {
			mobile.resetApp();
		}

		/**
		 * places the app in background And Then relaunches it
		 */
		public static void refreshMobileApp() {
			mobile.refreshMobileApp();
		}

		/**
		 * returns if mobile driver is used
		 * 
		 * @return
		 */
		public static boolean mobile_isMobile() {
			return mobile.isMobile();
		}

		/**
		 * returns true if browser is ie
		 * 
		 * @return
		 */
		public static boolean isInternetExplorer() {
			return mobile.isIeExplorer();
		}

		/**
		 * is ios driver
		 * 
		 * @return
		 */
		public static boolean isIOS() {
			return mobile.isIOS();
		}

		/**
		 * is android driver
		 * 
		 * @return
		 */
		public static boolean isAndroid() {
			return mobile.isAndroid();

		}

		/**
		 * is web driver
		 * 
		 * @return
		 */
		public static boolean isWebDriver() {
			return mobile.isWebDriver();
		}

		/**
		 * sets gps location on ios simulator
		 * 
		 * @param location
		 */
		public static void mobile_setLocation(Location location) {
			mobile.setLocation(location);
		}

		/**
		 * swipe right on the screen
		 */
		public void mobile_swipeRight(double durationInSeconds) {
			mobile.mobile_swipeRight(durationInSeconds);
		}

		/**
		 * swipe right on the screen based on element position
		 */
		public void mobile_swipeRight(EnhancedBy element, int index, double durationInSeconds) {
			mobile.mobile_swipeRight(element, index, durationInSeconds);
		}

		/**
		 * swipe right on the screen
		 */
		public void mobile_swipeLeft(double durationInSeconds) {
			mobile.mobile_swipeLeft(durationInSeconds);
		}

		/**
		 * swipe left on the screen based on element position
		 */
		public void mobile_swipeLeft(EnhancedBy element, int index, double durationInSeconds) {
			mobile.mobile_swipeLeft(element, index, durationInSeconds);
		}

		/**
		 * swipe up on the screen based on element position
		 */
		public void mobile_swipeUp(EnhancedBy element, int index, double durationInSeconds) {
			mobile.mobile_swipeUp(element, index, durationInSeconds);
		}

		/**
		 * swipe up on the screen
		 */
		public void mobile_swipeUp(double durationInSeconds) {
			mobile.mobile_swipeUp(durationInSeconds);
		}

		/**
		 * swipe down on the screen
		 */
		public void mobile_swipeDown(double durationInSeconds) {
			mobile.mobile_swipeDown(durationInSeconds);
		}

		/**
		 * swipe down on the screen based on element position
		 */
		public void mobile_swipeDown(EnhancedBy element, int index, double durationInSeconds) {
			mobile.mobile_swipeDown(element, index, durationInSeconds);
		}

		/**
		 * sets native context for android And ios apps
		 * 
		 * @param context
		 */
		public static void mobile_switchToNativeView() {
			mobile.mobile_switchToNativeView();
		}

		/**
		 * sets web context for android And ios apps
		 * 
		 * @param context
		 */
		public static void mobile_switchToWebView() {
			mobile.mobile_switchToWebView();

		}

		/**
		 * switch to view
		 * 
		 * @param view
		 */
		public void mobile_switchToView(String view) {
			mobile.mobile_switchToView(view);
		}

		/**
		 * returns the list of mobile context. eg. webview, native view
		 * 
		 * @return
		 */
		public Set<String> mobile_getContextList() {
			return mobile.mobile_getContextList();
		}

		/**
		 * press per retry presses the target element
		 * 
		 * @param target
		 * @param index
		 * @param miliSeconds
		 * @param expected
		 */
		public static void mobile_longPress(EnhancedBy target, long miliSeconds) {
			mobile.mobile_longPress(target, miliSeconds);
		}

		/**
		 * long press And expect element
		 * 
		 * @param target
		 * @param miliSeconds
		 * @param expected
		 */
		public static void mobile_longPressAndExpect(EnhancedBy target, long miliSeconds, EnhancedBy expected) {
			mobile.mobile_longPressAndExpect(target, miliSeconds, expected);
		}

		/**
		 * zoom in based on zoom levels. eg. 3 equals zooming out 3 times
		 * 
		 * @param zoomLevel
		 */
		public static void mobile_zoomOut(int zoomLevel) {
			mobile.mobile_zoomOut(zoomLevel);
		}

		/**
		 * zooms out And checks if target level indicator has been reached
		 * 
		 * @param zoomLevel
		 * @param indicator
		 */
		public static void mobile_zoomOut(EnhancedBy indicator) {
			mobile.mobile_zoomOut(indicator);
		}

		/**
		 * zooms in And checks if target level indicator has been reached
		 * 
		 * @param zoomLevel
		 * @param indicator
		 */
		public static void mobile_zoomIn(EnhancedBy indicator) {
			mobile.mobile_zoomIn(indicator);
		}

		/**
		 * zoom in based on zoom levels. eg. 3 equals zooming in 3 times
		 * 
		 * @param zoomLevel
		 */
		public static void mobile_zoomIn(int zoomLevel) {
			mobile.mobile_zoomIn(zoomLevel);
		}

		/**
		 * scrolls down android
		 */
		public static void scrollDown() {
			mobile.scrollDown();
		}

		/**
		 * scrolls to mobile element until element is becomes visible
		 * 
		 * @param target
		 */
		public static void mobile_scrollToElement(EnhancedBy target) {
			mobile.mobile_scrollToElement(target);
		}

		// PageHelper

		/**
		 * maximizes web page
		 */
		public static void maximizePage() {
			page.maximizePage();
		}

		/**
		 * sets page size
		 * 
		 * @param x
		 * @param y
		 */
		public static void setPageSize(int x, int y) {
			page.setPageSize(x, y);
		}

		/**
		 * reload page
		 */
		public static void refreshPage() {
			page.refreshPage();
		}

		/**
		 * switches frame to frame specified
		 * 
		 * @param frame
		 */
		public static void switchIframe(EnhancedBy frame) {
			page.switchIframe(frame);
		}

		/**
		 * switches to default frame
		 */
		public static void switchToDefaultFrame() {
			page.switchToDefaultFrame();
		}

		/**
		 * dismisses alert by selecting ok or cancel return alert message
		 * 
		 * @param alert
		 */
		public static String dimissAlert() {
			return page.dimissAlert();
		}

		/**
		 * accepts alert by selecting ok or cancel
		 * 
		 * @param alert
		 */
		public static String acceptAlert() {
			return page.acceptAlert();
		}

		/**
		 * returns alert message value
		 * 
		 * @return
		 */
		public static String getAlertValue() {
			return page.getAlertValue();
		}

		/**
		 * return the current window handle
		 * 
		 * @return
		 */
		public static String currentWindow() {
			return page.currentWindow();
		}

		/**
		 * switch to the new opened window
		 * 
		 * @param defaultWindow
		 */
		public static void switchToNewWindow(String defaultWindow) {
			page.switchToNewWindow(defaultWindow);
		}

		/**
		 * close the window And return to the defaultWindow
		 * 
		 * @param defaultWindow
		 */
		public static void CloseAndReturn(String defaultWindow) {
			page.CloseAndReturn(defaultWindow);
		}

		/**
		 * gets page source
		 */
		public static void refreshPageSource() {
			page.refreshPageSource();
		}

		/**
		 * verify page title
		 * 
		 * @param appTitle
		 */
		public static void verifyTitle(String appTitle) {
			page.verifyTitle(appTitle);
		}

		/**
		 * switch webdriver use for switching between different drivers
		 * 
		 * @param driver
		 */
		public static void switchDriver(WebDriver driver) {
			page.switchDriver(driver);
		}
		
		public static void setDriver(WebDriver driver) {
			page.switchDriver(driver);
		}

		/**
		 * switch to next tab
		 */
		public static void switchToNextTab() {
			page.switchToNextTab();
		}

		/**
		 * switch to next tab circles back to initial tab if it reaches the last tab
		 */
		public static void switchToPreviousTab() {
			page.switchToPreviousTab();
		}

		/**
		 * switch to previous tab circle back to the last tab
		 */
		public static void switchToTab(int index) {
			page.switchToTab(index);
		}

		/**
		 * switch to tab by index
		 */
		public static void verifyNumberOfTabs(int tabs) {
			page.verifyNumberOfTabs(tabs);
		}

		/**
		 * returns the title of the page
		 * 
		 * @return
		 */
		public static String getPageTitle() {
			return page.getPageTitle();
		}

		/**
		 * returns the current url
		 * 
		 * @return
		 */
		public static String getCurrentUrl() {
			return page.getCurrentUrl();
		}

		/**
		 * gets page source
		 * 
		 * @return
		 */
		public static String getPageSource() {
			return page.getPageSource();
		}

		/**
		 * navigates back
		 */
		public static void navigateBack() {
			page.navigateBack();
		}

		/**
		 * navigate forward
		 */
		public static void navigateForward() {
			page.navigateForward();
		}

		/**
		 * delete all cookies
		 */
		public static void deleteAllCookies() {
			page.deleteAllCookies();
		}

		/**
		 * delete cookie named
		 * 
		 * @param name
		 */
		public static void deleteCookieNamed(String name) {
			page.deleteCookieNamed(name);
		}

		/**
		 * brings current browser to front
		 */
		public static void bringPageToFront() {
			page.bringPageToFront();
		}

		/**
		 * navigate to a different url
		 * 
		 * @param url
		 */
		public static void swtichUrl(String url) {
			page.swtichUrl(url);
		}
		
		/**
		 * navigate to a different url
		 * 
		 * @param url
		 */
		public static void getUrl(String url) {
			page.navigateToUrl(url);
		}

		/**
		 * navigate to a different url
		 * 
		 * @param url
		 */
		public static void navigateToUrl(String url) {
			page.navigateToUrl(url);
		}

		/**
		 * retrieves the clip board data
		 * 
		 * @return
		 */
		public static String getClipboardData() {
			return page.getClipboardData();
		}

		/**
		 * quits the current web driver
		 */
		public static void quitCurrentDriver() {
			page.quitCurrentDriver();
		}

		/**
		 * quit driver
		 * 
		 * @param driver
		 */
		public static void quitDriver(WebDriver driver) {
			page.quitDriver(driver);
		}

		/**
		 * quits all drivers in the current test
		 */
		public static void quitAllCurrentTestDrivers() {
			page.quitAllCurrentTestDrivers();
		}

		/**
		 * returns true if the element is visible in the current page only for web
		 * applications
		 * 
		 * @param by
		 * @return
		 */
		public static Boolean isVisibleInViewport(EnhancedBy by, int index) {
			return page.isVisibleInViewport(by, index);
		}
		
		/**
		 * get current  webdriver
		 * @return
		 */
		public static WebDriver getWebdriver() {
			return page.getWebdriver();
		}
		
		/**
		 * scroll to bottom of browser
		 */
		public static void scrollBottomPageBrowser() {
			page.scrollBottomPageBrowser();
		}
		
		/**
		 * scroll up the browser
		 */
		public static void scrollUpBrowser(int count) {
			page.scrollUpBrowser(count);
		}
		
		/**
		 * scroll up the browser
		 */
		public static void scrollUpBrowser() {
			page.scrollUpBrowser();
		}
		
		/**
		 * scroll down the browser
		 */
		public static void scrollDownBrowser(int count) {
			page.scrollDownBrowser(count);
		}
		
		/**
		 * scroll down the browser
		 */
		public static void scrollDownBrowser() {
			page.scrollDownBrowser();
		}
		
		/**
		 * scroll to web element
		 * @param element
		 */
		public static void scrollToWebElement(EnhancedBy element) {
			page.scrollToWebElement(element);
		}
		
		public boolean isFirefox() {
			return page.isFirefox();
		}
		
		public boolean isChrome() {
			return page.isChrome();
		}
		
		public boolean isSafari() {
			return page.isSafari();
		}
		
		public boolean isMicrosoftEdge() {
			return page.isMicrosoftEdge();
		}
		
		/**
		 * scroll to element
		 * @param element
		 * @param index
		 */
		public static void scrollToWebElement(EnhancedBy element, int index) {
			page.scrollToWebElement(element, index);
		}
		

		/**
		 * returns true if the element is visible in the current page only for web
		 * applications
		 * 
		 * @param by
		 * @return
		 */
		public static Boolean isVisibleInViewport(EnhancedBy by) {
			return page.isVisibleInViewport(by);
		}

		/**
		 * gets specified attribute of the element
		 * 
		 * @param byValue
		 * @param index
		 * @param attribute
		 */
		public static String getAttribute(EnhancedBy byValue, String attribute) {
			Helper.wait.waitForElementToLoad(byValue);
			return ElementHelper.getAttribute(byValue, attribute);
		}

		/**
		 * returns true if element contains class value
		 * 
		 * @param by
		 * @param classValue
		 * @return
		 */
		public static boolean isElementContainingClass(EnhancedBy by, String classValue) {
			return ElementHelper.isElementContainingClass(by, classValue);
		}

		/**
		 * returns true if element contains attribute value
		 * 
		 * @param by
		 * @param classValue
		 * @return
		 */
		public static boolean isAttributeContaining(EnhancedBy by, String attribute, String value) {
			return ElementHelper.isAttributeContaining(by, attribute, value);
		}

		/**
		 * gets specified attribute of the element based on index
		 * 
		 * @param byValue
		 * @param index
		 * @param attribute
		 */
		public static String getAttribute(EnhancedBy byValue, int index, String attribute) {
			return ElementHelper.getAttribute(byValue, index, attribute);
		}

		/**
		 * sets attribute value of an element
		 * 
		 * @param by
		 * @param attribute
		 * @param value
		 */
		public static void setAttribute(EnhancedBy by, String attribute, String value) {
			ElementHelper.setAttribute(by, attribute, value);
		}

		/**
		 * sets attribute value of an element
		 * 
		 * @param by
		 * @param index
		 * @param attribute
		 * @param value
		 */
		public static void setAttribute(EnhancedBy by, int index, String attribute, String value) {
			ElementHelper.setAttribute(by, index, attribute, value);
		}

		/**
		 * returns element dimension
		 * 
		 * @param by
		 * @return
		 */
		public static Dimension getElementSize(EnhancedBy by) {
			return ElementHelper.getElementSize(by);
		}

		/**
		 * get element position on display
		 * 
		 * @param by
		 * @return
		 */
		public static Point getElementPosition(EnhancedBy by) {
			return ElementHelper.getElementPosition(by);
		}

		/**
		 * get element position on display
		 * 
		 * @param by
		 * @param index
		 * @return
		 */
		public static Point getElementPosition(EnhancedBy by, int index) {
			return ElementHelper.getElementPosition(by, index);
		}

		/**
		 * returns the center coordinates of the target element
		 * 
		 * @param target
		 * @return
		 */
		public static int[] findElementCoordinates(EnhancedBy target) {
			return ElementHelper.findMiddleOfElement(target);
		}

		/**
		 * returns the center coordinates of the target element
		 * 
		 * @param target
		 * @return
		 */
		public static int[] findMiddleOfElement(EnhancedBy target) {
			return ElementHelper.findMiddleOfElement(target);
		}

		// WaitHelper

		/**
		 * waits for element to be displayed for amount of time specified by 60 seconds
		 * 
		 * @param target
		 */
		public static void waitForElementToLoad(final EnhancedBy target) {
			wait.waitForElementToLoad(target);
		}

		/**
		 * waits for element to load count() checks if the element is displayed Then
		 * gets the count number
		 * 
		 * @param target
		 * @param time
		 */
		public static boolean waitForElementToLoad(final EnhancedBy target, int time) {
			return wait.waitForElementToLoad(target, time);
		}

		/**
		 * waits for either element to load returns true When first item loads
		 * 
		 * @param element1
		 * @param element2
		 * @param time
		 * @return
		 */
		public static boolean waitForFirstElementToLoad(final EnhancedBy element1, final EnhancedBy element2,
				final EnhancedBy element3) {
			return wait.waitForFirstElementToLoad(element1, element2, element3);
		}

		/**
		 * waits for either element to load returns true When first item loads
		 * 
		 * @param element1
		 * @param element2
		 * @param time
		 * @return
		 */
		public static boolean waitForFirstElementToLoad(final EnhancedBy element1, final EnhancedBy element2,
				final EnhancedBy element3, int time) {
			return wait.waitForFirstElementToLoad(element1, element2, element3, time);
		}

		/**
		 * waits for either element to load returns true When first item loads
		 * 
		 * @param element1
		 * @param element2
		 * @param time
		 * @return
		 */
		public static boolean waitForFirstElementToLoad(final EnhancedBy element1, final EnhancedBy element2, int time) {
			return wait.waitForFirstElementToLoad(element1, element2, time);
		}

		/**
		 * waits for either element to load returns true When first item loads
		 * 
		 * @param element1
		 * @param element2
		 * @param time
		 * @return
		 */
		public static boolean waitForFirstElementToLoad(final EnhancedBy element1, final EnhancedBy element2) {
			return wait.waitForFirstElementToLoad(element1, element2);
		}

		/**
		 * waits for element to load And refreshes the app each time to renew the dom
		 * 
		 * @param target
		 */
		public static void mobile_waitAndRefreshForElementToLoad(final EnhancedBy target, int time) {
			wait.mobile_waitAndRefreshForElementToLoad(target, time);
		}

		/**
		 * waits for element to load And refreshes the app each time to renew the dom
		 * 
		 * @param target
		 */
		public static void mobile_waitAndRefreshForElementToLoad(final EnhancedBy target) {
			wait.mobile_waitAndRefreshForElementToLoad(target);
		}

		/**
		 * waits for element to load If mobile device, scrolls down the page until
		 * element is visible
		 * 
		 * @param target: element to wait for
		 * @param time:   max time to wait
		 * @param count:  minimum count of elements to wait for in list
		 * @return
		 */
		public static boolean waitForElementToLoad(final EnhancedBy target, int time, int count) {
			return wait.waitForElementToLoad(target, time, count);
		}

		/**
		 * waits for element count to increase from the originalCount Usefull When
		 * waiting for a list to expand with additional items
		 * 
		 * @param target
		 * @param originalCount
		 */
		public static void waitForAdditionalElementsToLoad(final EnhancedBy target, final int originalCount) {
			wait.waitForAdditionalElementsToLoad(target, originalCount);
		}

		/**
		 * waits for element count to increase from the originalCount Usefull When
		 * waiting for a list to expand with additional items
		 * 
		 * @param target
		 * @param originalCount
		 */
		public static void waitForAdditionalElementsToLoad(final EnhancedBy target, final int originalCount, int time) {
			wait.waitForAdditionalElementsToLoad(target, originalCount, time);
		}

		/**
		 * waits for element to not be displayed wait for maximum of 60 seconds
		 * 
		 * @param target
		 * @return 
		 */
		public static boolean waitForElementToBeRemoved(final EnhancedBy target) {
			return wait.waitForElementToBeRemoved(target);
		}

		/**
		 * waits for element to not be displayed
		 * 
		 * @param target
		 * @param time   : maximum amount of time in seconds to wait
		 */
		public static boolean waitForElementToBeRemoved(final EnhancedBy target, int time) {
			return wait.waitForElementToBeRemoved(target, time);
		}
		
		/**
		 * waits for element to not be displayed
		 * 
		 * @param target
		 * @param time   : maximum amount of time in seconds to wait. use AbstractDriver.TIMEOUT_SECONDS for default timeout
		 * @param waitForTargetToLoadInSeconds wait for element to load before waiting for element to be removed  
		 */
		public static boolean waitForElementToBeRemoved(final EnhancedBy target, int time, int waitForTargetToLoadInSeconds) {
			return wait.waitForElementToBeRemoved(target, time, waitForTargetToLoadInSeconds);
		}

		/**
		 * waits for number of seconds
		 * 
		 * @param seconds
		 */
		public static void waitForSeconds(double seconds) {
			wait.waitForSeconds(seconds);
		}

		/**
		 * waits for webpage to load
		 */
		public static void waitForPageToLoad() {
			wait.waitForPageToLoad();
		}

		/**
		 * waits for item containing in list to load
		 * 
		 * @param list
		 * @param option
		 * @param time
		 */
		public static void waitForListItemToLoad_Contains(final EnhancedBy list, String option) {
			wait.waitForListItemToLoad_Contains(list, option);
		}

		/**
		 * waits for text to be loaded for amount of time specified by 60 seconds
		 * 
		 * @param target
		 */
		public static void waitForTextToLoad(final EnhancedBy target, String text) {
			wait.waitForTextToLoad(target, text);
		}

		/**
		 * make sure only one element And caller needs to take responsibility to have
		 * text in the element
		 * 
		 * @param target
		 * @param time
		 */
		public static void waitForTextToLoad(final EnhancedBy target, int time, String text) {
			wait.waitForTextToLoad(target, text);
		}

		/**
		 * wait for element to become clickable causes instability with hybrid mobile
		 * app
		 * 
		 * @param selector
		 * @return
		 */
		public static boolean waitForElementToBeClickable(EnhancedBy selector) {
			return wait.waitForElementToBeClickable(selector);
		}

		/**
		 * wait for element to become clickable causes instability with hybrid mobile
		 * app
		 * 
		 * @param selector
		 * @return
		 */
		public static boolean waitForElementToBeClickable(EnhancedBy selector, int timeInSeconds) {
			return wait.waitForElementToBeClickable(selector, timeInSeconds);
		}

		/**
		 * wait for class to contain
		 * 
		 * @param target
		 * @param index
		 * @param value
		 * @return
		 */
		public boolean waitForClassContain(final EnhancedBy target, int index, String value) {
			return wait.waitForClassContain(target, index, value);
		}

		/**
		 * wait for class to contain value
		 * 
		 * @param target
		 * @param index
		 * @param value
		 * @param time
		 * @return
		 */
		public boolean waitForClassContain(final EnhancedBy target, int index, String value, int time) {
			return wait.waitForClassContain(target, index, value, time);
		}

		/**
		 * wait for any text strings to become available
		 * 
		 * @param target
		 * @param time
		 * @param text
		 * @return
		 */
		public static boolean waitForAnyTextToLoadContaining(final EnhancedBy target, String... text) {
			return wait.waitForAnyTextToLoad(target, text);
		}

		/**
		 * wait for any text strings to become available
		 * 
		 * @param target
		 * @param time
		 * @param text
		 * @return
		 */
		public static boolean waitForAnyTextToLoadContaining(final EnhancedBy target, int time, String... text) {
			return wait.waitForAnyTextToLoad(target, time, text);
		}

		/**
		 * wait for any text strings to become available
		 * 
		 * @param target
		 * @param time
		 * @param text
		 * @return
		 */
		public static boolean waitForAnyTextToLoad(final EnhancedBy target, String... text) {
			return wait.waitForAnyTextToLoad(target, text);
		}

		/**
		 * wait for any text strings to become available
		 * 
		 * @param target
		 * @param time
		 * @param text
		 * @return
		 */
		public static boolean waitForAnyTextToLoad(final EnhancedBy target, int time, String... text) {
			return wait.waitForAnyTextToLoad(target, time, text);
		}

		// StopWatchHelper

		/**
		 * StopWatchHelper watch = Helper.start(); do something long passedTimeInMs =
		 * watch.time(); long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
		 * 
		 * @return
		 */
		public static StopWatchHelper start() {
			return StopWatchHelper.start();
		}

		// UtilityHelper

		/**
		 * generates random string of length len
		 * 
		 * @param len
		 * @return
		 */
		public static String generateRandomString(int len) {
			return UtilityHelper.generateRandomString(len);
		}

		/**
		 * generates random int of length len
		 * 
		 * @param len
		 * @return
		 */
		public static String generateRandomInteger(int len) {
			return UtilityHelper.generateRandomInteger(len);
		}

		/**
		 * generates random number between two numbers, min, max
		 * 
		 * @param min
		 * @param max
		 * @return
		 */
		public static int generateRandomNumber(int min, int max) {
			return UtilityHelper.generateRandomNumber(min, max);
		}
		
		/**
		 * generate uuid
		 * @return
		 */
		public static String generateUUID() {
			return UtilityHelper.generateUUID();
		}
		
		/**
		 * generate uuid
		 * @param includeDash
		 * @return
		 */
		public static String generateUUID(boolean includeDash) {
			return UtilityHelper.generateUUID(includeDash);
		}
		
		/**
		 * generate uuid
		 * @param length
		 * @return
		 */
		public static String generateUUID(int length) {
			return UtilityHelper.generateUUID(length);
		}
		
		/**
		 * generate uuid
		 * @param length
		 * @return
		 */
		public static String generateUUID(int length, boolean includeDash) {
			return UtilityHelper.generateUUID(length, includeDash);
		}

		/**
		 * highlights the web element use on clicks and send key elements
		 * 
		 * @param by
		 * @param index
		 */
		public static void highLightWebElement(EnhancedBy by, int index) {
			UtilityHelper.highLightWebElement(by, index);
		}

		/**
		 * returns true if OS is mac
		 * 
		 * @return
		 */
		public static boolean isMac() {
			return UtilityHelper.isMac();
		}

		/**
		 * returns true if OS is windows
		 * 
		 * @return
		 */
		public static boolean isWindows() {
			return UtilityHelper.isWindows();
		}

		/**
		 * returns true if OS is unix or linux
		 * 
		 * @return
		 */
		public static boolean isUnix() {
			return UtilityHelper.isUnix();
		}

		/**
		 * is the string value UDID
		 * 
		 * @param value
		 * @return
		 */
		public static boolean isUUID(String value) {
			return UtilityHelper.isUUID(value);
		}

		/**
		 * returns if string is boolean
		 * 
		 * @param value
		 * @return
		 */
		public static boolean isBoolean(String value) {
			return UtilityHelper.isBoolean(value);
		}

		/**
		 * return if string is a number
		 * 
		 * @param str
		 * @return
		 */
		public static boolean isNumeric(String value) {
			return UtilityHelper.isNumeric(value);
		}

		/**
		 * execute javascript
		 * 
		 * @param script
		 */
		public static Object executeJs(String script, Object... args) {
			return UtilityHelper.executeJs(script, args);
		}

		/**
		 * execute javascript
		 * 
		 * @param script
		 */
		public static List<String> executeJsWithListReturn(String script, Object... args) {
			return UtilityHelper.executeJsWithListReturn(script, args);
		}

		/**
		 * execute javascript
		 * 
		 * @param script
		 */
		public static String executeJsWithStringReturn(String script, Object... args) {
			return UtilityHelper.executeJsWithStringReturn(script, args);
		}

		/**
		 * get numeric value from string
		 * 
		 * @param value
		 * @param isFailOnNoInt
		 * @return
		 */
		public static int getIntFromString(String value) {
			return UtilityHelper.getIntFromString(value, false);

		}
		
		/**
		 * get numeric value from string
		 * 
		 * @param value
		 * @param isFailOnNoInt
		 * @return
		 */
		public static int getNumber(String value) {
			return UtilityHelper.getIntFromString(value, false);
		}
		
		public static int getFirstNumber(String value) {
			return UtilityHelper.getFirstNumber(value);
		}

		/**
		 * get numeric value from string
		 * 
		 * @param value
		 * @param isFailOnNoInt
		 * @return
		 */
		public static int getIntFromString(String value, boolean isFailOnNoInt) {
			return UtilityHelper.getIntFromString(value, isFailOnNoInt);
		}

		/**
		 * get int value from string
		 * 
		 * @param value
		 * @param isFailOnNoInt
		 * @return
		 */
		public static double getDoubleFromString(String value) {
			return UtilityHelper.getDoubleFromString(value, false);
		}

		/**
		 * does string have only numeric value
		 * 
		 * @param value
		 * @return
		 */
		public static boolean isStringContainOnlyNumber(String value) {
			return UtilityHelper.isStringContainOnlyNumber(value);
		}

		/**
		 * does string have numeric value
		 * 
		 * @param value
		 * @return
		 */
		public static boolean isStringContainNumber(String value) {
			return UtilityHelper.isStringContainNumber(value);
		}

		/**
		 * remove surrounding double quotes from the string
		 * 
		 * @param value
		 * @return
		 */
		public static String removeSurroundingQuotes(String value) {
			return UtilityHelper.removeSurroundingQuotes(value);
		}

		/**
		 * converts url string to url object
		 * 
		 * @param url
		 * @return
		 */
		public static URL convertToUrl(String url) {
			return UtilityHelper.convertToUrl(url);
		}

		/**
		 * check if able to connect to source url
		 * 
		 * @param source
		 * @param proxy  set to null if no proxy
		 * @return
		 */
		public static boolean isUrlAbleToConnect(URL source, Proxy proxy) {
			return UtilityHelper.isUrlAbleToConnect(source, proxy);
		}
		
		/**
		 * get proxy state from proxy enabled config
		 * values: true, false, auto
		 * if auto is set, then through proxy detection, we set value to true or false
		 * @param url
		 * @return
		 */
		public static boolean isProxyRequired(URL url) {
			return UtilityHelper.isProxyRequired(url);
		}

		/**
		 * get numeric value from string
		 * 
		 * @param value
		 * @param isFailOnNoInt
		 * @return
		 */
		public static double getDoubleFromString(String value, boolean isFailOnNoInt) {
			return UtilityHelper.getDoubleFromString(value, isFailOnNoInt);
		}
		
		/**
		 * does file contain the given text value
		 * @param value
		 * @param file
		 * @return
		 */
		public static boolean isFileContainString(String value, File file) {
			return UtilityHelper.isFileContainString(value, file);
		}
		
		/**
		 * does file contain the given text value
		 * @param value
		 * @param file
		 * @return
		 */
		
		public static boolean isFileContainsString(String value, File file) {
			return UtilityHelper.isLineInFileStartWithString(value, file);
		}
		
		/**
		 * get line in file starting with string
		 * @param value
		 * @param file
		 * @return
		 */
		public static List<String> getLinesInFileStartingWith(String value, File file) {
			return UtilityHelper.getLinesInFileStartingWith(value, file);
		}
		
		/**
		 * get line in file containing string
		 * @param value
		 * @param file
		 * @return
		 */
		public static List<String> getLinesInFileContainingWith(String value, File file) {
			return UtilityHelper.getLinesInFileContainingWith(value, file);
		}
		
		/**
		 * prints page source to console
		 */
		public static void printPageSource() {
			UtilityHelper.printPageSource();
		}
		
		/**
		 * convert array list to string separated by ","
		 * @param list
		 * @return
		 */
		public static String convertListToString(ArrayList<String> list, String separator) {
			return UtilityHelper.convertListToString(list, separator);

		}
		
		/**
		 * convert array list to string separated by ","
		 * @param list
		 * @return
		 */
		public static String convertListToString(ArrayList<String> list) { 
			return UtilityHelper.convertListToString(list);
		}
		
		/**
		 * normalizes string removes space, new line chars
		 * 
		 * @param value
		 * @return
		 */
		public static String stringNormalize(String value) {
			return UtilityHelper.stringNormalize(value);
		}

		/**
		 * normalizes string removes space, new line chars
		 * 
		 * @param value
		 * @return
		 */
		public static String stringRemoveLines(String value) {
			return UtilityHelper.stringRemoveLines(value);
		}

		/**
		 * returns kills the process if running
		 * 
		 * @param serviceName
		 * @return
		 * @throws Exception
		 */
		public static void killWindowsProcess(String serviceName) {
			UtilityHelper.killWindowsProcess(serviceName);
		}

		/**
		 * returns kills the process if running
		 * 
		 * @param serviceName
		 * @return
		 * @throws Exception
		 */
		public static void killMacProcess(String serviceName) {
			UtilityHelper.killMacProcess(serviceName);
		}

		/**
		 * create directories and files based on absolute path
		 * 
		 * @param path
		 */
		public static File createFileFromPath(String absolutePath) {
			return UtilityHelper.createFileFromPath(absolutePath);
		}

		/**
		 * Create file with path starting from root directory (where pom.xml is) and
		 * write to it. eg. writeFile("something","", "myFile", "txt");
		 * 
		 * @param value    value in file
		 * @param path     path from root
		 * @param filename name of the file
		 * @param type     type of file
		 */
		public static void writeFile(String value, String path, String filename, String type) {
			UtilityHelper.writeFile(value, path, filename, type);
		}

		/**
		 * Create file (where pom.xml is) and write to it
		 * 
		 * @param value
		 * @param absolutePath
		 */
		public static void writeFile(String value, String absolutePath) {
			UtilityHelper.writeFile(value, absolutePath);
		}

		/**
		 * delete file
		 * 
		 * @param absolutePath
		 */
		public static void deleteFile(String absolutePath) {
			UtilityHelper.deleteFile(absolutePath);
		}

		/**
		 * appends to existing file
		 * 
		 * @param value
		 * @param absolutePath
		 */
		public static void appendToFile(String value, String absolutePath) {
			UtilityHelper.appendToFile(value, absolutePath);
		}

		/**
		 * appends to existing file
		 * 
		 * @param value
		 * @param directory
		 * @param filename
		 * @param type
		 */
		public static void appendToFile(String value, String directory, String filename, String type) {
			UtilityHelper.appendToFile(value, directory, filename, type);

		}

		/**
		 * run command and return results as array list will run bash on linux or mac
		 * will run batch command on windows
		 * 
		 * @param command
		 * @return
		 * @throws IOException
		 */
		public static ArrayList<String> executeCommand(String command) {
			return UtilityHelper.executeCommand(command);
		}

		/**
		 * run script file and return results as array list will run bash on linux or
		 * mac will run batch command on windows
		 * 
		 * @param filePath path from the root directory ( where pom.xml is )
		 * @return the results as arraylist
		 */
		public static ArrayList<String> executeCommandFromFile(String filePath) {
			return UtilityHelper.excuteCommandFromFile(filePath);
		}

		/**
		 * Copies directory And all content from dirFrom to dirTo overwrites the content
		 * 
		 * @param dirFrom
		 * @param dirTo
		 */
		public static void copyDirectory(String dirFrom, String dirTo) {
			UtilityHelper.copyDirectory(dirFrom, dirTo);
		}

		/**
		 * zip folder
		 * 
		 * @param srcFolder
		 * @param destZipFile
		 * @return
		 * @throws Exception
		 */
		public static ZipOutputStream zipFolder(String srcFolder, String destZipFile) {
			try {
				return UtilityHelper.zipFolder(srcFolder, destZipFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Helper.assertTrue("zip file not created", false);
			return null;
		}

		public static ZipOutputStream createZip(String destZipFile) {
			try {
				return UtilityHelper.createZip(destZipFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Helper.assertTrue("zip file not created", false);
			return null;
		}

		public static void addFileToZip(String path, String srcFile, ZipOutputStream zip) {
			try {
				UtilityHelper.addFileToZip(path, srcFile, zip);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * gets a list of string from 'source' starting with 'value'
		 * 
		 * @param source
		 * @param value
		 * @return
		 */
		public static List<String> getAllValuesStartringWith(String source, String value) {
			return UtilityHelper.getAllValuesStartringWith(source, value);
		}

		/**
		 * 
		 * @param str
		 * @param pattern regular expression pattern
		 * @return
		 */
		public static String getValueFromPattern(String str, String pattern) {
			return UtilityHelper.getValueFromPattern(str, pattern);
		}

		/**
		 * 
		 * @param str
		 * @param pattern regular expression pattern
		 * @return
		 */
		public static List<String> getValuesFromPattern(String str, String pattern) {
			return UtilityHelper.getValuesFromPattern(str, pattern);
		}

		/**
		 * get current directory
		 * 
		 * @return
		 */
		public static String getRootDir() {
			return UtilityHelper.getRootDir();
		}

		/**
		 * get file from file path
		 * 
		 * @param directoryPath
		 * @return
		 */
		public static File getFile(String directoryPath) {
			return UtilityHelper.getFile(directoryPath);
		}

		/**
		 * get file by name
		 * 
		 * @param path
		 * @param filename
		 * @return
		 */
		public static File getFileByName(String path, String filename) {
			return UtilityHelper.getFileByName(path, filename);
		}
		
		/**
		 * get file by name
		 * 
		 * @param path
		 * @param filename
		 * @return
		 */
		public static File getFileByName(String path, String filename, boolean includeSubDir) {
			return UtilityHelper.getFileByName(path, filename, includeSubDir);
		}

		/**
		 * gets the list of files
		 * 
		 * @return
		 */
		public static ArrayList<File> getFileListByType(String directoryPath, String type, boolean includeSubtype) {
			return UtilityHelper.getFileListByType(directoryPath, type, includeSubtype);
		}

		/**
		 * gets the list of files
		 * 
		 * @return
		 */
		public static ArrayList<File> getFileListByType(String directoryPath, String type) {
			return UtilityHelper.getFileListByType(directoryPath, type);
		}

		/**
		 * returns the list of files in directory
		 * 
		 * @param directoryPath
		 * @return
		 */
		public static ArrayList<File> getFileList(String directoryPath) {
			return UtilityHelper.getFileList(directoryPath);
		}
		
		/**
		 * gets full path from relative path
		 * relative path is from root directory ( where pom.xml file is located )
		 * @param path
		 * @return
		 */
		public static String getFullPath(String path) {
			return UtilityHelper.getFullPath(path);
		}
		
		/**
		 * returns the list of files in directory
		 * 
		 * @param directoryPath
		 * @return
		 */
		public static ArrayList<File> getFileList(String directoryPath, boolean includeSubDir) {
			return UtilityHelper.getFileList(directoryPath, includeSubDir);
		}
		
		/**
		 * gets list of files including from sub folder based on type. eg. ".csv"
		 * 
		 * @return
		 */
		public static List<File> getFileListWithSubfolders(String directoryName, List<File> files) {
			return UtilityHelper.getFileListWithSubfolders(directoryName, files);
		}

		/**
		 * gets list of files including from sub folder based on type. eg. ".csv"
		 * 
		 * @return
		 */
		public static List<File> getFileListWithSubfolders(String directoryName, String type, List<File> files) {
			return UtilityHelper.getFileListWithSubfolders(directoryName, type, files);
		}

		/**
		 * gets file content as String
		 * 
		 * @param absolutePath
		 * @return
		 */
		public static String readFileContent(String absolutePath) {
			return UtilityHelper.readFileContent(absolutePath);
		}

		/**
		 * gets file content as String
		 * 
		 * @param absolutePath
		 * @return
		 */
		public static String getFileContent(String absolutePath) {
			return UtilityHelper.getFileContent(absolutePath);
		}
		
		/**
		 * gets file content as String
		 * 
		 * @param absolutePath
		 * @return
		 */
		public static String getFileContent(String absolutePath, boolean verifyFileExists) {
			return UtilityHelper.getFileContent(absolutePath, verifyFileExists);
		}

		/**
		 * captures screenshot And attaches to extent test report
		 * 
		 * @param description
		 */
		public synchronized static void captureExtentReportScreenshot() {
			UtilityHelper.captureReportScreenshot();
		}
		
		public synchronized static void screenshotCapture() {
			UtilityHelper.captureReportScreenshot();
		}

		// action helper

		/**
		 * hover over element
		 * 
		 * @param by
		 */
		public static void hoverBy(EnhancedBy by) {
			ElementActionHelper.hoverBy(by, 0);
		}

		/**
		 * hover over element
		 * 
		 * @param by
		 */
		public static void hoverBy(EnhancedBy by, int index) {
			ElementActionHelper.hoverBy(by, index);
		}

		/*
		 * Enter text to an element by action
		 */
		public void inputTextByAction(EnhancedBy by, String text) {
			ElementActionHelper.inputTextByAction(by, text);
		}

		/*
		 * Double click an element
		 */
		public static void doubleClickBy(EnhancedBy by) {
			ElementActionHelper.doubleClickBy(by);
		}

		/**
		 * Hover on the x,y points
		 *
		 * @param x
		 * @param y
		 */
		public static void hoverPoints(int x, int y) {
			ElementActionHelper.hoverPoints(x, y);
		}

		/**
		 * move to element by using action
		 * 
		 * @param target
		 * @param index
		 */
		public static void moveToElement(EnhancedBy target, int index) {
			ElementActionHelper.moveToElement(target, index);
		}

		/**
		 * move to element by using action
		 * 
		 * @param target
		 * @param index
		 */
		public static void moveToElement(EnhancedBy target, int index, int xOffset, int yOffset) {
			ElementActionHelper.moveToElement(target, index, xOffset, yOffset);
		}

		// email helper
		/**
		 * sends email to recipient using email object
		 * 
		 * @throws Exception
		 */
		public static void sendMail(EmailObject email) {
			EmailSendHelper.sendMail(email);
		}

		// login helper
		/**
		 * if single signin enabled, And new test user is different form previous,
		 * shutdown webdriver And restart
		 * 
		 * @param newUserName
		 * @throws Exception
		 */
		public static void handleDifferentUser() {
			LoginHelper.handleDifferentUser();
		}

		// Localization handler
		/**
		 * gets local language from json file specified in properties file
		 * 
		 * @param key
		 * @return
		 */
		public static String localize(String key) {
			return LocalizationHelper.localize(key);
		}

		/**
		 * loads localization map from a json file to the test object data
		 * 
		 * @param fileName
		 */
		public static void localizationLoadJson(String fileName) {
			LocalizationHelper.localizationLoadJson(fileName);
		}

		/**
		 * sets the language for csv localization file
		 * 
		 * @param language
		 */
		public static void localizationSetupCsv(String language) {
			LocalizationHelper.localizationSetupCsv(language);
		}
		// restApiHelper

		/**
		 * runApiContaining("name", "zzz_","getCompanies",
		 * "id","companyId","deleteCompany") get all companies with name containing
		 * zzz_, Then gets id of these companies, stores them in companyId variable And
		 * calls deleteCompany
		 * 
		 * @param getApi:      api to search for identifier. eg. name containing "zzz"
		 * @param prefix:      value containing in getApi. eg. name containing "zzz"
		 * @param identifier:  api to call to get all values. eg. getCompanies
		 * @param targetApiId: id used to call target api.eg. id for deleteCompany api
		 * @param variable:    variable the id is stored in csv keyword file. eg
		 *                     companyId
		 * @param targerApi:   api to call. eg. deleteCompany
		 * @throws JSONException
		 */
		public static void runApiContaining(String identifier, String prefix, String getApi, String targetApiId,
				String variable, String targerApi) throws JSONException {
			RestApiHelper.runApiContaining(identifier, prefix, getApi, targetApiId, variable, targerApi);
		}

		/**
		 * runApiEquals("name", "test123","getCompanies",
		 * "id","companyId","deleteCompany") get all companies with name equals test123,
		 * Then gets id of these companies And calls delete with id
		 * 
		 * @param getApi:      api to search for identifier. eg. name equals "test123"
		 * @param value:       value containing in getApi. eg. name equals "test123"
		 * @param identifier:  api to call to get all values. eg. getCompanies
		 * @param targetApiId: id used to call target api.eg. id for deleteCompany api
		 * @param variable:    variable the id is stored in csv keyword file. eg
		 *                     companyId
		 * @param targerApi:   api to call. eg. deleteCompany
		 * @throws JSONException
		 */
		public static void runApiEquals(String identifier, String value, String getApi, String targetApiId, String variable,
				String targerApi) throws JSONException {
			RestApiHelper.runApiEquals(identifier, value, getApi, targetApiId, variable, targerApi);
		}
		
		/**
		 * runs method from external class
		 * @param sourceFile
		 * @param methodName
		 * @param parameterList
		 * @return
		 * @throws Exception
		 */
		public static Object runInternalClass(String sourcePath, String methodName, List<KeyValue> parameterList) throws Exception {
			return ExternalClassHelper.runInternalClass(sourcePath, methodName, parameterList);
		}

}