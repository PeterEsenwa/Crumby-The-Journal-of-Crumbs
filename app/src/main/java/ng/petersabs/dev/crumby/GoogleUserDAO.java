package ng.petersabs.dev.crumby;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface GoogleUserDAO {
    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    long newGoogleUser(GoogleUser googleUser);

/*    @Delete
    void deleteAccount(GoogleUser googleUser);*/

    @Update
    void updateUserDetails(GoogleUser googleUser);

    @Query("SELECT * FROM google_users WHERE email LIKE :userEmail")
    GoogleUser doesEmailExists(String userEmail);
}
