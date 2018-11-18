package com.example.dima.colorrecognition;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DB {
    private final String TAG = "MyApp";

    private static final String DB_NAME = "color_recognize";
    private static final int DB_VERSION = 1;

    private static final String COLOR_TABLE = "color";
    private static final String COLOR_ID = "_id";
    static final String COLOR_NAME = "name";
    static final String COLOR_VALUE = "value";

    private static final String DB_CREATE =
            "CREATE TABLE " + COLOR_TABLE + " (" +
                    COLOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLOR_NAME +" TEXT NOT NULL UNIQUE," +
                    COLOR_VALUE + " INTEGER NOT NULL" +
            ");";

    private final Context ctx;

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    DB(Context context) {
        Log.d(TAG, "DB конструктор");
        ctx = context;
    }

    void open() {
        Log.d(TAG, "DB открытие соединения");
        dbHelper = new DBHelper(ctx, DB_NAME, null, DB_VERSION);
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        Log.d(TAG, "DB закрытие соединения");
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    Cursor getColorByNames(ArrayList<String> names) {
        Log.d(TAG, "DB getColorByNames");
        Log.d(TAG, names.toString());
        StringBuilder whereStmt = new StringBuilder(COLOR_NAME + " IN(");
        for (int i = 0; i < names.size() - 1; i++) {
            whereStmt.append("?, ");
        }
        whereStmt.append("?)");

        Log.d(TAG, whereStmt.toString());

        String[] args = names.toArray(new String[0]);

        return db.query(COLOR_TABLE, new String[]{ COLOR_VALUE }, whereStmt.toString(), args, null, null, null, "1");
    }

    public void addColor(String name, int color) {
        Log.d(TAG, "DB addColor");
        Log.d(TAG, name);
        Log.d(TAG, Integer.toHexString(color));
        ContentValues cv = new ContentValues();
        cv.put(COLOR_NAME, name);
        cv.put(COLOR_VALUE, color);
        db.insert(COLOR_TABLE, null, cv);
    }

    private class DBHelper extends SQLiteOpenHelper {

        DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                 int version) {
            super(context, name, factory, version);
            Log.d(TAG, "DBHelper конструктор");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "DBHelper onCreate");
            db.execSQL(DB_CREATE);

            ContentValues cv = new ContentValues();

            HashMap<String, Integer> colors = new HashMap<>();
            colors.put("черный", Color.BLACK);
            colors.put("белый", Color.WHITE);
            colors.put("красный", Color.RED);
            colors.put("лайм", Color.rgb(0, 255, 0));
            colors.put("синий", Color.BLUE);
            colors.put("желтый", Color.YELLOW);
            colors.put("magenta", Color.MAGENTA);
            colors.put("циан", Color.CYAN);

            colors.put("оранжевый", Color.rgb(255, 165, 0));
            colors.put("зеленый", Color.rgb(0, 128, 0));
            colors.put("голубой", Color.rgb(66, 170, 255));
            colors.put("фиолетовый", Color.rgb(139, 0 , 255));

            colors.put("бирюзовый", Color.rgb(48, 213, 200));
            colors.put("золотой", Color.rgb(255, 215, 0));
            colors.put("изумрудный", Color.rgb(80, 200, 120));
            colors.put("коричневый", Color.rgb(150, 75, 0));
            colors.put("малиновый", Color.rgb(220, 20, 60));
            colors.put("персиковый", Color.rgb(255, 229, 180));
            colors.put("розовый", Color.rgb(255, 192, 203));
            colors.put("салатовый", Color.rgb(153, 255, 153));
            colors.put("серый", Color.rgb(128, 128, 128));
            colors.put("хаки", Color.rgb(128, 107, 42));
            colors.put("шоколадный", Color.rgb(210, 105, 30));

            colors.put("цвет адского пламени", Color.rgb(231, 26, 41));
            colors.put("бабушкины яблоки", Color.rgb(168, 228, 160));
            colors.put("цвет бедра испуганной нимфы", Color.rgb(255, 238, 221));
            colors.put("последний вздох жако", Color.rgb(255, 146, 24));
            colors.put("синий экран смерти", Color.rgb(18, 47, 170));
            colors.put("сюрприз дофина", Color.rgb(247, 242, 26));
            colors.put("цвет яндекса", Color.rgb(255, 204, 0));

            for (Map.Entry<String, Integer> color : colors.entrySet()) {
                cv.put(COLOR_NAME, color.getKey());
                cv.put(COLOR_VALUE, color.getValue());
                db.insert(COLOR_TABLE, null, cv);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    }
}
