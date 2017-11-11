package core.helpers;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.Location;

import core.helpers.CsvHelper.CsvHelper;
import core.helpers.CsvHelper.CsvObject;
import core.helpers.ExcelHelper.ExcelHelper;
import core.helpers.ExcelHelper.ExcelObject;
import core.webElement.EnhancedBy;
import core.webElement.EnhancedWebElement;

public class Helper {

	 // CsvHelper
	/**
	 * gets single cell data in csv file
	 * @param csv - requires. csv.row, csv.column, csv.csvFile
	 * @return
	 * @throws Exception
	 */
	public static String csv_getCellData(CsvObject csv) throws Exception {
		return CsvHelper.getCellData(csv);
	}
	
	/**
	 * gets all csv data in list of string arrays
	 * @param csv - requires. csv.csvFile
	 * @return
	 * @throws Exception
	 */
	public static List<String[]> csv_getAllCsvData(CsvObject csv) throws Exception {
		return CsvHelper.getAllCsvData(csv);	
	}
	
	/**
	 * 
	 * @param csv - required: csv.csvFile, csv.value
	 *  value: String [] record = "3,David,Feezor,USA,40".split(",");
	 * @throws Exception
	 */
	public static void csv_writeNewCsv(CsvObject csv) throws Exception{
		 CsvHelper.writeNewCsv(csv);
	}
	
	/**
	 * 
	 * @param csv - required: csv.csvFile, csv.value
	 *  value: String [] record = "3,David,Feezor,USA,40".split(",");
	 * @throws Exception
	 */
	public static void csv_appendCsv(CsvObject csv) throws Exception{
		CsvHelper.appendCsv(csv);
	}
	
	// ExcelHelper
	/**
	 * gets the excel file and the work sheet 
	 * @param Path
	 * @param SheetName
	 * @throws Exception 
	 */
	public static ExcelObject excel_setExcelFile(ExcelObject excel) throws Exception {
		return ExcelHelper.setExcelFile(excel);
	}
	
	/**
	 * returns all the column data as array list of string
	 * @param colNum
	 * @return
	 * @throws Exception
	 */
	public static List<String> excel_getColumData(ExcelObject excel) throws Exception {
		return ExcelHelper.getColumData(excel);
	}
	
	/**
	 * This method is to read the test data from the Excel cell, in this we are
	 * passing parameters as Row num and Col num
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
	 * This method is to write in the Excel cell, Row num and Col num are the
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
	
	//AssertHelper
	
	/**
	 * assert true
	 * 
	 * @param message
	 * @param value
	 */
	public static void assertTrue(String message, boolean value) {
		AssertHelper.assertTrue(message, value);
	}
	
	
	// Element Helper
	/**
	 * finds element based on parent element
	 * 
	 * @param element
	 * @param parent
	 * @return
	 */
	public static EnhancedWebElement findElements(EnhancedBy child, WebElement parent) {
		return Element.findElements(child, parent);
	}
	
	/**
	 * finds list of elements
	 * 
	 * @param element
	 * @return
	 */
	public static EnhancedWebElement findElements(EnhancedBy element) {
		return Element.findElements(element);
	}
	
	/**
	 * finds a list of elements based on parent element
	 * 
	 * @param element
	 * @param parent
	 * @return
	 */
	public static EnhancedWebElement findElements(EnhancedBy child, EnhancedWebElement parent) {
		return Element.findElements(child, parent);
	}
	
	/**
	 * sets the by value with by selector and name of the element
	 * 
	 * @param by
	 * @param name
	 * @return
	 */
	public static EnhancedBy bySelector(By by, String name) {
		return Element.bySelector(by, name);
	}
	
