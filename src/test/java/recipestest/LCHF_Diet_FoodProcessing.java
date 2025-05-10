package recipestest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import baseclass.BaseClass;
import drivers.DriverManager;
import utilities.AdHandler;
import utilities.ConfigReader;
import utilities.DBConnection;
import utilities.ReceipePojo;

public class LCHF_Diet_FoodProcessing extends BaseClass {
	private static final Logger logger = LoggerFactory.getLogger(LCHF_Diet_FoodProcessing.class);

	static WebDriver driver;
	ConfigReader reader = new ConfigReader();
	List<String> urls = new ArrayList<>();
	AdHandler ads = new AdHandler();

	@BeforeClass
	public void setUp() throws InterruptedException {
		DriverManager.createDriver("chrome", false);
		driver = DriverManager.getDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
		driver.get(ConfigReader.getProperty("url"));
		scrolldown();
	}

	@Test
	public void scrapeAndFilterFoodRecipes() throws Exception {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		WebElement recipeslink = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Recipes List')]")));

		Assert.assertTrue(recipeslink.isDisplayed(), "Element is not visible");

		// Close the ad if present
		if (driver.getCurrentUrl().equals("https://www.tarladalal.com/#google_vignette")) {
			WebElement adx = driver.findElement(By.xpath("//*[contains(text(),'Close')]"));
			adx.click();
		}

		recipeslink.click();

		int currentPage = 1;
		while (true) {
			try {
				if (currentPage == 5)
					break; // Limit to 5 pages

				new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions
						.presenceOfAllElementsLocatedBy(By.xpath("//div[@class='overlay-content']//a[@href]")));

				List<WebElement> recipeLinks = driver
						.findElements(By.xpath("//div[@class='overlay-content']//a[@href]"));
				for (WebElement link : recipeLinks) {
					urls.add(link.getAttribute("href"));
				}

				currentPage++;
				WebElement nextPageLink = driver.findElement(By.xpath("//a[contains(text(),'Next')]"));
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextPageLink);
				Thread.sleep(500);
				((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -150);");
				Thread.sleep(300);
				ads.closeAdIfPresent(driver);
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextPageLink);

			} catch (Exception e) {
				logger.info("No further page found or error: " + e.getMessage());
				break;
			}
		}

		System.out.println("Collected URLs: " + urls.size());
		List<ReceipePojo> allRecipes = new ArrayList<>();

		for (String url : urls) {
			ReceipePojo recipe = extractRecipe(url);
			if (recipe != null) {
				allRecipes.add(recipe);
			}
		}

		// Define the list of acceptable food processing methods
		List<String> foodProcessingMethods = Arrays.asList("Raw", "Steamed", "Boiled", "Poached", "Sautéed", "Airfryed",
				"Pan fried");

		// Call the filtering method directly
		List<ReceipePojo> filtered = createAddList(allRecipes, foodProcessingMethods);

		for (ReceipePojo accepted : filtered) {
			DBConnection.createTable("lchf_food_processing");
			DBConnection.insertRecipe(accepted, "lchf_food_processing");
		}
	}

	// Method to filter recipes based on avoid keywords and food processing methods
	private List<ReceipePojo> createAddList(List<ReceipePojo> allRecipes, List<String> foodProcessingMethods) {
		List<ReceipePojo> acceptedRecipes = new ArrayList<>();
		int rejectedCount = 0;

		for (ReceipePojo pojo : allRecipes) {
			boolean isValid = true;

			// Combine recipe name and tag to check for unwanted keywords
			String combinedText = (pojo.recipe_name + " " + pojo.tag).toLowerCase();

			// Check if the food processing method is valid
			if (pojo.food_processing != null && !foodProcessingMethods.contains(pojo.food_processing)) {
				logger.info(
						"❌ Rejected (invalid food processing): " + pojo.food_processing + " in " + pojo.recipe_name);
				isValid = false;
			}

			// If the recipe is valid, add it to the accepted list
			if (isValid) {
				acceptedRecipes.add(pojo);
				logger.info("✅ Included: " + pojo.recipe_name);
			} else {
				rejectedCount++;
			}
		}

		// Print summary of processed recipes

		logger.info("🔍 Total processed: " + allRecipes.size());
		logger.info("✅ Accepted: " + acceptedRecipes.size());
		logger.info("❌ Rejected: " + rejectedCount);

		return acceptedRecipes;
	}

	private ReceipePojo extractRecipe(String url) {
		driver.navigate().to(url);
		ReceipePojo recipe = new ReceipePojo();

		try {
			recipe.recipe_URL = url;
			recipe.recipe_name = driver.getTitle();
			String id = url.replaceAll(".*-(\\d+)r$", "$1");
			recipe.recipe_id = id;

			WebElement prep = driver.findElement(By.xpath("//h6[text()='Preparation Time']/..//strong"));
			recipe.preparation_time = prep.getText();

			WebElement cook = driver.findElement(By.xpath("//h6[text()='Cooking Time']/..//strong"));
			recipe.cooking_time = cook.getText();

			WebElement servings = driver.findElement(By.xpath("//h6[text()='Makes ']/..//strong"));
			recipe.no_of_servings = servings.getText();

			// Extracting ingredients
			List<WebElement> ingredientElements = driver.findElements(By.xpath("//div[@class='ingredients']//p"));
			List<String> ingredients = new ArrayList<>();
			for (WebElement ingredient : ingredientElements) {
				ingredients.add(ingredient.getText().toLowerCase());
			}
			recipe.ingredients = String.join(", ", ingredients);

			// Extracting preparation method
			WebElement method = driver.findElement(By.xpath("//div[@id='methods']"));
			recipe.preparation_method = method.getText();

			// Extracting recipe tags
			WebElement Tags = driver.findElement(By.xpath("//ul[@class='tags-list']"));
			recipe.tag = Tags.getText();

			// Extracting nutrient values
			WebElement Nutrients = driver.findElement(By.id("nutrients"));
			recipe.nutrient_values = Nutrients.getText();

			// Extracting food processing method (newly added)
			WebElement processingMethod = driver.findElement(By.xpath("//h6[text()='Food Processing']/..//strong"));
			recipe.food_processing = processingMethod.getText();

		} catch (Exception e) {
			logger.info("⚠️ Failed to extract: " + url + ", reason: " + e.getMessage());
			return null;
		}

		return recipe;
	}

	public void scrolldown() throws InterruptedException {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		long initialHeight = ((Number) js.executeScript("return document.body.scrollHeight")).longValue();
		while (true) {
			js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
			Thread.sleep(1000);
			long newHeight = ((Number) js.executeScript("return document.body.scrollHeight")).longValue();
			if (newHeight == initialHeight)
				break;
			initialHeight = newHeight;
		}
	}
}
