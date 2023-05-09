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

public class Fields {
    Faker faker=new Faker();
    String fieldID;
    String fieldName;
    String fieldCode;
    String schoolID    =   "6390f3207a3bcb6a7ac977f9";

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
    public void createField()  {

        Map<String,String> fields     =   new HashMap<>();
        fieldName           =   faker.harryPotter().book();
        fieldCode           =   faker.address().zipCode();

        fields.put("name",fieldName);
        fields.put("code",fieldCode);
        fields.put("schoolId",schoolID);
        fields.put("type","STRING");

        fieldID  =
                given()
                        .spec(requSpec)
                        .body(fields)

                        .when()
                        .post("/school-service/api/entity-field")

                        .then()
                        .statusCode(201)
                        .extract().path("id");
        ;

        System.out.println("Field is added successfully!");
        System.out.println("Field ID : "+fieldID);
        System.out.println("Field Name : "+fieldName);
    }

    @Test(dependsOnMethods = "createField")
    public void createFieldNegative()  {

        Map<String,String> fields     =   new HashMap<>();

        fields.put("name",fieldName);
        fields.put("code",fieldCode);


        given()
                .spec(requSpec)
                .body(fields)

                .when()
                .post("/school-service/api/entity-field")

                .then()
                .statusCode(400)
        ;

        System.out.println("Couldn't add because same information exists!");

    }

    @Test(dependsOnMethods = "createFieldNegative")
    public void updateField()  {

        Map<String,String> fields     =   new HashMap<>();
        fieldName           =   faker.beer().name();
        fields.put("id",fieldID);
        fields.put("name",fieldName);
        fields.put("code",fieldCode);
        fields.put("schoolId",schoolID);
        fields.put("type","STRING");



        given()
                .spec(requSpec)
                .body(fields)



                .when()
                .put("/school-service/api/entity-field")

                .then()
                .statusCode(200)
                .body("name", equalTo(fieldName))

        ;
        System.out.println("Name of Field is updated!");
    }

    @Test(dependsOnMethods = "updateField")
    public void deleteField()  {

        given()
                .spec(requSpec)
                .pathParam("fieldID", fieldID)


                .when()
                .delete("/school-service/api/entity-field/{fieldID}")

                .then()
                .statusCode(204)
        ;
        System.out.println("Field of info is deleted successfully!");
    }

    @Test(dependsOnMethods = "deleteField")
    public void deleteFieldNegative()  {

        given()
                .spec(requSpec)
                .pathParam("fieldID", fieldID)

                .when()
                .delete("/school-service/api/entity-field/{fieldID}")

                .then()
                .statusCode(400)
                .body("message",equalTo("EntityField not found"))
        ;

        System.out.println("Could not delete because the requested field information could not be found!");

    }
}
