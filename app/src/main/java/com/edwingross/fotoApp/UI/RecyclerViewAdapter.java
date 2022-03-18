package com.edwingross.fotoApp.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edwingross.fotoApp.Database.DatabaseHandler;
import com.edwingross.fotoApp.Fragments.ImageViewFragment;
import com.edwingross.fotoApp.Model.PictureObject;
import com.edwingross.fotoApp.R;
import com.edwingross.fotoApp.Util.Constants;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<PictureObject> pictureObjectList;
    private FragmentActivity activity;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private LayoutInflater inflater;

    public RecyclerViewAdapter(Context context, List<PictureObject> pictureObjectList, FragmentActivity activity){
        this.context = context;
        this.pictureObjectList = pictureObjectList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_image, parent, false);
        return new ViewHolder(view, context, activity);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PictureObject pictureObject = pictureObjectList.get(position);

        holder.photoTitle.setText(pictureObject.getName());
        holder.photoDate.setText(pictureObject.getDateAdded());
        holder.photoThumbnail.setImageBitmap(pictureObject.getImage());
    }

    @Override
    public int getItemCount() {
        return pictureObjectList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public PictureObject pictureObject;

        //Vorschauseite
        private ImageView previewImageView;
        private TextView previewNoPicText;
        private Button previewSaveButton;
        //

        public TextView photoTitle;
        public TextView photoDate;
        public ImageView photoThumbnail;
        public Button editButton;
        public Button deleteButton;
        public CardView cardView;
        public int id;

        public ViewHolder(@NonNull View itemView, Context ctx, FragmentActivity activity){
            super(itemView);
            context = ctx;
            photoTitle = itemView.findViewById(R.id.namePicture);
            photoDate = itemView.findViewById(R.id.datePicture);
            photoThumbnail = itemView.findViewById(R.id.thumbnailPicture);
            editButton = itemView.findViewById(R.id.editButtonPicture);
            deleteButton = itemView.findViewById(R.id.deleteButtonPicture);
            cardView = itemView.findViewById(R.id.cardViewPicture);

            previewImageView = activity.findViewById(R.id.imageView);
            previewNoPicText = activity.findViewById(R.id.no_pic_text);
            previewSaveButton = activity.findViewById(R.id.bt_save);

            editButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);

            itemView.setOnClickListener(view -> {
                //TODO Design für Dark und Light
                pictureObject = pictureObjectList.get(getAdapterPosition());
                previewSaveButton.setVisibility(View.INVISIBLE);
                previewNoPicText.setVisibility(View.INVISIBLE);
                previewImageView.setImageBitmap(pictureObject.getImage());

                //Geht direkt zur Vorschau
                TabLayout tabHost = (TabLayout) activity.findViewById(R.id.tabs) ;
                tabHost.getTabAt(Constants.PHOTO_VIEW_TAB).select();

            });
        }

        @Override
        public void onClick(View v){
            switch(v.getId()){
                case R.id.editButtonPicture:
                    int position = getAdapterPosition();
                    PictureObject pictureObject = pictureObjectList.get(position);
                    editItem(pictureObject);
                    break;
                case R.id.deleteButtonPicture:
                    position = getAdapterPosition();
                    pictureObject = pictureObjectList.get(position);
                    deleteItem(pictureObject);
                    break;
            }
        }

        public void editItem(PictureObject pictureObject){
            dialogBuilder = new AlertDialog.Builder(context);

            inflater = LayoutInflater.from(context);
            final View view = inflater.inflate(R.layout.pop_up_save_image, null); //Layout from save_image but with another title

            final EditText pictureName = view.findViewById(R.id.photo_name);
            final TextView viewTitle = view.findViewById(R.id.photoTitleView);

            viewTitle.setText(Constants.EDIT_PHOTO_TITLE);
            pictureName.setText(pictureObject.getName());

            Button saveButton = view.findViewById(R.id.saveButtonImage);

            dialogBuilder.setView(view);
            dialog = dialogBuilder.create();
            dialog.show();

            saveButton.setOnClickListener(view1 -> {
                DatabaseHandler db = new DatabaseHandler(context);

                pictureObject.setName(pictureName.getText().toString());

                if(!pictureObject.getName().isEmpty()){
                    db.updatePicture(pictureObject);
                    notifyItemChanged(getAdapterPosition(), pictureObject);
                }else{
                    Snackbar.make(view, "Bitte einen Titel eingeben!", Snackbar.LENGTH_LONG).show();
                }
                dialog.dismiss();
            });
        }

        public void deleteItem(PictureObject pictureObject){
            dialogBuilder = new AlertDialog.Builder(context);
            inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.pop_up_delete_image, null);

            Button noBtn = view.findViewById(R.id.noButton);
            Button yesBtn = view.findViewById(R.id.yesButton);

            dialogBuilder.setView(view);
            dialog = dialogBuilder.create();
            dialog.show();

            noBtn.setOnClickListener(view1 -> {
                dialog.dismiss();
            });

            yesBtn.setOnClickListener(view2 -> {
                DatabaseHandler db = new DatabaseHandler(context);
                db.deletePicture(pictureObject);
                pictureObjectList.remove(getAdapterPosition());
                notifyItemRemoved(getAdapterPosition());

                dialog.dismiss();
            });
        }

    }
}
