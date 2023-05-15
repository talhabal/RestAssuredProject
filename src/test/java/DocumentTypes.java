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

public class DocumentTypes {

    Faker faker = new Faker();
    String documentID;
    String schoolID="6390f3207a3bcb6a7ac977f9";
    String documentName;
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
    public void createDocument() {

        Map<String, Object> documents = new HashMap<>();
        documentName = faker.number().digits(5) + faker.file().fileName();
        documents.put("name", documentName);
        documents.put("attachmentStages", new String[]{"EXAMINATION"});
        documents.put("description", "This a description for this document");
        documents.put("schoolId", schoolID);

        documentID =
                given()
                        .spec(recSpec)
                        .body(documents)
                        .log().body()

                        .when()
                        .post("/school-service/api/attachments/create")

                        .then()
                        .log().body()
                        .statusCode(201)
                        .extract().path(("id"))
        ;

        System.out.println("documentID = " + documentID);
    }

    @Test
    public void createDocumentNegative() {

        Map<String, Object> documents = new HashMap<>();

        documents.put("name", documentName);
        documents.put("attachmentStages", new String[]{"EXAMINATION"});
        documents.put("description", "This a description for this document");
        documents.put("schoolId", schoolID);

        given()
                .spec(recSpec)
                .body(documents)
                .log().body()

                .when()
                .post("/school-service/api/attachments/create")

                .then()
                .log().body()
                .statusCode(400)
                .body("message", containsString("already"))
        ;
    }

    @Test (dependsOnMethods = "createDocument")
    public void updateDocument(){

        Map<String, Object> documents = new HashMap<>();
        documents.put("id", documentID);

        documentName="documentsID-update"+faker.number().digits(7);
        documents.put("name", documentName);
        documents.put("attachmentStages", new String[]{"EXAMINATION"});
        documents.put("description", "This a description for this document");
        documents.put("schoolId", schoolID);


        given()
                .spec(recSpec)
                .body(documents)
                //.log().body()

                .when()
                .put("/school-service/api/attachments")

                .then()
                .log().body()
                .statusCode(200)
                .body("name" , equalTo(documentName))
        ;

    }

    @Test (dependsOnMethods = "updateDocument")
    public void deleteDocument() {
        given()
                .spec(recSpec)
                .pathParam("documentID", documentID)
                .log().uri()

                .when()
                .delete("/school-service/api/attachments/{documentID}")

                .then()
                .log().body()
                .statusCode(200)

        ;

    }


    @Test (dependsOnMethods = "deleteDocument")
    public void deleteDocumentNegative() {
        given()
                .spec(recSpec)
                .pathParam("documentID", documentID)
                .log().uri()

                .when()
                .delete("/school-service/api/attachments/{documentID}")

                .then()
                .log().body()
                .statusCode(400)
                .body("message" , equalTo("Attachment Type not found"))
        ;

    }

}


