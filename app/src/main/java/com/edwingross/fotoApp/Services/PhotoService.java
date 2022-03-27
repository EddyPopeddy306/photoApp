package com.edwingross.fotoApp.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.edwingross.fotoApp.Database.DatabaseHandler;
import com.edwingross.fotoApp.Model.PictureObject;

import java.util.Random;

public class PhotoService extends Service {

    // Binder given to clients
    private LocalBinder binder = new LocalBinder();

    private PictureObject pictureObject;
    private DatabaseHandler db;

    public class LocalBinder extends Binder {
        public PhotoService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PhotoService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void initPictureObject(){
        pictureObject = new PictureObject();
    }

    public PictureObject getPictureObject(){
        return this.pictureObject;
    }

    public void setPictureObject(PictureObject pictureObject){
        this.pictureObject = pictureObject;
    }

    public void addToDb(){
        db = new DatabaseHandler(this);
        db.addPicture(this.pictureObject);
    }

    public void testService(){
        Log.d("ServiceTest", "Test vom Service");
    }


}
