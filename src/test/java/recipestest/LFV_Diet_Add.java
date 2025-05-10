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
import utilities.LoggerReader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import baseclass.BaseClass;
import drivers.DriverManager;
import utilities.AdHandler;
import utilities.ConfigReader;
import utilities.ReceipePojo;
import utilities.DBConnection;

public class LFV_Diet_Add extends BaseClass {

	static WebDriver driver;
	ConfigReader reader = new ConfigReader();
	List<String> urls = new ArrayList<String>();
	AdHandler ads = new AdHandler();

	List<String> addIngredients = Arrays.asList("lettuce", "kale", "chard", "arugula", "spinach", "cabbage", "pumpkin",
			"sweet potatoes", "purple potatoes", "yams", "turnip", "parsnip", "karela", "bittergourd", "beet", "carrot",
			"cucumber", "red onion", "white onion", "broccoli", "cauliflower", "celery", "artichoke", "bell pepper",
			"mushroom", "tomato", "banana", "mango", "papaya", "plantain", "apple", "orange", "pineapple", "pear",
			"tangerine", "berry", "melon", "peach", "plum", "nectarine", "avocado", "amaranth", "rajgira", "ramdana",
			"barnyard", "sanwa", "samvat ke chawal", "buckwheat", "kuttu", "finger millet", "ragi", "nachni",
			"foxtail millet", "kangni", "kakum", "kodu", "kodon", "little millet", "moraiyo", "kutki", "shavan", "sama",
			"pearl millet", "bajra", "broom corn millet", "chena", "sorghum", "jowar", "lentil", "pulse", "moong dhal",
			"masoor dhal", "toor dhal", "urd dhal", "lobia", "rajma", "matar", "chana", "almond", "cashew", "pistachio",
			"brazil nut", "walnut", "pine nut", "hazelnut", "macadamia nut", "pecan", "peanut", "hemp seed",
			"sun flower seed", "sesame seed", "chia seed", "flax seed");

	@BeforeClass
	public void setUp() throws InterruptedException {

		DriverManager.createDriver("chrome", true); // This should create and set the driver
		driver = DriverManager.getDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		driver.get(ConfigReader.getProperty("url"));
		scrolldown();

	}

