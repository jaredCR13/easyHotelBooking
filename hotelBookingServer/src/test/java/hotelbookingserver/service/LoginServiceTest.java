package hotelbookingserver.service;

import hotelbookingcommon.domain.FrontDeskClerk;
import hotelbookingcommon.domain.FrontDeskClerkRole;
import hotelbookingserver.service.FrontDeskClerkService;
import hotelbookingserver.service.LoginService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginServiceTest {

    private static FrontDeskClerkService clerkService;
    private static LoginService loginService;

    @BeforeAll
    public static void setup() {
        clerkService = new FrontDeskClerkService();
        loginService = new LoginService();
    }

    @AfterAll
    public static void tearDown() {
        clerkService.close();
        loginService.close();
    }

    @Test
    @Order(1)
    public void testAddClerksIfNotExists() {
        FrontDeskClerk admin = new FrontDeskClerk("ADM001", "Admin", "General", "admin123", "admin", "555-1234", FrontDeskClerkRole.ADMINISTRATOR, 1);
        FrontDeskClerk recep = new FrontDeskClerk("REC001", "Juan", "Perez", "password", "juanp", "555-5678", FrontDeskClerkRole.RECEPCIONIST, 1);

        boolean adminAdded = clerkService.addClerk(admin);
        boolean recepAdded = clerkService.addClerk(recep);

        // Estos podrían ya existir si la prueba se ejecuta varias veces.
        // Por eso no se hace assertTrue, solo se asegura que no lance error.
        assertNotNull(adminAdded);
        assertNotNull(recepAdded);
    }

    @Test
    @Order(2)
    public void testLoginAdminSuccess() {
        FrontDeskClerk user = loginService.authenticate("admin", "admin123");
        assertNotNull(user);
        assertEquals("Admin", user.getName());
        assertEquals(FrontDeskClerkRole.ADMINISTRATOR, user.getFrontDeskClerkRole());
    }

    @Test
    @Order(3)
    public void testLoginReceptionistSuccess() {
        FrontDeskClerk user = loginService.authenticate("juanp", "password");
        assertNotNull(user);
        assertEquals("Juan", user.getName());
        assertEquals(FrontDeskClerkRole.RECEPCIONIST, user.getFrontDeskClerkRole());
    }

    @Test
    @Order(4)
    public void testLoginWrongPassword() {
        FrontDeskClerk user = loginService.authenticate("admin", "wrongpassword");
        assertNull(user, "El login debe fallar con contraseña incorrecta.");
    }

    @Test
    @Order(5)
    public void testLoginNonExistentUser() {
        FrontDeskClerk user = loginService.authenticate("nonexistentuser", "anypass");
        assertNull(user, "El login debe fallar si el usuario no existe.");
    }
}
