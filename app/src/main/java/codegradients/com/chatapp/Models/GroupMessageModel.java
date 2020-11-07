package codegradients.com.chatapp.Models;

import java.io.Serializable;

public class GroupMessageModel implements Serializable {

    private  String message, groupId, from, key, senderName, timestamp, messageType;
    private String replySenderMessageName = "", replyMessageText = "", replyMessageImageLink = "", replyMessageSelectedTime = "";

    public GroupMessageModel(String message, String groupId, String from, String key, String senderName, String timestamp, String messageType) {
        this.message = message;
        this.groupId = groupId;
        this.from = from;
        this.key = key;
        this.senderName = senderName;
        this.timestamp = timestamp;
        this.messageType = messageType;
    }

    public GroupMessageModel() {
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
