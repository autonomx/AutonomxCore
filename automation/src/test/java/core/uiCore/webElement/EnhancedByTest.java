package core.uiCore.webElement;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import core.helpers.Element.LocatorType;

public class EnhancedByTest {

	@Test
	public void allWebLocatorFactoriesPreserveStrategyAndLocatorValue() {
		EnhancedBy selectors = new EnhancedBy()
				.byCss(".card", "card")
				.byXpath("//button", "button")
				.byId("user-id", "user")
				.byName("email", "email")
				.byClass("primary", "primary")
				.byTagName("article", "article")
				.byLinkText("Read more", "link")
				.byPartialLinkText("Read", "partial");

		assertEquals(selectors.elementObject.size(), 8);
		assertLocator(selectors, 0, LocatorType.css, ".card", By.cssSelector(".card"));
		assertLocator(selectors, 1, LocatorType.xpath, "//button", By.xpath("//button"));
		assertLocator(selectors, 2, LocatorType.id, "user-id", By.id("user-id"));
		assertLocator(selectors, 3, LocatorType.name, "email", By.name("email"));
		assertLocator(selectors, 4, LocatorType.classType, "primary", By.className("primary"));
		assertLocator(selectors, 5, LocatorType.tagName, "article", By.tagName("article"));
		assertLocator(selectors, 6, LocatorType.linkText, "Read more", By.linkText("Read more"));
		assertLocator(selectors, 7, LocatorType.partialLinkText, "Read", By.partialLinkText("Read"));
		assertEquals(selectors.name, "partial");
	}

	@Test
	public void oneArgumentTagNameUsesTagNameStrategy() {
		EnhancedBy selector = new EnhancedBy().byTagName("section");

		assertLocator(selector, 0, LocatorType.tagName, "section", By.tagName("section"));
	}

	@Test
	public void historicTagnameAliasStillUsesTagNameStrategy() {
		EnhancedBy selector = new EnhancedBy().byTagname("section");

		assertLocator(selector, 0, LocatorType.tagName, "section", By.tagName("section"));
	}

	@Test
	public void oneArgumentLinkTextUsesLinkTextStrategy() {
		EnhancedBy selector = new EnhancedBy().byLinkText("Details");

		assertLocator(selector, 0, LocatorType.linkText, "Details", By.linkText("Details"));
	}

	@Test
	public void oneArgumentPartialLinkTextUsesPartialLinkTextStrategy() {
		EnhancedBy selector = new EnhancedBy().byPartialLinkText("Detail");

		assertLocator(selector, 0, LocatorType.partialLinkText, "Detail", By.partialLinkText("Detail"));
	}

	@Test
	public void locatorFactoriesAreFluentAndKeepInsertionOrder() {
		EnhancedBy selector = new EnhancedBy();

		assertSame(selector.byId("first", "first-name"), selector);
		assertSame(selector.byCss(".second"), selector);

		assertEquals(selector.elementObject.size(), 2);
		assertEquals(selector.elementObject.get(0).locator, "first");
		assertEquals(selector.elementObject.get(1).locator, ".second");
		assertEquals(selector.elementObject.get(1).name, "first-name");
	}

	@Test
	public void accessibilityLocatorKeepsAccessibilityMetadata() {
		EnhancedBy selector = new EnhancedBy().byAccessibility("submit-button", "Submit");
		ElementObject locator = selector.elementObject.get(0);

		assertEquals(locator.locatorType, LocatorType.accessibiliy);
		assertEquals(locator.locator, "submit-button");
		assertEquals(locator.name, "Submit");
		assertTrue(locator.by.toString().toLowerCase().contains("accessibility"));
	}

	private static void assertLocator(EnhancedBy selector, int index, LocatorType type, String value, By expectedBy) {
		ElementObject locator = selector.elementObject.get(index);
		assertEquals(locator.locatorType, type);
		assertEquals(locator.locator, value);
		assertEquals(locator.by.toString(), expectedBy.toString());
	}
}
