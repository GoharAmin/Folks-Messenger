package codegradients.com.chatapp.Models;

import java.util.ArrayList;

public class CompleteStatusInfo {
    private String name;
    private ArrayList<StatusInfoModel> mStatusInfoModels = new ArrayList<>();

    public CompleteStatusInfo(String name, ArrayList<StatusInfoModel> statusInfoModels) {
        this.name = name;
        mStatusInfoModels = statusInfoModels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<StatusInfoModel> getStatusInfoModels() {
        return mStatusInfoModels;
    }

    public void setStatusInfoModels(ArrayList<StatusInfoModel> statusInfoModels) {
        mStatusInfoModels = statusInfoModels;
    }
}
