package com.edwingross.fotoApp.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.edwingross.fotoApp.Database.DatabaseHandler;
import com.edwingross.fotoApp.Model.PictureObject;
import com.edwingross.fotoApp.R;
import com.edwingross.fotoApp.UI.RecyclerViewAdapter;
import com.edwingross.fotoApp.Util.Constants;
import com.google.android.material.tabs.TabLayout;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class PhotoFragment extends Fragment {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    PreviewView previewView;

    private TextView noPicText;

    private Button cameraButton;
    private Button saveButton;

    private ImageCapture imageCapture;
    private DatabaseHandler db;

    public static PictureObject pictureObject;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photo_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraButton = getView().findViewById(R.id.bt_open);
        previewView = getView().findViewById(R.id.previewView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this.getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            }catch (ExecutionException e){
                e.printStackTrace();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }, getExecutor());

        //Wenn die Kamerafreigabe erlaubt ist, dann soll der Knopftext von "Kamera öffnen" auf "Foto aufnehmen" geändert werden
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            cameraButton.setText("Foto aufnehmen");
        }

        cameraButton.setOnClickListener(view1 -> {
            //Nach Kamerarechten fragen
            if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{
                                Manifest.permission.CAMERA
                        }, 100);
            }else{
                capturePhoto();
            }
        });



    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this.getContext());
    }

    private void startCameraX(ProcessCameraProvider cameraProvider){
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

    private void capturePhoto() {
        Context context = getContext();
        saveButton = getActivity().findViewById(R.id.bt_save);
        noPicText = getActivity().findViewById(R.id.no_pic_text);
        imageCapture.takePicture(
                getExecutor(),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        pictureObject = new PictureObject();
                        Toast.makeText(context, "Photo captured: " + image.toString(), Toast.LENGTH_SHORT).show();
                        super.onCaptureSuccess(image);

                        @SuppressLint("UnsafeOptInUsageError") Bitmap bitmap = convertToBitmap(image.getImage());
                        pictureObject.setImage(bitmap);

                        saveButton.setVisibility(View.VISIBLE);
                        noPicText.setVisibility(View.GONE);

                        //Geht direkt zur Vorschau
                        TabLayout tabHost = (TabLayout) getActivity().findViewById(R.id.tabs) ;
                        tabHost.getTabAt(Constants.PHOTO_VIEW_TAB).select();

                        image.close();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(context, "Photo not captured: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        super.onError(exception);
                    }
                }
        );
    }

    private Bitmap convertToBitmap(Image image){

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);

        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        Bitmap scaledBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        ImageView img = getActivity().findViewById(R.id.imageView);
        img.setImageBitmap(rotatedBitmap);

        return rotatedBitmap;
    }

}
