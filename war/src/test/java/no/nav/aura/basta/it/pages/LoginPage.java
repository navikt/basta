package no.nav.aura.basta.it.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage {

	private WebDriver driver;

	public LoginPage(WebDriver driver, String url) {
		this.driver = driver;
		driver.get(url);
	}


	public void login(String user, String password) {
        WebElement loginLink = driver.findElement(By.id("login_link"));
        WebElement logoutLink = driver.findElement(By.id("logout_link"));
        loginLink.click();
		WebElement loginForm = driver.findElement(By.id("loginForm"));
		WebElement usernameInput = loginForm.findElement(By.id("login_username"));
		WebElement passwordInput = loginForm.findElement(By.id("login_password"));
		usernameInput.sendKeys(user);
		passwordInput.sendKeys(password);
		passwordInput.submit();
	}

	public boolean isLoggedIn() {
        WebElement currentUser = driver.findElement(By.id("currentUser"));
        System.out.println(currentUser.getText());
        return !currentUser.getText().isEmpty();
	}

}