	/**
	 * gets element by css value
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byCss(String element, String name) {
		return Element.byCss(element, name);
	}
	
	/**
	 * gets element by id
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byId(String element, String name) {
		return Element.byId(element, name);
	}
	
	/**
	 * gets element by xpath
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byXpath(String element, String name) {
		return Element.byXpath(element, name);
	}
	
	/**
	 * gets element by accessibility id
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byAccessibility(String element, String name) {
		return Element.byAccessibility(element, name);
	}
	
	/**
	 * gets element by class (for mobile)
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byMobileClass(String element, String name) {
		return Element.byMobileClass(element, name);
	}
	
	// ClickHelper
	/**
	 * clicks target and waits for expected element to display retries 10 times
	 * 
	 * @param target
	 * @param expected
	 */
	public static void clickAndExpect(EnhancedBy target, EnhancedBy expected) {
		ClickHelper.clickAndExpect(target, expected);
	}
	
	/**
	 * clicks element based on index and waits for expected element to be displayed
	 * 
	 * @param target
	 * @param index
	 * @param expected
	 */
	public static void clickAndExpect(EnhancedBy target, int index, EnhancedBy expected) {
		ClickHelper.clickAndExpect(target, index, expected);
	}
	
	/**
	 * clicks target and waits for expected element to show up also waits for
	 * spinner element to be removed from display
	 * 
	 * @param target
	 * @param expected
	 * @param spinner
	 */
	public static void clickAndExpect(EnhancedBy target, EnhancedBy expected, EnhancedBy spinner) {
		ClickHelper.clickAndExpect(target, expected, spinner);
	}
	
	/**
	 * clicks target and waits for expected to not be displayed retries 10 times
	 * 
	 * @param target
	 * @param expected
	 */
	public static void clickAndNotExpect(EnhancedBy target, EnhancedBy expected) {
		ClickHelper.clickAndNotExpect(target, expected);
	}
	
	public static void clickAndNotExpect(EnhancedBy target, int index, EnhancedBy expected) {
		ClickHelper.clickAndNotExpect(target, index, expected);
	}
	
	// VerifyHelper
	/**
	 * verifies if element(s) is (are) displayed
	 * 
	 * @param by
	 */
	public static void verifyElementIsDisplayed(EnhancedBy by) {
		VerifyHelper.verifyElementIsDisplayed(by);
	}
	
	/**
	 * returns true if element is displayed
	 * 
	 * @param element
	 * @return
	 */
	public static boolean isPresent(EnhancedBy element) {
		return VerifyHelper.isPresent(element);
	}
	
	/**
	 * verifies if element(s) is (are) not displayed
	 * 
	 * @param by
	 */
	public static void verifyElementIsNotDisplayed(EnhancedBy by) {
		VerifyHelper.verifyElementIsNotDisplayed(by);
	}
	
	// FormHelper Class
	
	/**
	 * sets field text value by index
	 * hides keyboard if on ios device
	 * @param value
	 * @param field
	 * @param index
	 */
	public static void setField(String value, EnhancedBy field, int index) {
		FormHelper.setField(value, field, index);
	}
	
	/**
	 * set field value if value is not empty
	 * 
	 * @param value
	 * @param field
	 */
	public static void setField(String value, EnhancedBy field) {
		FormHelper.clearAndSetField(value, field);
	}
	
	/**
	 * sets field
	 * clears field before setting the value
	 * @param value
	 * @param field
	 */
	public static void clearAndSetField(String value, EnhancedBy field) {
		FormHelper.clearAndSetField(value, field);
	}
	
	/**
	 * sets field
	 * clears field before setting the value
	 * @param value
	 * @param field
	 * @param index
	 */
	public static void clearAndSetField(String value, EnhancedBy field, int index) {
		FormHelper.clearAndSetField(value, field, index);
	}
	
	/**
	 * sets key based on nested text field
	 * @param value
	 * @param parent
	 * @param parentIndex
	 * @param child
	 * @param childIndex
	 */
	public static void setChildField(String value, EnhancedBy parent, int parentIndex,  EnhancedBy child, int childIndex) {
		FormHelper.setKeyChildField(value, parent, parentIndex, child, childIndex);
	}
	
