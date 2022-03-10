package com.trikorasolutions.example;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;

import com.trikorasolutions.example.model.Fruit;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import javax.ws.rs.core.MediaType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
public class FruitReactiveResourceTest {

  @Test
  public void testGetUnknown() {
    this.get("unknown").then().statusCode(NOT_FOUND.getStatusCode());
  }

  @Test
  public void testCreateGetOk() {
    final Fruit fruit = new Fruit("pear", "Pear", "Rosaceae", false);
    this.delete(fruit.name);
    this.create(fruit).then().statusCode(OK.getStatusCode()).body("name", is(fruit.name));
    this.get(fruit.name).then().statusCode(OK.getStatusCode()).body("name", is(fruit.name));
  }

  @Test
  public void testListAll() {
    final Fruit appleFruit = new Fruit("apple", "Apple", "Rosaceae", false);
    final Fruit pearFruit = new Fruit("pear", "Pear", "Rosaceae", false);
    this.delete(appleFruit.name);
    this.delete(pearFruit.name);
    this.create(appleFruit);
    this.create(pearFruit);
    given().when().get("/fruitreact/listAll").then().statusCode(OK.getStatusCode())
        .body("$.size()", Matchers.greaterThanOrEqualTo(2),
            "name", Matchers.hasItems(appleFruit.name, pearFruit.name)
            , "description", Matchers.hasItems(appleFruit.description, pearFruit.description)
        );
  }

  @Test
  public void testCreateDuplicate() {
    final Fruit fruit = new Fruit("pear", "Pear", "Rosaceae", false);
    this.delete(fruit.name);
    this.create(fruit).then().statusCode(OK.getStatusCode()).body("name", is(fruit.name));
    this.create(fruit).then().statusCode(CONFLICT.getStatusCode());
  }


  @Test
  public void testDeleteUnknown() {
    this.delete("unknown").then().statusCode(NOT_FOUND.getStatusCode());
  }

  @Test
  public void testDeleteOk() {
    final Fruit fruit = new Fruit("deleteOk", "Delete OK", "Delete", false);
    this.get(fruit.name).then().statusCode(NOT_FOUND.getStatusCode());
    this.create(fruit);
    this.get(fruit.name).then().statusCode(OK.getStatusCode());
    this.delete(fruit.name).then().statusCode(OK.getStatusCode());
    this.get(fruit.name).then().statusCode(NOT_FOUND.getStatusCode());
  }

  @Test
  public void testUpdate() {
    final Fruit pineappleFruit = new Fruit("pineapple", "Pineapple", "Bromeliaceae", false);
    final Fruit lemonFruit = new Fruit("lemon", "Lemon", "Rutaceae", false);
    final Fruit apricotFruit = new Fruit("apricot", "Apricot", "Rosaceae", false);
    final Fruit plumFruit = new Fruit("plum", "Plum", "Rosaceae", false);
    this.delete(apricotFruit.name);
    this.delete(lemonFruit.name);
    this.delete(pineappleFruit.name);
    this.delete(plumFruit.name);
    this.create(apricotFruit);
    this.create(lemonFruit);
    this.create(pineappleFruit);
    this.create(plumFruit);

    given().when().put("/fruitreact/ripe/Rosaceae").then().statusCode(OK.getStatusCode());
    this.get(apricotFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", is(apricotFruit.name), "ripen", Matchers.is(true));
    this.get(lemonFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", is(lemonFruit.name), "ripen", Matchers.is(false));
    this.get(pineappleFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", is(pineappleFruit.name), "ripen", Matchers.is(false));
    this.get(plumFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", is(plumFruit.name), "ripen", Matchers.is(true));
  }

  protected Response delete(final String fruit) {
    return given().when().delete(String.format("/fruitreact/%s", fruit));
  }

  protected Response create(final Fruit fruit) {
    return given().when().body(fruit).contentType(MediaType.APPLICATION_JSON)
        .post("/fruitreact/create");
  }

  protected Response get(final String fruit) {
    return given().when().get(String.format("/fruitreact/name/%s", fruit));
  }
}