package com.example.aml.emojify;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapUtils {


    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";

    /**
     * creates the temporary image file in the casche directory.
     *
     * @return The temporary image file.
     * @throws IOException Thrown if there is an error creating the file
     */
      static File createTempImageFile (Context context) throws IOException {
          String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss" , Locale.getDefault()).format(new Date());
          String imageFileName = "JPEG_" + timeStamp +"_";
          File storageDir = context.getExternalCacheDir();
          return  File.createTempFile(imageFileName /* prefix */
                  , ".jpeg"  /* suffix */
                  , storageDir /* directory */
          );
      }


    /**
     * Resample the captured photo to fit the screen for better memory usage.
     * @param  context
     * @param imagePath The path of the photo to be resampled.
     * @return The resampled bitmap

     */
    public static Bitmap resamplePic(Context context, String imagePath) {
        // Get device screen size information
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manger = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        manger.getDefaultDisplay().getMetrics(metrics);

        int targetH = metrics.heightPixels;
        int targetW = metrics.widthPixels;

        // Get the dimension of the original bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true ;
        BitmapFactory.decodeFile(imagePath , bmOptions);
        int photoW = bmOptions.outWidth ;
        int photoH  = bmOptions.outHeight ;


        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the view
        bmOptions.inJustDecodeBounds = false ;
        bmOptions.inSampleSize = scaleFactor ;

        return BitmapFactory.decodeFile(imagePath);


    }


    /**
     * Helper method for adding the photo to the system photo gallery so it can be accessed
     * from other apps
     *
     * @param imagePath The path of the saved Image
     */

private static void  galleryAddPic (Context context , String imagePath) {
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    File f = new File(imagePath);
    Uri contentUri = Uri.fromFile(f);
    mediaScanIntent.setData(contentUri);
    context.sendBroadcast(mediaScanIntent);
}

    /**
     * Helper method for saving the image.
     * @param  context The application context
     * @param  image The image to be saved.
     * @return The path of the saved image.
     */

 static String saveImage (Context context , Bitmap image ) {
     String savedImagePath = null;

     //Create the new file in the external storage
     String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss" ,
             Locale.getDefault()).format(new Date());
     String imageFileName = "JPEG_" + timeStamp + ".jpg";
     File storageDir = new File(
             Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
             +"/Emojify");

     boolean success = true;
     if (!storageDir.exists()) {
         success = storageDir.mkdirs();

 }

// Save the new Bitmap
     if (success) {
         File imageFile = new File(storageDir , imageFileName);
         savedImagePath = imageFile.getAbsolutePath();
     }
     try {
         OutputStream fOut = new FileOutputStream(imageFileName);
         image.compress(Bitmap.CompressFormat.JPEG , 100 ,fOut);
         fOut.close();
     } catch (Exception e) {
         e.printStackTrace();
     }

         // Add the image to the system gallery
     galleryAddPic(context , savedImagePath);

     // Show a Toast with the save location
     String savedMessage = context.getString(R.string.saved_message , savedImagePath);
     Toast.makeText(context,savedMessage , Toast.LENGTH_SHORT).show();


    return savedImagePath ;

    }

    /**
     * Helper method for sharing an image
     *
     * @param context The image context
     * @param imagePath The Path of the image to be shared.
     */
    static void shareImage (Context context , String imagePath){
        // Create the share intent and start the share activity
        File imageFile = new File(imagePath);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri photoURI = FileProvider.getUriForFile(context , FILE_PROVIDER_AUTHORITY , imageFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM , photoURI);
        context.startActivity(shareIntent);
    }


    /**
     * Deletes image file for a given path.
     *
     * @param  context THe application context.
     * @param  imagePath The path of the photo to be deleted.
     */
     static boolean deleteImageFile (Context context , String imagePath) {

         //Get the file
         File imageFile = new File(imagePath);

         //Delete the image
         boolean deleted = imageFile.delete();
         if (!deleted) {
             String errorMessage = context.getString(R.string.error);
             Toast.makeText(context , errorMessage , Toast.LENGTH_SHORT).show();
         }
         return deleted ;
     }


}
