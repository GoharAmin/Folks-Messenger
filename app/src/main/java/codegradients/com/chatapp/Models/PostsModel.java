package codegradients.com.chatapp.Models;

import java.util.List;

public class PostsModel {

    private String postId, description, posterId, posterName, posterImage;
    private long created_at;
    List<DataModel> data;
    List<String> likedBy;
    int commentsCount = 0;

    public PostsModel(String postId, String description, String posterId, String posterName, String posterImage, long created_at, List<DataModel> data, List<String> likedBy, int commentsCount) {
        this.commentsCount = commentsCount;
        this.postId = postId;
        this.description = description;
        this.created_at = created_at;
        this.data = data;
        this.likedBy = likedBy;
        this.posterId = posterId;
        this.posterImage = posterImage;
        this.posterName = posterName;
    }

    public PostsModel() {
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public String getPosterImage() {
        return posterImage;
    }

    public void setPosterImage(String posterImage) {
        this.posterImage = posterImage;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String id) {
        this.postId = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public List<DataModel> getData() {
        return data;
    }

    public void setData(List<DataModel> data) {
        this.data = data;
    }
}
