package core.helpers;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;

public class WaitHelperTest {

	@Test
	public void defaultElementLoadDelegatesToConfiguredTimeoutAndSingleElementCount() {
		RecordingWaitHelper wait = new RecordingWaitHelper();
		EnhancedBy target = new EnhancedBy();

		assertTrue(wait.waitForElementToLoad(target));
		assertSame(wait.target, target);
		assertEquals(wait.time, AbstractDriver.TIMEOUT_SECONDS);
		assertEquals(wait.count, 1);
	}

	@Test
	public void explicitElementLoadTimeoutStillDefaultsToSingleElementCount() {
		RecordingWaitHelper wait = new RecordingWaitHelper();
		EnhancedBy target = new EnhancedBy();

		assertTrue(wait.waitForElementToLoad(target, 17));
		assertSame(wait.target, target);
		assertEquals(wait.time, 17);
		assertEquals(wait.count, 1);
	}

	@Test
	public void defaultClickableWaitDelegatesToConfiguredTimeout() {
		RecordingWaitHelper wait = new RecordingWaitHelper();
		EnhancedBy target = new EnhancedBy();

		assertTrue(wait.waitForElementToBeClickable(target));
		assertSame(wait.target, target);
		assertEquals(wait.time, AbstractDriver.TIMEOUT_SECONDS);
	}

	@Test
	public void defaultRemovalWaitDelegatesToConfiguredTimeout() {
		RecordingWaitHelper wait = new RecordingWaitHelper();
		EnhancedBy target = new EnhancedBy();

		assertTrue(wait.waitForElementToBeRemoved(target));
		assertSame(wait.target, target);
		assertEquals(wait.time, AbstractDriver.TIMEOUT_SECONDS);
	}

	@Test
	public void driverDependentWaitsFailFastWhenNoDriverIsAvailable() {
		WaitHelper wait = new WaitHelper();
		EnhancedBy target = new EnhancedBy();

		assertFalse(wait.waitForElementToLoad(target, 0, 1));
		assertFalse(wait.waitForElementToBeVisible(target, 0, 1));
		assertFalse(wait.waitForElementToBeClickable(target, 0));
	}

	private static class RecordingWaitHelper extends WaitHelper {
		private EnhancedBy target;
		private int time = -1;
		private int count = -1;

		@Override
		public boolean waitForElementToLoad(EnhancedBy target, int time, int count) {
			this.target = target;
			this.time = time;
			this.count = count;
			return true;
		}

		@Override
		public boolean waitForElementToBeClickable(EnhancedBy target, int time) {
			this.target = target;
			this.time = time;
			return true;
		}

		@Override
		public boolean waitForElementToBeRemoved(EnhancedBy target, int time) {
			this.target = target;
			this.time = time;
			return true;
		}
	}
}
