package com.edwingross.fotoApp.Util;

import android.util.Log;

public class Constants {
    public static final int DB_VERSION = 3;
    public static final String DB_NAME = "pictureDB";
    public static final String TABLE_NAME = "pictureTable";

    public static final String KEY_PICTURE_ID = "id";
    public static final String KEY_PICTURE_NAME = "pictureItem";
    public static final String KEY_PICTURE_IMAGE = "pictureData";
    public static final String KEY_PICTURE_DATE = "pictureDate";

    public static final int PHOTO_CAPTURE_TAB = 0;
    public static final int PHOTO_VIEW_TAB = 1;
    public static final int PHOTO_LIST_TAB = 2;

    public static final String EDIT_PHOTO_TITLE = "Foto bearbeiten";

    public static void lol(String msg){
        Log.d("lol", msg);
    }

}
