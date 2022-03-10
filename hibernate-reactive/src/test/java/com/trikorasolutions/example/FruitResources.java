package com.trikorasolutions.example;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;

import com.trikorasolutions.example.model.Fruit;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MediaType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@ApplicationScoped
public class FruitResources {

    protected Response delete(final String fruit) {
    return given().when().delete(String.format("/fruit/%s", fruit));
  }

  protected Response create(final Fruit fruit) {
    return given().when().body(fruit).contentType(MediaType.APPLICATION_JSON)
        .post("/fruit/create");
  }

  protected Response get(final String fruit) {
    return given().when().get(String.format("/fruit/name/%s", fruit));
  }
}