package com.qisiemoji.apksticker.whatsapp.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.View;

import com.qisiemoji.apksticker.BuildConfig;
import com.qisiemoji.apksticker.R;
import com.qisiemoji.apksticker.whatsapp.SelectAlbumStickersActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.EXTRA_SELECTED_LIST;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.REQUEST_CODE_SELECT_ALBUM_STICKERS;
import static com.qisiemoji.apksticker.whatsapp.manager.WAStickerManager.RESULT_CODE_FINISH_EDIT_IMAGE;

public class ChooseImageSourceDialogFragment extends BasicDialogFragment implements View.OnClickListener {
    public static final String DIALOG_FRAGMENT = "choose_image_source_dialog_fragment";

    private static final int REQ_CAPTURE_PIC = 200;
    private static final int REQ_CHECK_CAMERA_PERMISSION = 100;

    public static ChooseImageSourceDialogFragment newInstance() {
        ChooseImageSourceDialogFragment f = new ChooseImageSourceDialogFragment();
        return f;
    }

    private View mGiphy;
    private View mTakePhoto;
    private View mGallery;

    private String takePhotoImagePath;

    public interface ChooseImageSourceDialogFragmentCallback {
        void onGetImagePathFromOutside(ArrayList<String> imagePaths);
    }

    private ChooseImageSourceDialogFragmentCallback mChooseImageSourceDialogFragmentCallback;

    public void setCallBack(ChooseImageSourceDialogFragmentCallback callBack) {
        mChooseImageSourceDialogFragmentCallback = callBack;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.dialogfg_choose_image_source;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void setupViews(View view) {
        mGiphy = view.findViewById(R.id.get_image_from_giphy);
        mTakePhoto = view.findViewById(R.id.get_image_from_taking_photo);
        mGallery = view.findViewById(R.id.get_image_from_gallery);

        mGiphy.setOnClickListener(this);
        mTakePhoto.setOnClickListener(this);
        mGallery.setOnClickListener(this);

        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_image_from_giphy:
                break;

            case R.id.get_image_from_taking_photo:
                if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CHECK_CAMERA_PERMISSION);
                } else {
                    onCameraPermissionGranted();
                }
                break;

            default:
                Intent intent = new Intent(getContext(), SelectAlbumStickersActivity.class);
                intent.putExtra(SelectAlbumStickersActivity.EXTRA_SELECTED_ITEM_MIN, 1);
                startActivityForResult(intent, REQUEST_CODE_SELECT_ALBUM_STICKERS);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CHECK_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onCameraPermissionGranted();
            } else {
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CAPTURE_PIC:
                if (resultCode != Activity.RESULT_OK) {
                    return;
                }

                ArrayList<String> cameraPath = new ArrayList<>();
                cameraPath.add(takePhotoImagePath);
                onCallback(cameraPath);
                break;

            case REQUEST_CODE_SELECT_ALBUM_STICKERS:
                if (resultCode != RESULT_CODE_FINISH_EDIT_IMAGE) {
                    return;
                }

                ArrayList<String> list = data.getStringArrayListExtra(EXTRA_SELECTED_LIST);
                if (list == null) {
                    list = new ArrayList<>();
                }
                onCallback(list);
                break;
        }

    }

    private void onCallback(ArrayList<String> imagePaths) {
        mChooseImageSourceDialogFragmentCallback.onGetImagePathFromOutside(imagePaths);
        dismiss();
    }

    private void onCameraPermissionGranted() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider.files", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQ_CAPTURE_PIC);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        takePhotoImagePath = image.getAbsolutePath();
        return image;
    }
}
