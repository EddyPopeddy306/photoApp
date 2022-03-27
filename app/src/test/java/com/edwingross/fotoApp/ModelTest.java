package com.edwingross.fotoApp;

import android.graphics.Bitmap;

import com.edwingross.fotoApp.Database.DatabaseHandler;
import com.edwingross.fotoApp.Model.PictureObject;
import com.edwingross.fotoApp.Util.Constants;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.DateFormat;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ModelTest {

    private int id;
    private String name;
    @Mock
    private Bitmap image;
    private String dateAdded;

    @Test
    public void testPictureObject(){
        id = 1;
        name = "TestPitcure";

        DateFormat dateFormat = DateFormat.getDateInstance();
        String formatDate = dateFormat.format(System.currentTimeMillis());

        dateAdded = formatDate;

        PictureObject pictureObject = new PictureObject();
        pictureObject.setId(id);
        pictureObject.setName(name);
        pictureObject.setImage(image);
        pictureObject.setDateAdded(dateAdded);

        assertEquals(pictureObject.getId(),id);
        assertEquals(pictureObject.getName(),name);
        assertEquals(pictureObject.getImage(),image);
        assertEquals(pictureObject.getDateAdded(),dateAdded);
    }
}
