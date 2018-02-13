package de.codecentric.nbyl.lift_and_openshift_ri;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.get;

public class PropertyResourceAT {

    @BeforeClass
    public static void configureBaseUrl() {
        RestAssured.baseURI = System.getProperty("acceptanceTest.baseUri", "http://localhost:8080");
    }

    @AfterClass
    public static void resetRestAssured() {
        RestAssured.reset();
    }

    @Test
    public void rootPathReturnsJson() {
        get("/")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }
}
