package core.helpers;

import java.util.List;

import core.logger.TestLog;
import core.webElement.EnhancedBy;
import core.webElement.EnhancedWebElement;

public class ListHelper {

	/**
	 * selects an element in list by its index value
	 * 
	 * @param list
	 * @param index
	 */
	public static void selectElementInList(EnhancedBy list, int index) {

		EnhancedWebElement listElement = Element.findElements(list);
		listElement.click(index);
	}

	/**
	 * selects an element in list by its index value and waits for expected element
	 * 
	 * @param list
	 * @param index
	 * @param expected
	 */
	public static void selectElementInList(EnhancedBy list, int index, EnhancedBy expected) {

		ClickHelper.clickAndExpect(list, index, expected);
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

		FormHelper.setFieldAndEnter(searchQuery, byTarget);
		WaitHelper.waitForElementToBeRemoved(spinner);
	}

	/**
	 * selects list item by the string option provided
	 * 
	 * @param list
	 * @param option
	 */
	public static void selectListItemEqualsByName(EnhancedBy list, String option) {
		
		WaitHelper.waitForElementToLoad(list);
		int index = getElementIndexEqualsByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);
		selectElementInList(list, index);
		TestLog.logPass("I select list option '" + option + "' from list '" + list.name + "'");
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
		WaitHelper.waitForElementToLoad(list);
		int index = getElementIndexEqualsByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);

		selectElementInList(target, index);
		TestLog.logPass("I select list option '" + option + "' from list '" + list.name + "'");
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
		WaitHelper.waitForElementToLoad(list);
		int index = getElementIndexContainByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);

		selectElementInList(target, index);
		TestLog.logPass("I select list option '" + option + "' from list '" + list.name + "'");
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
		WaitHelper.waitForElementToLoad(list);
		int index = getElementIndexContainByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);

		EnhancedWebElement containerList = Element.findElements(list);
		EnhancedWebElement targetElement = Element.findElements(target, containerList.get(index));

		targetElement.click();
		TestLog.logPass("I select list option '" + option + "' from list '" + list.name + "'");
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
		WaitHelper.waitForElementToLoad(list);
		int index = getElementIndexContainByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);
		selectElementInList(list, index);
		TestLog.logPass("I select list option '" + option + "' from list '" + list.name + "'");
	}

	/**
	 * selects list item by the string option provided
	 * 
	 * @param list
	 * @param option
	 */
	public static void selectListItemByIndex(EnhancedBy list, int index) {;
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);
		selectElementInList(list, index);
		TestLog.logPass("I select list option at index'" + index + "' from list '" + list.name + "'");
	}

	/**
	 * returns the number of elements in list
	 * 
	 * @param list
	 * @return
	 */
	public static int getListCount(EnhancedBy list) {
		EnhancedWebElement listElements = Element.findElements(list);
		return listElements.count();
	}

	/**
	 * returns the index of text value in a list
	 * normalizes the list option when comparing using Helper.stringNormalize()
	 * @param list
	 * @param option
	 * @return
	 */
	
	public static int getElementIndexEqualsByText(EnhancedBy list, String option) {

		int optionIndex = -1;
		WaitHelper.waitForListItemToLoad_Contains(list, option);
		EnhancedWebElement listElements = Element.findElements(list);
	    List<String> stringList =  listElements.getTextList();
        
	    String value = null;
		 int listCount = stringList.size();
		 for (int i = 0; i < listCount; i++) {
			 value = Helper.stringNormalize(stringList.get(i));
			if (value.equalsIgnoreCase(option)) {
				optionIndex = i;
				break;
			}
		}
	    return optionIndex;
	}
	

	/**
	 * retuns index of element in list which contains in text
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public static int getElementIndexContainByText(EnhancedBy list, String option) {
		WaitHelper.waitForListItemToLoad_Contains(list, option);
		EnhancedWebElement listElements = Element.findElements(list);
	    List<String> stringList =  listElements.getTextList();

	    return getStringIndexContainByText(stringList, option);
	}
	
	/**
	 * returns the index of string value in list of strings
	 * @param stringList  normalized
	 * @param option  normalized
	 * @return
	 */
	public static int getStringIndexContainByText(List<String> stringList, String option) {
		int optionIndex = -1;
		String listValue;
	    
	    int listCount = stringList.size();
		 for (int i = 0; i < listCount; i++) {
			 listValue = UtilityHelper.stringNormalize(stringList.get(i));
			 option = UtilityHelper.stringNormalize(option);
			if (listValue.contains(option)) {
				optionIndex = i;
				break;
			}
		}
	    return optionIndex;	
	}

	/**
	 * verifies if option value is in the list index = -1 indicates the value is not
	 * in list
	 * 
	 * @param list
	 * @param option
	 */
	public static void verifyContainsIsInList(EnhancedBy list, String option) {
		WaitHelper.waitForElementToLoad(list);
		int index = getElementIndexContainByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);
	}

	/**
	 * verifies if option value is in the list index = -1 indicates the value is not
	 * in list
	 * 
	 * @param list
	 * @param option
	 */
	public static void verifyIsInList(EnhancedBy list, String option) {
		WaitHelper.waitForElementToLoad(list);
		int index = getElementIndexEqualsByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);
	}

	/**
	 * verify text option in list based on key value in the list
	 * 
	 * @param list
	 * @param indicator
	 * @param option
	 */
	public static void verifyIsInList(EnhancedBy list, String indicator, String option) {
		int index = getElementIndexEqualsByText(list, option);
		EnhancedWebElement listElements = Element.findElements(list);
		boolean isInList = listElements.getText(index).contains(option);
		AssertHelper.assertTrue("option not found in list: " + list.name, isInList);
	}

	/**
	 * return if element is contained in list
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public static boolean isContainedInList(EnhancedBy list, String option) {
		EnhancedWebElement listElements = Element.findElements(list);
	    List<String> stringList =  listElements.getTextList();
	    
	    int index = getStringIndexContainByText(stringList, option);
		return index != -1;
	}
	
	/**
	 * return if element is an exact match in list
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public static boolean isExactMatchInList(EnhancedBy list, String option) {
		int index = getElementIndexEqualsByText(list, option);
		return index != -1;
	}
	
	/**
	 * returns the list of values in a list 
	 * @param list
	 * @return
	 */
	public static List<String> getListValues(EnhancedBy list) {
		EnhancedWebElement listElements = Element.findElements(list);
	    return listElements.getTextList();
	}
}