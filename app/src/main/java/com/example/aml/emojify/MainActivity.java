package com.example.aml.emojify;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.image_view) ImageView mImageView;


    @BindView (R.id.emojify_button) Button mEmojifyButton;
    @BindView(R.id.share_button) FloatingActionButton mShareFab;
    @BindView(R.id.save_button) FloatingActionButton mSaveFab;
    @BindView(R.id.clear_button) FloatingActionButton mClearFab;

    @BindView(R.id.title_text_view) TextView mTitleTextView;

    private String mTempPhotoPath;

    private Bitmap mResultsBitmap;

    private static final  int REQUEST_IMAGE_CAPTURE = 1 ;
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind the views
//        mImageView = (ImageView) findViewById(R.id.image_view);
//        mEmojifyButton = (Button) findViewById(R.id.emojify_button);
//        mShareFab = (FloatingActionButton) findViewById(R.id.share_button);
//        mSaveFab = (FloatingActionButton) findViewById(R.id.save_button);
//        mClearFab = (FloatingActionButton) findViewById(R.id.clear_button);
//        mTitleTextView = (TextView) findViewById(R.id.title_text_view);

        // Bind the views
        ButterKnife.bind(this);

        // Set up Timber
        Timber.plant(new Timber.DebugTree());

        // Bind the views
        ButterKnife.bind(this);

        // Set up Timber
        Timber.plant(new Timber.DebugTree());


    }

    /**
     * Create a temporary image file and capture a picture to store in it.
     */

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void launchCamera() {
        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activityt to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(getApplicationContext());
            } catch ( IOException ex) {
                // Error occurred while creating the file
                ex.printStackTrace();
            }
            //  continue only if the file was sucessfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();
            }
        }

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content uri for the image file
                Uri photoURI = FileProvider.getUriForFile(this , FILE_PROVIDER_AUTHORITY
                        ,photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT , photoURI);


                // Launch the camera activity
                startActivityForResult(takePictureIntent , REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    private  void  processAndSetImage () {
        // Toggle Visibility of the views
        mEmojifyButton.setVisibility(View.GONE);
        mTitleTextView.setVisibility(View.GONE);
        mSaveFab.setVisibility(View.VISIBLE);
        mShareFab.setVisibility(View.VISIBLE);
        mClearFab.setVisibility(View.VISIBLE);

        // Resample the saved image to fit the ImageView
        mResultsBitmap = BitmapUtils.resamplePic(this, mTempPhotoPath);

        // Detect the faces and overlay the appropriate emoji
        mResultsBitmap = Emojifier.detectFacesandOverlayEmoji(this , mResultsBitmap);

        // Set the new bitmap to the ImageView
        mImageView.setImageBitmap(mResultsBitmap);
    }

    @OnClick(R.id.emojify_button)
    public void emojifyMe(View view) {

        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this , new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE} ,REQUEST_STORAGE_PERMISSION);
        } else  {
            launchCamera();
        }


    }

@OnClick(R.id.save_button)
    public void saveMe(View view) {
        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);

        // Save the image
        BitmapUtils.saveImage(this, mResultsBitmap);
    }

    @OnClick(R.id.share_button)
    public void shareMe(View view) {
        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);

        // Save the image
        BitmapUtils.saveImage(this, mResultsBitmap);

        // Share the image
        BitmapUtils.shareImage(this, mTempPhotoPath);
    }

    /**
     * OnClick for the clear button, resets the app to original state.
     *
     * @param view The clear button.
     */
    @OnClick(R.id.clear_button)
    public void clearImage(View view) {
        // Clear the image and toggle the view visibility
        mImageView.setImageResource(0);
        mEmojifyButton.setVisibility(View.VISIBLE);
        mTitleTextView.setVisibility(View.VISIBLE);
        mShareFab.setVisibility(View.GONE);
        mSaveFab.setVisibility(View.GONE);
        mClearFab.setVisibility(View.GONE);

        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Process the image and set it to the TextView
            processAndSetImage();
        } else {

            // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       // called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION :
            {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    launchCamera();
                } else {

                    // If you do not get permission, Show a Toast
                    Toast.makeText(this , R.string.permission_denied , Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }




    }
}
