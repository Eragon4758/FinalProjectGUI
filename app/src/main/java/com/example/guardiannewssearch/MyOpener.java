package com.example.guardiannewssearch;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class MyOpener extends SQLiteOpenHelper {

    protected final static String DATABASE_NAME = "faveDB";
    protected final static int VERSION_NUM = 2;
    public final static String TABLE_NAME = "SAVED";
    public final static String TABLE_NAME2 = "DELETED";
    public final static String TABLE_NAME3 = "RECOMENDED";
    public final static String COL_TITLE = "TITLE";
    public final static String COL_SECTION = "SECTION";
    public final static String COL_URL = "URL";
    public final static String COL_ID = "_id";

    public MyOpener(Context ctx)
    {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }


    //This function gets called if no database file exists.
    //Look on your device in the /data/data/package-name/database directory.
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " text,"
                + COL_SECTION  + " text," + COL_URL +" text );");
        db.execSQL("CREATE TABLE " + TABLE_NAME2 + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " text,"
                + COL_SECTION  + " text," + COL_URL +" text );");
        db.execSQL("CREATE TABLE " + TABLE_NAME3 + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " text,"
                + COL_SECTION  + " text," + COL_URL +" text );"); // add or remove columns
    }


    //this function gets called if the database version on your device is lower than VERSION_NUM
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {   //Drop the old table:
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME2);
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME3);

        //Create the new table:
        onCreate(db);
    }

    //this function gets called if the database version on your device is higher than VERSION_NUM
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {   //Drop the old tables:
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME2);
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME3);

        //Create the new tables:
        onCreate(db);
    }
}