	/**
	 * sets field and presses the enter key
	 * @param value
	 * @param field
	 */
	public static void setFieldAndEnter(String value, EnhancedBy field) {
		FormHelper.setFieldAndEnter(value, field);
	}
	
	/**
	 * select submit button and wait for expected element to load
	 * 
	 * @param button
	 * @param expected
	 * 
	 */
	public static void formSubmit(EnhancedBy button, EnhancedBy expected) {
		FormHelper.formSubmit(button, expected);
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
		FormHelper.formSubmit(button, expected, spinner);
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
		FormHelper.selectDropDown(option, field, list);
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
		FormHelper.selectDropDown(option, field, field_Identifier, list);
	}
	
	   /**
	    * select drop down by index from the drop down list
	    * @param index
	    * @param field
	    * @param list
	    */
	public static void selectDropDown(int index, EnhancedBy field, EnhancedBy list) {
		FormHelper.selectDropDown(index, field, list);
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
		FormHelper.selectDropDown(option, field, list, listIndex);
	}
	
	/**
	 * selects drop down based on index of the drop down field
	 * @param option
	 * @param field
	 * @param index
	 * @param list
	 */
	public static void selectDropDown(String option, EnhancedBy field, int index, EnhancedBy list) {
		FormHelper.selectDropDown(option, field, index, list);
	}
	
	/**
	 * selects radio button by radio button description
	 * 
	 * @param option
	 * @param buttons
	 */
	public static void selectRadioButton(String option, EnhancedBy buttons) {
		FormHelper.selectRadioButton(option, buttons);
	}
	
	/**
	 * selects a button
	 * @param button
	 */
	public static void selectRadioButton(EnhancedBy button) {
		FormHelper.selectRadioButton(button);
	}
	
	/**
	 * selects multiple checkbox options
	 * 
	 * @param selections
	 * @param checkboxes
	 */
	public static void selectMultipleCheckboxOptions(List<String> selections, EnhancedBy checkboxes) {
		FormHelper.selectMultipleCheckboxOptions(selections, checkboxes);
	}
	
	/**
	 * uploads file by specifying file location relative to main path
	 * 
	 * @param location
	 * @param imageButton
	 */
	public static void uploadFile(String location, EnhancedBy imageButton) {
		FormHelper.uploadFile(location, imageButton);
	}
	
	/**
	 * sets the image based on location
	 * 
	 * @param location
	 * @param imageButton
	 * @param images
	 *            : uploaded image
	 */
	public void uploadImages(List<String> locations, EnhancedBy imageButton, EnhancedBy images) {
		FormHelper.uploadImages(locations, imageButton, images);
	}
	
	/**
	 * sets the image based on location
	 * 
	 * @param location
	 * @param imageButton
	 * @param images
	 *            : uploaded image
	 */
	public static void uploadImage(String location, EnhancedBy imageButton, EnhancedBy images) {
		FormHelper.uploadImage(location, imageButton, images);
	}
	
	/**
	 * gets the text value from an element
	 * @param element
	 * @return
	 */
	public static String getTextValue(EnhancedBy element) {
		return FormHelper.getTextValue(element);
	}
	
	// ListHelper
	
	/**
	 * selects an element in list by its index value
	 * 
	 * @param list
	 * @param index
	 */
	public static void selectElementInList(EnhancedBy list, int index) {
		ListHelper.selectElementInList(list, index);
	}
	
	/**
	 * selects an element in list by its index value and waits for expected element
	 * 
	 * @param list
	 * @param index
	 * @param expected
	 */
	public static void selectElementInList(EnhancedBy list, int index, EnhancedBy expected) {
		ListHelper.selectElementInList(list, index);
	}
	
	/**
	 * enters value into the search field and selects enter waits for the loading
	 * spinner to be removed
	 * 
	 * @param searchQuery
	 * @param byTarget
	 * @param spinner
	 */
	public static void searchAndWaitForResults(String searchQuery, EnhancedBy byTarget, EnhancedBy spinner) {
		ListHelper.searchAndWaitForResults(searchQuery, byTarget, spinner);
	}
	
