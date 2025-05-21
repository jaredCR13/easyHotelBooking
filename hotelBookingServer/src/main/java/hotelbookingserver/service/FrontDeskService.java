package hotelbookingserver.service;

import hotelbookingcommon.domain.FrontDesk;
import hotelbookingserver.filemanager.BinaryFileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FrontDeskService {
    private static final Logger logger = LogManager.getLogger(FrontDeskService.class);
    private static final String FRONTDESK_FILE = "C:\\Users\\XT\\Documents\\ProyectoProgra2\\easyHotelBooking\\hotelBookingServer\\frontdeskdata.dat";

    public List<FrontDesk> addFrontDesk(FrontDesk frontDesk) {
        List<FrontDesk> frontDeskList = loadFrontDesk();
        frontDeskList.add(frontDesk);
        saveFrontDesk(frontDeskList);
        return frontDeskList;
    }

    public List<FrontDesk> getAllFrontDesks() {
        logger.info("Cargando recepcionistas");
        List<? extends Serializable> rawList = BinaryFileManager.readData(FRONTDESK_FILE);
        return new ArrayList<>((List<FrontDesk>) rawList);
    }

    public void saveFrontDesk(List<FrontDesk> frontDeskList) {
        logger.info("Guardando recepcionistas en archivo: {}", FRONTDESK_FILE);
        BinaryFileManager.writeData(FRONTDESK_FILE, frontDeskList);
    }

    public FrontDesk findByEmployeeId(String employeeId) {
        List<FrontDesk> frontDeskList = loadFrontDesk();
        for (FrontDesk frontDesk : frontDeskList) {
            if (frontDesk.getEmployeeId() == employeeId) {
                return frontDesk;
            }
        }
        return null;
    }

    private List<FrontDesk> loadFrontDesk() {
        List<? extends Serializable> rawList = BinaryFileManager.readData(FRONTDESK_FILE);
        return new ArrayList<>((List<FrontDesk>) rawList);
    }
    
}
