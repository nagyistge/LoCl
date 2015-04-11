package locl.experiment.naivecode.localstore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by root on 11/4/15.
 */
public class UploadActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "UploadActivity";
    private String mServerIP;
    TextView messageText;
    Button uploadButton;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;

    String upLoadServerUrl = null;
    private final int SELECT_PHOTO = 1;

    /**********  File Path *************/
    final String uploadFilePath =null;
    final String uploadFileName = "service_lifecycle.png";
    private Button imagePickButton;
    private ImageView imageView;
    private File mUploadFile;
    private String mImagePath;
    private String mImageFolder;
    private String mImageName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        mServerIP = i.getStringExtra(MainActivity.SERVER_IP);
        upLoadServerUrl = "http://" + mServerIP + ":5000/uploadMobile";
        setContentView(R.layout.upload_to_server);
        initUIElements();

        messageText.setText("Uploading file path :- "+uploadFileName);
    }

    private void initUIElements() {
        uploadButton = (Button)findViewById(R.id.bUploadButton);
        uploadButton.setOnClickListener(this);
        messageText  = (TextView)findViewById(R.id.tvMessageText);
        imagePickButton = (Button) findViewById(R.id.bPickImage);
        imagePickButton.setOnClickListener(this);
        imageView = (ImageView)findViewById(R.id.ivImageToUpload);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bUploadButton:
                dialog = ProgressDialog.show(UploadActivity.this, "", "Uploading file...", true);

                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                messageText.setText("uploading started.....");
                            }
                        });

                        //uploadFile(uploadFilePath + "" + uploadFileName);
                        postImage();

                    }
                }).start();
                break;
            case R.id.bPickImage:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);

                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        mUploadFile = new File(getPath(imageUri));

                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }

    private void postImage(){

        try
        {
            HttpClient client = new DefaultHttpClient();
            Log.d(TAG,"URL POST is : " + upLoadServerUrl);

            HttpPost post = new HttpPost(upLoadServerUrl);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);


//            entityBuilder.addTextBody(USER_ID, userId);
            entityBuilder.addTextBody("ImageFolder", mImageFolder);
//            entityBuilder.addTextBody(TYPE, type);
//            entityBuilder.addTextBody(COMMENT, comment);
//            entityBuilder.addTextBody(LATITUDE, String.valueOf(User.Latitude));
//            entityBuilder.addTextBody(LONGITUDE, String.valueOf(User.Longitude));
//            ContentType contentType;
            Log.d(TAG,"entity builder is " + entityBuilder.toString());
            if(mUploadFile != null )
            {
                Log.d(TAG,"mUploadFile is " + mUploadFile);
                entityBuilder.addBinaryBody("file",mUploadFile, ContentType.DEFAULT_BINARY,mImageName);
            }

            HttpEntity entity = entityBuilder.build();
//            entityBuilder.setMode(HttpMultipartMode.valueOf("image/"));
            post.setEntity(entity);


            HttpResponse response = client.execute(post);

            HttpEntity httpEntity = response.getEntity();

            String result = EntityUtils.toString(httpEntity);

            Log.v("result", result);
            dialog.dismiss();
            messageText.setText("Result : " + result);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        //Get all the names with '/' appended since always forming a pth
        mImagePath = cursor.getString(column_index);
        mImageName = mImagePath.substring(mImagePath.lastIndexOf('/'));
        mImageFolder = mImagePath.substring(0,mImagePath.length() - mImageName.length());
        mImageFolder = mImageFolder.substring(mImageFolder.lastIndexOf('/'));

        Log.d(TAG,"Image Path " + mImagePath + " Image Folder " + mImageFolder + " Image Name" + mImageName);

        return mImagePath;

    }
}

/*
* Error:Execution failed for task ':app:packageDebug'.
> Duplicate files copied in APK META-INF/DEPENDENCIES
  	File 1: /root/.gradle/caches/modules-2/files-2.1/org.apache.httpcomponents/httpcore/4.4.1/f5aa318bda4c6c8d688c9d00b90681dcd82ce636/httpcore-4.4.1.jar
  	File 2: /root/.gradle/caches/modules-2/files-2.1/org.apache.httpcomponents/httpcore/4.4.1/f5aa318bda4c6c8d688c9d00b90681dcd82ce636/httpcore-4.4.1.jar*/
