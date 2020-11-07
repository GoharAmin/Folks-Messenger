package codegradients.com.chatapp.Models;

public class ForwardModel {

    private String messageType, messageKey, message, senderId;

    public ForwardModel(String messageType, String messageKey, String message, String senderId) {
        this.messageType = messageType;
        this.message = message;
        this.senderId = senderId;
        this.messageKey = messageKey;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
