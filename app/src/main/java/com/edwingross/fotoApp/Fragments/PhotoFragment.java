package com.edwingross.fotoApp.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import androidx.lifecycle.LifecycleOwner;

import com.edwingross.fotoApp.Database.DatabaseHandler;
import com.edwingross.fotoApp.Model.PictureObject;
import com.edwingross.fotoApp.R;
import com.edwingross.fotoApp.Services.PhotoService;
import com.edwingross.fotoApp.Util.Constants;
import com.google.android.material.tabs.TabLayout;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class PhotoFragment extends Fragment {

    private PhotoService boundService;
    private boolean isBound;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private boolean isFront;

    PreviewView previewView;

    private TextView noPicText;

    private Button cameraButton;
    private Button rotateButton;
    private Button saveButton;

    private ImageCapture imageCapture;
    private DatabaseHandler db;

    public static PictureObject pictureObject;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bindService();
        return inflater.inflate(R.layout.photo_fragment_layout, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraButton = getView().findViewById(R.id.bt_open);
        rotateButton = getView().findViewById(R.id.bt_rotate);
        previewView = getView().findViewById(R.id.previewView);
        isFront = false;

        cameraProviderFuture = ProcessCameraProvider.getInstance(this.getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider, CameraSelector.LENS_FACING_BACK);
            }catch (ExecutionException e){
                e.printStackTrace();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }, getExecutor());

        //Wenn die Kamerafreigabe erlaubt ist, dann soll der Knopftext von "Kamera öffnen" auf "Foto aufnehmen" geändert werden und der Rotationsknopf soll eingefügt werden
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            cameraButton.setText("Foto aufnehmen");
            rotateButton.setVisibility(View.VISIBLE);
        }

        cameraButton.setOnClickListener(view1 -> {
            //Nach Kamerarechten fragen
            if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{
                                Manifest.permission.CAMERA
                        }, 100);
            }else{
                if(isBound){
                    boundService.testService();
                }
                capturePhoto();
            }
        });

        rotateButton.setOnClickListener(view1 -> {
            isFront = !isFront;
            if(isFront){
                try{
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    startCameraX(cameraProvider, CameraSelector.LENS_FACING_FRONT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }else{
                try{
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    startCameraX(cameraProvider, CameraSelector.LENS_FACING_BACK);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this.getContext());
    }

    private void startCameraX(ProcessCameraProvider cameraProvider, int facing){
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(facing)
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
                        boundService.initPictureObject();
                        Toast.makeText(context, "Photo captured: " + image.toString(), Toast.LENGTH_SHORT).show();
                        super.onCaptureSuccess(image);

                        @SuppressLint("UnsafeOptInUsageError") Bitmap bitmap = convertToBitmap(image.getImage(), isFront);
                        boundService.getPictureObject().setImage(bitmap);

                        saveButton.setVisibility(View.VISIBLE);
                        noPicText.setVisibility(View.GONE);

                        //Geht direkt zur Vorschau
                        TabLayout tabHost = (TabLayout) getActivity().findViewById(R.id.tabs);
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

    public Bitmap convertToBitmap(Image image, boolean isFront){

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        if(isFront){//If the camera faces front you have to prescale the image to not be mirrored
            matrix.preScale(-1.0f, 1.0f);
        }

        Bitmap scaledBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        ImageView img = getActivity().findViewById(R.id.imageView);
        img.setImageBitmap(rotatedBitmap);

        return rotatedBitmap;
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
