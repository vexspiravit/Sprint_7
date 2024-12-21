import io.qameta.allure.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

@Epic("Courier Management")
@Feature("Courier Login")
public class CourierLoginTests {

    private String courierLogin = "KatnissEverdeen";
    private String courierPassword = "11111";
    private String courierFirstName = "Katniss";
    private String courierId;

    @Before
    public void setUp() {
        try {
            courierId = CourierUtils.loginCourier(courierLogin, courierPassword);
            CourierUtils.deleteCourier(courierId);
        } catch (RuntimeException e) {
            System.out.println("Courier not found or already deleted: " + e.getMessage());
        }

        CourierUtils.createCourier(courierLogin, courierPassword, courierFirstName);
    }

    @Test
    @Story("Courier logs in successfully with correct credentials")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test verifies that a courier can log in with valid credentials.")
    public void courierLoginTest() {
        courierId = CourierUtils.loginCourier(courierLogin, courierPassword);
        assertThat(courierId, notNullValue());
    }

    @Test
    @Story("Fail to log in without a login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test ensures that a courier cannot log in without providing a login.")
    public void loginWithoutLoginTest() {
        String response = CourierUtils.loginCourier(null, courierPassword);
        assertThat(response, containsString("Недостаточно данных для входа"));
    }

    @Test
    @Story("Fail to log in without a password")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test ensures that a courier cannot log in without providing a password.")
    public void loginWithoutPasswordTest() {
        String response = CourierUtils.loginCourier(courierLogin, null);
        assertThat(response, containsString("Недостаточно данных для входа"));
    }

    @Test
    @Story("Fail to log in with an incorrect login")
    @Severity(SeverityLevel.NORMAL)
    @Description("This test verifies that attempting to log in with an incorrect login fails.")
    public void loginWithIncorrectLoginTest() {
        try {
            CourierUtils.loginCourier("IncorrectLogin", courierPassword);
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("Учетная запись не найдена"));
        }
    }

    @Test
    @Story("Fail to log in with an incorrect password")
    @Severity(SeverityLevel.NORMAL)
    @Description("This test verifies that attempting to log in with an incorrect password fails.")
    public void loginWithIncorrectPasswordTest() {
        try {
            CourierUtils.loginCourier(courierLogin, "IncorrectPassword");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("Учетная запись не найдена"));
        }
    }

    @After
    public void tearDown() {
        if (courierId != null) {
            try {
                CourierUtils.deleteCourier(courierId);
            } catch (RuntimeException e) {
                System.out.println("Failed to delete courier: " + e.getMessage());
            }
        }
    }
}
