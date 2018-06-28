package ng.petersabs.dev.crumby;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    long createNewUser(User user);

    @Update
    void updateUserDetails(User user);

    @Delete
    void deleteAccount(User user);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT * FROM users WHERE email LIKE :userEmail")
    User doesEmailExists(String userEmail);

    @Query("SELECT * FROM users WHERE username LIKE :userName")
    User doesNameExists(String userName);

    @Query("SELECT * FROM users WHERE password LIKE :password AND (username LIKE :user OR email LIKE :user)")
    User getUserForLogin(String user, String password);

    @Query("SELECT * FROM users WHERE password LIKE :password AND (username LIKE :user OR email LIKE :user)")
    LiveData<User> getUserForSync(String user, String password);


}
