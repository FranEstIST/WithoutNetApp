package pt.ulisboa.tecnico.withoutnet.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.AutoMigrationSpec;

import pt.ulisboa.tecnico.withoutnet.models.Message;
import pt.ulisboa.tecnico.withoutnet.models.Node;

@Database(entities = {
        Message.class,
        },
        version = 1,
        exportSchema = false
)
public abstract class WithoutNetAppDatabase extends RoomDatabase {
    private static volatile WithoutNetAppDatabase INSTANCE;

    public abstract MessageDao messageDao();

    public static WithoutNetAppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (WithoutNetAppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    WithoutNetAppDatabase.class, "wn_app_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static class AutoMigration implements AutoMigrationSpec {};
}
