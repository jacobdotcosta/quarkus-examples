package com.trikorasolutions.example;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;

import com.trikorasolutions.example.model.Fruit;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FruitResourceTest {
  
  @Inject
  private FruitResources fruitResources;

  @Test
  public void testGetUnknown() {
    fruitResources.get("unknown").then().statusCode(NOT_FOUND.getStatusCode());
  }

  @Test
  public void testCreateGetOk() {
    final Fruit fruit = new Fruit("pear", "Pear", "Rosaceae", false);
    fruitResources.delete(fruit.name);
    fruitResources.create(fruit).then().statusCode(OK.getStatusCode()).body("name", is(fruit.name));
    fruitResources.get(fruit.name).then().statusCode(OK.getStatusCode()).body("name", is(fruit.name));
  }

  @Test
  public void testListAll() {
    final Fruit appleFruit = new Fruit("apple", "Apple", "Rosaceae", false);
    final Fruit pearFruit = new Fruit("pear", "Pear", "Rosaceae", false);
    fruitResources.delete(appleFruit.name);
    fruitResources.delete(pearFruit.name);
    fruitResources.create(appleFruit);
    fruitResources.create(pearFruit);
    given().when().get("/fruit/listAll").then().statusCode(OK.getStatusCode())
        .body("$.size()", Matchers.greaterThanOrEqualTo(2),
            "name", Matchers.hasItems(appleFruit.name, pearFruit.name)
            , "description", Matchers.hasItems(appleFruit.description, pearFruit.description)
        );
    fruitResources.delete(appleFruit.name);
    fruitResources.delete(pearFruit.name);
  }

  @Test
  public void testCreateDuplicate() {
    final Fruit fruit = new Fruit("pear", "Pear", "Rosaceae", false);
    fruitResources.delete(fruit.name);
    fruitResources.create(fruit).then().statusCode(OK.getStatusCode()).body("name", is(fruit.name));
    fruitResources.create(fruit).then().statusCode(CONFLICT.getStatusCode());
  }


  @Test
  public void testDeleteUnknown() {
    fruitResources.delete("unknown").then().statusCode(NOT_FOUND.getStatusCode());
  }

  @Test
  public void testDeleteOk() {
    final Fruit fruit = new Fruit("deleteOk", "Delete OK", "Delete", false);
    fruitResources.get(fruit.name).then().statusCode(NOT_FOUND.getStatusCode());
    fruitResources.create(fruit);
    fruitResources.get(fruit.name).then().statusCode(OK.getStatusCode());
    fruitResources.delete(fruit.name).then().statusCode(OK.getStatusCode());
    fruitResources.get(fruit.name).then().statusCode(NOT_FOUND.getStatusCode());
  }

  @Test
  public void testUpdate() {
    final Fruit pineappleFruit = new Fruit("pineapple", "Pineapple", "Bromeliaceae", false);
    final Fruit lemonFruit = new Fruit("lemon", "Lemon", "Rutaceae", false);
    final Fruit apricotFruit = new Fruit("apricot", "Apricot", "Rosaceae", false);
    final Fruit plumFruit = new Fruit("plum", "Plum", "Rosaceae", false);
    fruitResources.delete(apricotFruit.name);
    fruitResources.delete(lemonFruit.name);
    fruitResources.delete(pineappleFruit.name);
    fruitResources.delete(plumFruit.name);
    fruitResources.create(apricotFruit);
    fruitResources.create(lemonFruit);
    fruitResources.create(pineappleFruit);
    fruitResources.create(plumFruit);

    given().when().put("/fruit/ripe/Rosaceae").then().statusCode(OK.getStatusCode());
    fruitResources.get(apricotFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", is(apricotFruit.name), "ripen", Matchers.is(true));
    fruitResources.get(lemonFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", is(lemonFruit.name), "ripen", Matchers.is(false));
    fruitResources.get(pineappleFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", is(pineappleFruit.name), "ripen", Matchers.is(false));
    fruitResources.get(plumFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", is(plumFruit.name), "ripen", Matchers.is(true));

    fruitResources.delete(apricotFruit.name);
    fruitResources.delete(lemonFruit.name);
    fruitResources.delete(pineappleFruit.name);
    fruitResources.delete(plumFruit.name);
  }


}