import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Order Management")
@Feature("Order Creation")
@RunWith(Parameterized.class)
public class OrderCreationTests {

    private final String[] colors;
    private final String firstName = "John";
    private final String lastName = "Doe";
    private final String address = "123 Main St";
    private final String metroStation = "5";
    private final String phone = "+1234567890";
    private final int rentTime = 5;
    private final String deliveryDate = "2023-12-31";
    private final String comment = "Test order";

    public OrderCreationTests(String[] colors) {
        this.colors = colors;
    }

    @Parameterized.Parameters(name = "Test with colors: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new String[]{"BLACK"}},         // Один цвет
                {new String[]{"GREY"}},          // Другой цвет
                {new String[]{"BLACK", "GREY"}}, // Оба цвета
                {new String[]{}}                 // Без цвета
        });
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @Story("Create order with specified colors")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test verifies that orders can be created with various color combinations.")
    @Step("Create order with colors: {0}")
    public void createOrderTest() {
        Allure.addAttachment("Test Colors", Arrays.toString(colors));

        System.out.println("Running test with colors: " + Arrays.toString(colors));

        Response response = given()
                .contentType("application/json")
                .body(buildOrderRequest())
                .when()
                .post("/api/v1/orders");

        response.then().log().all();

        response.then().statusCode(201);
        response.then().body("track", notNullValue());
    }

    private String buildOrderRequest() {
        String colorJsonArray = Arrays.stream(colors)
                .map(color -> "\"" + color + "\"")
                .collect(Collectors.joining(", ", "[", "]"));

        return "{\n" +
                "    \"firstName\": \"" + firstName + "\",\n" +
                "    \"lastName\": \"" + lastName + "\",\n" +
                "    \"address\": \"" + address + "\",\n" +
                "    \"metroStation\": \"" + metroStation + "\",\n" +
                "    \"phone\": \"" + phone + "\",\n" +
                "    \"rentTime\": " + rentTime + ",\n" +
                "    \"deliveryDate\": \"" + deliveryDate + "\",\n" +
                "    \"comment\": \"" + comment + "\",\n" +
                "    \"color\": " + colorJsonArray + "\n" +
                "}";
    }
}
