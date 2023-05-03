import com.github.javafaker.Faker;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class OrnekCountryTest {

    Faker faker=new Faker();
    String countryID;

    String countryName;
    RequestSpecification recSpec;

    @BeforeClass
    public void Setup()  {
        baseURI="https://test.mersys.io";

        Map<String,String> userCredential=new HashMap<>();
        userCredential.put("username","turkeyts");
        userCredential.put("password","TechnoStudy123");
        userCredential.put("rememberMe","true");

        Cookies cookies=
                given()
                        .contentType(ContentType.JSON)
                        .body(userCredential)

                        .when()
                        .post("/auth/login")

                        .then()
                        //.log().all()
                        .statusCode(200)
                        .extract().response().getDetailedCookies()
                ;

        recSpec= new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addCookies(cookies)
                .build();
    }

    @Test
    public void createCountry()  {

        Map<String,String> country=new HashMap<>();
        countryName=faker.address().country()+faker.number().digits(5);
        country.put("name",countryName);
        country.put("code",faker.address().countryCode()+faker.number().digits(5));

        countryID=
                given()
                        .spec(recSpec)
                        .body(country)
                        .log().body()

                        .when()
                        .post("/school-service/api/countries")

                        .then()
                        .log().body()
                        .statusCode(201)
                        .extract().path("id");
        ;

        System.out.println("countryID = " + countryID);
    }

    @Test(dependsOnMethods = "createCountry")
    public void createCountryNegative()  {

        Map<String,String> country=new HashMap<>();
        country.put("name",countryName);
        country.put("code",faker.address().countryCode()+faker.number().digits(5));

        given()
                .spec(recSpec)
                .body(country) // giden body
                .log().body() // giden body yi log olarak göster

                .when()
                .post("/school-service/api/countries")

                .then()
                .log().body() // gelen body yi log olarak göster
                .statusCode(400)
                .body("message", containsString("already"))  // gelen body deki...
        ;
    }

    @Test(dependsOnMethods = "createCountryNegative")
    public void updateCountry()  {

        Map<String,String> country=new HashMap<>();
        country.put("id",countryID);

        countryName="ismet ülkesi"+faker.number().digits(7);
        country.put("name",countryName);
        country.put("code",faker.address().countryCode()+faker.number().digits(5));

        given()
                .spec(recSpec)
                .body(country) // giden body
                //.log().body() // giden body yi log olarak göster

                .when()
                .put("/school-service/api/countries")

                .then()
                .log().body() // gelen body yi log olarak göster
                .statusCode(200)
                .body("name", equalTo(countryName))
        ;
    }

    @Test(dependsOnMethods = "updateCountry")
    public void deleteCountry()  {

        given()
                .spec(recSpec)
                .pathParam("countryID", countryID)
                .log().uri()

                .when()
                .delete("/school-service/api/countries/{countryID}")

                .then()
                .log().body() // gelen body yi log olarak göster
                .statusCode(200)
        ;

    }

    @Test(dependsOnMethods = "deleteCountry")
    public void deleteCountryNegative()  {

        given()
                .spec(recSpec)
                .pathParam("countryID", countryID)
                .log().uri()

                .when()
                .delete("/school-service/api/countries/{countryID}")

                .then()
                .log().body() // gelen body yi log olarak göster
                .statusCode(400)
                .body("message",equalTo("Country not found"))
        ;

    }

}
