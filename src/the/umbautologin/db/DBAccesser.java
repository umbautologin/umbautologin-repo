
package the.umbautologin.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import the.umbautologin.model.HistoryItem;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class DBAccesser
{
    private SQLiteDatabase db;
    private DBCreator      dbCreator;

    public DBAccesser(Context context)
    {
        dbCreator = new DBCreator(context);
    }

    public void addHistoryItem(HistoryItem historyItem)
    {
        try
        {
            db = dbCreator.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("date", historyItem.getDate().getTime());
            contentValues.put("success", historyItem.isSuccess() ? 1 : 0);
            contentValues.put("message", historyItem.getMessage());
            db.insert("history", null, contentValues);
        } finally
        {
            if(db != null)
                db.close();
        }
    }

    public void addHistoryItems(ArrayList<HistoryItem> historyItems)
    {
        for(HistoryItem historyItem : historyItems)
        {
            addHistoryItem(historyItem);
        }
    }

    @SuppressWarnings("unused")
    private HistoryItem getHistoryItem(int id)
    {
        try
        {
            db = dbCreator.getReadableDatabase();
            Cursor cursor = db.rawQuery("select * from history where _id = " + id, null);
            try
            {
                if(cursor.getCount() > 0)
                {
                    cursor.moveToFirst();
                    return readHistoryItem(cursor);
                } else
                    return null;
            } finally
            {
                cursor.close();
            }
        } finally
        {
            if(db != null)
                db.close();
        }
    }

    private HistoryItem readHistoryItem(Cursor cursor)
    {
        HistoryItem historyItem = new HistoryItem();
        historyItem.setId(cursor.getInt(0));
        historyItem.setDate(new Date(cursor.getLong(1)));
        historyItem.setSuccess(cursor.getInt(2) != 0);
        historyItem.setMessage(cursor.getString(3));
        return historyItem;
    }

    public ArrayList<HistoryItem> getHistoryItems(int n)
    {
        ArrayList<HistoryItem> historyItems = new ArrayList<HistoryItem>();
        try
        {
            db = dbCreator.getReadableDatabase();
            Cursor cursor = db.rawQuery("select * from history order by date desc limit " + n + ";", null);
            try
            {
                cursor.moveToFirst();
                while(!cursor.isAfterLast())
                {
                    historyItems.add(readHistoryItem(cursor));
                    cursor.moveToNext();
                }
                return historyItems;
            } finally
            {
                cursor.close();
            }
        } finally
        {
            if(db != null)
                db.close();
        }
    }

    public void removeHistoryItem(int id)
    {
        try
        {
            db = dbCreator.getWritableDatabase();
            db.delete("history", "_id = " + id, null);
        } finally
        {
            db.close();
        }
    }

    public void removeHistoryItems()
    {
        try
        {
            db = dbCreator.getWritableDatabase();
            db.delete("history", null, null);
        } finally
        {
            if(db != null)
                db.close();
        }

    }

    public int getMaxId()
    {
        try
        {
            db = dbCreator.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT max(_id) from history", null);
            try
            {
                if(cursor.getCount() > 0)
                {
                    cursor.moveToFirst();
                    return cursor.getInt(0);
                } else
                {
                    return 0;
                }
            } finally
            {
                cursor.close();
            }
        } finally
        {
            if(db != null)
                db.close();
        }
    }

}
