package recipe;

import io.github.bonigarcia.wdm.WebDriverManager;
import recipestest.LFV_Diet_Add;
import utilities.AdHandler;
import utilities.ReceipePojo;

import org.openqa.selenium.JavascriptExecutor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

public class Recipe {

	WebDriver driver;
	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
	List<ReceipePojo> allReceipes;
	AdHandler ads = new AdHandler();

	public static void main(String[] args) throws Exception {

		Recipe rec = new Recipe();
		rec.init();
		rec.getAllRecipeUrls();

	}

	public void init() throws Exception {

		WebDriverManager.chromedriver().setup();
		// WebDriver driver = new ChromeDriver();
		ChromeOptions options = new ChromeOptions();
		Map<String, Object> prefs = new HashMap<>();
		allReceipes = new ArrayList<ReceipePojo>();
		prefs.put("profile.managed_default_content_settings.images", 2); // Disable images
		options.setExperimentalOption("prefs", prefs);
		driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		driver.manage().window().maximize();
		Thread.sleep(2000);
		driver.get("https://m.tarladalal.com/");
		Thread.sleep(2000);
	}

	public void getAllRecipeUrls() throws Exception {
		Thread.sleep(5000);
		//List<String> urls = new ArrayList<String>();
		Set<String> urls = new LinkedHashSet<>(); // avoids duplicates
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
		WebElement recipeslink = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Recipes List')]")));
		Assert.assertTrue(recipeslink.isDisplayed(), "Element is not visible");
		if (driver.getCurrentUrl().equals("https://www.tarladalal.com/#google_vignette")) {
			WebElement adx = driver.findElement(By.xpath("//*[contains(text(),'Close')]"));
			adx.click();
			System.out.println("closed the ad before clicking the Recipes link");
		}
		recipeslink.click();

		if (!driver.getCurrentUrl().equals("https://www.tarladalal.com/recipes/")) {
			System.out.println("URL mismatch! Current URL is: " + driver.getCurrentUrl());
			ads.closeAdIfPresent(driver);
			System.out.println("current page is: " + driver.getCurrentUrl());
		}
		
		int currentPage = 1;
		while (true) {
			if(currentPage>=2) break;

		    try {
		        // Wait for the recipes to load
		        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
		            ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@class='overlay-content']//a[@href]"))
		        );

		        // Extract and print all recipe URLs from current page
		        List<WebElement> recipeLinks = driver.findElements(By.xpath("//div[@class='overlay-content']//a[@href]"));
		        for (WebElement recipeLink : recipeLinks) {
		            String recipeUrl = recipeLink.getAttribute("href");
		            if (recipeUrl != null && recipeUrl.contains("recipe")) {
		            	urls.add(recipeUrl); 
		                System.out.println("Recipe URL: " + recipeUrl);
		            }
		        }

		        // Try to locate the next page button
		     
		        WebElement nextPageLink = driver.findElement(By.xpath(
		                "//ul[@class='pagination justify-content-center align-items-center']//a[contains(text(), 'Next')]"
		            ));
		        if (nextPageLink.isDisplayed() && nextPageLink.isEnabled()) {
		            js.executeScript("arguments[0].scrollIntoView(true);", nextPageLink);
		            System.out.println(nextPageLink);
		            ads.closeAdIfPresent(driver);
		            js.executeScript("arguments[0].click();", nextPageLink);
		            Thread.sleep(1500); // allow page to load
		            currentPage++;
		        } else {
		            break;
		        }
		        
		    } catch (Exception e) {
		        System.out.println("No further page found or error: " + e.getMessage());
		        break; // break the loop when next page is not found
		    }
		}
		for (String url : urls) {
			recipeDetails(url);}
		LFV_Diet_Add lfv_Add = new LFV_Diet_Add();
		List<ReceipePojo> addRecipeList = lfv_Add.createAddList(allReceipes);
		
		System.out.println(allReceipes.size());
		System.out.println(allReceipes);
		
		System.out.println("----------------------");
		System.out.println(addRecipeList.size());
		System.out.println(addRecipeList);

	}

	public void recipeDetails(String recipeURL) throws Exception {

		LFV_Diet_Add lfv_Add = new LFV_Diet_Add();
		List<ReceipePojo> addRecipeList = lfv_Add.createAddList(allReceipes);
		ReceipePojo pojo = new ReceipePojo();

		// To get the RecipeURL
		if (recipeURL != null && recipeURL.contains("recipe")) {

			//System.out.println(recipeURL);

		}
		pojo.recipe_URL = recipeURL;

		driver.navigate().to(recipeURL); // Navigate to the recipe page

		// To get the RecipeName
		System.out.println("RecipeName: " + driver.getTitle());
		Thread.sleep(1000);
		pojo.recipe_name = driver.getTitle();

		// To get the RecipeID
		String recipeId = recipeURL.replaceAll(".*-(\\d+)r$", "$1");
		System.out.println("RecipeID: " + recipeId);

		// To get the Preparation time
		WebElement prepTime = driver
				.findElement(By.xpath("//div[@class='content']//h6[text()='Preparation Time']/..//strong"));
		System.out.println("Preparation Time: " + prepTime.getText());

		// To get the Cooking time
		WebElement cookTime = driver
				.findElement(By.xpath("//div[@class='content']//h6[text()='Cooking Time']/..//strong"));
		System.out.println("Cooking Time: " + cookTime.getText());

		// To get the Makes
		WebElement servings = driver.findElement(By.xpath("//div[@class='content']//h6[text()='Makes ']/..//strong"));
		System.out.println("Makes: " + servings.getText());

		// To Extract ingredients

		List<WebElement> ingredientElements = driver.findElements(By.xpath("//div[@class='ingredients']//p"));
		System.out.println("Ingredients:");
		List<String> ingredients = new ArrayList<String>();
		for (WebElement ingredient : ingredientElements) {
			System.out.println("- " + ingredient.getText());
			ingredients.add(ingredient.getText().toLowerCase());
		}
//		pojo.ingredients = ingredients;

		// To get the Preparation Method
		WebElement method = driver.findElement(By.xpath("//div[@id='methods']"));
		System.out.println("Preparation_method: " + method.getText());

		// To get the Recipe Tags
		List<WebElement> tagEles = driver.findElements(By.xpath("//ul[@class='tags-list']/li"));
		List<String> Tags = new ArrayList<String>();
		for (WebElement tag : tagEles) {
			String tagText = tag.getText().toLowerCase();
			System.out.println("Tag: " + tagText);
			Tags.add(tagText);
		}

		// To get the Nutrient values
		WebElement Nutrients = driver.findElement(By.id("nutrients"));
		System.out.println("Nutrients Values: " + Nutrients.getText());

		// To get the Cuisine Category
		List<WebElement> tagElements = driver.findElements(By.xpath("//ul[contains(@class, 'tags-list')]//li"));
		String cuisineCategory = "";

		List<String> knownCuisines = Arrays.asList("Indian", "South Indian", "Rajathani", "Punjabi", "Bengali",
				"orissa", "Gujarati", "Maharashtrian", "Andhra", "Kerala", "Goan", "Kashmiri", "Himachali",
				"Tamil nadu", "Karnataka", "Sindhi", "Chhattisgarhi", "Madhya pradesh", "Assamese", "Manipuri",
				"Tripuri", "Sikkimese", "Mizo", "Arunachali", "uttarakhand", "Haryanvi", "Awadhi", "Bihari",
				"Uttar pradesh", "Delhi", "North Indian");

		for (WebElement tag : tagElements) {
			String tagText = tag.getText().trim().toLowerCase();
			for (String cuisine : knownCuisines) {
				if (tagText.contains(cuisine.toLowerCase())) {
					cuisineCategory = cuisine;
					break;
				}
			}
			if (!cuisineCategory.isEmpty()) {
				break;
			}
		}

		System.out.println("Cuisine Category: " + cuisineCategory);
		pojo.cuisine_category = cuisineCategory;
		allReceipes.add(pojo);

		// To get the Recipe Category
		final String[] RECIPE_CATEGORY_OPTIONS = { "breakfast", "lunch", "snack", "dinner" };
		List<WebElement> tagsList = driver.findElements(By.xpath("//ul[@class='tags-list']/li"));
		String tagloca = "";
		for (WebElement tag : tagsList) {
			// tagTexts.add(tag.getText());
			tagloca = tagloca + " " + tag.getText();
		}

		String recipeCategory = "";
		for (String recipeCategoryOption : RECIPE_CATEGORY_OPTIONS) {
			if (tagloca.toLowerCase().contains(recipeCategoryOption.toLowerCase())) {
				recipeCategory = recipeCategoryOption;
				break;
			}
		}

		System.out.println("Recipe Category:" + recipeCategory);

		System.out.println("--------------------");

	}
}