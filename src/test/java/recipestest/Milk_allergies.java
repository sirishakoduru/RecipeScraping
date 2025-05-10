package recipestest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

import drivers.DriverManager;
import utilities.AdHandler;
import utilities.ConfigReader;
import utilities.DBConnection;
import utilities.ReceipePojo;

public class Milk_allergies {
	
	static WebDriver driver;
	ConfigReader reader = new ConfigReader();
	
	AdHandler ads = new AdHandler();
	private static final Logger logger = LoggerFactory.getLogger(LFV_Diet_Eliminate.class);
	
	List<String> excludeIngredients = Arrays.asList( "pork", "meat", "poultry", "fish", "sausage", "ham", "salami", "bacon", "milk", "cheese",
	           "yogurt", "butter", "ice cream", "egg", "prawn", "oil", "olive oil", "coconut oil", "soybean oil",
	            "corn oil", "safflower oil", "sunflower oil", "rapeseed oil", "peanut oil", "cottonseed oil",
	            "canola oil", "mustard oil", "cereals", "tinned vegetable", "bread", "maida", "atta", "sooji", "poha",
	            "cornflake", "cornflour", "pasta", "white rice", "pastry", "cakes", "biscuit", "soy", "soy milk",
	            "white miso paste", "soy sauce", "soy curls", "edamame", "soy yogurt", "soy nut", "tofu", "pies",
	            "chip", "cracker", "potatoe", "sugar", "jaggery", "glucose", "fructose", "corn syrup", "cane sugar",
	            "aspartame", "cane solid", "maltose", "dextrose", "sorbitol", "mannitol", "xylitol", "maltodextrin",
	            "molasses", "brown rice syrup", "splenda", "nutra sweet", "stevia", "barley malt"); 
	
