package core.support.objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.safari.SafariOptions;
import org.testng.annotations.Test;

import core.support.objects.ActionObject.ACTION;
import core.uiCore.webElement.EnhancedBy;

public class SupportObjectBuildersTest {

	@Test
	public void actionObjectDefaultsAreSafeAndEmpty() {
		ActionObject action = new ActionObject();

		assertNull(action.getElement1());
		assertNull(action.getElement2());
		assertNull(action.getElement3());
		assertEquals(action.getValue(), "");
		assertNull(action.getAction());
	}

	@Test
	public void actionObjectFluentBuilderStoresAllValues() {
		EnhancedBy first = new EnhancedBy();
		EnhancedBy second = new EnhancedBy();
		EnhancedBy third = new EnhancedBy();
		ActionObject action = new ActionObject();

		assertSame(action.withElement1(first), action);
		assertSame(action.withElement2(second), action);
		assertSame(action.withElement3(third), action);
		assertSame(action.withValue("value"), action);
		assertSame(action.withAction(ACTION.OPTIONAL_CLICK_AND_EXPECT), action);

		assertSame(action.getElement1(), first);
		assertSame(action.getElement2(), second);
		assertSame(action.getElement3(), third);
		assertEquals(action.getValue(), "value");
		assertEquals(action.getAction(), ACTION.OPTIONAL_CLICK_AND_EXPECT);
	}

	@Test
	public void loginObjectDefaultsToLoggedOutAndEmptyCredentials() {
		LoginObject login = new LoginObject();

		assertEquals(login.getUsername(), "");
		assertEquals(login.getPassword(), "");
		assertEquals(login.getLoggedInUsername(), "");
		assertEquals(login.getLoggedInPassword(), "");
		assertFalse(login.getIsLoggedIn());
		assertEquals(login.getLoginSequence().size(), 0);
	}

	@Test
	public void loginObjectFluentBuilderKeepsCredentialsAndOrderedSequence() {
		ActionObject first = new ActionObject().withAction(ACTION.FIELD);
		ActionObject second = new ActionObject().withAction(ACTION.SUBMIT);
		LoginObject login = new LoginObject();

		assertSame(login.withUsername("alice"), login);
		assertSame(login.withPassword("secret"), login);
		assertSame(login.withLoggedInUsername("alice"), login);
		assertSame(login.withLoggedInPassword("secret"), login);
		assertSame(login.withIsLoggedIn(true), login);
		assertSame(login.withLoginSequence(first), login);
		assertSame(login.withLoginSequence(second), login);

		assertEquals(login.getUsername(), "alice");
		assertEquals(login.getPassword(), "secret");
		assertEquals(login.getLoggedInUsername(), "alice");
		assertEquals(login.getLoggedInPassword(), "secret");
		assertEquals(login.getIsLoggedIn(), Boolean.TRUE);
		assertEquals(login.getLoginSequence().size(), 2);
		assertSame(login.getLoginSequence().get(0), first);
		assertSame(login.getLoginSequence().get(1), second);
	}

	@Test
	public void driverOptionRoundTripsEverySupportedBrowserOption() {
		ChromeOptions chrome = new ChromeOptions();
		FirefoxOptions firefox = new FirefoxOptions();
		EdgeOptions edge = new EdgeOptions();
		SafariOptions safari = new SafariOptions();
		InternetExplorerOptions ie = new InternetExplorerOptions();
		DriverOption options = new DriverOption();

		assertSame(options.withChromeOptions(chrome), options);
		assertSame(options.withFirefoxOptions(firefox), options);
		assertSame(options.withEdgeOptions(edge), options);
		assertSame(options.withSafariOptions(safari), options);
		assertSame(options.withInternetExplorerOptions(ie), options);

		assertSame(options.getChromeOptions(), chrome);
		assertSame(options.getFirefoxOptions(), firefox);
		assertSame(options.getEdgeOptions(), edge);
		assertSame(options.getSafariOptions(), safari);
		assertSame(options.getInternetExplorerOptions(), ie);
	}
}
