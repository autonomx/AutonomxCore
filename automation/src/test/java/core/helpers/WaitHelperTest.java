package core.helpers;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

import core.uiCore.webElement.EnhancedBy;

public class WaitHelperTest {

	@Test
	public void elementLoadOverloadsRemainPublicBooleanContracts() throws Exception {
		assertPublicBoolean("waitForElementToLoad", EnhancedBy.class);
		assertPublicBoolean("waitForElementToLoad", EnhancedBy.class, int.class);
		assertPublicBoolean("waitForElementToLoad", EnhancedBy.class, int.class, int.class);
	}

	@Test
	public void visibilityContractRemainsPublicBoolean() throws Exception {
		assertPublicBoolean("waitForElementToBeVisible", EnhancedBy.class, int.class, int.class);
	}

	@Test
	public void clickableOverloadsRemainPublicBooleanContracts() throws Exception {
		assertPublicBoolean("waitForElementToBeClickable", EnhancedBy.class);
		assertPublicBoolean("waitForElementToBeClickable", EnhancedBy.class, int.class);
	}

	@Test
	public void removalOverloadsRemainPublicBooleanContracts() throws Exception {
		assertPublicBoolean("waitForElementToBeRemoved", EnhancedBy.class);
		assertPublicBoolean("waitForElementToBeRemoved", EnhancedBy.class, int.class);
		assertPublicBoolean("waitForElementToBeRemoved", EnhancedBy.class, int.class, int.class);
	}

	private static void assertPublicBoolean(String name, Class<?>... parameterTypes) throws Exception {
		Method method = WaitHelper.class.getMethod(name, parameterTypes);
		assertTrue(Modifier.isPublic(method.getModifiers()), name + " must stay public");
		assertEquals(method.getReturnType(), boolean.class, name + " must return boolean");
	}
}
