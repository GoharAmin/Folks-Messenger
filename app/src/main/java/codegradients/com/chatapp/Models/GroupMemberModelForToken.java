package codegradients.com.chatapp.Models;

public class GroupMemberModelForToken {

    private String personId, personName, personToken;

    public GroupMemberModelForToken(String personId, String personName, String personToken) {
        this.personId = personId;
        this.personName = personName;
        this.personToken = personToken;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonToken() {
        return personToken;
    }

    public void setPersonToken(String personToken) {
        this.personToken = personToken;
    }
}
