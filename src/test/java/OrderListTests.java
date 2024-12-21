import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Order Management")
@Feature("Order List Retrieval")
public class OrderListTests {

    private String courierId;
    private int orderId;

    private final String firstName = "John";
    private final String lastName = "Doe";
    private final String address = "123 Main St";
    private final String metroStation = "5";
    private final String phone = "+1234567890";
    private final int rentTime = 5;
    private final String deliveryDate = "2023-12-31";
    private final String comment = "Test order";

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        String courierLogin = "122testLogin";
        try {
            CourierUtils.deleteCourier(courierLogin);
        } catch (Exception e) {
            System.out.println("No courier to delete for login: " + courierLogin);
        }

        courierId = CourierUtils.createCourier(courierLogin, "testPassword", "John");
        System.out.println("Courier created with ID: " + courierId);

        courierId = CourierUtils.loginCourier(courierLogin, "testPassword");
        System.out.println("Courier logged in with ID: " + courierId);

        orderId = createOrder(new String[]{"BLACK"});
        System.out.println("Order created with ID: " + orderId);

        assignOrderToCourier(orderId, courierId);
        System.out.println("Order with ID " + orderId + " assigned to courier with ID " + courierId);
    }

    @Test
    @Story("Retrieve orders for a valid courier")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test verifies that a valid courier ID can successfully retrieve the list of orders.")
    @Step("Get orders list for courier ID: {courierId}")
    public void getOrdersListWithValidCourierIdTest() {
        Response response = given()
                .contentType("application/json")
                .param("courierId", courierId)
                .when()
                .get("/api/v1/orders");

        response.then().statusCode(200);
        response.then().body("orders", notNullValue());
        response.then().body("orders.size()", greaterThan(0));
    }

    @Test
    @Story("Retrieve orders for an invalid courier")
    @Severity(SeverityLevel.NORMAL)
    @Description("This test verifies that using an invalid courier ID results in a 404 error.")
    @Step("Get orders list for invalid courier ID: 99999")
    public void getOrdersListWithInvalidCourierIdTest() {
        String invalidCourierId = "99999";

        Response response = given()
                .contentType("application/json")
                .param("courierId", invalidCourierId)
                .when()
                .get("/api/v1/orders");

        response.then().statusCode(404);
        response.then().body("message", equalTo("Курьер с идентификатором " + invalidCourierId + " не найден"));
    }

    @Test
    @Story("Retrieve orders without specifying a courier")
    @Severity(SeverityLevel.NORMAL)
    @Description("This test verifies that not specifying a courier ID results in a 404 error.")
    @Step("Get orders list without courier ID")
    public void getOrdersListWithoutCourierIdTest() {
        Response response = given()
                .contentType("application/json")
                .when()
                .get("/api/v1/orders");

        response.then().statusCode(404);
        response.then().body("message", equalTo("Курьер не найден"));
    }

    private int createOrder(String[] colors) {
        String colorJsonArray = String.join(", ", colors);
        String orderRequestBody = "{\n" +
                "    \"firstName\": \"" + firstName + "\",\n" +
                "    \"lastName\": \"" + lastName + "\",\n" +
                "    \"address\": \"" + address + "\",\n" +
                "    \"metroStation\": \"" + metroStation + "\",\n" +
                "    \"phone\": \"" + phone + "\",\n" +
                "    \"rentTime\": " + rentTime + ",\n" +
                "    \"deliveryDate\": \"" + deliveryDate + "\",\n" +
                "    \"comment\": \"" + comment + "\",\n" +
                "    \"color\": [\"" + colorJsonArray + "\"]\n" +
                "}";

        Response response = given()
                .contentType("application/json")
                .body(orderRequestBody)
                .when()
                .post("/api/v1/orders");

        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to create order: " + response.asString());
        }

        return response.jsonPath().getInt("track");
    }

    private void assignOrderToCourier(int orderId, String courierId) {
        Response response = given()
                .contentType("application/json")
                .param("courierId", courierId)
                .when()
                .put("/api/v1/orders/accept/" + orderId);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to assign order to courier: " + response.asString());
        }
    }

    private void finishOrder(int orderId) {
        String requestBody = "{ \"id\": " + orderId + " }";

        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .put("/api/v1/orders/finish/" + orderId);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to finish order: " + response.asString());
        }

        System.out.println("Order with ID " + orderId + " has been finished.");
    }

    @After
    public void tearDown() {
        finishOrder(orderId);
        System.out.println("Order with ID " + orderId + " has been deleted.");

        CourierUtils.deleteCourier(courierId);
        System.out.println("Courier with ID " + courierId + " has been deleted.");
    }
}