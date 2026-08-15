package core.support.objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import core.uiCore.driverProperties.driverType.DriverType;

public class DriverObjectTest {

	@BeforeMethod
	public void resetDriverRegistry() {
		DriverObject.driverList.clear();
	}

	@AfterMethod
	public void cleanupDriverRegistry() {
		DriverObject.driverList.clear();
	}

	@Test
	public void firstAvailableDriverIsClaimedAndMarkedUnavailable() {
		WebDriver busy = fakeWebDriver("busy");
		WebDriver available = fakeWebDriver("available");
		DriverObject.driverList.put(busy, new DriverObject().withIsAvailable(false));
		DriverObject.driverList.put(available, new DriverObject().withIsAvailable(true));

		WebDriver claimed = DriverObject.getFirstAvailableDriver();

		assertSame(claimed, available);
		assertFalse(DriverObject.driverList.get(available).isAvailable);
	}

	@Test
	public void noDriverIsReturnedWhenEveryDriverIsBusy() {
		WebDriver first = fakeWebDriver("first");
		WebDriver second = fakeWebDriver("second");
		DriverObject.driverList.put(first, new DriverObject().withIsAvailable(false));
		DriverObject.driverList.put(second, new DriverObject().withIsAvailable(false));

		assertNull(DriverObject.getFirstAvailableDriver());
	}

	@Test
	public void concurrentClaimsNeverAllocateTheSameDriverTwice() throws Exception {
		int driverCount = 8;
		for (int i = 0; i < driverCount; i++) {
			DriverObject.driverList.put(fakeWebDriver("driver-" + i), new DriverObject().withIsAvailable(true));
		}

		ExecutorService executor = Executors.newFixedThreadPool(driverCount * 2);
		try {
			List<Callable<WebDriver>> claims = new ArrayList<Callable<WebDriver>>();
			for (int i = 0; i < driverCount * 2; i++)
				claims.add(DriverObject::getFirstAvailableDriver);

			List<Future<WebDriver>> futures = executor.invokeAll(claims);
			List<WebDriver> claimed = new ArrayList<WebDriver>();
			for (Future<WebDriver> future : futures) {
				WebDriver driver = future.get();
				if (driver != null)
					claimed.add(driver);
			}

			Set<WebDriver> unique = Collections.synchronizedSet(new HashSet<WebDriver>(claimed));
			assertEquals(claimed.size(), driverCount);
			assertEquals(unique.size(), driverCount);
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void builderMethodsPreserveCoreDriverMetadata() {
		DriverOption options = new DriverOption();
		DriverObject driver = new DriverObject();

		assertSame(driver.withApp("portal"), driver);
		assertSame(driver.withUrl("https://example.test/start"), driver);
		assertSame(driver.withDriverVersion("123"), driver);
		assertSame(driver.withDriverType(DriverType.LOCAL_WEBDRIVER), driver);
		assertSame(driver.withDriverOptions(options), driver);

		assertEquals(driver.app, "portal");
		assertEquals(driver.initialURL, "https://example.test/start");
		assertEquals(driver.driverVersion, "123");
		assertEquals(driver.driverType, DriverType.LOCAL_WEBDRIVER);
		assertSame(driver.getOptions(), options);
	}

	@Test
	public void validInitialUrlIsParsedAndInvalidUrlReturnsNull() {
		DriverObject valid = new DriverObject().withUrl("https://example.test/path?q=1");
		URL parsed = valid.getInitURL();

		assertNotNull(parsed);
		assertEquals(parsed.getProtocol(), "https");
		assertEquals(parsed.getHost(), "example.test");
		assertEquals(parsed.getPath(), "/path");

		assertNull(new DriverObject().withUrl("not a url").getInitURL());
	}

	@Test
	public void apiAndGenericFactoriesProduceApiDriverObjects() {
		DriverObject api = new DriverObject().withApiDriver("service-api");
		DriverObject generic = new DriverObject().withGenericDriver("generic-service");

		assertEquals(api.driverType, DriverType.API);
		assertEquals(api.app, "service-api");
		assertEquals(generic.driverType, DriverType.API);
		assertEquals(generic.app, "generic-service");
	}

	@Test
	public void availabilityBuilderCanToggleState() {
		DriverObject driver = new DriverObject();
		assertTrue(driver.isAvailable);

		driver.withIsAvailable(false);
		assertFalse(driver.isAvailable);

		driver.withIsAvailable(true);
		assertTrue(driver.isAvailable);
	}

	private static WebDriver fakeWebDriver(String id) {
		return (WebDriver) Proxy.newProxyInstance(
				DriverObjectTest.class.getClassLoader(),
				new Class<?>[] { WebDriver.class },
				(proxy, method, args) -> {
					String methodName = method.getName();
					if ("hashCode".equals(methodName))
						return System.identityHashCode(proxy);
					if ("equals".equals(methodName))
						return proxy == args[0];
					if ("toString".equals(methodName))
						return "FakeWebDriver(" + id + ")";
					Class<?> returnType = method.getReturnType();
					if (!returnType.isPrimitive())
						return null;
					if (returnType == boolean.class)
						return false;
					if (returnType == char.class)
						return '\0';
					if (returnType == byte.class)
						return (byte) 0;
					if (returnType == short.class)
						return (short) 0;
					if (returnType == int.class)
						return 0;
					if (returnType == long.class)
						return 0L;
					if (returnType == float.class)
						return 0F;
					if (returnType == double.class)
						return 0D;
					return null;
				});
	}
}
