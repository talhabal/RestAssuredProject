
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import com.github.javafaker.Faker;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class SchoolLocations {

    Faker faker = new Faker();

    String locationID;

    String locationName;
    String locationShortName;  // ahmet
    int locationCapacity;

    String locationType;
    String locationSchool       =   "6390f3207a3bcb6a7ac977f9";



    RequestSpecification requSpec;

    @BeforeClass
    public void Login() {

        baseURI = "https://test.mersys.io";

        Map<String, String> location = new HashMap<>();
        location.put("username", "turkeyts");
        location.put("password", "TechnoStudy123");
        location.put("rememberMe", "true");

        Cookies cookies =
                given()
                        .contentType(ContentType.JSON)
                        .body(location)

                        .when()
                        .post("/auth/login")


                        .then()

                        .log().body()
                        .statusCode(200)
                        .extract().response().getDetailedCookies();

        requSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addCookies(cookies)
                .build();

    }

    @Test
    public void createLocation() {
        Map<String, Object> location = new HashMap<>();
        locationName            =   faker.address().cityName();
        locationShortName       =   faker.address().firstName();
        locationCapacity        =   faker.number().numberBetween(10,500);
        locationType            =   faker.country().name();


        location.put("name",locationName);
        location.put("shortName",locationShortName);
        location.put("capacity", locationCapacity);
        location.put("type","CLASS");
        location.put("school",locationSchool);



        locationID  =
        given()
                .spec(requSpec)
                .body(location)
                .log().body()

                .when()
                .post("/school-service/api/location")

                .then()
                .log().body()
                .statusCode(201)
                .extract().path("id");
    }

    @Test(dependsOnMethods = "createLocation")
    public void createLocationNegative() {
        Map<String, Object> location = new HashMap<>();

        location.put("name",locationName);
        location.put("shortName",locationShortName);
        location.put("capacity", locationCapacity);
        location.put("type","CLASS");
        location.put("school",locationSchool);

        given()
                .spec(requSpec)
                .body(location)
                .log().body()

                .when()
                .post("/school-service/api/location")

                .then()
                .log().body()
                .statusCode(400);

    }

    @Test(dependsOnMethods = "createLocationNegative")
    public void updateLocation() {
        Map<String, Object> location     = new HashMap<>();
        String locationUpdateName          =   faker.harryPotter().location();

        location.put("id",locationID);
        location.put("name",locationUpdateName);
        location.put("shortName",locationShortName);
        location.put("capacity", locationCapacity);
        location.put("type","CLASS");
        location.put("school",locationSchool);

        given()
                .spec(requSpec)
                .body(location)
                .log().body()

                .when()
                .put("/school-service/api/location")

                .then()
                .log().body()
                .statusCode(200)
                .body("name",equalTo(locationUpdateName));
    }

    @Test(dependsOnMethods = "updateLocation")
    public void deleteLocation() {
        given()
                .spec(requSpec)
                .pathParam("locID", locationID)


                .when()
                .delete("/school-service/api/location/{locID}")

                .then()
                .statusCode(200);


    }

    @Test(dependsOnMethods = "deleteLocation")
    public void deleteLocationNegative() {
        given()
                .spec(requSpec)
                .pathParam("locID", locationID)

                .when()
                .delete("/school-service/api/location/{locID}")

                .then()
                .statusCode(400)
                .body("message",equalTo("School Location not found"));
        System.out.println("Location bulunamadğından dolayı silinemedi!");


    }


}