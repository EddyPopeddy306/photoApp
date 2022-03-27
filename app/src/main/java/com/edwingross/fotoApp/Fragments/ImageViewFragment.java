package com.edwingross.fotoApp.Fragments;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.edwingross.fotoApp.Activities.MainActivity;
import com.edwingross.fotoApp.Database.DatabaseHandler;
import com.edwingross.fotoApp.Model.PictureObject;
import com.edwingross.fotoApp.R;
import com.edwingross.fotoApp.Services.PhotoService;
import com.edwingross.fotoApp.Util.Constants;

import java.util.ArrayList;
import java.util.List;

public class ImageViewFragment extends Fragment {

    private PhotoService boundService;
    private boolean isBound;

    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    private ImageView imageView;
    private EditText photoTitle;
    private TextView noPicText;
    private Button saveButton;
    private Button popUpSaveButton;

    private DatabaseHandler db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        doBindService();
        return inflater.inflate(R.layout.image_view_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.imageView);
        noPicText = view.findViewById(R.id.no_pic_text);
        saveButton = view.findViewById(R.id.bt_save);
        saveButton.setOnClickListener(view12 -> {
            createPopUpDialog();
        });
    }

    public void createPopUpDialog() {//Vllt auslagern und pictureObject static machen
        builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.pop_up_save_image, null);
        photoTitle = view.findViewById(R.id.photo_name);
        photoTitle.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        popUpSaveButton = view.findViewById(R.id.saveButtonImage);

        //Öffnet automatisch die Tastatur und fokussiert das Fototitel Feld
        photoTitle.requestFocus();
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();

        popUpSaveButton.setOnClickListener(view1 -> {
            if(!photoTitle.getText().toString().isEmpty()){
                saveImageToDB();
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                saveButton.setVisibility(View.INVISIBLE);
                alertDialog.dismiss();
            }
        });
    }

    private void saveImageToDB() {
        //db = new DatabaseHandler(getContext());
        //PhotoFragment.pictureObject.setName(photoTitle.getText().toString());
        boundService.getPictureObject().setName(photoTitle.getText().toString());
        //Image got set in capturePhoto() method

        //db.addPicture(PhotoFragment.pictureObject);
        //db.addPicture(boundService.getPictureObject());
        boundService.addToDb();

        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
        //SQLite braucht einen restart vom Intent, da die Daten sonst nicht richtig gespeichert sind

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            boundService = ((PhotoService.LocalBinder) service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            boundService = null;
            isBound = false;
        }
    };

    void doBindService() {
        getActivity().bindService(new Intent(getActivity(), PhotoService.class), mConnection, Context.BIND_AUTO_CREATE);
        getActivity().startService(new Intent(getActivity(), PhotoService.class));
        isBound = true;
    }

    void doUnbindService() {
        if (isBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            isBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
