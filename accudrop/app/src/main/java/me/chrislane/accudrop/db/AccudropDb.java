package me.chrislane.accudrop.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {Jump.class, Position.class}, version = 3)
@TypeConverters(DateConverter.class)
public abstract class AccudropDb extends RoomDatabase {

    private static AccudropDb INSTANCE;

    /**
     * Get an instance of the database.
     *
     * @param context A context in the application.
     * @return An instance of an <code>AccudropDb</code> database.
     */
    public static AccudropDb getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AccudropDb.class, "accudrop")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    /**
     * Remove the database instance.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Get a data access object for the <code>jump</code> table.
     *
     * @return A data access object for the <code>jump</code> table.
     */
    public abstract JumpDao jumpModel();

    /**
     * Get a data access object for the <code>position</code> table.
     *
     * @return A data access object for the <code>position</code> table.
     */
    public abstract PositionDao locationModel();
}