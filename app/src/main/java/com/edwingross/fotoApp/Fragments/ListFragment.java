package com.edwingross.fotoApp.Fragments;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edwingross.fotoApp.Database.DatabaseHandler;
import com.edwingross.fotoApp.Model.PictureObject;
import com.edwingross.fotoApp.R;
import com.edwingross.fotoApp.Services.PhotoService;
import com.edwingross.fotoApp.UI.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListFragment extends Fragment {

    private PhotoService boundService;
    private boolean isBound;

    private List<PictureObject> pictureObjectList;
    private List<PictureObject> listItems;

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bindService();
        return inflater.inflate(R.layout.list_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialisierung der Listen
        pictureObjectList = new ArrayList<>();
        listItems = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerViewIDPicture);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        pictureObjectList = new ArrayList<>();
        listItems = new ArrayList<>();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            boundService = ((PhotoService.LocalBinder) service).getService();

            pictureObjectList = boundService.getAllPicturesFromDb();
            for(PictureObject p : pictureObjectList){
                Log.d("Test", "Name: " + p.getName() + " Pic: " + p.getImage().toString());
                PictureObject pictureObject = new PictureObject();
                pictureObject.setId(p.getId());
                pictureObject.setName(p.getName());
                pictureObject.setImage(p.getImage());
                pictureObject.setDateAdded("Aufgenommen am: " + p.getDateAdded());

                listItems.add(pictureObject);
            }

            recyclerViewAdapter = new RecyclerViewAdapter(getContext(), listItems, getActivity());
            recyclerView.setAdapter(recyclerViewAdapter);

            recyclerViewAdapter.notifyDataSetChanged();
        }

        public void onServiceDisconnected(ComponentName className) {
            boundService = null;
            isBound = false;
        }
    };

    void bindService() {
        getActivity().bindService(new Intent(getActivity(), PhotoService.class), mConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    void unbindService() {
        if (isBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            isBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService();
    }

}
