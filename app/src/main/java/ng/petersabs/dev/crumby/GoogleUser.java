package ng.petersabs.dev.crumby;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "google_users")
public class GoogleUser {
    @ColumnInfo(name = "username")
    @NonNull
    private String userName;
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "email")
    private String userEmail;
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    public GoogleUser(@NonNull String userName, @NonNull String userEmail, @NonNull String id) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.id = id;
    }


    @NonNull
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(@NonNull String userEmail) {
        this.userEmail = userEmail;
    }

    @NonNull
    public String getUserName() {
        return userName;
    }

    public void setUserName(@NonNull String userName) {
        this.userName = userName;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }
}
