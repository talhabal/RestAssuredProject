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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class PositionsCategories {

        Faker faker=new Faker();
        String PosCateID;

        String PosCateName;
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
        public void createPositionsCategories()  {

            Map<String,String> category=new HashMap<>();
            PosCateName=faker.job().position();
            category.put("name",PosCateName);

            PosCateID=
                    given()
                            .spec(recSpec)
                            .body(category)
                            .log().body()

                            .when()
                            .post("/school-service/api/position-category")

                            .then()
                            .log().body()
                            .statusCode(201)
                            .extract().path("id");
            ;

            System.out.println("PosCateID = " + PosCateID);
        }

        @Test(dependsOnMethods = "createPositionsCategories")
        public void createPositionsCategoriesNegative()  {

            Map<String,String> category=new HashMap<>();
            category.put("name",PosCateName);

            given()
                    .spec(recSpec)
                    .body(category)
                    .log().body()

                    .when()
                    .post("/school-service/api/position-category")

                    .then()
                    .log().body()
                    .statusCode(400)
                    .body("message", containsString("already"))
            ;
        }

        @Test(dependsOnMethods = "createPositionsCategoriesNegative")
        public void updatePositionsCategories()  {

            Map<String,String> category=new HashMap<>();
            PosCateName     =   faker.job().title();

            category.put("id",PosCateID);
            category.put("name",PosCateName);


            given()
                    .spec(recSpec)
                    .body(category)

                    .when()
                    .put("/school-service/api/position-category")

                    .then()
                    .log().body()
                    .statusCode(200)
                    .body("name", equalTo(PosCateName))
            ;
        }

        @Test(dependsOnMethods = "updatePositionsCategories")
        public void deletePositionsCategories()  {

            given()
                    .spec(recSpec)
                    .pathParam("PosCateID", PosCateID)
                    .log().uri()

                    .when()
                    .delete("/school-service/api/position-category/{PosCateID}")

                    .then()
                    .log().body()
                    .statusCode(204)
            ;

        }

        @Test(dependsOnMethods = "deletePositionsCategories")
        public void deletePositionsCategoriesNegative()  {

            given()
                    .spec(recSpec)
                    .pathParam("PosCateID", PosCateID)
                    .log().uri()

                    .when()
                    .delete("/school-service/api/position-category/{PosCateID}")

                    .then()
                    .log().body()
                    .statusCode(400)
                    .body("message",equalTo("PositionCategory not  found"))
            ;



    }

}
