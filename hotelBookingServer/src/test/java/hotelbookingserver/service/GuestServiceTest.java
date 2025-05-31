package hotelbookingserver.service;

import hotelbookingcommon.domain.Guest;
import hotelbookingserver.datamanager.GuestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para GuestService")
class GuestServiceTest {

    @Mock
    private GuestData guestData;

    @InjectMocks
    private GuestService guestService;

    @BeforeEach
    void setUp() {
        // MockitoExtension ya se encarga de inicializar los mocks
    }

    // --- PRUEBAS PARA addGuest ---
    @Test
    @DisplayName("Debe agregar un huésped exitosamente cuando no existe")
    void addGuest_ShouldAddGuestSuccessfully_WhenGuestDoesNotExist() throws IOException {
        // Instancia de Guest completa y correcta
        Guest newGuest = new Guest(
                1,              // id
                123456789,      // credential
                "Alice",        // name
                "Smith",        // lastName
                "123 Main St",  // address
                "alice@example.com", // email
                "555-1234",     // phoneNumber
                "USA"           // nativeCountry
        );

        when(guestData.findById(newGuest.getId())).thenReturn(null);

        boolean result = guestService.addGuest(newGuest);

        assertTrue(result, "El huésped debería haberse agregado exitosamente.");
        verify(guestData, times(1)).insert(newGuest);
        verify(guestData, times(1)).findById(newGuest.getId());
    }

    @Test
    @DisplayName("No debe agregar un huésped si ya existe un huésped con el mismo ID")
    void addGuest_ShouldNotAddGuest_WhenGuestAlreadyExists() throws IOException {
        // Instancia de Guest completa y correcta
        Guest existingGuest = new Guest(
                2,
                987654321,
                "Bob",
                "Johnson",
                "456 Oak Ave",
                "bob@example.com",
                "555-5678",
                "Canada"
        );

        when(guestData.findById(existingGuest.getId())).thenReturn(existingGuest);

        boolean result = guestService.addGuest(existingGuest);

        assertFalse(result, "El huésped no debería haberse agregado si ya existe.");
        verify(guestData, never()).insert(any(Guest.class));
        verify(guestData, times(1)).findById(existingGuest.getId());
    }

    @Test
    @DisplayName("Debe manejar IOException al agregar un huésped")
    void addGuest_ShouldHandleIOException_WhenInsertFails() throws IOException {
        // Instancia de Guest completa y correcta
        Guest newGuest = new Guest(
                3,
                112233445,
                "Charlie",
                "Brown",
                "789 Pine Ln",
                "charlie@example.com",
                "555-9012",
                "Germany"
        );

        when(guestData.findById(newGuest.getId())).thenReturn(null);
        doThrow(new IOException("Error de simulación de E/S al insertar")).when(guestData).insert(newGuest);

        boolean result = guestService.addGuest(newGuest);

        assertFalse(result, "La operación debería fallar si hay una IOException.");
        verify(guestData, times(1)).insert(newGuest);
    }

