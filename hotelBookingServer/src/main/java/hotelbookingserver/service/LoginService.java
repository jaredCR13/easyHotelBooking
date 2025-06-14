package hotelbookingserver.service; // O el paquete donde tengas tus servicios

import hotelbookingcommon.domain.FrontDeskClerk;
import hotelbookingcommon.domain.FrontDeskClerkRole;
import hotelbookingcommon.domain.Hotel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginService {

    private static final Logger logger = LogManager.getLogger(LoginService.class);
    private FrontDeskClerkService clerkService;

    public LoginService() {
        // Se inicializa el FrontDeskClerkService, que a su vez maneja los archivos binarios.
        this.clerkService = new FrontDeskClerkService();
    }

    public FrontDeskClerk authenticate(String username, String password) {
        // 1. Buscar el FrontDeskClerk por su nombre de usuario
        FrontDeskClerk clerk = clerkService.getClerkByUsername(username);

        if (clerk == null) {
            logger.warn("Intento de login fallido: Usuario '{}' no encontrado.", username);
            return null; // Usuario no encontrado
        }

        // 2. Verificar la contraseña usando el método checkPassword de FrontDeskClerk.
        //    Este método compara la contraseña en texto plano con el hash almacenado.
        if (clerk.checkPassword(password)) {
            logger.info("Login exitoso para el usuario: '{}' con rol: {}", username, clerk.getFrontDeskClerkRole());
            return clerk; // Autenticación exitosa
        } else {
            logger.warn("Intento de login fallido: Contraseña incorrecta para el usuario '{}'.", username);
            return null; // Contraseña incorrecta
        }
    }

    // Método para cerrar los recursos si es necesario
    public void close() {
        clerkService.close();
    }

    public static void main(String[] args) {
        HotelService hotelService = new HotelService();
        Hotel hotel = new Hotel(1, "Hotel Central", "Ciudad X");
        hotelService.addHotel(hotel);
        hotelService.close();
        System.out.println("--- Iniciando pruebas de LoginService ---");

        // ==============================================================================
        // AGREGAR RECEPCIONISTAS DE PRUEBA

        FrontDeskClerkService tempClerkService = new FrontDeskClerkService();
        FrontDeskClerk adminToAdd = new FrontDeskClerk("ADM001", "Admin", "General", "admin123", "admin", "555-1234", FrontDeskClerkRole.ADMINISTRATOR, 1);
        FrontDeskClerk recepToAdd = new FrontDeskClerk("REC001", "Juan", "Perez", "password", "juanp", "555-5678", FrontDeskClerkRole.RECEPCIONIST, 1);

        System.out.println("\nIntentando agregar usuarios de prueba...");
        boolean adminAdded = tempClerkService.addClerk(adminToAdd);
        if (adminAdded) {
            System.out.println("Usuario 'admin' agregado exitosamente.");
        } else {
            System.out.println("Usuario 'admin' NO agregado (quizás ya existe o hubo un error).");
        }

        boolean recepAdded = tempClerkService.addClerk(recepToAdd);
        if (recepAdded) {
            System.out.println("Usuario 'juanp' agregado exitosamente.");
        } else {
            System.out.println("Usuario 'juanp' NO agregado (quizás ya existe o hubo un error).");
        }
        tempClerkService.close();

        // ==============================================================================

        System.out.println("\n--- Realizando pruebas de Login ---");
        LoginService loginService = new LoginService();

        // Prueba 1: Login exitoso de un Admin
        System.out.println("\nIntentando login como 'admin' con 'admin123'...");
        FrontDeskClerk adminUser = loginService.authenticate("admin", "admin123");
        if (adminUser != null) {
            System.out.println("¡Login de Admin exitoso! Bienvenido, " + adminUser.getName() + " (" + adminUser.getFrontDeskClerkRole() + ")");
        } else {
            System.out.println("Login de Admin fallido.");
        }

        // Prueba 2: Login exitoso de un Recepcionista
        System.out.println("\nIntentando login como 'juanp' con 'password'...");
        FrontDeskClerk receptionUser = loginService.authenticate("juanp", "password");
        if (receptionUser != null) {
            System.out.println("¡Login de Recepcionista exitoso! Bienvenido, " + receptionUser.getName() + " (" + receptionUser.getFrontDeskClerkRole() + ")");
        } else {
            System.out.println("Login de Recepcionista fallido.");
        }

        // Prueba 3: Login fallido (contraseña incorrecta)
        System.out.println("\nIntentando login como 'admin' con 'wrongpassword'...");
        FrontDeskClerk failedPassword = loginService.authenticate("admin", "wrongpassword");
        if (failedPassword == null) {
            System.out.println("Login fallido como 'admin' (contraseña incorrecta) - ¡Correcto!");
        }

        // Prueba 4: Login fallido (usuario no encontrado)
        System.out.println("\nIntentando login como 'nonexistentuser' con 'anypass'...");
        FrontDeskClerk failedUser = loginService.authenticate("nonexistentuser", "anypass");
        if (failedUser == null) {
            System.out.println("Login fallido como 'nonexistentuser' (usuario no encontrado) - ¡Correcto!");
        }

        loginService.close();
        System.out.println("\n--- Fin de las pruebas ---");
    }
}