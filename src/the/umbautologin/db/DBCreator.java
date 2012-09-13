
package the.umbautologin.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class DBCreator extends SQLiteOpenHelper
{
    private static final String TAG = "SbAutoLogin";
    private static final String DATABASE_NAME    = "sbautologin.db";
    private static final int    DATABASE_VERSION = 1;
    private Context context;

    private static final String CREATE_TABLE     = "create table history "
            + " (_id integer primary key autoincrement, "
            + "date integer, " + "success integer, " + "message text);";

    public DBCreator(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        sqLiteDatabase.execSQL("drop table if exists history");
        onCreate(sqLiteDatabase);
    }

    public synchronized SQLiteDatabase getWritableDatabase()
    {
        SQLiteDatabase db;
        try
        {
            db = super.getWritableDatabase();
        } catch (SQLiteException e)
        {
            Log.e(TAG, e.getMessage());
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }
        return db;
    }

    public synchronized SQLiteDatabase getReadableDatabase()
    {
        SQLiteDatabase db;
        try
        {
            db = super.getReadableDatabase();
        } catch (SQLiteException e)
        {
            Log.e(TAG, e.getMessage());
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }
        return db;
    }

}
