package codegradients.com.chatapp.Models;

public class ScheduleMessageModel {

    private String message, receiver_name, receiver_id, timestamp, messageKey, messageType;
    private int receiverType;

    public ScheduleMessageModel(String message, String receiver_id, String receiver_name, String timestamp, String messageKey, String messageType, int receiverType) {
        this.message = message;
        this.receiver_id = receiver_id;
        this.receiver_name = receiver_name;
        this.receiver_id = receiver_id;
        this.timestamp = timestamp;
        this.messageKey = messageKey;
        this.messageType = messageType;
        this.receiverType = receiverType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver_name() {
        return receiver_name;
    }

    public void setReceiver_name(String receiver_name) {
        this.receiver_name = receiver_name;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public int getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(int receiverType) {
        this.receiverType = receiverType;
    }
}
