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

public class GradeLevels {
    Faker faker=new Faker();
    String gradeID;
    String gradeName;
    String gradeShortName;
    int gradeOrder;

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
    public void createGradeLevel()  {

        Map<String,String> gradeStr     =   new HashMap<>();
        gradeName           =   faker.university().name();
        gradeShortName      =   faker.university().prefix();
        gradeOrder          =   faker.number().numberBetween(1,12);

        gradeStr.put("name",gradeName);
        gradeStr.put("shortName",gradeShortName);
        gradeStr.put("order", String.valueOf(gradeOrder));


        gradeID  =
                given()
                        .spec(requSpec)
                        .body(gradeStr)

                        .when()
                        .post("/school-service/api/grade-levels")

                        .then()
                        .statusCode(201)
                        .extract().path("id");
        ;

        System.out.println("Grade level is added successfully!");
        System.out.println("Grade level ID : "+gradeID);
    }

    @Test(dependsOnMethods = "createGradeLevel")
    public void createGradeLevelNegative()  {

        Map<String,String> gradeStr     =   new HashMap<>();
        gradeName           =   faker.university().name();
        gradeShortName      =   faker.university().prefix();
        gradeOrder          =   faker.number().numberBetween(1,12);

        gradeStr.put("name",gradeName);
        gradeStr.put("shortName",gradeShortName);
        gradeStr.put("order", String.valueOf(gradeOrder));


        given()
                .spec(requSpec)
                .body(gradeStr)

                .when()
                .post("/school-service/api/grade-levels")

                .then()
                .statusCode(400)
        ;

        System.out.println("Couldn't add because same information exists!");

    }

    @Test(dependsOnMethods = "createGradeLevelNegative")
    public void updateGradeLevel()  {

        Map<String,String> gradeStr     =   new HashMap<>();
        gradeShortName      =   faker.university().suffix();

        gradeStr.put("id",gradeID);
        gradeStr.put("name",gradeName);
        gradeStr.put("shortName",gradeShortName);



        given()
                .spec(requSpec)
                .body(gradeStr)
                .log().body()


                .when()
                .put("/school-service/api/grade-levels")

                .then()
                .log().body()
                .statusCode(200)
                .body("shortName", equalTo(gradeShortName))

        ;
        System.out.println("Grade level's shortname and order is updated!");
    }

    @Test(dependsOnMethods = "updateGradeLevel")
    public void deleteGradeLevel()  {

        given()
                .spec(requSpec)
                .pathParam("gradeID", gradeID)
                .log().uri()

                .when()
                .delete("/school-service/api/grade-levels/{gradeID}")

                .then()
                .statusCode(200)
        ;
        System.out.println("Grade level of info is deleted successfully!");
    }

    @Test(dependsOnMethods = "deleteGradeLevel")
    public void deleteGradeLevelNegative()  {

        given()
                .spec(requSpec)
                .pathParam("gradeID", gradeID)
                .log().uri()

                .when()
                .delete("/school-service/api/grade-levels/{gradeID}")

                .then()
                .statusCode(400)
                .body("message",equalTo("Grade Level not found."))
        ;

        System.out.println("Could not delete because the requested class level information could not be found!");

    }
}
