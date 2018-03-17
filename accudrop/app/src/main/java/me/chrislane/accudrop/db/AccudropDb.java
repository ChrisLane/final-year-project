package me.chrislane.accudrop.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import me.chrislane.accudrop.db.converter.DateConverter;
import me.chrislane.accudrop.db.converter.FallTypeConverter;
import me.chrislane.accudrop.db.converter.UuidConverter;

@Database(entities = {Jump.class, Position.class, User.class}, version = 7)
@TypeConverters({DateConverter.class, UuidConverter.class, FallTypeConverter.class})
public abstract class AccudropDb extends RoomDatabase {

    private static AccudropDb INSTANCE;
    private static String DB_NAME = "accudrop";

    /**
     * Get an instance of the database.
     *
     * @param context A context in the application.
     * @return An instance of an <code>AccudropDb</code> database.
     */
    public static AccudropDb getDatabase(Context context) {

        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AccudropDb.class, DB_NAME)
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

    public static void clearDatabase(Context context) {
        context.deleteDatabase(DB_NAME);
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