    // --- PRUEBAS PARA getAllGuests ---
    @Test
    @DisplayName("Debe retornar todos los huéspedes existentes")
    void getAllGuests_ShouldReturnAllGuests() throws IOException {
        List<Guest> expectedGuests = Arrays.asList(
                new Guest(1, 111, "Guest", "A", "Addr A", "a@mail.com", "111", "CountryA"),
                new Guest(2, 222, "Guest", "B", "Addr B", "b@mail.com", "222", "CountryB")
        );

        when(guestData.findAll()).thenReturn(expectedGuests);

        List<Guest> actualGuests = guestService.getAllGuests();

        assertNotNull(actualGuests, "La lista de huéspedes no debería ser nula.");
        assertEquals(expectedGuests.size(), actualGuests.size(), "Debería retornar el número correcto de huéspedes.");
        assertEquals(expectedGuests, actualGuests, "La lista de huéspedes retornada debería ser la misma que la esperada.");
        verify(guestData, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar una lista vacía si no hay huéspedes")
    void getAllGuests_ShouldReturnEmptyList_WhenNoGuestsExist() throws IOException {
        when(guestData.findAll()).thenReturn(Collections.emptyList());

        List<Guest> actualGuests = guestService.getAllGuests();

        assertNotNull(actualGuests, "La lista de huéspedes no debería ser nula.");
        assertTrue(actualGuests.isEmpty(), "La lista debería estar vacía.");
        verify(guestData, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException si hay un IOException al obtener todos los huéspedes")
    void getAllGuests_ShouldThrowRuntimeException_WhenIOExceptionOccurs() throws IOException {
        when(guestData.findAll()).thenThrow(new IOException("Error de simulación de E/S al leer todos los huéspedes"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> guestService.getAllGuests());

        assertTrue(thrown.getMessage().contains("Error al obtener huéspedes"));
        assertTrue(thrown.getCause() instanceof IOException);
        verify(guestData, times(1)).findAll();
    }

    // --- PRUEBAS PARA getGuestById ---
    @Test
    @DisplayName("Debe retornar un huésped por ID si existe")
    void getGuestById_ShouldReturnGuest_WhenGuestExists() throws IOException {
        // Instancia de Guest completa y correcta
        Guest expectedGuest = new Guest(
                10,
                334455667,
                "Diana",
                "Prince",
                "Themyscira",
                "diana@example.com",
                "555-3344",
                "Themyscira"
        );

        when(guestData.findById(10)).thenReturn(expectedGuest);

        Guest actualGuest = guestService.getGuestById(10);

        assertNotNull(actualGuest, "El huésped debería haber sido encontrado.");
        assertEquals(expectedGuest, actualGuest, "El huésped retornado debería ser el correcto.");
        verify(guestData, times(1)).findById(10);
    }

    @Test
    @DisplayName("Debe retornar null si el huésped no es encontrado por ID")
    void getGuestById_ShouldReturnNull_WhenGuestDoesNotExist() throws IOException {
        when(guestData.findById(99)).thenReturn(null);

        Guest actualGuest = guestService.getGuestById(99);

        assertNull(actualGuest, "El huésped no debería haber sido encontrado.");
        verify(guestData, times(1)).findById(99);
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException si hay un IOException al obtener huésped por ID")
    void getGuestById_ShouldThrowRuntimeException_WhenIOExceptionOccurs() throws IOException {
        when(guestData.findById(15)).thenThrow(new IOException("Error de simulación de E/S al leer huésped por ID"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> guestService.getGuestById(15));

        assertTrue(thrown.getMessage().contains("Error al obtener huésped por ID"));
        assertTrue(thrown.getCause() instanceof IOException);
        verify(guestData, times(1)).findById(15);
    }

    // --- PRUEBAS PARA updateGuest ---
    @Test
    @DisplayName("Debe actualizar un huésped exitosamente")
    void updateGuest_ShouldUpdateGuestSuccessfully() throws IOException {
        // Instancia de Guest completa y correcta
        Guest guestToUpdate = new Guest(
                20,
                887766554,
                "Eva",
                "Green",
                "901 Elm St",
                "eva.new@example.com", // Email modificado para la actualización
                "555-5566",
                "France"
        );

        when(guestData.update(guestToUpdate)).thenReturn(true);

        boolean result = guestService.updateGuest(guestToUpdate);

        assertTrue(result, "El huésped debería haberse actualizado exitosamente.");
        verify(guestData, times(1)).update(guestToUpdate);
    }

    @Test
    @DisplayName("No debe actualizar un huésped si no existe")
    void updateGuest_ShouldNotUpdateGuest_WhenGuestDoesNotExist() throws IOException {
        // Instancia de Guest completa y correcta
        Guest guestToUpdate = new Guest(
                25,
                123123123,
                "Frank",
                "White",
                "321 Maple Rd",
                "frank@example.com",
                "555-7788",
                "USA"
        );

        when(guestData.update(guestToUpdate)).thenReturn(false);

        boolean result = guestService.updateGuest(guestToUpdate);

        assertFalse(result, "El huésped no debería haberse actualizado si no existe.");
        verify(guestData, times(1)).update(guestToUpdate);
    }

    @Test
    @DisplayName("Debe manejar IOException al actualizar un huésped")
    void updateGuest_ShouldHandleIOException_WhenUpdateFails() throws IOException {
        // Instancia de Guest completa y correcta
        Guest guestToUpdate = new Guest(
                30,
                445566778,
                "Grace",
                "Kelly",
                "100 Hollywood Blvd",
                "grace@example.com",
                "555-9900",
                "Monaco"
        );

        doThrow(new IOException("Error de simulación de E/S al actualizar")).when(guestData).update(guestToUpdate);

        boolean result = guestService.updateGuest(guestToUpdate);

        assertFalse(result, "La operación debería fallar si hay una IOException.");
        verify(guestData, times(1)).update(guestToUpdate);
    }

    // --- PRUEBAS PARA deleteGuest ---
    @Test
    @DisplayName("Debe eliminar un huésped exitosamente")
    void deleteGuest_ShouldDeleteGuestSuccessfully() throws IOException {
        int guestIdToDelete = 40;

        when(guestData.delete(guestIdToDelete)).thenReturn(true);

        boolean result = guestService.deleteGuest(guestIdToDelete);

        assertTrue(result, "El huésped debería haberse eliminado exitosamente.");
        verify(guestData, times(1)).delete(guestIdToDelete);
    }

    @Test
    @DisplayName("No debe eliminar un huésped si no existe")
    void deleteGuest_ShouldNotDeleteGuest_WhenGuestDoesNotExist() throws IOException {
        int guestIdToDelete = 45;

        when(guestData.delete(guestIdToDelete)).thenReturn(false);

        boolean result = guestService.deleteGuest(guestIdToDelete);

        assertFalse(result, "El huésped no debería haberse eliminado si no existe.");
        verify(guestData, times(1)).delete(guestIdToDelete);
    }

    @Test
    @DisplayName("Debe manejar IOException al eliminar un huésped")
    void deleteGuest_ShouldHandleIOException_WhenDeleteFails() throws IOException {
        int guestIdToDelete = 50;

        doThrow(new IOException("Error de simulación de E/S al eliminar")).when(guestData).delete(guestIdToDelete);

        boolean result = guestService.deleteGuest(guestIdToDelete);

        assertFalse(result, "La operación debería fallar si hay una IOException.");
        verify(guestData, times(1)).delete(guestIdToDelete);
    }

    // --- PRUEBAS PARA close ---
    @Test
    @DisplayName("Debe cerrar GuestData sin errores")
    void close_ShouldCloseGuestDataSuccessfully() throws IOException {
        guestService.close();
        verify(guestData, times(1)).close();
    }

    @Test
    @DisplayName("Debe manejar IOException al cerrar GuestData")
    void close_ShouldHandleIOException_WhenCloseFails() throws IOException {
        doThrow(new IOException("Error de simulación de E/S al cerrar")).when(guestData).close();

        guestService.close();
        verify(guestData, times(1)).close();
    }
}