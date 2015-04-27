package no.nav.aura.basta.it;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import no.nav.aura.basta.JettyTest;

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
		cliArgsCap.add("--load-images=false");

		capabilities.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS, true);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
		capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);
		driver = new PhantomJSDriver(capabilities);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		
		baseUrl = "http://localhost:" + jetty.getPort();
	}

	@AfterClass
	public static void cleanup() {
		driver.quit();
	}


	@Test
	public void headerShouldBeSet() {
		System.out.println("getting");
		driver.get(baseUrl);
		Assert.assertEquals("basta", driver.getTitle());
	}

	@Test
	public void shouldShowOrderListAsFirstPage() {
		OrderListPage orderListPage = new OrderListPage(driver, baseUrl);
		List<WebElement> orders = orderListPage.getOrders();
		assertEquals(1, orders.size());
		// OrderPage orderPage = orderListPage.clickOnOrderLink(orders.get(0));
		// assertEquals("1", orderPage.getTitle());
	}

	// @Test
	// public void shouldShowOrderPageByDirectUrl() {
	// OrderPage orderListPage = new OrderPage(driver,);
	// assertEquals(1, orderListPage.getOrders().size());
	// }

}
