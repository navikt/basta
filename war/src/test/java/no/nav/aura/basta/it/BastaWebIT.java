package no.nav.aura.basta.it;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import no.nav.aura.basta.JettyTest;
import no.nav.aura.basta.it.pages.LoginPage;
import no.nav.aura.basta.it.pages.OrderListPage;
import no.nav.aura.basta.it.pages.OrderPage;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class BastaWebIT extends JettyTest {
	private static WebDriver driver;
	private static String baseUrl;

	@BeforeClass
	public static void openBrowser() {
		// driver = new ChromeDriver();
		jetty.createTestData();
		ArrayList<String> cliArgsCap = new ArrayList<String>();
		DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
		cliArgsCap.add("--web-security=false");
		cliArgsCap.add("--ssl-protocol=any");
		cliArgsCap.add("--ignore-ssl-errors=true");
		cliArgsCap.add("--webdriver-loglevel=INFO");

		capabilities.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS, true);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
		capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);
		driver = new PhantomJSDriver(capabilities);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		
		baseUrl = "http://localhost:" + jetty.getPort();
	}

	@AfterClass
	public static void cleanup() {
		driver.quit();
	}


	@Test
	public void titleShouldBeSet() {
		driver.get(baseUrl);
		Assert.assertEquals("basta", driver.getTitle());
	}

	@Test
	public void shouldShowOrderListAsFirstPage() {
		OrderListPage orderListPage = new OrderListPage(driver, baseUrl);
		// System.out.println(orderListPage.getTitle());
		assertThat(orderListPage.getHeader(), containsString("Order history"));
		List<WebElement> orders = orderListPage.getOrders();
		assertEquals(1, orders.size());
		OrderPage orderPage = orderListPage.clickOnOrderLink(orders.get(0));
		assertThat(orderPage.getHeader(), containsString("Jboss"));
	}

	@Test
	 public void shouldShowOrderPageByDirectUrl() {
		OrderPage orderPage = new OrderPage(driver, baseUrl, 1);
		String title = orderPage.getHeader();
		assertThat(title, containsString("1"));
		assertThat(title, containsString("Create Vm of type Jboss"));
	 }

	// Virker ikke enda. Finner ikke login
	public void shouldLogin() {
		LoginPage loginPage = new LoginPage(driver, baseUrl);
        assertFalse("not logged in", loginPage.isLoggedIn());
		loginPage.login("user", "user");
		assertTrue("logged in", loginPage.isLoggedIn());

	}

}
