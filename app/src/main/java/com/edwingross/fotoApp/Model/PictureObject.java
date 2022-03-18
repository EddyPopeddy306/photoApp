package com.edwingross.fotoApp.Model;

import android.graphics.Bitmap;

public class PictureObject {

    private int id;
    private String name;
    private Bitmap image;
    private String dateAdded;

    public PictureObject(int id, String name, Bitmap image, String dateAdded) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.dateAdded = dateAdded;
    }

    public PictureObject(){
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
}
