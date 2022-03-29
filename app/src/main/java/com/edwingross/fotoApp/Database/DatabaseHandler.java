package com.edwingross.fotoApp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.edwingross.fotoApp.Model.PictureObject;
import com.edwingross.fotoApp.Util.Constants;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private Context ctx;
    private ByteArrayOutputStream objectByteArrayOutputStream;
    private byte[] imageInBytes;

    public DatabaseHandler(Context context){
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
        this.ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + Constants.TABLE_NAME + "("
                + Constants.KEY_PICTURE_ID + " INTEGER PRIMARY KEY,"
                + Constants.KEY_PICTURE_NAME + " TEXT,"
                + Constants.KEY_PICTURE_IMAGE + " BLOB,"
                + Constants.KEY_PICTURE_DATE + " LONG);";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME);
        onCreate(db);
    }

    public List<PictureObject> getAllPictures(){
        SQLiteDatabase db = this.getReadableDatabase();

        List<PictureObject> pictureObjectList = new ArrayList<>();

        Cursor cursor = db.query(Constants.TABLE_NAME, new String[]{
                Constants.KEY_PICTURE_ID, Constants.KEY_PICTURE_NAME, Constants.KEY_PICTURE_IMAGE, Constants.KEY_PICTURE_DATE
        }, null, null, null, null, Constants.KEY_PICTURE_DATE + " DESC");

        byte[] imageBytes;

        if(cursor.moveToFirst()){
            do{
                PictureObject pictureObject = new PictureObject();
                pictureObject.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(Constants.KEY_PICTURE_ID))));
                pictureObject.setName(cursor.getString(cursor.getColumnIndexOrThrow(Constants.KEY_PICTURE_NAME)));

                DateFormat dateFormat = DateFormat.getDateInstance();
                String formatDate = dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.KEY_PICTURE_DATE))));

                pictureObject.setDateAdded(formatDate);

                //Byte Array to Bitmap
                imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(Constants.KEY_PICTURE_IMAGE));

                Bitmap objBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                pictureObject.setImage(objBitmap);

                pictureObjectList.add(pictureObject);

            }while(cursor.moveToNext());
        }

        return pictureObjectList;
    }

    public int getPictureCount(){
        SQLiteDatabase db = this.getReadableDatabase();

        String countQuery = "SELECT * FROM " + Constants.TABLE_NAME;

        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

    public long addPicture(PictureObject picture){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        //Bitmap to byte array
        Bitmap imageToStoreBitmap = picture.getImage();
        objectByteArrayOutputStream = new ByteArrayOutputStream();
        imageToStoreBitmap.compress(Bitmap.CompressFormat.JPEG, 80, objectByteArrayOutputStream);
        imageInBytes = objectByteArrayOutputStream.toByteArray();
        //
        values.put(Constants.KEY_PICTURE_NAME, picture.getName());
        values.put(Constants.KEY_PICTURE_IMAGE, imageInBytes);
        values.put(Constants.KEY_PICTURE_DATE, java.lang.System.currentTimeMillis());

        long result = db.insert(Constants.TABLE_NAME, null, values);
        Log.d("Added", "Saved to DB: " + values);
        return result;
    }

    public void updatePicture(PictureObject picture){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Constants.KEY_PICTURE_NAME, picture.getName());

        //Bitmap to byte array
        Bitmap imageToStoreBitmap = picture.getImage();
        objectByteArrayOutputStream = new ByteArrayOutputStream();
        imageToStoreBitmap.compress(Bitmap.CompressFormat.JPEG, 100, objectByteArrayOutputStream);
        imageInBytes = objectByteArrayOutputStream.toByteArray();
        //
        values.put(Constants.KEY_PICTURE_IMAGE, imageInBytes);


        db.update(Constants.TABLE_NAME, values, Constants.KEY_PICTURE_ID + "=?", new String[]{
                String.valueOf(picture.getId())
        });
        Log.d("Updated", "Updated picture: " + values);
    }

    public void deletePicture(PictureObject picture){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Constants.TABLE_NAME, Constants.KEY_PICTURE_ID + "=?", new String[]{
                String.valueOf(picture.getId())
        });
        db.close();
        Log.d("Deleted", "Deleted picture: " + picture.getName() + " with ID: " + picture.getId());
    }
}
