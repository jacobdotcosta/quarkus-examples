package com.trikorasolutions.example;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.is;

import com.trikorasolutions.example.dto.FruitDto;
import com.trikorasolutions.example.dto.TreeDto;
import com.trikorasolutions.example.model.Fruit;
import com.trikorasolutions.example.model.Tree;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.hamcrest.CoreMatchers;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
public class KindOfPersistedEntitiesTest {
  @Inject
  Mutiny.SessionFactory sf;

  private static final Logger LOGGER = LoggerFactory.getLogger(KindOfPersistedEntitiesTest.class);

  @BeforeEach
  public void clearDatabase() {

    LOGGER.warn("delete from database");
    Integer res = sf.withTransaction((s, t) -> s.createQuery("DELETE FROM Fruit").executeUpdate())
        .await()
        .atMost(Duration.ofSeconds(30));
    Integer res2 = sf.withTransaction((s, t) -> s.createQuery("DELETE FROM Tree").executeUpdate())
        .await()
        .atMost(Duration.ofSeconds(30));
    LOGGER.warn("{} records removed", res + res2);
  }

  /**
   * <p>Add existing fruits to existing tree.</p>
   */
//  @Test
  public void addExistingFruitsToExistingTree() {
    // Persist the fruits
    given().when().body(new Fruit("pear", "Pear", "Rosaceae", false))
        .contentType(MediaType.APPLICATION_JSON)
        .post("/fruitreact/create").then().statusCode(OK.getStatusCode());
    given().when().body(new Fruit("apple", "Apple", "Rosaceae", false))
        .contentType(MediaType.APPLICATION_JSON)
        .post("/fruitreact/create").then().statusCode(OK.getStatusCode());

    // Persist the tree
    Tree tree = new Tree("addExistingFruitsToExistingTree");
    given().when().body(tree).contentType(MediaType.APPLICATION_JSON).post("/tree/create").then()
        .statusCode(OK.getStatusCode()).body("name", CoreMatchers.is("alreadyPersistedTreeTest"));

    // Ensure the tree and the fruits have been UPDATED
    given().when().body(tree).contentType(MediaType.APPLICATION_JSON).post("/tree/persist/Rosaceae")
        .then().statusCode(OK.getStatusCode())
        .body("name", is(tree.name), "fruits.size()", is(2), "fruits.name",
            hasItems("pear", "apple"));

    // Ensure that the fruits persisted when persisting the tree are in th db
    given().when().get("/fruitreact/name/pear").then().statusCode(OK.getStatusCode())
        .body("name", is("pear"), "tree", is(tree.name));
    given().when().get("/fruitreact/name/apple").then().statusCode(OK.getStatusCode())
        .body("name", is("apple"), "tree", is(tree.name));
  }

//  @Test
  public void NotPersistedTreeAndFruitsTest() { // Tree does not exist, Fruits do not exist

    TreeDto tree = new TreeDto("NotPersistedTreeAndFruitsTest");

    // In this example, fruits are not related with the tree.
    final List<FruitDto> fruits = new ArrayList<>() {{
      add(new FruitDto("orange", "Updated-Pear", "persistFam1", true));
      add(new FruitDto("Carrot", "not a fruit", "persistFam1", true));
    }};
    tree.setFruits(fruits);

    // Ensure the tree and the fruits have been INSERTED
    given().when().body(tree).contentType(MediaType.APPLICATION_JSON).post("/tree/persistall")
        .then()
        .statusCode(OK.getStatusCode())
        .body("name", is(tree.name), "fruits.size()", is(2), "fruits.name",
            hasItems("orange", "Carrot"));

    // Ensure that the fruits persisted when persisting the tree are in th db
    given().when().get("/fruitreact/name/orange").then().statusCode(OK.getStatusCode())
        .body("name", CoreMatchers.is("orange"), "tree", is(tree.name));
    given().when().get("/fruitreact/name/Carrot").then().statusCode(OK.getStatusCode())
        .body("name", CoreMatchers.is("Carrot"), "tree", is(tree.name));
  }

  /**
   * Tree exist but Fruits do not exist
   */
  @Test
  public void addNewFruitsToExistingTree() {

    Tree tree = new Tree("addNewFruitsToExistingTree");
    // Ensure the tree is persisted
    given().when().body(tree).contentType(MediaType.APPLICATION_JSON).post("/tree/create").then()
        .statusCode(OK.getStatusCode()).body("name", CoreMatchers.is(tree.name));

    // In this example, fruits are not related with the tree.
    final List<FruitDto> fruits = new ArrayList<>() {{
      add(new FruitDto("orange", "Updated-Pear", "persistFam1", true));
      add(new FruitDto("Carrot", "not a fruit", "persistFam1", true));
    }};

    // Ensure the tree is UPDATED and the fruits have been INSERTED
    given().when().body(fruits).contentType(MediaType.APPLICATION_JSON)
        .post(String.format("/tree/addFruitsToTree/%s", tree.name)).then()
        .statusCode(OK.getStatusCode())
        .body("name", is(tree.name), "fruits.size()", is(2), "fruits.name",
            hasItems("orange", "Carrot"));

    // Ensure that the fruits persisted when persisting the tree are in th db
    given().when().get("/fruitreact/name/orange").then().statusCode(OK.getStatusCode())
        .body("name", CoreMatchers.is("orange"), "tree", is(tree.name));
    given().when().get("/fruitreact/name/Carrot").then().statusCode(OK.getStatusCode())
        .body("name", CoreMatchers.is("Carrot"), "tree", is(tree.name));
  }

  /**
   * <p>Tree do not exit, fruits exist.</p>
   */
//  @Test
  public void addExistingFruitsToNewTree() {
    // Persist the fruits
    given().when().body(new Fruit("pear", "Pear", "Rosaceae", false)).contentType(MediaType.APPLICATION_JSON)
      .post("/fruitreact/create").then().statusCode(OK.getStatusCode());
    given().when().body(new Fruit("apple", "Apple", "Rosaceae", false)).contentType(MediaType.APPLICATION_JSON)
      .post("/fruitreact/create").then().statusCode(OK.getStatusCode());

    // Create the tree without persisting it
    Tree tree = new Tree("alreadyPersistedTreeTest");

    // Ensure the tree is INSERTED and the fruits have been UPDATED
    given().when().body(tree).contentType(MediaType.APPLICATION_JSON).post("/tree/persist/Rosaceae").then()
      .statusCode(OK.getStatusCode()).body("name", is(tree.name), "fruits.size()", is(2)
        , "fruits.name", hasItems("pear", "apple"));

    // Ensure that the fruits persisted when persisting the tree are in th db
    given().when().get("/fruitreact/name/pear").then().statusCode(OK.getStatusCode()).body("name", is("pear"), "tree",is(tree.name));
    given().when().get("/fruitreact/name/apple").then().statusCode(OK.getStatusCode()).body("name", is("apple"), "tree",is(tree.name));
  }


}
