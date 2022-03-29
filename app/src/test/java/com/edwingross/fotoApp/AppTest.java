package com.edwingross.fotoApp;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;

import com.edwingross.fotoApp.Fragments.ImageViewFragment;
import com.edwingross.fotoApp.Fragments.ListFragment;
import com.edwingross.fotoApp.Fragments.PhotoFragment;
import com.edwingross.fotoApp.Model.PictureObject;
import com.edwingross.fotoApp.TabHandler.SectionsPagerAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.text.DateFormat;

import static org.junit.Assert.*;

import androidx.fragment.app.FragmentManager;

@RunWith(RobolectricTestRunner.class)
public class AppTest {

    private int id;
    private String name;
    @Mock
    private Bitmap bitmap;
    @Mock
    private Image image;
    private String dateAdded;

    private SectionsPagerAdapter sectionsPagerAdapter;

    @Mock
    private Context context;
    @Mock
    private FragmentManager fragmentManager;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

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
        pictureObject.setImage(bitmap);
        pictureObject.setDateAdded(dateAdded);

        assertEquals(id, pictureObject.getId());
        assertEquals(name, pictureObject.getName());
        assertEquals(bitmap, pictureObject.getImage());
        assertEquals(dateAdded, pictureObject.getDateAdded());
    }

    @Test
    public void getFragmentTest(){
        sectionsPagerAdapter = new SectionsPagerAdapter(context, fragmentManager);
        assertEquals(PhotoFragment.class, sectionsPagerAdapter.getItem(0).getClass());
        assertEquals(ImageViewFragment.class, sectionsPagerAdapter.getItem(1).getClass());
        assertEquals(ListFragment.class, sectionsPagerAdapter.getItem(2).getClass());
    }

    @Test
    public void getFragmentCountTest(){
        sectionsPagerAdapter = new SectionsPagerAdapter(context, fragmentManager);
        assertEquals(3 , sectionsPagerAdapter.getCount());
    }
}