	/**
	 * selects list item by the string option provided
	 * 
	 * @param list
	 * @param option
	 */
	public static void selectListItemEqualsByName(EnhancedBy list, String option) {
		ListHelper.selectListItemEqualsByName(list, option);
	}
	
	/**
	 * finds target element which is in the same container and has the same index as
	 * the parent eg. delete button in the list of customers, both having index 2.
	 * we find the index by name, and use that to find the target element
	 * 
	 * @param list
	 * @param option
	 * @param target
	 */
	public static void selectListItemEqualsByName(EnhancedBy list, String option, EnhancedBy target) {
		ListHelper.selectListItemEqualsByName(list, option, target);
	}
	
	/**
	 * selects list item containing string eg. a list of athletes names containing a
	 * delete button
	 * 
	 * @param list
	 * @param option
	 * @param target
	 */
	public static void selectListItemContainsByName(EnhancedBy list, String option, EnhancedBy target) {
		ListHelper.selectListItemContainsByName(list, option, target);
	}
	
	/**
	 * Selects list item from a parent container eg. delete button in a list defined
	 * by name find the container containing the name and then finds the delete
	 * button in that container as target
	 * 
	 * @param list
	 * @param option
	 * @param target
	 */
	public static void selectListItemContainsFromContainer(EnhancedBy list, String option, EnhancedBy target) {
		ListHelper.selectListItemContainsFromContainer(list, option, target);
	}
	
	/**
	 * finds target element which is in the same container and has the same index as
	 * the parent eg. delete button in the list of customers, both having index 2.
	 * we find the index containing name, and use that to find the target element
	 * 
	 * @param list
	 * @param option
	 */
	public static void selectListItemContainsByName(EnhancedBy list, String option) {
		ListHelper.selectListItemContainsByName(list, option);
	}
	
	/**
	 * selects list item by the string option provided
	 * 
	 * @param list
	 * @param option
	 */
	public static void selectListItemByIndex(EnhancedBy list, int index) {;
	   ListHelper.selectListItemByIndex(list, index);
	}
	
	/**
	 * returns the number of elements in list
	 * 
	 * @param list
	 * @return
	 */
	public static int getListCount(EnhancedBy list) {
		return ListHelper.getListCount(list);
	}
	
	/**
	 * returns the index of text value in a list
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public static int getElementIndexEqualsByText(EnhancedBy list, String option) {
		return ListHelper.getElementIndexEqualsByText(list, option);
	}
	
	/**
	 * retuns index of element in list which contains in text
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public static int getElementIndexContainByText(EnhancedBy list, String option) {
		return ListHelper.getElementIndexContainByText(list, option);
	}
	
	/**
	 * returns the index of string value in list of strings
	 * @param stringList  normalized
	 * @param option  normalized
	 * @return
	 */
	public static int getStringIndexContainByText(List<String> stringList, String option) {
		return ListHelper.getStringIndexContainByText(stringList, option);
	}
	
	/**
	 * verifies if option value is in the list index = -1 indicates the value is not
	 * in list
	 * 
	 * @param list
	 * @param option
	 */
	public static void verifyContainsIsInList(EnhancedBy list, String option) {
		ListHelper.verifyContainsIsInList(list, option);
	}
	
	/**
	 * verifies if option value is in the list index = -1 indicates the value is not
	 * in list
	 * 
	 * @param list
	 * @param option
	 */
	public static void verifyIsInList(EnhancedBy list, String option) {
		ListHelper.verifyIsInList(list, option);
	}
	
	/**
	 * verify text option in list based on key value in the list
	 * 
	 * @param list
	 * @param indicator
	 * @param option
	 */
	public static void verifyIsInList(EnhancedBy list, String indicator, String option) {
		ListHelper.verifyIsInList(list, indicator, option);
	}
	
