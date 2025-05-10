package utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
	
	 private static ConfigReader configReader = new ConfigReader();
	   
	   /**
   * Establishes and returns a PostgreSQL database connection.
   *
   * @return Connection to the database.
   * @throws SQLException if a database access error occurs.
   */
  public static Connection getConn() throws SQLException {
      try {
      	String jdbcUrl = ConfigReader.getProperty("jdbcURL");
	        String username = ConfigReader.getProperty("username");
	        String password = ConfigReader.getProperty("password");
	      
	        Class.forName("org.postgresql.Driver");
	        System.out.println("jdbcUrl, username, password: " + jdbcUrl + "," + username + "," + password);
	       return DriverManager.getConnection(jdbcUrl, username, password);
      } catch (ClassNotFoundException e) {
          throw new SQLException("PostgreSQL Driver not found!", e);
      }
  }

  /**
   * Creates the table with the provided name if it does not already exist.
   * The table schema aligns with the recipe properties used for insertion.
   *
   * @param tableName the name of the table to create.
   */
  public static void createTable(String tableName) {
      String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
              + "recipe_id VARCHAR(200) PRIMARY KEY, "
              + "recipe_name VARCHAR(2000), "
              + "recipe_category VARCHAR(2000), "
              + "food_category VARCHAR(2000), "
              + "ingredients VARCHAR(2000), "
              + "preparation_time VARCHAR(200), "
              + "cooking_time VARCHAR(200), "
              + "tag VARCHAR(2000), "
              + "no_of_servings VARCHAR(2000), "
              + "cuisine_category VARCHAR(2000), "
              + "recipe_description VARCHAR(5000), "
              + "preparation_method VARCHAR(5000), "
              + "nutrient_values VARCHAR(2000), "
              + "recipe_url VARCHAR(2000)"
              + ");";
      try (Connection conn = getConn();
           Statement stmt = conn.createStatement()) {

          stmt.executeUpdate(sql);
          System.out.println("Table '" + tableName + "' created or already exists.");
      } catch (SQLException e) {
          System.err.println("Error creating table: " + e.getMessage());
      }
  }

  /**
   * Inserts a recipe record into the specified table.
   *
   * @param recipe    the RecipePojo object containing recipe details.
   * @param tableName the name of the table in which to insert the record.
   */
  public static void insertRecipe(ReceipePojo recipe, String tableName) {
      String sql = "INSERT INTO " + tableName 
              + " (recipe_id, recipe_name, recipe_category, food_category, ingredients, "
              + "preparation_time, cooking_time, tag, no_of_servings, cuisine_category, "
              + "recipe_description, preparation_method, nutrient_values, recipe_url) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
      try (Connection conn = getConn();
           PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

          preparedStatement.setString(1, recipe.recipe_id);
          preparedStatement.setString(2, recipe.recipe_name);
          preparedStatement.setString(3, recipe.recipe_category);
          preparedStatement.setString(4, recipe.food_category);
          preparedStatement.setString(5, recipe.ingredients);
          preparedStatement.setString(6, recipe.preparation_time);
          preparedStatement.setString(7, recipe.cooking_time);
          preparedStatement.setString(8, recipe.tag);
          preparedStatement.setString(9, recipe.no_of_servings);
          preparedStatement.setString(10, recipe.cuisine_category);
          preparedStatement.setString(11, recipe.recipe_description);
          preparedStatement.setString(12, recipe.preparation_method);
          preparedStatement.setString(13, recipe.nutrient_values);
          preparedStatement.setString(14, recipe.recipe_URL);

          preparedStatement.executeUpdate();
          System.out.println("Recipe inserted successfully into table '" + tableName + "'.");
      } catch (SQLException e) {
          System.err.println("Error inserting recipe: " + e.getMessage());
      }
  }

  /**
   * Closes the provided database connection. (Not strictly required when using try-with-resources)
   *
   * @param conn the Connection to close.
   */
  public static void closeConn(Connection conn) {
      try {
          if (conn != null && !conn.isClosed()) {
              conn.close();
              System.out.println("Connection closed.");
          }
      } catch (SQLException e) {
          e.printStackTrace();
      }
  }

}