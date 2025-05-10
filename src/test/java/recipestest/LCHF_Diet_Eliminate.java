package recipestest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import baseclass.BaseClass;
import io.github.bonigarcia.wdm.WebDriverManager;
import utilities.AdHandler;
import utilities.ConfigReader;
import utilities.DBConnection;
import utilities.ReceipePojo;

public class LCHF_Diet_Eliminate extends BaseClass {

	private List<String> recipeUrls = new ArrayList<>();
	private AdHandler ads = new AdHandler();
	
	// Exclusion list for LCHF elimination (case-insensitive)
	public static List<String> excludeIngredients = Arrays.asList("Ham", "sausage", "tinned fish", "tuna", "sardines",
			"yams", "beets", "parsnip", "turnip", "rutabagas", "carrot", "yuca", "kohlrabi", "celery root",
			"horseradish", "daikon", "jicama", "radish", "pumpkin", "squash", "Whole fat milk", "low fat milk",
			"fat free milk", "Evaporated milk", "condensed milk", "curd", "buttermilk", "ice cream", "flavored milk",
			"sweetened yogurt", "soft cheese", "grain", "Wheat", "oat", "barely", "rice", "millet", "jowar", "bajra",
			"corn", "dal", "lentil", "banana", "mango", "papaya", "plantain", "apple", "orange", "pineapple", "pear",
			"tangerine", "all melon varieties", "peach", "plum", "nectarine", "Avocado", "olive oil", "coconut oil",
			"soybean oil", "corn oil", "safflower oil", "sunflower oil", "rapeseed oil", "peanut oil", "cottonseed oil",
			"canola oil", "mustard oil", "sugar", "jaggery", "glucose", "fructose", "corn syrup", "cane sugar",
			"aspartame", "cane solids", "maltose", "dextrose", "sorbitol", "mannitol", "xylitol", "maltodextrin",
			"molasses", "brown rice syrup", "splenda", "nutra sweet", "stevia", "barley malt", "potato", "corn", "pea");

	@BeforeClass
	public void setUpTest() throws Exception {
		// Initialize WebDriver using WebDriverManager and ChromeOptions
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("profile.managed_default_content_settings.images", 2);
		options.setExperimentalOption("prefs", prefs);
		driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		// Open the homepage URL from configuration.
		driver.get(ConfigReader.getProperty("url"));
		// Call scroll method (assuming BaseClass or a helper provides this)
		scrolldown();

		// Create target database table if it doesn't exist.
		DBConnection.createTable("LCHF_Diet_Eliminate");
	}

