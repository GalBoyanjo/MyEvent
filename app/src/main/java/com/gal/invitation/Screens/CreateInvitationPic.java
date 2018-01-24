package com.gal.invitation.Screens;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gal.invitation.Entities.InvitationPic;
import com.gal.invitation.Entities.User;
import com.gal.invitation.R;
import com.gal.invitation.Utils.MyStringRequest;
import com.gal.invitation.Utils.ScreenUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CreateInvitationPic extends AppCompatActivity {

    public static String systemLanguage;
    private RequestQueue netRequestQueue;
    private final static String url_update_invitation_pic = "http://master1590.a2hosted.com/invitations/updateInvitationPic.php";
    private final static String url_get_invitation_pic = "http://master1590.a2hosted.com/invitations/getUserInvitationPic.php";
    private final static String url_get_invitation_pic_address = "http://master1590.a2hosted.com/invitations/images/";
    private final static String TAG_SUCCESS = "success";
    private User user = null;
    private String userType = null;
    private ProgressDialog progressDialog;

    ImageView imgEventPic;
    Button addPic;
    Bitmap eventPic;
    String eventPicBytes = "";
    String eventPicPath = "";
    private static int RESULT_LOAD_IMG = 1, CAMERA = 2;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private boolean hasWRITE_EXTERNAL_STORAGEPermission = false;
    private boolean hasCAMERAPermission = false;
    private static final String IMAGE_DIRECTORY = "/Invitation";
    private InvitationPic userInvitationPic;

    View view;


    Bitmap out;
    Bitmap decodedByte;

    Boolean noPic = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtil.setLocale(CreateInvitationPic.this, getString(R.string.title_activity_create_invitations));
        setContentView(R.layout.activity_create_invitation_pic);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        netRequestQueue = Volley.newRequestQueue(this);

        user = (User) getIntent().getSerializableExtra("user");
        userType = getIntent().getStringExtra("userType");


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_invitation));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        getUserInvitation();


        hasWRITE_EXTERNAL_STORAGEPermission = ContextCompat.checkSelfPermission(CreateInvitationPic.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        hasCAMERAPermission = ContextCompat.checkSelfPermission(CreateInvitationPic.this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;


        addPic = (Button) findViewById(R.id.eventAddPic);
        imgEventPic = (ImageView) findViewById(R.id.eventPic);

        addPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });



    }

    @Override
    public void onBackPressed(){
        Intent profileIntent = new Intent(CreateInvitationPic.this, Profile.class);
        profileIntent.putExtra("user", user);
        profileIntent.putExtra("userType", userType);
        startActivity(profileIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_invitation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_ok) {
            //updateDB();
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Upload");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.show();
            new UploadImage().execute();

        }

        return true;
    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle(getString(R.string.select_action));
        String[] pictureDialogItems = {
                getString(R.string.select_photo_from_gallery),
                getString(R.string.capture_photo_from_camera)};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                if (hasWRITE_EXTERNAL_STORAGEPermission) {
                                    choosePhotoFromGallery();
                                } else {
                                    requestPermission();
                                }
                                break;
                            case 1:
                                if (hasCAMERAPermission) {
                                    takePhotoFromCamera();
                                } else {
                                    requestPermissionCamera();
                                }
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    private void takePhotoFromCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        try{
//            if(requestCode == RESULT_LOAD_IMG && requestCode == RESULT_OK && null !=data){
//
//                Uri selectedImage = data.getData();
//                String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//
//                //get the cursor
//                Cursor cursor = getContentResolver().query(selectedImage,filePathColumn,null,null,null);
//                //move to first row
//                cursor.moveToFirst();
//
//                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                imgDecodableString = cursor.getString(columnIndex);
//                cursor.close();
//                ImageView imgEventPic = (ImageView)findViewById(R.id.imgEventPic);
//                imgEventPic.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
//
//            }
//            else{
//                Toast.makeText(this,"ERROR picking pic",Toast.LENGTH_SHORT).show();
//            }

        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == RESULT_LOAD_IMG) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    eventPic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    eventPicPath = saveImage(eventPic);
                    Toast.makeText(CreateInvitationPic.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    imgEventPic.setImageBitmap(eventPic);
                    noPic = false;

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(CreateInvitationPic.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            eventPic = (Bitmap) data.getExtras().get("data");
            imgEventPic.setImageBitmap(eventPic);
            eventPicPath = saveImage(eventPic);
            Toast.makeText(CreateInvitationPic.this, "Image Saved!", Toast.LENGTH_SHORT).show();
            noPic = false;
        }
//
//        }catch(Exception e){
//            Toast.makeText(this,"ERROR",Toast.LENGTH_SHORT).show();
//        }
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        byte[] byte_arr = bytes.toByteArray();
        eventPicBytes = Base64.encodeToString(byte_arr, 0);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        } else {
            hasWRITE_EXTERNAL_STORAGEPermission = true;
            choosePhotoFromGallery();
        }

    }

    private void requestPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);

        } else {
            hasCAMERAPermission = true;
            takePhotoFromCamera();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasWRITE_EXTERNAL_STORAGEPermission = true;
                    choosePhotoFromGallery();
                } else {
                    hasWRITE_EXTERNAL_STORAGEPermission = false;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(CreateInvitationPic.this);
                    builder.setTitle(getResources().getString(R.string.required_permission_title));
                    builder.setMessage(getResources().getString(R.string.required_gallery_permission_message));
                    builder.setCancelable(false);
                    builder.setPositiveButton(getResources().getString(R.string.required_permission_ask_again), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            requestPermission();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();

                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasCAMERAPermission = true;
                    takePhotoFromCamera();
                } else {
                    hasCAMERAPermission = false;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(CreateInvitationPic.this);
                    builder.setTitle(getResources().getString(R.string.required_permission_title));
                    builder.setMessage(getResources().getString(R.string.required_camera_permission_message));
                    builder.setCancelable(false);
                    builder.setPositiveButton(getResources().getString(R.string.required_permission_ask_again), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            requestPermissionCamera();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();

                }
                return;
            }
        }

    }

    private void updateDB() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("UserID", String.valueOf(user.getID()));
            if (eventPic != null && eventPicBytes != null && !eventPicBytes.isEmpty()) {
                params.put("Picture", eventPicBytes);
            }
            MyStringRequest request = new MyStringRequest(Request.Method.POST,
                    url_update_invitation_pic, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt(TAG_SUCCESS) == 1) {
                            Toast.makeText(CreateInvitationPic.this,
                                    (getString(R.string.contact_saves_in_db)),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(CreateInvitationPic.this,
                                (getString(R.string.error_saving_contact_in_db)),
                                Toast.LENGTH_LONG).show();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                    Toast.makeText(CreateInvitationPic.this,
                            (getString(R.string.error_saving_contact_in_db)),
                            Toast.LENGTH_LONG).show();

                }
            });
            request.setRetryPolicy(new DefaultRetryPolicy(
                    300000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(CreateInvitationPic.this,
                    (getString(R.string.error_saving_contact_in_db)),
                    Toast.LENGTH_LONG).show();

        }

    }

    class UploadImage extends AsyncTask<String, String, Boolean> {

        protected Boolean doInBackground(String... args) {

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(eventPicPath);
            String fileName = "temp_file_name.jpg";
            if (!sourceFile.isFile()) {
                Log.e("uploadFile", "Source File not exist :" + eventPicPath);
                return false;
            } else {
                try {
                    Map<String,Object> params = new LinkedHashMap<>();
                    params.put("UserID", String.valueOf(user.getID()));
                    StringBuilder postData = new StringBuilder();
                    for (Map.Entry<String,Object> param : params.entrySet()) {
                        if (postData.length() != 0) postData.append('&');
                        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                        postData.append('=');
                        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                    }
                    byte[] postDataBytes = postData.toString().getBytes("UTF-8");
                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(url_update_invitation_pic);

                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);
                    conn.setRequestProperty("USERID", String.valueOf(user.getID()));
                //    conn.getOutputStream().write(postDataBytes);

                    dos = new DataOutputStream(conn.getOutputStream());


                    dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    //dos.writeBytes(postData.toString() + lineEnd);
                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    // send multipart form data necessary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    boolean success = false;

                    if(serverResponseCode == 200) {
                        InputStream in = new BufferedInputStream(conn.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        try {
                            JSONObject jObj = new JSONObject(result.toString());
                            success = jObj.getInt(TAG_SUCCESS) == 1;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                    return success;
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(CreateInvitationPic.this,
                        (getString(R.string.picture_save_success)),
                        Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                Intent profileIntent = new Intent(CreateInvitationPic.this, Profile.class);
                profileIntent.putExtra("user", user);
                profileIntent.putExtra("userType", userType);
                startActivity(profileIntent);
                finish();
            } else {
                Toast.makeText(CreateInvitationPic.this,
                        (getString(R.string.saving_picture_faild_please_try_again)),
                        Toast.LENGTH_LONG).show();
                progressDialog.dismiss();

            }
        }


    }

    private void getUserInvitation() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("UserID", String.valueOf(user.getID()));

            MyStringRequest request = new MyStringRequest(Request.Method.POST,
                    url_get_invitation_pic, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt(TAG_SUCCESS) == 1) {
                            userInvitationPic = new InvitationPic(jsonObject.getString("Picture"));

                            showInvitationPic();
                        } else {
                            progressDialog.dismiss();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(CreateInvitationPic.this,
                                getString(R.string.error_occurred),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                    progressDialog.dismiss();
                    Toast.makeText(CreateInvitationPic.this,
                            getString(R.string.error_occurred),
                            Toast.LENGTH_LONG).show();
                }
            });
            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(CreateInvitationPic.this,
                    getString(R.string.error_occurred),
                    Toast.LENGTH_LONG).show();
        }

    }

    private void showInvitationPic() {

        if (userInvitationPic.getPicture() != null && !userInvitationPic.getPicture().isEmpty()) {
            eventPicBytes = String.valueOf(userInvitationPic.getPicture());
            //set the base64 bitmap image to bitmap into ImageView:
            if (eventPicBytes != null && !eventPicBytes.isEmpty()) {
                //byte[] decodedString = Base64.decode(eventPicBytes, Base64.DEFAULT);
                //eventPic = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                try {
//                    URL url = new URL(url_get_invitation_pic_address+userInvitationPic);
//                    eventPic = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//                    imgEventPic.setImageBitmap(eventPic);
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                // show The Image in a ImageView
                new DownloadImageTask(imgEventPic)
                        .execute(url_get_invitation_pic_address + eventPicBytes);


            }

        }
        progressDialog.dismiss();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
