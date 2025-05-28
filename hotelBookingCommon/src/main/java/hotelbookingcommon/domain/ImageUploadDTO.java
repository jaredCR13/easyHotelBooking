package hotelbookingcommon.domain;


import java.io.Serializable; // Es buena práctica para DTOs que se envían por red

public class ImageUploadDTO implements Serializable {
    private int roomNumber;   // El número de la habitación a la que se asocia la imagen
    private String fileName;  // El nombre original del archivo
    private byte[] imageData;


    public ImageUploadDTO(int roomNumber, String fileName, byte[] imageData) {
        this.roomNumber = roomNumber;
        this.fileName = fileName;
        this.imageData = imageData;
    }


    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
