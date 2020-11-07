package codegradients.com.chatapp.Models;

public class CallRecordModel {

    private String callId, callerId, calleeId, placingTime, startingTime, endingTime;
    private boolean isVideo;

    public CallRecordModel(String callId, String callerId, String calleeId, String placingTime, String startingTime, String endingTime, boolean isVideo) {
        this.callId = callId;
        this.callerId = callerId;
        this.calleeId = calleeId;
        this.placingTime = placingTime;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.isVideo = isVideo;
    }

    public CallRecordModel() {
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public String getCalleeId() {
        return calleeId;
    }

    public void setCalleeId(String calleeId) {
        this.calleeId = calleeId;
    }

    public String getPlacingTime() {
        return placingTime;
    }

    public void setPlacingTime(String placingTime) {
        this.placingTime = placingTime;
    }

    public String getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(String startingTime) {
        this.startingTime = startingTime;
    }

    public String getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(String endingTime) {
        this.endingTime = endingTime;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }
}
