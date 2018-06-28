package ng.petersabs.dev.crumby;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(tableName = "users",primaryKeys = {"username", "email"})
public class User {
    @ColumnInfo(name = "username")
    @NonNull
    private String userName;
    @NonNull
    @ColumnInfo(name = "email")
    private String userEmail;
    @NonNull
    @ColumnInfo(name = "password")
    private String password;

    public User(){

    }

    public User(String userName, @NonNull String userEmail, @NonNull String password) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @NonNull
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(@NonNull String userEmail) {
        this.userEmail = userEmail;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }
}