	@Test
	public void scrapeReceipeUrls() throws Exception {

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement recipeslink = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Recipes List')]")));
		Assert.assertTrue(recipeslink.isDisplayed(), "Element is not visible");
		if (driver.getCurrentUrl().equals("https://www.tarladalal.com/#google_vignette")) {
			WebElement adx = driver.findElement(By.xpath("//*[contains(text(),'Close')]"));
			adx.click();
			LoggerReader.info("closed the ad before clicking the Recipes link");
		}
		recipeslink.click();

		if (!driver.getCurrentUrl().equals("https://www.tarladalal.com/recipes/")) {
			System.out.println("URL mismatch! Current URL is: " + driver.getCurrentUrl());
			AdHandler.closeAdIfPresent(driver);
			LoggerReader.info("current page is: " + driver.getCurrentUrl());
		}
		int currentPage = 1;
		while (true) {
			try {
				if (currentPage == 5) {
					break;
				}
				// Wait for the recipes to load
				new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions
						.presenceOfAllElementsLocatedBy(By.xpath("//div[@class='overlay-content']//a[@href]")));

				// Extract and print all recipe URLs from current page
				List<WebElement> recipeLinks = driver
						.findElements(By.xpath("//div[@class='overlay-content']//a[@href]"));

				for (WebElement link : recipeLinks) {
					urls.add(link.getAttribute("href"));
				}
				currentPage += 1;

				// Try to locate the next page button
				System.out.println("Pagination URL" + driver.getCurrentUrl());
				WebElement nextPageLink = driver.findElement(By.xpath(
						"//ul[@class='pagination justify-content-center align-items-center']//a[contains(text(), 'Next')]"));
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextPageLink);
				Thread.sleep(500);
				((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -150);");
				Thread.sleep(300);
				AdHandler.closeAdIfPresent(driver);
				Thread.sleep(300);
				nextPageLink = driver.findElement(By.xpath("//a[contains(text(),'Next')]")); // re-find to avoid stale
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextPageLink);

			} catch (Exception e) {
				LoggerReader.info("No further page found or error: " + e.getMessage());
				break; // break the loop when next page is not found
			}
		}
		System.out.println("Collected URLs: " + urls.size());

		for (String url : urls) {
			System.out.println("page URL:" + url);
			recipeDetails(driver, url);
		}
	}

	public void recipeDetails(WebDriver driver, String recipeURL) throws Exception {
		// To get the RecipeURL
		if (recipeURL == null && !recipeURL.contains("recipe"))
			return;
		driver.navigate().to(recipeURL); // Navigate to the recipe page

		ReceipePojo recipe = new ReceipePojo();
		recipe.recipe_URL = recipeURL;

		// To get the RecipeID
		String recipeId = recipeURL.replaceAll(".*-(\\d+)r$", "$1");
		recipe.recipe_id = recipeId;

		// To get the RecipeName
		recipe.recipe_name = driver.getTitle();
		Thread.sleep(1000);

		// To get the Preparation time
		try {
			WebElement prepTime = driver
					.findElement(By.xpath("//div[@class='content']//h6[text()='Preparation Time']/..//strong"));
			LoggerReader.info("Preparation Time: " + prepTime.getText());
			recipe.preparation_time = prepTime.getText();
		} catch (Exception e) {
			recipe.preparation_time = " ";
		}

		// To get the Cooking time
		try {
			WebElement cookTime = driver
					.findElement(By.xpath("//div[@class='content']//h6[text()='Cooking Time']/..//strong"));
			LoggerReader.info("Cooking Time: " + cookTime.getText());
			recipe.cooking_time = cookTime.getText();
		} catch (Exception e) {
			recipe.cooking_time = " ";
		}

		// To get the Makes
		try {
			WebElement servings = driver
					.findElement(By.xpath("//div[@class='content']//h6[text()='Makes ']/..//strong"));
			LoggerReader.info("Makes: " + servings.getText());
			recipe.no_of_servings = servings.getText();
		} catch (Exception e) {
			recipe.no_of_servings = " ";
		}

		// To Extract ingredients

		List<WebElement> ingredientElements = driver.findElements(By.xpath("//div[@class='ingredients']//p"));
		System.out.println("Ingredients:");
		List<String> ingredients = new ArrayList<>();

		for (WebElement ingredient : ingredientElements) {
			String text = ingredient.getText().toLowerCase();
			System.out.println("- " + text);
			ingredients.add(text);

			for (String add : addIngredients) {
				if (text.contains(add.toLowerCase())) {
					LoggerReader.info("Add recipe contains added ingredient: " + add + ")");
					return;
				}
			}
		}

		recipe.ingredients = String.join(", ", ingredients);

		// To get the Preparation Method
		try {
			WebElement method = driver.findElement(By.xpath("//div[@id='methods']"));
			LoggerReader.info("Preparation_method: " + method.getText());
			recipe.preparation_method = method.getText();
		} catch (Exception e) {
			recipe.preparation_method = " ";
		}

		// To get the Recipe Tags
		try {
			WebElement Tags = driver.findElement(By.xpath("//ul[@class='tags-list']"));
			LoggerReader.info("Tags: " + Tags.getText());
			recipe.tag = Tags.getText();
		} catch (Exception e) {
			recipe.tag = " ";
		}

		// To get the Nutrient values
		try {
			WebElement Nutrients = driver.findElement(By.id("nutrients"));
			LoggerReader.info("Nutrients Values: " + Nutrients.getText());
			recipe.nutrient_values = Nutrients.getText();
		} catch (Exception e) {
			recipe.nutrient_values = " ";
		}

		// To get the Recipe Category
		final String[] RECIPE_CATEGORY_OPTIONS = { "breakfast", "lunch", "snack", "dinner" };
		List<WebElement> tagsList = driver.findElements(By.xpath("//ul[@class='tags-list']/li"));
		String tagloca = "";
		for (WebElement tag : tagsList) {
			tagloca = tagloca + " " + tag.getText(); // Concatenate all tag texts
		}
		LoggerReader.info("Recipe Tag:" + tagloca);

		String recipeCategory = "";
		for (String recipeCategoryOption : RECIPE_CATEGORY_OPTIONS) {
			if (tagloca.toLowerCase().contains(recipeCategoryOption.toLowerCase())) {
				recipeCategory = recipeCategoryOption;
				break; // Stop at first match
			}
		}
		recipe.recipe_category = recipeCategory.toString();
		LoggerReader.info("Recipe Category:" + recipeCategory);

		// To get food category
		String text = recipe.ingredients.toLowerCase();
		if (text.contains("meat") || text.contains("chicken") || text.contains("fish")) {
			recipe.food_category = "Non-Veg";
		} else if (text.contains("egg") || text.contains("eggs")) {
			recipe.food_category = "Eggitarian";
		} else if (text.contains("butter") || text.contains("ghee") || text.contains("yougurt") || text.contains("curd")
				|| text.contains("cream") || text.contains("paneer")) {
			recipe.food_category = "Vegetarian";
		} else if (!text.contains("onion") || !text.contains("garlic") || !text.contains("potato")
				|| !text.contains("radish") || !text.contains("carrot")) {
			recipe.food_category = "Jain";
		} else {
			recipe.food_category = "Vegan";
		}

		// To get the Cuisine Category
		List<WebElement> tagElements = driver.findElements(By.xpath("//ul[@class='tags-list']//li"));
		String cuisineCategory = "";

		List<String> knownCuisines = Arrays.asList("Indian", "South Indian", "Rajathani", "Punjabi", "Bengali",
				"orissa", "Gujarati", "Maharashtrian", "Andhra", "Kerala", "Goan", "Kashmiri", "Himachali",
				"Tamil nadu", "Karnataka", "Sindhi", "Chhattisgarhi", "Madhya pradesh", "Assamese", "Manipuri",
				"Tripuri", "Sikkimese", "Mizo", "Arunachali", "uttarakhand", "Haryanvi", "Awadhi", "Bihari",
				"Uttar pradesh", "Delhi", "North Indian");

		for (WebElement tag : tagElements) {
			String tagText = tag.getText().trim();
			for (String cuisine : knownCuisines) {
				if (tagText.equalsIgnoreCase(cuisine)) {
					cuisineCategory = cuisine;
					break;
				}
			}
			if (!cuisineCategory.isEmpty()) {
				break;
			}
		}

		LoggerReader.info("Cuisine Category: " + cuisineCategory);
		recipe.cuisine_category = cuisineCategory;

		// To get recipe description
		try {
			WebElement recipe_description = driver.findElement(By.xpath("//*[@id='aboutrecipe']/p[1]"));
			LoggerReader.info("Description of the recipe: " + recipe_description.getText());
			recipe.recipe_description = recipe_description.getText();
		} catch (Exception e) {
			recipe.recipe_description = " ";
		}
		LoggerReader.info("--------------------");
		//DBConnection.createTable("lfv_diet_add");
		DBConnection.insertRecipe(recipe, "lfv_diet_add");

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

}
