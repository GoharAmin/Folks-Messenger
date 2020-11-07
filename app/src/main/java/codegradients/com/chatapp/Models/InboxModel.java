package codegradients.com.chatapp.Models;

public class InboxModel {

    public static final int PERSONEL_TYPE=0;
    public static final int GROUP_TYPE=1;

    public int type;
    //public  data;
    public String img = "", name = "", otherId = "", lastMessage = "", lastMessageTimeStamp = "", lastMessageSender = "", groupId = "", phone = "";

    public InboxModel(int type, String img, String name, String otherId, String lastMessage, String lastMessageTimeStamp, String lastMessageSender, String groupId, String phone) {
        this.type = type;
        this.img = img;
        this.name = name;
        this.otherId = otherId;
        this.lastMessage = lastMessage;
        this.lastMessageTimeStamp = lastMessageTimeStamp;
        this.lastMessageSender = lastMessageSender;
        this.groupId = groupId;
        this.phone = phone;
    }

    public InboxModel() {
    }
}
