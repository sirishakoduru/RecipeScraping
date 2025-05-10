package utilities;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AdHandler {
	public static void closeAdIfPresent(WebDriver driver) {
	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
	    
	    try {
	        // Switch to the known iframe by ID
	        driver.switchTo().frame("aswift_8");
	        System.out.println("Switched to iframe: aswift_8");

	        // Optionally switch to inner iframe if the dismiss button is inside another frame
	        List<WebElement> nestedFrames = driver.findElements(By.tagName("iframe"));
	        if (!nestedFrames.isEmpty()) {
	            driver.switchTo().frame(nestedFrames.get(0));
	            System.out.println("Switched to nested iframe inside aswift_8");
	        }

	        // Now try to locate and click the dismiss button
	        WebElement dismissBtn = wait.until(ExpectedConditions.elementToBeClickable(
	            By.xpath("//div[@id='dismiss-button' and @role='button']")));
	        dismissBtn.click();
	        System.out.println("Dismiss button clicked.");
	    } catch (Exception e) {
	        System.out.println("Dismiss button not found or not clickable: " + e.getMessage());
	    } finally {
	        // Always return to the main page
	        driver.switchTo().defaultContent();
	    }
	}
}