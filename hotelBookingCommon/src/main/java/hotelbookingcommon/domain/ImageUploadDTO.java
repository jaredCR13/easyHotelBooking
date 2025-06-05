package hotelbookingcommon.domain;


import java.io.Serializable;

public class ImageUploadDTO implements Serializable {
    private int roomNumber;
    private String fileName;
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
