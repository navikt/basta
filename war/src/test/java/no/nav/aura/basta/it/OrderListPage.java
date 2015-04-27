package no.nav.aura.basta.it;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OrderListPage {

	private WebDriver driver;
	private String baseUrl;

	public OrderListPage(WebDriver driver, String baseUrl) {
		this.driver = driver;
		this.baseUrl = baseUrl;
		driver.get(baseUrl);
	}

	public List<WebElement> getOrders() {
		List<WebElement> orderLinks = driver.findElements(By.partialLinkText("#"));
		// List<WebElement> orderLinks =
		// driver.findElements(By.xpath("//a[@href='#/order_details/*]'"));
		return orderLinks;
	}

	public OrderPage clickOnOrderLink(WebElement orderLink) {

		orderLink.click();
		return new OrderPage(driver);
	}

}
