package com.trikorasolutions.example;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.is;

import com.trikorasolutions.example.model.Fruit;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
public class CombineTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(CombineTest.class);

  @Inject
  private FruitResources fruitResources;

  @Test
  public void testCombine() {
    final Fruit appleFruit = new Fruit("apple", "Apple", "Rosaceae", false);
    final Fruit apricotFruit = new Fruit("apricot", "Apricot", "Rosaceae", false);
    final Fruit lemonFruit = new Fruit("lemon", "Lemon", "Rutaceae", false);
    final Fruit pearFruit = new Fruit("pear", "Pear", "Rosaceae", false);
    final Fruit pineappleFruit = new Fruit("pineapple", "Pineapple", "Bromeliaceae", false);
    final Fruit plumFruit = new Fruit("plum", "Plum", "Rosaceae", false);
    fruitResources.create(appleFruit);
    fruitResources.create(apricotFruit);
    fruitResources.create(lemonFruit);
    fruitResources.create(pearFruit);
    fruitResources.create(pineappleFruit);
    fruitResources.create(plumFruit);
    given().when().contentType(MediaType.APPLICATION_JSON).get("/tree/combine2/Rosaceae/Rutaceae")
        .then()
        .statusCode(OK.getStatusCode())
        .body("name", is("combine_tree"), "fruits.size()", Matchers.greaterThanOrEqualTo(5),
            "fruits.name",
            hasItems(appleFruit.name, apricotFruit.name, lemonFruit.name, pearFruit.name,
                plumFruit.name));
  }
}
