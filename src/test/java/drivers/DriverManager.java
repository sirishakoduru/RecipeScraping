package drivers;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;
import utilities.ConfigReader;
import utilities.LoggerReader;

public class DriverManager {
	  // ThreadLocal to manage separate WebDriver instances for each thread
    public static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    public static ConfigReader configReader;
    // Constructor initializing the configuration reader
    public DriverManager() {
        configReader = new ConfigReader();
    }
    // Responsible for creating a new driver instance
    public static void createDriver(String browser, boolean headless) {
        WebDriver webDriver;
//       LoggerLoad.info("Inside DriverManager: " + browser + ", Headless: " + headless);
        switch (browser.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--start-maximized");
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                    chromeOptions.addArguments("--disable-gpu");
                    chromeOptions.addArguments("--window-size=1920,1080");
                    chromeOptions.addArguments("--disable-extensions");
                }
                webDriver = new ChromeDriver(chromeOptions);
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
        driver.set(webDriver);
    }
    // Returns the driver associated with the current thread
    public static WebDriver getDriver() {
        return driver.get();
    }
    // Quits the driver and clears the ThreadLocal storage
    public static void quitDriver() {
        WebDriver webDriver = driver.get();
        if (webDriver != null) {
            webDriver.quit();
            driver.remove();
        }
    }

}