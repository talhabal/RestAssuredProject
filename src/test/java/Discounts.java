import com.github.javafaker.Faker;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class Discounts {

    Faker faker = new Faker();
    String discountID;
    String description;
    String code;
    String priority;
    RequestSpecification recSpec;

    @BeforeClass
    public void Setup() {
        baseURI = "https://test.mersys.io";

        Map<String, String> userCredential = new HashMap<>();
        userCredential.put("username", "turkeyts");
        userCredential.put("password", "TechnoStudy123");
        userCredential.put("rememberMe", "true");

        Cookies cookies =
                given()
                        .contentType(ContentType.JSON)
                        .body(userCredential)

                        .when()
                        .post("/auth/login")

                        .then()
                        .statusCode(200)
                        .extract().response().getDetailedCookies();

        recSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addCookies(cookies)
                .build();
    }

    @Test
    public void createDiscount() {

        Map<String, String> discounts = new HashMap<>();
        description=faker.address().cityName();
        discounts.put("description", description);
        code=faker.address().zipCode();
        discounts.put("code", code);
        discounts.put("priority","25");


        discountID =
                given()
                        .spec(recSpec)
                        .body(discounts)
                        .log().body()

                        .when()
                        .post("/school-service/api/discounts")

                        .then()
                        .log().body()
                        .statusCode(201)
                        .extract().path(("id"))
        ;

        System.out.println("discountID = " + discountID);
    }

    @Test(dependsOnMethods = "createDiscount")
    public void createDiscountNegative() {

        Map<String, String> discounts = new HashMap<>();

        discounts.put("description", description);
        discounts.put("code", code);
        discounts.put("priority","25");

        given()
                .spec(recSpec)
                .body(discounts)
                .log().body()

                .when()
                .post("/school-service/api/discounts")

                .then()
                .log().body()
                .statusCode(400)
                .body("message", containsString("already"))
        ;
    }

    @Test (dependsOnMethods = "createDiscountNegative")
    public void updateDiscount(){

        Map<String, String> discounts = new HashMap<>();
        discounts.put("id", discountID);

        description="discountID-update"+faker.number().digits(7);
        discounts.put("description", description);
        discounts.put("code", code);
        discounts.put("priority","25");


        given()
                .spec(recSpec)
                .body(discounts)
                //.log().body()

                .when()
                .put("/school-service/api/discounts")

                .then()
                .log().body()
                .statusCode(200)
                .body("description" , equalTo(description))
        ;

    }


    @Test (dependsOnMethods = "updateDiscount")
    public void deleteDiscount() {
        given()
                .spec(recSpec)
                .pathParam("discountID", discountID)
                .log().uri()

                .when()
                .delete("/school-service/api/discounts/{discountID}")

                .then()
                .log().body()
                .statusCode(200)

        ;

    }


    @Test (dependsOnMethods = "deleteDiscount")
    public void deleteDiscountNegative() {
        given()
                .spec(recSpec)
                .pathParam("discountID", discountID)
                .log().uri()

                .when()
                .delete("/school-service/api/discounts/{discountID}")

                .then()
                .log().body()
                .statusCode(400)
                .body("message" , equalTo("Discount not found"))
        ;

    }



}
