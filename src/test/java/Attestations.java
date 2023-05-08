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

public class Attestations {
    Faker faker=new Faker();
    String attestationID;
    String attestationName;

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
    public void createAttestations()  {

        Map<String,String> attestations     =   new HashMap<>();
        attestationName           =   faker.app().name();


        attestations.put("name",attestationName);

        attestationID  =
                given()
                        .spec(requSpec)
                        .body(attestations)

                        .when()
                        .post("/school-service/api/attestation")

                        .then()
                        .statusCode(201)
                        .extract().path("id");
        ;

        System.out.println("Attestations is added successfully!");
        System.out.println("Attestations ID : "+attestationID);
        System.out.println("Attestations Name : "+attestationName);
    }

    @Test(dependsOnMethods = "createAttestations")
    public void createAttestationsNegative()  {

        Map<String,String> attestations     =   new HashMap<>();
        attestations.put("name",attestationName);


        given()
                .spec(requSpec)
                .body(attestations)

                .when()
                .post("/school-service/api/attestation")

                .then()
                .statusCode(400)
        ;

        System.out.println("Couldn't add because same information exists!");

    }

    @Test(dependsOnMethods = "createAttestationsNegative")
    public void updateAttestations()  {

        Map<String,String> attestations     =   new HashMap<>();
        attestationName           =   faker.app().name()+" "+faker.app().version();
        attestations.put("name",attestationName);
        attestations.put("id",attestationID);



        given()
                .spec(requSpec)
                .body(attestations)
                .log().body()


                .when()
                .put("/school-service/api/attestation")

                .then()
                .log().body()
                .statusCode(200)
                .body("name", equalTo(attestationName))

        ;
        System.out.println("Attestation name is updated!");
    }

    @Test(dependsOnMethods = "updateAttestations")
    public void deleteAttestations()  {

        given()
                .spec(requSpec)
                .pathParam("attID", attestationID)
                //.log().uri()

                .when()
                .delete("/school-service/api/attestation/{attID}")

                .then()
                .statusCode(204)
        ;
        System.out.println("Attestation of info is deleted successfully!");
    }

    @Test(dependsOnMethods = "deleteAttestations")
    public void deleteAttestationsNegative()  {

        given()
                .spec(requSpec)
                .pathParam("attestationID", attestationID)
                .log().uri()

                .when()
                .delete("/school-service/api/attestation/{attestationID}")

                .then()
                .statusCode(400)
                .body("message",equalTo("attestation not found"))
        ;

        System.out.println("Could not delete because the requested class level information could not be found!");

    }
}
