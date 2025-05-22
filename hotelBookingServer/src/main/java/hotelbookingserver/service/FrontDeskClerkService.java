package hotelbookingserver.service;

import hotelbookingcommon.domain.FrontDeskClerk;
import hotelbookingserver.filemanager.BinaryFileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FrontDeskClerkService {
    private static final Logger logger = LogManager.getLogger(FrontDeskClerkService.class);
    private static final String FRONTDESKCLERK_FILE = "C:\\Users\\Lexis\\Documents\\ProyectoProgra2\\easyHotelBooking\\hotelBookingServer\\frontdeskdata.dat";

    public List<FrontDeskClerk> addFrontDesk(FrontDeskClerk frontDeskClerk) {
        List<FrontDeskClerk> frontDeskClerkList = loadFrontDeskClerk();
        frontDeskClerkList.add(frontDeskClerk);
        saveFrontDeskClerk(frontDeskClerkList);
        return frontDeskClerkList;
    }

    public List<FrontDeskClerk> getAllFrontDesks() {
        logger.info("Cargando recepcionistas");
        List<? extends Serializable> rawList = BinaryFileManager.readData(FRONTDESKCLERK_FILE);
        return new ArrayList<>((List<FrontDeskClerk>) rawList);
    }

    public void saveFrontDeskClerk(List<FrontDeskClerk> frontDeskClerkList) {
        logger.info("Guardando recepcionistas en archivo: {}", FRONTDESKCLERK_FILE);
        BinaryFileManager.writeData(FRONTDESKCLERK_FILE, frontDeskClerkList);
    }

    public FrontDeskClerk findByEmployeeId(String employeeId) {
        List<FrontDeskClerk> frontDeskClerkList = loadFrontDeskClerk();
        for (FrontDeskClerk frontDeskClerk : frontDeskClerkList) {
            if (frontDeskClerk.getEmployeeId() == employeeId) {
                return frontDeskClerk;
            }
        }
        return null;
    }

    private List<FrontDeskClerk> loadFrontDeskClerk() {
        List<? extends Serializable> rawList = BinaryFileManager.readData(FRONTDESKCLERK_FILE);
        return new ArrayList<>((List<FrontDeskClerk>) rawList);
    }
    
}
