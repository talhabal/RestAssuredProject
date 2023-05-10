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

public class Departments {

        Faker faker=new Faker();
        String departID;
        String departCode;

        String departName;

        String school="6390f3207a3bcb6a7ac977f9";
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
        public void createDepartmens()  {

            Map<String,String> departments=new HashMap<>();
            departName  =   faker.company().name();
            departCode  =   faker.company().suffix()+faker.company().industry();

            departments.put("name",departName);
            departments.put("code",departCode);
            departments.put("school",school);

            departID=
                    given()
                            .spec(recSpec)
                            .body(departments)
                            .log().body()

                            .when()
                            .post("/school-service/api/department")

                            .then()
                            .log().body()
                            .statusCode(201)
                            .extract().path("id");
            ;

            System.out.println("departID = " + departID);
        }

        @Test(dependsOnMethods = "createDepartmens")
        public void createDepartmentsNegative()  {

           Map<String,String> departments=new HashMap<>();

               departments.put("name",departName);
               departments.put("code",departCode);
               departments.put("school",school);

            given()
                    .spec(recSpec)
                    .body(departments)
                    .log().body()

                    .when()
                    .post("/school-service/api/department")

                    .then()
                    .log().body()
                    .statusCode(400)
                    .body("message", containsString("already"))
            ;
        }

        @Test(dependsOnMethods = "createDepartmentsNegative")
        public void updateDepartments()  {

           Map<String,String> departments=new HashMap<>();

              departName=faker.company().name();

              departments.put("name",departName);
              departments.put("code",departCode);
              departments.put("school",school);
              departments.put("id",departID);

            given()
                    .spec(recSpec)
                    .body(departments) 
                    .log().body()

                    .when()
                    .put("/school-service/api/department")

                    .then()
                    .log().body()
                    .statusCode(200)
                    .body("name", equalTo(departName))
            ;
        }

        @Test(dependsOnMethods = "updateDepartments")
        public void deleteDepartments()  {

            given()
                    .spec(recSpec)
                    .pathParam("departID", departID)
                    .log().uri()

                    .when()
                    .delete("/school-service/api/department/{departID}")

                    .then()
                    .log().body()
                    .statusCode(204)
            ;

        }

        @Test(dependsOnMethods = "deleteDepartments")
        public void deleteDepartmentsNegative()  {

            given()
                    .spec(recSpec)
                    .pathParam("departID", departID)
                    .log().uri()

                    .when()
                    .delete("/school-service/api/department/{departID}")

                    .then()
                    .log().body()
                    .statusCode(204)

            ;



    }

}