	@Test
	public void scrapeAndInsertRecipes() throws Exception {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		JavascriptExecutor js = (JavascriptExecutor) driver;

		// Click the "Recipes List" link.
		WebElement recipesLink = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Recipes List')]")));
		Assert.assertTrue(recipesLink.isDisplayed(), "Recipes List link is not visible!");
		if (driver.getCurrentUrl().contains("#google_vignette")) {
			WebElement adClose = driver.findElement(By.xpath("//*[contains(text(),'Close')]"));
			adClose.click();
			System.out.println("Closed ad on landing page.");
		}
		recipesLink.click();

		// Verify that we are on the recipes page.
		if (!driver.getCurrentUrl().contains("https://www.tarladalal.com/recipes/")) {
			System.out.println("URL mismatch: " + driver.getCurrentUrl());
			ads.closeAdIfPresent(driver);
		}

		// Simple pagination loop: collect recipe URLs from up to n pages.
		int currentPage = 1;
		while (true) {
		    try {
		        // Wait for recipe links on the current page.
		        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
		            ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@class='overlay-content']//a[@href]"))
		        );
		        List<WebElement> recipeLinks = driver.findElements(By.xpath("//div[@class='overlay-content']//a[@href]"));
		        for (WebElement link : recipeLinks) {
		            String url = link.getAttribute("href");
		            if (url != null && url.contains("recipe")) {
		                recipeUrls.add(url);
		            }
		        }

				currentPage++;
				System.out.println("Collected URLs from page: " + driver.getCurrentUrl());
				WebElement nextPageLink = driver
						.findElement(By.xpath("//ul[contains(@class,'pagination')]//a[contains(text(),'Next')]"));
				js.executeScript("arguments[0].scrollIntoView(true);", nextPageLink);
				Thread.sleep(500);
				js.executeScript("window.scrollBy(0, -150);");
				Thread.sleep(300);
				ads.closeAdIfPresent(driver);
				Thread.sleep(300);
				// Use re-finding to avoid stale element exception.
				nextPageLink = driver.findElement(By.xpath("//a[contains(text(),'Next')]"));
				js.executeScript("arguments[0].click();", nextPageLink);
			} catch (Exception e) {
				System.out.println("Pagination ended: " + e.getMessage());
				break;
			}
		}
		System.out.println("Total recipe URLs collected: " + recipeUrls.size());

		// Process each recipe.
		for (String url : recipeUrls) {
			System.out.println("Processing recipe URL: " + url);
			extractAndInsertRecipe(url);
		}
	}

	private void extractAndInsertRecipe(String recipeURL) throws Exception {
		if (recipeURL == null || !recipeURL.contains("recipe"))
			return;
		driver.navigate().to(recipeURL);
		Thread.sleep(1000);

		ReceipePojo recipe = new ReceipePojo();
		recipe.recipe_URL = recipeURL;
		// Extract recipe ID from URL; adjust regex as needed.
		String recipeId = recipeURL.replaceAll(".*-(\\d+)r$", "$1");
		recipe.recipe_id = recipeId;
		recipe.recipe_name = driver.getTitle();

		// Extract Preparation Time.
		try {
			WebElement prepElem = driver
					.findElement(By.xpath("//div[@class='content']//h6[text()='Preparation Time']/..//strong"));
			recipe.preparation_time = prepElem.getText();
			System.out.println("Preparation Time: " + recipe.preparation_time);
		} catch (Exception e) {
			recipe.preparation_time = " ";
		}
		// Extract Cooking Time.
		try {
			WebElement cookElem = driver
					.findElement(By.xpath("//div[@class='content']//h6[text()='Cooking Time']/..//strong"));
			recipe.cooking_time = cookElem.getText();
			System.out.println("Cooking Time: " + recipe.cooking_time);
		} catch (Exception e) {
			recipe.cooking_time = " ";
		}
		// Extract Servings.
		try {
			WebElement servesElem = driver
					.findElement(By.xpath("//div[@class='content']//h6[text()='Makes ']/..//strong"));
			recipe.no_of_servings = servesElem.getText();
			System.out.println("Servings: " + recipe.no_of_servings);
		} catch (Exception e) {
			recipe.no_of_servings = " ";
		}
		// Extract Ingredients with elimination check.
		List<WebElement> ingElems = driver.findElements(By.xpath("//div[@class='ingredients']//p"));
		List<String> ingList = new ArrayList<>();
		boolean skipRecipe = false;
		System.out.println("Ingredients:");
		for (WebElement ing : ingElems) {
			String text = ing.getText().toLowerCase();
			System.out.println("- " + text);
			ingList.add(text);
			for (String exclude : excludeIngredients) {
				if (text.contains(exclude.toLowerCase())) {
					System.out.println("Skipping recipe (contains excluded ingredient: " + exclude + ")");
					skipRecipe = true;
					break;
				}
			}
			if (skipRecipe)
				break;
		}
		if (skipRecipe)
			return;
		recipe.ingredients = String.join(", ", ingList);
		// Extract Preparation Method.
		try {
			WebElement methodElem = driver.findElement(By.xpath("//div[@id='methods']"));
			recipe.preparation_method = methodElem.getText();
			System.out.println("Preparation Method: " + recipe.preparation_method);
		} catch (Exception e) {
			recipe.preparation_method = " ";
		}
		// Extract Tags.
		try {
			WebElement tagsElem = driver.findElement(By.xpath("//ul[contains(@class,'tags-list')]"));
			recipe.tag = tagsElem.getText();
			System.out.println("Tags: " + recipe.tag);
		} catch (Exception e) {
			recipe.tag = " ";
		}
		// Extract Nutrient Values.
		try {
			WebElement nutrientsElem = driver.findElement(By.id("nutrients"));
			recipe.nutrient_values = nutrientsElem.getText();
			System.out.println("Nutrients: " + recipe.nutrient_values);
		} catch (Exception e) {
			recipe.nutrient_values = " ";
		}
		// Determine Recipe Category from tags.
		final String[] RECIPE_CATEGORY_OPTIONS = { "breakfast", "lunch", "snack", "dinner" };
		List<WebElement> tagElems = driver.findElements(By.xpath("//ul[contains(@class,'tags-list')]/li"));
		String combTags = "";
		for (WebElement t : tagElems) {
			combTags += " " + t.getText();
		}
		String recCategory = "";
		for (String option : RECIPE_CATEGORY_OPTIONS) {
			if (combTags.toLowerCase().contains(option.toLowerCase())) {
				recCategory = option;
				break;
			}
		}
		recipe.recipe_category = recCategory;
		System.out.println("Recipe Category: " + recipe.recipe_category);
		// Determine Food Category based on ingredients.
		String ingText = recipe.ingredients.toLowerCase();
		if (ingText.contains("meat") || ingText.contains("chicken") || ingText.contains("fish"))
			recipe.food_category = "Non-Veg";
		else if (ingText.contains("egg") || ingText.contains("eggs"))
			recipe.food_category = "Eggitarian";
		else if (ingText.contains("butter") || ingText.contains("ghee") || ingText.contains("yogurt")
				|| ingText.contains("curd") || ingText.contains("cream") || ingText.contains("paneer"))
			recipe.food_category = "Vegetarian";
		else
			recipe.food_category = "Vegan";
		System.out.println("Food Category: " + recipe.food_category);
		// Determine Cuisine Category.
		List<WebElement> cuisineElems = driver.findElements(By.xpath("//ul[contains(@class,'tags-list')]//li"));
		String cuisineCategory = "";
		List<String> knownCuisines = Arrays.asList("Indian", "South Indian", "Rajathani", "Punjabi", "Bengali",
				"orissa", "Gujarati", "Maharashtrian", "Andhra", "Kerala", "Goan", "Kashmiri", "Himachali",
				"Tamil nadu", "Karnataka", "Sindhi", "Chhattisgarhi", "Madhya pradesh", "Assamese", "Manipuri",
				"Tripuri", "Sikkimese", "Mizo", "Arunachali", "uttarakhand", "Haryanvi", "Awadhi", "Bihari",
				"Uttar pradesh", "Delhi", "North Indian");
		for (WebElement cuisine : cuisineElems) {
			String txt = cuisine.getText().trim().toLowerCase();
			for (String kc : knownCuisines) {
				if (txt.contains(kc.toLowerCase())) {
					cuisineCategory = kc;
					break;
				}
			}
			if (!cuisineCategory.isEmpty())
				break;
		}
		recipe.cuisine_category = cuisineCategory;
		System.out.println("Cuisine Category: " + recipe.cuisine_category);
		System.out.println("--------------------");

		// Insert the accepted recipe into the database.
		createEliminate(recipe);
	}

	private void createEliminate(ReceipePojo recipe) throws Exception {
		System.out.println("Accepted Recipe: " + recipe);
		DBConnection.insertRecipe(recipe, "LCHF_Diet_Eliminate");
	}

	public void scrolldown() throws InterruptedException {

		JavascriptExecutor js = (JavascriptExecutor) driver;
		long initialHeight = ((Number) js.executeScript("return document.body.scrollHeight")).longValue();
		while (true) {

			js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
			Thread.sleep(1000); // adjust sleep based on load time
			long newHeight = ((Number) js.executeScript("return document.body.scrollHeight")).longValue();
			if (newHeight == initialHeight) {
				break; // stop if height hasn't changed
			}
			initialHeight = newHeight;
		}

	}
	 @AfterClass
	    public void tearDownTest() {
	        if (driver != null) {
	            driver.quit();
	        }
	    }


}