	/**
	 * return if element is contained in list
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public static boolean isContainedInList(EnhancedBy list, String option) {
		return ListHelper.isContainedInList(list, option);
	}
	
	/**
	 * return if element is an exact match in list
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public static boolean isExactMatchInList(EnhancedBy list, String option) {
		return ListHelper.isExactMatchInList(list, option);
	}
	
	/**
	 * returns the list of values in a list 
	 * @param list
	 * @return
	 */
	public static List<String> getListValues(EnhancedBy list) {
		return ListHelper.getListValues(list);
	}
	
	// MobileHelper
	
	/**
	 * hides ios or android keyboard
	 * 
	 */
	public static void mobile_hideKeyboard() {
		MobileHelper.hideKeyboard();
	}
	
	/**
	 * select enter on android
	 */
	public static void pressEnterOnAndroid() {
		MobileHelper.pressEnterOnAndroid();
	}
	
	/**
	 * resets the app
	 */
	public static void mobile_resetApp() {
		MobileHelper.resetApp();
	}
	
	/**
	 * returns if mobile driver is used
	 * @return
	 */
	public static boolean mobile_isMobile() {
		return MobileHelper.isMobile();
	}
	
	/**
	 * is ios driver
	 * @return
	 */
	public static boolean mobile_isIOS() {
		return MobileHelper.isIOS();
	}
	
	/**
	 * sets gps location on ios simulator
	 * @param location
	 */
	public static void mobile_setLocation(Location location) {
		MobileHelper.setLocation(location);
	}
	
	/**
	 * ios gesture
	 * https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/ios/ios-xctest-mobile-gestures.md#mobile-scroll
	 * @param element
	 */
	public static void mobile_scrollToiOS(EnhancedBy element) {
		MobileHelper.scrollToiOS(element);
	}
	
	 /**
	  * swipe right on the screen
	  */
  public static void mobile_swipeRight(EnhancedBy element) {
	  MobileHelper.swipeRight(element);
  }
  
  // PageHelper
  
	/**
	 * maximizes web page
	 */
	public static void maximizePage() {
		PageHelper.maximizePage();
	}
	
	/**
	 * sets page size
	 * 
	 * @param x
	 * @param y
	 */
	public static void setPageSize(int x, int y) {
		PageHelper.setPageSize(x, y);
	}
	
	/**
	 * reload page
	 */
	public static void refreshPage() {
		PageHelper.refreshPage();
	}
	
	/**
	 * switches frame to frame specified
	 * 
	 * @param frame
	 */
	public static void switchIframe(EnhancedBy frame) {
		PageHelper.switchIframe(frame);
	}
	
	/**
	 * switches to default frame
	 */
	public static void switchToDefaultFrame() {
		PageHelper.switchToDefaultFrame();
	}
	
	/**
	 * dismisses alert by selecting ok or cancel
	 * return alert message
	 * @param alert
	 */
	public static String dimissAlert() {
		return PageHelper.dimissAlert();
	}
	
	/**
	 * accepts alert by selecting ok or cancel
	 * 
	 * @param alert
	 */
	public static String acceptAlert() {
		return PageHelper.acceptAlert();
	}
	
	/**
	 * returns alert message value
	 * @return
	 */
	public static String getAlertValue() {
		return PageHelper.getAlertValue();
	}
	
	/**
	 * return the current window handle
	 * 
	 * @return
	 */
	public static String currentWindow() {
		return PageHelper.currentWindow();
	}
	
	/**
	 * switch to the new opened window
	 * 
	 * @param defaultWindow
	 */
	public static void switchToNewWindow(String defaultWindow) {
		PageHelper.switchToNewWindow(defaultWindow);
	}
	
	/**
	 * close the window and return to the defaultWindow
	 * 
	 * @param defaultWindow
	 */
	public static void CloseAndReturn(String defaultWindow) {
		PageHelper.CloseAndReturn(defaultWindow);
	}
	
	// StopWatchHelper
	
