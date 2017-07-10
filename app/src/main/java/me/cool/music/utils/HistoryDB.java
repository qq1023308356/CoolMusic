package me.cool.music.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import me.cool.music.model.Music;

/**
 * Created by xiong on 17-7-10.
 */

public class HistoryDB extends SQLiteOpenHelper {
    private static HistoryDB dbhelper = null;
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "History.db";
    public static final String TABLE_NAME = "classtable";

    public static HistoryDB getInstens(Context context) {
        if (dbhelper == null) {
            dbhelper = new HistoryDB(context);
        }
        return dbhelper;
    }

    private HistoryDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists "+TABLE_NAME+" (_id integer primary key autoincrement,classtabledata text)";
        //String sql = "create table if not exists " + TABLE_NAME + " (Id integer primary key, CustomName text, OrderPrice integer, Country text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 保存
     *
     * @param student
     */
    public void saveHistoryMusic(Music student) {
        List<Music> musicList=getHistoryMusic();
        for (Music music : musicList){
            //Log.e("id",music.getId()+":"+student.getId());
            if (music.getId()==student.getId()){
                return;
            }
        }
        //Log.e("55",musicList.size()+"");
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(arrayOutputStream);
            objectOutputStream.writeObject(student);
            objectOutputStream.flush();
            byte data[] = arrayOutputStream.toByteArray();
            objectOutputStream.close();
            arrayOutputStream.close();
            SQLiteDatabase database = dbhelper.getWritableDatabase();
            if (musicList.size()>=50){
                //delete from tb where id in (select top n id from tb)
                database.execSQL("delete from "+TABLE_NAME+" where classtabledata=(select classtabledata from "+TABLE_NAME+" COMPANY LIMIT 1)");
                database.execSQL("insert into " + TABLE_NAME + " (classtabledata) values(?)", new Object[]{data});
            }else {
                database.execSQL("insert into " + TABLE_NAME + " (classtabledata) values(?)", new Object[]{data});
            }
            database.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<Music> getHistoryMusic() {
        List<Music> musicList=new ArrayList<Music>();
        Music student = null;
        SQLiteDatabase database = dbhelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from "+TABLE_NAME, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                byte data[] = cursor.getBlob(cursor.getColumnIndex("classtabledata"));
                ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
                try {
                    ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream);
                    student = (Music) inputStream.readObject();
                    inputStream.close();
                    arrayInputStream.close();
                    musicList.add(student);
                    //break;//这里为了测试就取一个数据
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return musicList;

    }
}
