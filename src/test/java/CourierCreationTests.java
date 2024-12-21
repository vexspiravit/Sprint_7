import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Courier Management")
@Feature("Courier Creation")
public class CourierCreationTests {

    private String courierLogin;
    private String courierPassword;
    private String courierId;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @Story("Create a new courier successfully")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test verifies that a courier can be created successfully with valid data.")
    public void createCourierTest() {
        courierLogin = "JohnSnow" + System.currentTimeMillis(); // Уникальный логин
        courierPassword = "12345";
        String firstName = "John";

        Response response = given()
                .contentType("application/json")
                .body("{\n" +
                        "    \"login\": \"" + courierLogin + "\",\n" +
                        "    \"password\": \"" + courierPassword + "\",\n" +
                        "    \"firstName\": \"" + firstName + "\"\n" +
                        "}")
                .when()
                .post("/api/v1/courier");

        response.then().statusCode(201);
        response.then().body("ok", equalTo(true));
    }

    @Test
    @Story("Prevent duplicate courier creation")
    @Severity(SeverityLevel.NORMAL)
    @Description("This test ensures that creating a courier with an existing login results in a conflict error.")
    public void createCourierConflictTest() {
        courierLogin = "courier_" + System.currentTimeMillis();
        courierPassword = "password123";
        String firstName = "John";

        given()
                .contentType("application/json")
                .body("{\"login\": \"" + courierLogin + "\", \"password\": \"" + courierPassword + "\", \"firstName\": \"" + firstName + "\"}")
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201);

        given()
                .contentType("application/json")
                .body("{\"login\": \"" + courierLogin + "\", \"password\": \"" + courierPassword + "\", \"firstName\": \"" + firstName + "\"}")
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется"));
    }

    @Test
    @Story("Fail to create courier without login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test verifies that creating a courier without providing a login returns an error.")
    public void createCourierWithoutLoginTest() {
        courierPassword = "12345";
        String firstName = "John";

        given()
                .contentType("application/json")
                .body("{\n" +
                        "    \"password\": \"" + courierPassword + "\",\n" +
                        "    \"firstName\": \"" + firstName + "\"\n" +
                        "}")
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @Story("Fail to create courier without password")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test verifies that creating a courier without providing a password returns an error.")
    public void createCourierWithoutPasswordTest() {
        courierLogin = "NoPasswordCourier" + System.currentTimeMillis();
        String firstName = "John";

        given()
                .contentType("application/json")
                .body("{\n" +
                        "    \"login\": \"" + courierLogin + "\",\n" +
                        "    \"firstName\": \"" + firstName + "\"\n" +
                        "}")
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @Story("Fail to create courier without first name")
    @Severity(SeverityLevel.MINOR)
    @Description("This test verifies that creating a courier without a first name returns an error.")
    public void createCourierWithoutFirstNameTest() {
        courierLogin = "NoFirstNameCourier" + System.currentTimeMillis();
        courierPassword = "12345";

        given()
                .contentType("application/json")
                .body("{\n" +
                        "    \"login\": \"" + courierLogin + "\",\n" +
                        "    \"password\": \"" + courierPassword + "\"\n" +
                        "}")
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @After
    public void tearDown() {
        if (courierLogin != null && courierPassword != null) {
            Response response = given()
                    .contentType("application/json")
                    .body("{\"login\": \"" + courierLogin + "\", \"password\": \"" + courierPassword + "\"}")
                    .when()
                    .post("/api/v1/courier/login");

            if (response.statusCode() == 200) {
                courierId = response.jsonPath().getString("id");

                given()
                        .when()
                        .delete("/api/v1/courier/" + courierId)
                        .then()
                        .statusCode(200);
                System.out.println("Courier with ID " + courierId + " has been deleted.");
            } else {
                System.out.println("Failed to retrieve courier ID for deletion.");
            }
        } else {
            System.out.println("Courier login or password was null, skipping deletion.");
        }
    }
}