    /**
     *   StopWatchHelper watch = Helper.start();
    	do something
    long passedTimeInMs = watch.time();
    long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
     * @return
     */
    public static StopWatchHelper start() {
    	   return StopWatchHelper.start();
    }
    
    // UtilityHelper
    
	/**
	 * generates random string of length len
	 * @param len
	 * @return
	 */
	public static String generateRandomString(int len) {
		return UtilityHelper.generateRandomString(len);
	}
    
	/**
	 * normalizes string
	 * removes space, new line chars
	 * @param value
	 * @return
	 */
	public static String stringNormalize(String value) {
		return UtilityHelper.stringNormalize(value);
	}
    
	// WaitHelper
	
	/**
	 * waits for element to be displayed for amount of time specified by 60 seconds
	 * 
	 * @param target
	 */
	public static void waitForElementToLoad(final EnhancedBy target) {
		WaitHelper.waitForElementToLoad(target);
	}
	
	/**
	 * waits for element to load count() checks if the element is displayed then
	 * gets the count number
	 * 
	 * @param target
	 * @param time
	 */
	public static boolean waitForElementToLoad(final EnhancedBy target, int time) {
		return WaitHelper.waitForElementToLoad(target, time);
	}
	
	/**
	 * waits for element to load
	 * If mobile device, scrolls down the page until element is visible
	 * @param target:
	 *            element to wait for
	 * @param time:
	 *            max time to wait
	 * @param count:
	 *            minimum count of elements to wait for in list
	 * @return
	 */
	public static boolean waitForElementToLoad(final EnhancedBy target, int time, int count) {
		return WaitHelper.waitForElementToLoad(target, time, count);
	}
	
	/**
	 * waits for element count to increase from the originalCount Usefull when
	 * waiting for a list to expand with additional items
	 * 
	 * @param target
	 * @param originalCount
	 */
	public static void waitForAdditionalElementsToLoad(final EnhancedBy target, final int originalCount) {
		WaitHelper.waitForAdditionalElementsToLoad(target, originalCount);
	}
	
	/**
	 * waits for element to not be displayed wait for maximum of 60 seconds
	 * 
	 * @param target
	 */
	public static void waitForElementToBeRemoved(final EnhancedBy target) {
		WaitHelper.waitForElementToBeRemoved(target);
	}
	
	/**
	 * waits for element to not be displayed
	 * 
	 * @param target
	 * @param time
	 *            : maximum amount of time in seconds to wait
	 */
	public static boolean waitForElementToBeRemoved(final EnhancedBy target, int time) {
		return WaitHelper.waitForElementToBeRemoved(target, time);
	}
	
	/**
	 * waits for number of seconds
	 * 
	 * @param seconds
	 */
	public static void waitForSeconds(double seconds) {
		WaitHelper.waitForSeconds(seconds);
	}
	
	/**
	 * waits for webpage to load
	 */
	public static void waitForPageToLoad() {
		WaitHelper.waitForPageToLoad();
	}
	
	/**
	 * waits for item containing in list to load
	 * 
	 * @param list
	 * @param option
	 * @param time
	 */
	public static void waitForListItemToLoad_Contains(final EnhancedBy list, String option) {
		WaitHelper.waitForListItemToLoad_Contains(list, option);
	}
	
	/**
	 * waits for text to be loaded for amount of time specified by 60 seconds
	 * 
	 * @param target
	 */
	public static void waitForTextToLoad(final EnhancedBy target, String text) {
		WaitHelper.waitForTextToLoad(target, text);
	}
	
	/**
	 * make sure only one element and caller needs to take responsibility to have
	 * text in the element
	 * 
	 * @param target
	 * @param time
	 */
	public static void waitForTextToLoad(final EnhancedBy target, int time, String text) {
		WaitHelper.waitForTextToLoad(target, text);
	}
	
	/**
	 * wait for element to become clickable
	 * @param selector
	 */
    public void waitForElementToBeClickable(EnhancedBy selector) {
    		WaitHelper.waitForElementToBeClickable(selector);
    }	
}