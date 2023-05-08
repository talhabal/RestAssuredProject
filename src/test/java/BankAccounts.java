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
public class BankAccounts {
    Faker faker=new Faker();
    String bankID;
    String bankAccountName;
    String bankIBAN;
    String schoolID;
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
    public void createAccount()  {

        Map<String,String> bank     =   new HashMap<>();
        bankAccountName     =   faker.internet().domainName();
        bank.put("name",bankAccountName);
        bankIBAN            =   faker.finance().iban();
        bank.put("iban",bankIBAN);
        bank.put("integrationCode","50");
        bank.put("currency","EUR");
        schoolID            =   "6390f3207a3bcb6a7ac977f9";
        bank.put("schoolId",schoolID);

        bankID  =
                given()
                        .spec(requSpec)
                        .body(bank)
                        .log().body()

                        .when()
                        .post("/school-service/api/bank-accounts")

                        .then()
                        .statusCode(201)
                        .extract().path("id");
        ;

        System.out.println("Banka hesabı başarılı bir şekilde eklendi!");
        System.out.println("Banka Hesap ID : "+bankID);
    }

    @Test(dependsOnMethods = "createAccount")
    public void createAccountNegative()  {

        Map<String,String> bank     =   new HashMap<>();
        bank.put("name",bankAccountName);
        bank.put("iban",bankIBAN);
        bank.put("integrationCode","50");
        bank.put("currency","EUR");
        bank.put("schoolId",schoolID);


                given()
                        .spec(requSpec)
                        .body(bank)
                        .log().body()

                        .when()
                        .post("/school-service/api/bank-accounts")

                        .then()
                        .statusCode(400)
        ;

        System.out.println("Banka hesabı mevcutta bulunduğundan eklenemedi!");

    }

    @Test(dependsOnMethods = "createAccountNegative")
    public void updateAccount()  {

        Map<String,String> bank     =   new HashMap<>();
        bank.put("id",bankID);
        bankAccountName             =   faker.internet().domainName();
        bank.put("name",bankAccountName);
        bank.put("iban",bankIBAN);
        bank.put("integrationCode","80");
        bank.put("currency","EUR");
        bank.put("schoolId",schoolID);

        given()
                .spec(requSpec)
                .body(bank)
                .log().body()


                .when()
                .put("/school-service/api/bank-accounts")

                .then()
                .log().body()
                .statusCode(200)
                .body("name", equalTo(bankAccountName))

        ;
        System.out.println("Banka hesabı güncellendi!");
    }

    @Test(dependsOnMethods = "updateAccount")
    public void deleteAccount()  {

        given()
                .spec(requSpec)
                .pathParam("bankID", bankID)
                .log().uri()

                .when()
                .delete("/school-service/api/bank-accounts/{bankID}")

                .then()
                .statusCode(200)
        ;
        System.out.println("Eklediğiniz banka hesabı silindi!");
    }

    @Test(dependsOnMethods = "deleteAccount")
    public void deleteAccountNegative()  {

        given()
                .spec(requSpec)
                .pathParam("bankID", bankID)
                .log().uri()

                .when()
                .delete("/school-service/api/bank-accounts/{bankID}")

                .then()
                .statusCode(400)
                .body("message",equalTo("Please, bank account must be exist"))
        ;

        System.out.println("Aranan banka hesabı bulunamadığından dolayı silinemedi!");

    }
}
