package no.nav.aura.basta.it;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OrderPage {

	private WebDriver driver;
	private String baseUrl;

	public OrderPage(WebDriver driver, String baseUrl, int orderId) {
		this.driver = driver;
		driver.get(baseUrl + "/#/order_details/" + orderId);
	}

	public OrderPage(WebDriver driver) {
		this.driver = driver;
	}

	public String getHeader() {
		System.out.println(driver.getCurrentUrl());
		WebElement h2 = driver.findElement(By.xpath("//orderdetails-header"));
		return h2.getText();

	}

}
