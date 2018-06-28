package ng.petersabs.dev.crumby;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
@Database(entities = {GoogleUser.class}, version = 1)
public abstract class GoogleUserDB extends RoomDatabase {
    abstract GoogleUserDAO userDAO();

    private static GoogleUserDB INSTANCE;

    static GoogleUserDB getINSTANCE(final Context context){
        if (INSTANCE == null) {
            synchronized (GoogleUserDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            GoogleUserDB.class, "google_users_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
