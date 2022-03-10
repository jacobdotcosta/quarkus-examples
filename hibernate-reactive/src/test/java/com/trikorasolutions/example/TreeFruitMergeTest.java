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
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
public class TreeFruitMergeTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TreeFruitMergeTest.class);

  @Inject
  private FruitResources fruitResources;

  @Inject
  private TreeResources treeResources;

  /**
   * <p>Add existing fruits to existing tree.</p>
   */
  @Test
  public void addExistingFruitsToExistingTree() {
    final Fruit appleFruit = new Fruit("apple", "Apple", "Rosaceae", false);
    final Fruit pearFruit = new Fruit("pear", "Pear", "Rosaceae", false);
    final Tree tree = new Tree("addExistingFruitsToExistingTree");
    fruitResources.create(pearFruit);
    fruitResources.create(appleFruit);
    treeResources.create(tree);

    // Add fruits to tree.
    given().when().body(tree).contentType(MediaType.APPLICATION_JSON).post("/tree/persist/Rosaceae")
        .then().statusCode(OK.getStatusCode())
        .body("name", is(tree.name), "fruits.size()", Matchers.greaterThanOrEqualTo(2),
            "fruits.name",
            hasItems("pear", "apple"));

    fruitResources.get(pearFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", is("pear"), "tree", is(tree.name));
    fruitResources.get(appleFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", is("apple"), "tree", is(tree.name));

    fruitResources.delete(pearFruit.name);
    fruitResources.delete(appleFruit.name);
  }

  /**
   * Tree does not exist, Fruits do not exist.
   */
  @Test
  public void persistNewTreeAndNewFruits() {
    final TreeDto tree = new TreeDto("persistNewTreeAndNewFruits");
    final FruitDto orangeFruit = new FruitDto("orange", "Updated-Pear", "persistFam1", true);
    final FruitDto carrotNotFruit = new FruitDto("Carrot", "not a fruit", "persistFam1", true);

    // In this example, fruits are not related with the tree.
    final List<FruitDto> fruits = new ArrayList<>() {{
      add(orangeFruit);
      add(carrotNotFruit);
    }};
    tree.setFruits(fruits);

    // Ensure the tree and the fruits have been INSERTED
    given().when().body(tree).contentType(MediaType.APPLICATION_JSON).post("/tree/persistall")
        .then()
        .statusCode(OK.getStatusCode())
        .body("name", is(tree.name), "fruits.size()", Matchers.greaterThanOrEqualTo(2),
            "fruits.name",
            hasItems(orangeFruit.name, carrotNotFruit.name));

    // Ensure that the fruits persisted when persisting the tree are in th db
    fruitResources.get(orangeFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", CoreMatchers.is("orange"), "tree", is(tree.name));
    fruitResources.get(carrotNotFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", CoreMatchers.is("Carrot"), "tree", is(tree.name));

    fruitResources.delete(orangeFruit.name);
    fruitResources.delete(carrotNotFruit.name);
  }

  /**
   * Tree exist but Fruits do not exist
   */
  @Test
  public void addNewFruitsToExistingTree() {
    final FruitDto orangeFruit = new FruitDto("orange", "Updated-Pear", "persistFam1", true);
    final FruitDto carrotNotFruit = new FruitDto("Carrot", "not a fruit", "persistFam1", true);
    final Tree tree = new Tree("addNewFruitsToExistingTree");

    treeResources.create(tree).then().statusCode(OK.getStatusCode())
        .body("name", CoreMatchers.is(tree.name));

    // In this example, fruits are not related with the tree.
    final List<FruitDto> fruits = new ArrayList<>() {{
      add(orangeFruit);
      add(carrotNotFruit);
    }};

    // Ensure the tree is UPDATED and the fruits have been INSERTED
    given().when().body(fruits).contentType(MediaType.APPLICATION_JSON)
        .post(String.format("/tree/addFruitsToTree/%s", tree.name)).then()
        .statusCode(OK.getStatusCode())
        .body("name", is(tree.name), "fruits.size()", Matchers.greaterThanOrEqualTo(2),
            "fruits.name",
            hasItems("orange", "Carrot"));

    // Ensure that the fruits persisted when persisting the tree are in th db
    fruitResources.get(orangeFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", CoreMatchers.is("orange"), "tree", is(tree.name));
    fruitResources.get(carrotNotFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", CoreMatchers.is("Carrot"), "tree", is(tree.name));

    fruitResources.delete(orangeFruit.name);
    fruitResources.delete(carrotNotFruit.name);
  }

  /**
   * <p>Tree do not exit, fruits exist.</p>
   */
  @Test
  public void addExistingFruitsToNewTree() {
    final Fruit appleFruit = new Fruit("apple", "Apple", "Rosaceae", false);
    final Fruit pearFruit = new Fruit("pear", "Pear", "Rosaceae", false);
    Tree tree = new Tree("alreadyPersistedTreeTest");
    fruitResources.create(pearFruit);
    fruitResources.create(appleFruit);

    // Ensure the tree is INSERTED and the fruits have been UPDATED
    given().when().body(tree).contentType(MediaType.APPLICATION_JSON).post("/tree/persist/Rosaceae")
        .then()
        .statusCode(OK.getStatusCode())
        .body("name", is(tree.name), "fruits.size()", Matchers.greaterThanOrEqualTo(2)
            , "fruits.name", hasItems("pear", "apple"));

    // Ensure that the fruits persisted when persisting the tree are in th db
    fruitResources.get(pearFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", is("pear"), "tree", is(tree.name));
    fruitResources.get(appleFruit.name).then().statusCode(OK.getStatusCode())
        .body("name", is("apple"), "tree", is(tree.name));

    fruitResources.delete(pearFruit.name);
    fruitResources.delete(appleFruit.name);
  }


}
