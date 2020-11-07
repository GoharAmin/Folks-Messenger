package codegradients.com.chatapp.Models;

import java.io.Serializable;

public class ChatModel implements Serializable {

    private String message, sender_name, sender_id, receiver_name, receiver_id, timestamp, messageKey, messageType, messageStatus, deliveredTime, readTime, autoDestructionTime;
    private boolean deletedByReceiver, deletedBySender, autoDestructionStatus;
    private String replySenderMessageName = "", replyMessageText = "", replyMessageImageLink = "", replyMessageSelectedTime = "";

    public ChatModel(String message, String sender_name, String sender_id, String receiver_name, String receiver_id, String timestamp, String messageKey, String messageType, boolean deletedByReceiver, boolean deletedBySender, String messageStatus, String deliveredTime, String readTime, boolean autoDestructionStatus, String autoDestructionTime) {
        this.autoDestructionStatus = autoDestructionStatus;
        this.autoDestructionTime = autoDestructionTime;
        this.message = message;
        this.sender_name = sender_name;
        this.sender_id = sender_id;
        this.receiver_name = receiver_name;
        this.receiver_id = receiver_id;
        this.readTime = readTime;
        this.deliveredTime = deliveredTime;
        this.timestamp = timestamp;
        this.messageKey = messageKey;
        this.messageType = messageType;
        this.deletedByReceiver = deletedByReceiver;
        this.deletedBySender = deletedBySender;
        this.messageStatus = messageStatus;
    }

    public ChatModel() {
    }

    public String getReplySenderMessageName() {
        return replySenderMessageName;
    }

    public void setReplySenderMessageName(String replySenderMessageName) {
        this.replySenderMessageName = replySenderMessageName;
    }

    public String getReplyMessageText() {
        return replyMessageText;
    }

    public void setReplyMessageText(String replyMessageText) {
        this.replyMessageText = replyMessageText;
    }

    public String getReplyMessageImageLink() {
        return replyMessageImageLink;
    }

    public void setReplyMessageImageLink(String replyMessageImageLink) {
        this.replyMessageImageLink = replyMessageImageLink;
    }

    public String getReplyMessageSelectedTime() {
        return replyMessageSelectedTime;
    }

    public void setReplyMessageSelectedTime(String replyMessageSelectedTime) {
        this.replyMessageSelectedTime = replyMessageSelectedTime;
    }

    public String getReadTime() {
        return readTime;
    }

    public void setReadTime(String readTime) {
        this.readTime = readTime;
    }

    public String getAutoDestructionTime() {
        return autoDestructionTime;
    }

    public boolean getAutoDestructionStatus() {
        return autoDestructionStatus;
    }

    public void setAutoDestructionStatus(boolean autoDestructionStatus) {
        this.autoDestructionStatus = autoDestructionStatus;
    }

    public String getDeliveredTime() {
        return deliveredTime;
    }

    public void setDeliveredTime(String deliveredTime) {
        this.deliveredTime = deliveredTime;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
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

    public boolean isDeletedByReceiver() {
        return deletedByReceiver;
    }

    public void setDeletedByReceiver(boolean deletedByReceiver) {
        this.deletedByReceiver = deletedByReceiver;
    }

    public boolean isDeletedBySender() {
        return deletedBySender;
    }

    public void setDeletedBySender(boolean deletedBySender) {
        this.deletedBySender = deletedBySender;
    }
}