	List<String> milk_allergy = Arrays.asList( "milk"); 

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
		List<String> urls = new ArrayList<String>();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
		    try {
		    	if (currentPage == 15) {
		    		break;
		    	}
		        // Wait for the recipes to load
		        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
		            ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@class='overlay-content']//a[@href]"))
		        );
		        
		        // Extract and print all recipe URLs from current page
		        List<WebElement> recipeLinks = driver.findElements(By.xpath("//div[@class='overlay-content']//a[@href]"));
		        
		        for (WebElement link : recipeLinks) {
					urls.add(link.getAttribute("href"));			
				}
		        currentPage += 1;
				

		        // Try to locate the next page button
				System.out.println("Pagination URL" + driver.getCurrentUrl());
		        WebElement nextPageLink = driver.findElement(By.xpath(
		                "//ul[@class='pagination justify-content-center align-items-center']//a[contains(text(), 'Next')]"
		            ));
		        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextPageLink);
		        Thread.sleep(500);
		        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -150);");
		        Thread.sleep(300);

		        ads.closeAdIfPresent(driver);
		        Thread.sleep(300);
		        nextPageLink = driver.findElement(By.xpath("//a[contains(text(),'Next')]")); // re-find to avoid stale
		        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextPageLink);

		    } catch (Exception e) {
		        System.out.println("No further page found or error: " + e.getMessage());
		        break; // break the loop when next page is not found
		    }
		}
		 System.out.println("Collected URLs: " + urls.size());
	
		for (String url : urls) {
			System.out.println("page URL:" + url);
			recipeDetails(driver, url);	
		}
	}
		
	public void recipeDetails(WebDriver driver,String recipeURL) throws Exception {
		// To get the RecipeURL
		if (recipeURL == null && !recipeURL.contains("recipe")) return ;
		driver.navigate().to(recipeURL); // Navigate to the recipe page
		
		ReceipePojo recipe = new ReceipePojo();
		recipe.recipe_URL=recipeURL;
		
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
			System.out.println("Preparation Time: " + prepTime.getText());
			recipe.preparation_time = prepTime.getText();
		} catch (Exception e) {
	        recipe.preparation_time = " ";
	    }

		// To get the Cooking time
		try {
		WebElement cookTime = driver
				.findElement(By.xpath("//div[@class='content']//h6[text()='Cooking Time']/..//strong"));
		System.out.println("Cooking Time: " + cookTime.getText());
		recipe.cooking_time = cookTime.getText();
		}catch (Exception e) {
			recipe.cooking_time = " ";
	    }
		

		// To get the Makes
		try {
		WebElement servings = driver.findElement(By.xpath("//div[@class='content']//h6[text()='Makes ']/..//strong"));
		System.out.println("Makes: " + servings.getText());
		recipe.no_of_servings = servings.getText();
		}catch (Exception e) {
			recipe.no_of_servings = " ";
	    }
	
	    List<WebElement> ingredientElements = driver.findElements(By.xpath("//div[@class='ingredients']//p"));
	    System.out.println("Ingredients:");
	    List<String> ingredients = new ArrayList<>();

	    for (WebElement ingredient : ingredientElements) {
	        String text = ingredient.getText().toLowerCase();
	        System.out.println("- " + text);
	        ingredients.add(text);

	    }

	    recipe.ingredients = String.join(", ", ingredients);
	 		
		// To get the Preparation Method
		try {
		WebElement method = driver.findElement(By.xpath("//div[@id='methods']"));
		System.out.println("Preparation_method: " + method.getText());
		recipe.preparation_method = method.getText();
		}catch (Exception e) {
			recipe.preparation_method = " ";
	    }

		// To get the Recipe Tags
		try {
		WebElement Tags = driver.findElement(By.xpath("//ul[@class='tags-list']"));
		System.out.println("Tags: " + Tags.getText());
		recipe.tag = Tags.getText();
		}catch (Exception e) {
			recipe.tag = " ";
	    }

		// To get the Nutrient values
		try {
		WebElement Nutrients = driver.findElement(By.id("nutrients"));
		System.out.println("Nutrients Values: " + Nutrients.getText());
		recipe.nutrient_values = Nutrients.getText();
	    }catch (Exception e) {
		recipe.nutrient_values = " ";
       }
		
		// To get the Recipe Category
		final String[] RECIPE_CATEGORY_OPTIONS = { "breakfast", "lunch", "snack", "dinner" }; 
		List<WebElement> tagsList = driver.findElements(By.xpath("//ul[@class='tags-list']/li"));
		String tagloca = "";
		for (WebElement tag : tagsList) {
			tagloca = tagloca + " " + tag.getText(); // Concatenate all tag texts
		}
		System.out.println("Recipe Tag:" + tagloca);

		String recipeCategory = "";
		for (String recipeCategoryOption : RECIPE_CATEGORY_OPTIONS) {
			if (tagloca.toLowerCase().contains(recipeCategoryOption.toLowerCase())) {
				recipeCategory = recipeCategoryOption;
				break; // Stop at first match
			}
		}
		recipe.recipe_category = recipeCategory.toString();
		System.out.println("Recipe Category:" + recipeCategory);
		
		// To get food category
		String text = recipe.ingredients.toLowerCase();
		if(text.contains("meat") || text.contains("chicken") || text.contains("fish")) {
			recipe.food_category = "Non-Veg";
		} else if(text.contains("egg") || text.contains("eggs")) {
			recipe.food_category = "Eggitarian";
		}else if(text.contains("butter") || text.contains("ghee") || text.contains("yougurt") 
				|| text.contains("curd") || text.contains("cream") || text.contains("paneer") ) {
			recipe.food_category = "Vegetarian";
		}else if(!text.contains("onion") || !text.contains("garlic") || !text.contains("potato") 
				|| !text.contains("radish")|| !text.contains("carrot")){
			recipe.food_category = "Jain";
		} else {
			recipe.food_category = "Vegan";
		}

		// To get the Cuisine Category
		List<WebElement> tagElements = driver.findElements(By.xpath("//ul[@class='tags-list']//li"));
		String cuisineCategory = "";

		List<String> knownCuisines = Arrays.asList("Indian","South Indian","Rajathani","Punjabi","Bengali","orissa",
				"Gujarati","Maharashtrian","Andhra","Kerala","Goan","Kashmiri","Himachali","Tamil nadu","Karnataka",
				"Sindhi","Chhattisgarhi","Madhya pradesh","Assamese","Manipuri","Tripuri","Sikkimese","Mizo","Arunachali",
				"uttarakhand","Haryanvi","Awadhi","Bihari","Uttar pradesh","Delhi","North Indian");

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

		System.out.println("Cuisine Category: " + cuisineCategory);
		recipe.cuisine_category = cuisineCategory;
		
		//To get recipe description
		try {
		WebElement recipe_description =  driver.findElement(By.xpath("//*[@id='aboutrecipe']/p[1]"));
		System.out.println("Description of the recipe: " + recipe_description.getText());
		recipe.recipe_description = recipe_description.getText();
	    }catch (Exception e) {
		recipe.recipe_description = " ";
       }
		
		if(isToavoidProcessedFoodUsed(recipe.tag, recipe.preparation_method, recipe.recipe_description)) {
			System.out.println("Skipping receipe due to processed food: " +recipe.recipe_name);
			return;
		}

		System.out.println("--------------------");
		DBConnection.createTable("LCHF_Diet_ToAvoid");
		DBConnection.insertRecipe(recipe,"LCHF_Diet_ToAvoid");
		
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
	
	public boolean isToavoidProcessedFoodUsed(String Tags, String method, String ingredientElements) {
		
//		String text = (Tags + " " + method + " " + recipe_description ).toLowerCase();
		String text = String.join(" ",
		        Optional.ofNullable(Tags).orElse(""),
		        Optional.ofNullable(method).orElse(""),
		        Optional.ofNullable(ingredientElements).orElse("")
		).toLowerCase();
		
		List<String> combinedFilters = new ArrayList<>();
	    combinedFilters.addAll(milk_allergy);
	    combinedFilters.addAll(excludeIngredients);
		
		for(String filters : combinedFilters) {
			if(text.contains(filters.toLowerCase())) {
				System.out.println("filtered food found: " +filters);
				System.out.println("Avoid list: " + milk_allergy);
				return true;
			}
		}
		return false;
			
	}
}
