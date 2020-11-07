package codegradients.com.chatapp.Models;

public class DataModel {

    private String link, type;
    private long uploaded_at;

    public DataModel(String link, String type, long uploaded_at) {
        this.link = link;
        this.type = type;
        this.uploaded_at = uploaded_at;
    }

    public DataModel() {
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getUploaded_at() {
        return uploaded_at;
    }

    public void setUploaded_at(long uploaded_at) {
        this.uploaded_at = uploaded_at;
    }
}
