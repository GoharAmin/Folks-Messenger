package codegradients.com.chatapp.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class StatusInfoModel implements Serializable {
    private String mUserId;
    private String mDuration, mStatusType, mStatusUploadDate, mUrl, mUSerName;
    private  String mStatusId;
    private ArrayList<StatusViewModel> mViewModelArrayList = new ArrayList<>();

    public StatusInfoModel(String userId, String duration, String statusType, String statusUploadDate, String url,String statusId) {
        mUserId = userId;
        mDuration = duration;
        mStatusType = statusType;
        mStatusUploadDate = statusUploadDate;
        mUrl = url;
        mStatusId = statusId;

    }

    public ArrayList<StatusViewModel> getViewModelArrayList() {
        return mViewModelArrayList;
    }

    public void setViewModelArrayList(ArrayList<StatusViewModel> viewModelArrayList) {
        mViewModelArrayList = viewModelArrayList;
    }

    public String getStatusId() {
        return mStatusId;
    }

    public void setStatusId(String statusId) {
        mStatusId = statusId;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        mDuration = duration;
    }

    public String getStatusType() {
        return mStatusType;
    }

    public void setStatusType(String statusType) {
        mStatusType = statusType;
    }

    public String getStatusUploadDate() {
        return mStatusUploadDate;
    }

    public void setStatusUploadDate(String statusUploadDate) {
        mStatusUploadDate = statusUploadDate;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUSerName() {
        return mUSerName;
    }

    public void setUSerName(String USerName) {
        mUSerName = USerName;
    }
}
