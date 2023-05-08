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

public class SubjectCategories {
    Faker faker=new Faker();
    String categoryID;
    String categoryName;
    String categoryCode;

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
    public void createCategory()  {

        Map<String,String> category     =   new HashMap<>();
        categoryName           =   faker.beer().malt();
        categoryCode           =   faker.country().countryCode2();

        category.put("name",categoryName);
        category.put("code",categoryCode);

        categoryID  =
                given()
                        .spec(requSpec)
                        .body(category)

                        .when()
                        .post("/school-service/api/subject-categories")

                        .then()
                        .statusCode(201)
                        .extract().path("id");
        ;

        System.out.println("Subject Category is added successfully!");
        System.out.println("Subject Category ID : "+categoryID);
        System.out.println("Subject Category Name : "+categoryName);
    }

    @Test(dependsOnMethods = "createCategory")
    public void createCategoryNegative()  {

        Map<String,String> category     =   new HashMap<>();

        category.put("name",categoryName);
        category.put("code",categoryCode);


        given()
                .spec(requSpec)
                .body(category)

                .when()
                .post("/school-service/api/subject-categories")

                .then()
                .statusCode(400)
        ;

        System.out.println("Couldn't add because same information exists!");

    }

    @Test(dependsOnMethods = "createCategoryNegative")
    public void updateCategory()  {

        Map<String,String> attestations     =   new HashMap<>();
        categoryCode           =   faker.address().zipCode();
        attestations.put("name",categoryName);
        attestations.put("code",categoryCode);
        attestations.put("id",categoryID);



        given()
                .spec(requSpec)
                .body(attestations)

                .when()
                .put("/school-service/api/subject-categories")

                .then()
                .statusCode(200)
                .body("code", equalTo(categoryCode))

        ;
        System.out.println("Code of Subject Category is updated!");
    }

    @Test(dependsOnMethods = "updateCategory")
    public void deleteCategory()  {

        given()
                .spec(requSpec)
                .pathParam("categoryID", categoryID)


                .when()
                .delete("/school-service/api/subject-categories/{categoryID}")

                .then()
                .statusCode(200)
        ;
        System.out.println("Subject category of info is deleted successfully!");
    }

    @Test(dependsOnMethods = "deleteCategory")
    public void deleteCategoryNegative()  {

        given()
                .spec(requSpec)
                .pathParam("categoryID", categoryID)


                .when()
                .delete("/school-service/api/subject-categories/{categoryID}")

                .then()
                .statusCode(400)
                .body("message",equalTo("SubjectCategory not  found"))
        ;

        System.out.println("Could not delete because the requested subject category information could not be found!");

    }
}
