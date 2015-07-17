package com.sammi.sky;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sammi on 7/17/15.
 */
public class DBAdapter
{
    DatabaseHelper DBHelper;
    SQLiteDatabase db;
    Context context;
    static final int DATABASE_VERSION = 1;

    public DBAdapter(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context,"sky");
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context,String DATABASE_NAME)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            //DB already in assets
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            //wont need this
        }
    }

    //opens the database
    public DBAdapter open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //closes the database
    public void close()
    {
        DBHelper.close();
    }

    //insert into table color
    public long insertColor(int colorvalue, String colorText)
    {
        ContentValues values = new ContentValues();
        values.put("value",colorvalue);
        values.put("color",colorText);

        return db.insert("color",null, values);
    }

    //Read table color
    //This will be used as trained data
    public Cursor getColors()
    {
        return db.query("color",new String[]{"value","color"},null,null,null,null,null);
    }

    public long insertSky(String path,String sky,String time)
    {
        ContentValues values = new ContentValues();
        values.put("path",path);
        values.put("sky",sky);
        values.put("time",time);

        return db.insert("sky",null,values);
    }

    public Cursor getBlue()
    {
        return db.query("sky",new String[]{"path","sky","time"},"color=blue",null,null,null,null);
    }

    public Cursor getWhite()
    {
        return db.query("sky",new String[]{"path","sky","time"},"color=white",null,null,null,null);
    }

    public Cursor getGrey()
    {
        return db.query("sky",new String[]{"path","sky","time"},"color=grey",null,null,null,null);
    }
}
