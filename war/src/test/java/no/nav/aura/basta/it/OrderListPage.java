package no.nav.aura.basta.it;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

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
		FluentIterable<WebElement> filteredLinks = FluentIterable.from(orderLinks).filter(new Predicate<WebElement>() {
			@Override
			public boolean apply(WebElement input) {
				return !"#".equals(input.getText());
			}

		});

		return filteredLinks.toList();
	}

	public String getHeader() {
		return driver.findElement(By.tagName("h2")).getText();
	}

	public OrderPage clickOnOrderLink(WebElement orderLink) {
		orderLink.click();
		return new OrderPage(driver);
	}

}
