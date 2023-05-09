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
import static org.hamcrest.Matchers.equalTo;

public class Positions {
    Faker faker=new Faker();
    String positionID;  // 24545787456
    String positionName;  //Senior Web Developer
    String positionShortName;
    String positionTenant       =   "6390ef53f697997914ec20c2";


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
    public void createPosition()  {

        Map<String,String> position     =   new HashMap<>();
        positionName                =   faker.job().position();
        positionShortName           =   faker.job().title();


        position.put("name",positionName);
        position.put("shortName",positionShortName);
        position.put("tenantId",positionTenant);

        positionID  =
                given()
                        .spec(requSpec)
                        .body(position)

                        .when()
                        .post("/school-service/api/employee-position")

                        .then()
                        .statusCode(201)
                        .extract().path("id");
        ;

        System.out.println("Position is added successfully!");
        System.out.println("Position ID : "+positionID);
        System.out.println("Position Name : "+positionName);
    }

    @Test(dependsOnMethods = "createPosition")
    public void createPositionNegative()  {

        Map<String,String> position     =   new HashMap<>();

        position.put("name",positionName);
        position.put("shortName",positionShortName);
        position.put("tenantId",positionTenant);


        given()
                .spec(requSpec)
                .body(position)


                .when()
                .post("/school-service/api/employee-position")

                .then()
                .log().body()
                .statusCode(400)
        ;

        System.out.println("Couldn't add because same information exists!");

    }

    @Test(dependsOnMethods = "createPositionNegative")
    public void updatePosition()  {

        Map<String,String> position     =   new HashMap<>();
        positionName                    =   faker.job().field();

        position.put("id",positionID);
        position.put("name",positionName);
        position.put("shortName",positionShortName);
        position.put("tenantId",positionTenant);



        given()
                .spec(requSpec)
                .body(position)

                .when()
                .put("/school-service/api/employee-position")

                .then()
                .log().body()
                .statusCode(200)
                .body("name", equalTo(positionName))

        ;
        System.out.println("Name of Position is updated!");
    }

    @Test(dependsOnMethods = "updatePosition")
    public void deletePosition()  {

        given()
                .spec(requSpec)
                .pathParam("positionID", positionID)


                .when()
                .delete("/school-service/api/employee-position/{positionID}")

                .then()
                .log().body()
                .statusCode(204)
        ;
        System.out.println("Position of info is deleted successfully!");
    }

    @Test(dependsOnMethods = "deletePosition")
    public void deletePositionNegative()  {

        given()
                .spec(requSpec)
                .pathParam("positionID", positionID)

                .when()
                .delete("/school-service/api/employee-position/{positionID}")

                .then()
                .statusCode(204)
        ;

        System.out.println("Could not delete because the requested position information could not be found!");

    }
}
