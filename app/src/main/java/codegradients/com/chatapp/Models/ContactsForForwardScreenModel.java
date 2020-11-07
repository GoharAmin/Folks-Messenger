package codegradients.com.chatapp.Models;

public class ContactsForForwardScreenModel {

    private String selectedId, selectedName, selectedImage;
    private boolean isGroup;

    public ContactsForForwardScreenModel(String selectedId, String selectedName, String selectedImage, boolean isGroup) {
        this.selectedId = selectedId;
        this.selectedName = selectedName;
        this.selectedImage = selectedImage;
        this.isGroup = isGroup;
    }

    public String getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(String selectedId) {
        this.selectedId = selectedId;
    }

    public String getSelectedName() {
        return selectedName;
    }

    public void setSelectedName(String selectedName) {
        this.selectedName = selectedName;
    }

    public String getSelectedImage() {
        return selectedImage;
    }

    public void setSelectedImage(String selectedImage) {
        this.selectedImage = selectedImage;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }
}
