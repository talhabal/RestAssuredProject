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

public class Nationalities {
    Faker faker=new Faker();
    String nationalityID;
    String nationalityName;

    RequestSpecification requSpec;
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
                        .statusCode(200)
                        .extract().response().getDetailedCookies()
                ;

        requSpec= new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addCookies(cookies)
                .build();
    }

    @Test
    public void createNationality()  {

        Map<String,String> nationality     =   new HashMap<>();
        nationalityName           =   faker.country().name()+" "+faker.address().zipCode();
        nationality.put("name",nationalityName);



        nationalityID  =
                given()
                        .spec(requSpec)
                        .body(nationality)

                        .when()
                        .post("/school-service/api/nationality")

                        .then()
                        .statusCode(201)
                        .extract().path("id");
        ;

        System.out.println("Nationality is added successfully!");
        System.out.println("Nationality ID : "+nationalityID);
        System.out.println("Nationality Name : "+nationalityName);
    }

    @Test(dependsOnMethods = "createNationality")
    public void createNationalityNegative()  {

        Map<String,String> nationality     =   new HashMap<>();
        nationality.put("name",nationalityName);


        given()
                .spec(requSpec)
                .body(nationality)

                .when()
                .post("/school-service/api/nationality")

                .then()
                .statusCode(400)
        ;

        System.out.println("Couldn't add because same information exists!");

    }

    @Test(dependsOnMethods = "createNationalityNegative")
    public void updateNationality()  {

        Map<String,String> nationality     =   new HashMap<>();
        nationalityName           = faker.country().name()+" "+faker.country().currencyCode();
        nationality.put("id",nationalityID);
        nationality.put("name",nationalityName);


        given()
                .spec(requSpec)
                .body(nationality)
                .log().body()


                .when()
                .put("/school-service/api/nationality")

                .then()
                .log().body()
                .statusCode(200)
                .body("name", equalTo(nationalityName))

        ;
        System.out.println("Nationality name is updated!");
    }

    @Test(dependsOnMethods = "updateNationality")
    public void deleteNationality()  {

        given()
                .spec(requSpec)
                .pathParam("nationalityID", nationalityID)
                .log().uri()

                .when()
                .delete("/school-service/api/nationality/{nationalityID}")

                .then()
                .statusCode(200)
        ;
        System.out.println("Nationality of info is deleted successfully!");
    }

    @Test(dependsOnMethods = "deleteNationality")
    public void deleteNationalityNegative()  {

        given()
                .spec(requSpec)
                .pathParam("nationalityID", nationalityID)
                .log().uri()

                .when()
                .delete("/school-service/api/nationality/{nationalityID}")

                .then()
                .statusCode(400)
                .body("message",equalTo("Nationality not  found"))
        ;

        System.out.println("Could not delete because the requested class level information could not be found!");

    }
}
