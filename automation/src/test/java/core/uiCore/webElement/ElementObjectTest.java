package core.uiCore.webElement;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import core.helpers.Element.LocatorType;

public class ElementObjectTest {

	@Test
	public void fullConstructorStoresLocatorMetadata() {
		By by = By.id("account");
		ElementObject object = new ElementObject(by, "Account", "account", LocatorType.id);

		assertSame(object.by, by);
		assertEquals(object.name, "Account");
		assertEquals(object.locator, "account");
		assertEquals(object.locatorType, LocatorType.id);
	}

	@Test
	public void shortConstructorStoresByAndName() {
		By by = By.cssSelector(".menu");
		ElementObject object = new ElementObject(by, "Menu");

		assertSame(object.by, by);
		assertEquals(object.name, "Menu");
	}

	@Test
	public void fluentMutatorsUpdateSameInstance() {
		ElementObject object = new ElementObject(By.id("before"), "Before");
		By replacement = By.name("after");

		assertSame(object.withBy(replacement), object);
		assertSame(object.withName("After"), object);
		assertSame(object.by, replacement);
		assertEquals(object.name, "After");
	}
}
