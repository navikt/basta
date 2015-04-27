package no.nav.aura.basta.it;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OrderPage {

	private WebDriver driver;

	public OrderPage(WebDriver driver) {
		this.driver = driver;
	}

	public String getTitle() {
		WebElement h2 = driver.findElement(By.tagName("h2"));
		// System.out.println(h2);
		return h2.getText();

	}

}
