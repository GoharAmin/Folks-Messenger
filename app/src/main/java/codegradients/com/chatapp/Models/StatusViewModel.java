package codegradients.com.chatapp.Models;

import java.io.Serializable;

public class StatusViewModel implements Serializable {
    private String mUSerId;
    private String mViewTime;

    public StatusViewModel(String USerId, String viewTime) {
        mUSerId = USerId;
        mViewTime = viewTime;
    }

    public String getUSerId() {
        return mUSerId;
    }

    public void setUSerId(String USerId) {
        mUSerId = USerId;
    }

    public String getViewTime() {
        return mViewTime;
    }

    public void setViewTime(String viewTime) {
        mViewTime = viewTime;
    }
}
