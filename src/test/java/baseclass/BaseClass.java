package baseclass;

import java.time.Duration;
import java.util.Date;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import drivers.DriverManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import utilities.ConfigReader;

public class BaseClass {
	
	public WebDriver driver;

	@BeforeClass
	public void setUp() throws InterruptedException {

		String browser = ConfigReader.getProperty("browser");
	    boolean headless = Boolean.parseBoolean(ConfigReader.getProperty("headless"));
	    DriverManager.createDriver(browser, headless);
	    driver = DriverManager.getDriver();
//	    driver.get(ConfigReader.getProperty("url"));

	    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
	    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
	    driver.manage().window().maximize(); // Optional: already added in ChromeOptions

	}

	@AfterClass
	public void tearDown() {
		//driver.manage().deleteAllCookies();
		DriverManager.quitDriver();
	}


}
