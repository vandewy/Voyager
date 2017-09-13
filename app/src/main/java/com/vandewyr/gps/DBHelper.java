package com.vandewyr.gps;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static com.google.android.gms.internal.zzt.TAG;


public class DBHelper extends SQLiteAssetHelper {

    private static String DB_PATH = "/data/user/0/com.wynkel.gps/databases/";
    private static String DB_NAME = "airfare_client.db";
    private SQLiteDatabase myDataBase;
    private Context myContext;

    private static int DATABASE_VERSION = 1;
    //private static String DATABASE_NAME = "airports.db";
    private String airport_KEY = "airport_name";
    private String airport_ID_KEY = "airport_id";
    private String airport_info_table = "airport_info";

    private double closestLat = 1000;
    private double closestLong = 1000;

    public DBHelper(Context context){
        super(context, DB_NAME, null, DATABASE_VERSION);

        DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        this.myContext = context;
    }

    //@Override
    //public void onCreate(SQLiteDatabase db){
    //String CREATE_TABLE_STUDENT = "CREATE TABLE airport_info (airport_name TEXT, airport_id TEXT)";

    //db.execSQL(CREATE_TABLE_STUDENT);


    //System.out.println("onCreate Called");
    //createDataBase();


    //}

    public void createDataBase() throws IOException
    {
        //If the database does not exist, copy it from the assets.

        boolean mDataBaseExist = checkDataBase();
        if(mDataBaseExist)
        {
            //this.getReadableDatabase();
            //this.close();
            try
            {
                //Copy the database from assests
                copyDataBase();
                Log.e(TAG, "createDatabase database created");
            }
            catch (IOException mIOException)
            {
                throw new Error("ErrorCopyingDataBase");
            }
        }else{
            Log.i("DB", "DB Should exist");
        }
    }

    private boolean checkDataBase()
    {
        File dbFile = new File(DB_PATH + DB_NAME);
        Log.v("dbFile ", dbFile + "   "+ dbFile.exists());
        return dbFile.exists();
    }

    //Copy the database from assets
    private void copyDataBase() throws IOException
    {
        Log.i("DATABASE ", "COPYING DATABASE");
        InputStream mInput = myContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0)
        {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    //Open the database, so we can query it
    public String openDataBase(double lat, double longitude) throws SQLException
    {
        String mPath = DB_PATH + DB_NAME;
        closestLat = 1000;
        closestLong = 1000;

        String airport = "";
        //Log.v("mPath", mPath);
        myDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        Log.i("DB = ", myDataBase.getPath());
        if(myDataBase != null){
            Log.i("NOT ", "NULL");
        }

        String sql;
        try
        {
            Log.i("QUERY", "RUNNING QUERY");
            sql ="SELECT * FROM airport_info"; // airport_info";

            Cursor mCur = myDataBase.rawQuery(sql, null);

            //iterate through and find closest airport
            while (mCur != null)
            {
                mCur.moveToNext();
                double curLat = mCur.getDouble(2);
                double curLong = mCur.getDouble(3);

                double tempLat = curLat - lat;
                double tempLong = curLong - longitude;

                if(tempLat < 0)
                    tempLat = tempLat * -1;

                if(tempLong < 0)
                    tempLong = tempLong * -1;

                if(tempLat < closestLat && tempLong < closestLong){
                    closestLat = tempLat;
                    closestLong = tempLong;
                    airport = mCur.getString(1);
                }

                if(mCur.isLast()){
                    mCur.close();
                    break;
                }
            }


            myDataBase.close();
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTestData >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return airport;
    }

    @Override
    public synchronized void close()
    {
        if(myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        db.execSQL("DROP TABLE IF EXISTS airport_info");

        onCreate(db);
    }

    public int insert(String airport, String airport_id){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(airport_KEY, airport);
        values.put(airport_ID_KEY, airport_id);

        long insert = db.insert(airport_info_table, null, values);
        return (int) insert;
    }

    public ArrayList<HashMap<String, String>> getAirportList() {
        //Open connection to read only
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery =  "SELECT * FROM airfare_info";

        //Student student = new Student();
        ArrayList<HashMap<String, String>> studentList = new ArrayList<HashMap<String, String>>();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> student = new HashMap<String, String>();
                student.put("airport_name", cursor.getString(cursor.getColumnIndex(airport_KEY)));
                student.put("airport_id", cursor.getString(cursor.getColumnIndex(airport_ID_KEY)));
                studentList.add(student);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return studentList;

    }

}