package com.trikorasolutions.example;

import com.trikorasolutions.example.dto.TreeDto;
import com.trikorasolutions.example.model.Tree;
import io.restassured.response.Response;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MediaType;

import static io.restassured.RestAssured.given;

@ApplicationScoped
public class TreeResources {

  protected Response delete(final String tree) {
    return given().when().delete(String.format("/tree/%s", tree));
  }

  protected Response create(final Tree tree) {
    return given().when().body(tree).contentType(MediaType.APPLICATION_JSON)
        .post("/tree/create");
  }

  protected Response create(final TreeDto tree) {
    return given().when().body(tree).contentType(MediaType.APPLICATION_JSON)
        .post("/tree/create");
  }

  protected Response get(final String tree) {
    return given().when().get(String.format("/tree/name/%s", tree));
  }
}