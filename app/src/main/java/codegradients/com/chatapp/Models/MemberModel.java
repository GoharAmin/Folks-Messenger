package codegradients.com.chatapp.Models;

public class MemberModel {

    private  String name, id, status, image;

    public MemberModel(String name, String id, String status, String image) {
        this.name = name;
        this.id = id;
        this.status = status;
        this.image = image;
    }

    public MemberModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
