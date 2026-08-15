package core.support.objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import core.support.objects.TestObject.testState;

public class TestObjectStateTest {

	@BeforeMethod
	public void resetStaticState() {
		TestObject.currentTestId.remove();
		TestObject.currentTestName.remove();
		TestObject.testInfo.clear();
	}

	@AfterMethod
	public void cleanupStaticState() {
		TestObject.currentTestId.remove();
		TestObject.currentTestName.remove();
		TestObject.testInfo.clear();
	}

	@Test
	public void testStateClassificationRecognizesLifecycleIdsCaseInsensitively() {
		assertEquals(TestObject.getTestState("suite-BeFoReSuItE"), testState.beforeSuite);
		assertEquals(TestObject.getTestState("suite-AfterSuite"), testState.suite);
		assertEquals(TestObject.getTestState("Checkout-BeforeClass"), testState.testClass);
		assertEquals(TestObject.getTestState("Checkout-AfterClass"), testState.testClass);
		assertEquals(TestObject.getTestState("orders-Parent"), testState.parent);
		assertEquals(TestObject.getTestState(TestObject.DEFAULT_TEST), testState.defaultState);
		assertEquals(TestObject.getTestState("Checkout-placesOrder"), testState.testMethod);
	}

	@Test
	public void settingClassAndTestNameBuildsStableThreadLocalTestId() {
		TestObject.setTestId("Checkout", "placesOrder");

		assertEquals(TestObject.getTestId(), "Checkout-placesOrder");
		assertTrue(TestObject.hasTestStarted());
	}

	@Test
	public void objectTestIdDerivesClassAndFullTestName() {
		TestObject object = new TestObject().withTestId("Checkout-completes-order-with-card");

		assertEquals(object.className, "Checkout");
		assertEquals(object.getClassName(), "Checkout");
		assertEquals(object.getTestName(), "completes-order-with-card");
	}

	@Test
	public void invocationCountFindsHighestContiguousDataProviderRun() {
		TestObject.testInfo.put("Checkout-test1", new TestObject());
		TestObject.testInfo.put("Checkout-test2", new TestObject());

		assertEquals(TestObject.getTestInvocationCount("Checkout"), 2);
		assertFalse(TestObject.isTestObjectSet("Checkout-test3"));
	}

	@Test
	public void testIdThreadLocalIsIsolatedAcrossWorkerThreads() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Callable<String> first = () -> {
				TestObject.setTestId("ClassA", "testA");
				try {
					return TestObject.getTestId();
				} finally {
					TestObject.currentTestId.remove();
				}
			};
			Callable<String> second = () -> {
				TestObject.setTestId("ClassB", "testB");
				try {
					return TestObject.getTestId();
				} finally {
					TestObject.currentTestId.remove();
				}
			};

			Future<String> firstResult = executor.submit(first);
			Future<String> secondResult = executor.submit(second);

			assertEquals(firstResult.get(), "ClassA-testA");
			assertEquals(secondResult.get(), "ClassB-testB");
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void runCountAndLifecycleFlagsAreFluentAndDeterministic() {
		TestObject object = new TestObject();

		object.withRunCount(2)
				.incremenetRunCount()
				.withIsFirstRun(true)
				.withIsForcedRestart(true)
				.withIsTestPass(true)
				.withIsTestComplete(true);

		assertEquals(object.runCount, 3);
		assertEquals(object.isFirstRun, Boolean.TRUE);
		assertEquals(object.isForcedRestart, Boolean.TRUE);
		assertEquals(object.isTestPass, Boolean.TRUE);
		assertEquals(object.isTestComplete, Boolean.TRUE);
	}
}
