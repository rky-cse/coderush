package me.rkycse.coderush.entity;

public class UpdatedRating {
    private String userName;
    private long newRating;

    public UpdatedRating(String userName, long newRating) {
        this.userName = userName;
        this.newRating = newRating;
    }

    public String getUserName() {
        return userName;
    }

    public long getNewRating() {
        return newRating;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setNewRating(long newRating) {
        this.newRating = newRating;
    }
}
