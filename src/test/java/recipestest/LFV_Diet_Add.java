package recipestest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import recipe.Recipe;
import utilities.ReceipePojo;


public class LFV_Diet_Add extends Recipe{
	
	List<String> addIngredients = Arrays.asList(
            "lettuce", "kale", "chard", "arugula", "spinach", "cabbage", "pumpkin", "sweet potatoes",
           "purple potatoes", "yams", "turnip", "parsnip", "karela", "bittergourd", "beet", "carrot",
            "cucumber", "red onion", "white onion", "broccoli", "cauliflower", "celery", "artichoke",
            "bell pepper", "mushroom", "tomato", "banana", "mango", "papaya", "plantain", "apple",
            "orange", "pineapple", "pear", "tangerine", "berry", "melon", "peach", "plum", "nectarine",
            "avocado", "amaranth", "rajgira", "ramdana", "barnyard", "sanwa", "samvat ke chawal",
            "buckwheat", "kuttu", "finger millet", "ragi", "nachni", "foxtail millet", "kangni",
            "kakum", "kodu", "kodon", "little millet", "moraiyo", "kutki", "shavan", "sama",
            "pearl millet", "bajra", "broom corn millet", "chena", "sorghum", "jowar", "lentil",
            "pulse", "moong dhal", "masoor dhal", "toor dhal", "urd dhal", "lobia", "rajma",
            "matar", "chana", "almond", "cashew", "pistachio", "brazil nut", "walnut", "pine nut",
            "hazelnut", "macadamia nut", "pecan", "peanut", "hemp seed", "sun flower seed",
            "sesame seed", "chia seed", "flax seed"); 

    
    public List<ReceipePojo> createAddList(List<ReceipePojo> Receipes) throws Exception {
    	
    	List<ReceipePojo> addReceipes = new ArrayList<ReceipePojo>();
    	
//    	for(ReceipePojo pojo:Receipes) {
//    		boolean value = canAddToAddList(pojo);
//    		if(value == true) {
//    			addReceipes.add(pojo);
//    		}
//    	}
	
    	return addReceipes;		
    	
	}
    
    //  check if ingredients from Receipe are exists in add ingredients.
//    private boolean canAddToAddList(ReceipePojo pojo) {
////    	List<String> ReceipeIngredients = pojo.ingredients; //list Receipe ingredients
//    	  	boolean containsIngredient=false;
//    	for(String addIng : addIngredients) {  
//    		for(String ReceipeIng : ReceipeIngredients) { //
//    			if(ReceipeIng.toLowerCase().contains(addIng)) {
//    				containsIngredient=true;
//    				break;
//  				}
//    		}			
//    	}
//    	System.out.println(containsIngredient);
//    	return containsIngredient;
//    	
//    }

}