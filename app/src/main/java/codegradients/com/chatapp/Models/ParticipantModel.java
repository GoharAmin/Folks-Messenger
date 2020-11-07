package codegradients.com.chatapp.Models;

public class ParticipantModel {

    private String userId, userName, userNumber,userImage, userAbout, userToken;
    private boolean isChecked;

    public ParticipantModel(String userId, String userName, String userNumber, String userImage, String userAbout, String userToken, boolean isChecked) {
        this.userId = userId;
        this.userName = userName;
        this.userNumber = userNumber;
        this.userImage = userImage;
        this.userAbout = userAbout;
        this.userToken = userToken;
        this.isChecked = isChecked;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserAbout() {
        return userAbout;
    }

    public void setUserAbout(String userAbout) {
        this.userAbout = userAbout;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
