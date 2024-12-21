import io.restassured.RestAssured;
import io.restassured.response.Response;

public class CourierUtils {

    private static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";

    public static String createCourier(String login, String password, String firstName) {
        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body("{ \"login\": \"" + login + "\", \"password\": \"" + password + "\", \"firstName\": \"" + firstName + "\" }")
                .when()
                .post("/api/v1/courier");

        if (response.statusCode() != 201 || !response.jsonPath().getBoolean("ok")) {
            throw new RuntimeException("Failed to create courier: " + response.asString());
        }

        System.out.println("Courier created: " + login);
        return login; // Возвращаем логин для дальнейших операций
    }

    public static String loginCourier(String login, String password) {
        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body("{\"login\": \"" + login + "\", \"password\": \"" + password + "\"}")
                .when()
                .post("/api/v1/courier/login");

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to login courier: " + response.asString());
        }

        return response.jsonPath().getString("id"); // Возвращаем ID курьера
    }

    // Удаление курьера по его ID
    public static void deleteCourier(String courierId) {
        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .when()
                .delete("/api/v1/courier/" + courierId);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to delete courier: " + response.asString());
        }

        System.out.println("Courier with ID " + courierId + " has been deleted.");
    }
}
