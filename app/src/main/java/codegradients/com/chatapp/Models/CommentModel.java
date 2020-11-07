package codegradients.com.chatapp.Models;

public class CommentModel {

    private String commentId, comment, time, commenterId, commenterName, commenterImage;

    public CommentModel(String commentId, String comment, String time, String commenterId, String commenterName, String commenterImage) {
        this.commentId = commentId;
        this.comment = comment;
        this.time = time;
        this.commenterId = commenterId;
        this.commenterName = commenterName;
        this.commenterImage = commenterImage;
    }

    public CommentModel() {
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCommenterId() {
        return commenterId;
    }

    public void setCommenterId(String commenterId) {
        this.commenterId = commenterId;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public void setCommenterName(String commenterName) {
        this.commenterName = commenterName;
    }

    public String getCommenterImage() {
        return commenterImage;
    }

    public void setCommenterImage(String commenterImage) {
        this.commenterImage = commenterImage;
    }
}
