package com.trikorasolutions.example;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;

import com.trikorasolutions.example.dto.FruitDto;
import com.trikorasolutions.example.dto.TreeDto;
import io.quarkus.test.junit.QuarkusTest;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
public class TreeDtoResourceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TreeDtoResourceTest.class);

  @Inject
  private FruitResources fruitResources;

  @Test
  public void getTreeOk() {
    final TreeDto tree = new TreeDto("tree_get_ok", null);

    given().when().body(tree).contentType(MediaType.APPLICATION_JSON).post("/tree/create").then()
        .statusCode(OK.getStatusCode()).body("name", is(tree.name));

    given().when().get(String.format("/tree/name/%s", tree.name)).then()
        .statusCode(OK.getStatusCode())
        .body("name", is(tree.name));
  }

  @Test
  public void getTreeNotFound() {
    given().when().get("/tree/name/unknown_tree").then().statusCode(NOT_FOUND.getStatusCode());
  }

  @Test
  public void createTreeOk() {
    final TreeDto tree = new TreeDto("tree_create_ok", null);

    given().when().body(tree).contentType(MediaType.APPLICATION_JSON).post("/tree/create").then()
      .statusCode(OK.getStatusCode()).body("name", is(tree.name));

    given().when().get(String.format("/tree/name/%s", tree.name)).then().statusCode(OK.getStatusCode())
      .body("name", is(tree.name));
  }

  @Test
  public void createTreeWithFruitsOk() {
    TreeDto tree = new TreeDto("createTreeWithFruitsOk");
    final List<FruitDto> fruits = new ArrayList<>() {{
      add(new FruitDto("orange", "Updated-Pear", "PearFam", true, tree.name));
      add(new FruitDto("Carrot", "not a fruit", "CarrotFam", true, tree.name));
    }};
    tree.setFruits(fruits);

    // Ensure the tree is persisted
    given().when().body(tree).contentType(MediaType.APPLICATION_JSON).post("/tree/create").then()
        .statusCode(OK.getStatusCode())
        .body("name", is("createTreeWithFruitsOk"), "fruits.size()", is(fruits.size()));

    // Ensure that the full tree is persisted
    given().when().get(String.format("/tree/name/%s", tree.name)).then()
        .statusCode(OK.getStatusCode())
        .body("name", is("createTreeWithFruitsOk"));

    // Ensure that the fruits persisted when persisting the tree are in th db
    fruitResources.get("orange").then().statusCode(OK.getStatusCode())
        .body("name", is("orange"));
    fruitResources.get("Carrot").then().statusCode(OK.getStatusCode())
        .body("name", is("Carrot"));

    // Ensure that the full tree is persisted
    given().when().get(String.format("/tree/getFull/name/%s", tree.name)).then()
        .statusCode(OK.getStatusCode())
        .body("name", is("createTreeWithFruitsOk"), "fruits.size()", is(fruits.size()));
  }
}
