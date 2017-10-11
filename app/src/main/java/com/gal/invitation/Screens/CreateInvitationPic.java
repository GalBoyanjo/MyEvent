package com.gal.invitation.Screens;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gal.invitation.Entities.InvitationPic;
import com.gal.invitation.Entities.User;
import com.gal.invitation.R;
import com.gal.invitation.Utils.MyStringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateInvitationPic extends AppCompatActivity {

    public static String systemLanguage;
    private RequestQueue netRequestQueue;
    private final static String url_update_invitation_pic = "http://master1590.a2hosted.com/invitations/updateInvitationPic.php";
    private final static String url_get_invitation_pic = "http://master1590.a2hosted.com/invitations/getUserInvitationPic.php";
    private final static String TAG_SUCCESS = "success";
    private User user = null;
    private ProgressDialog progressDialog;

    ImageView imgEventPic;
    Button addPic;
    Bitmap eventPic;
    String eventPicBytes= "";
    private static int RESULT_LOAD_IMG = 1, CAMERA = 2;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private boolean hasWRITE_EXTERNAL_STORAGEPermission = false;
    private boolean hasCAMERAPermission = false;
    private static final String IMAGE_DIRECTORY = "/Invitation";
    private InvitationPic userInvitationPic;

    View view;

    Button effect1;
    Button effect2;
    Button effect3;
    Bitmap out;
    ImageView changed;
    Bitmap decodedByte;

    Boolean noPic = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invitation_pic);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        netRequestQueue = Volley.newRequestQueue(this);

        user = (User) getIntent().getSerializableExtra("user");


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("LOADING INVITATION");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        getUserInvitation();



        addPic =(Button)findViewById(R.id.eventAddPic);
        imgEventPic = (ImageView)findViewById(R.id.eventPic);

        addPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });

        effect1 = (Button)findViewById(R.id.efffectBtn1);
        effect2 = (Button)findViewById(R.id.efffectBtn2);
        effect3 = (Button)findViewById(R.id.efffectBtn3);
        changed= (ImageView)findViewById(R.id.myView);


        effect1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((userInvitationPic.getPicture()!=null && !userInvitationPic.getPicture().isEmpty()) || noPic==false ) {

                    out = addEffect(eventPic, 5, 5.0, 6.0, 0.0);

                    changed.setImageBitmap(out);
                    imgEventPic.setImageBitmap(out);
                }
            }
        });
        effect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((userInvitationPic.getPicture()!=null && !userInvitationPic.getPicture().isEmpty()) || noPic==false ) {

                    out = addEffect(eventPic, 5, 5.0, 0.0, 10.0);

                    changed.setImageBitmap(out);
                    imgEventPic.setImageBitmap(out);
                }
            }
        });
        effect3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((userInvitationPic.getPicture()!=null && !userInvitationPic.getPicture().isEmpty()) || noPic==false ) {

                    out = addEffect(eventPic, 0, 0.0, 0.0, 0.0);

                    changed.setImageBitmap(out);
                    //return to normal:
                    imgEventPic.setImageBitmap(eventPic);
                }
            }
        });




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
                updateDB();
                Intent ProfileIntent = new Intent(CreateInvitationPic.this, Profile.class);
                ProfileIntent.putExtra("user", user);
                startActivity(ProfileIntent);
                finish();
        }

             return true;
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                hasWRITE_EXTERNAL_STORAGEPermission = ContextCompat.checkSelfPermission(CreateInvitationPic.this,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        == PackageManager.PERMISSION_GRANTED;
                                if (hasWRITE_EXTERNAL_STORAGEPermission) {
                                    choosePhotoFromGallery();
                                } else {
                                    requestPermission(view);
                                }
                                break;
                            case 1:
                                hasCAMERAPermission = ContextCompat.checkSelfPermission(CreateInvitationPic.this,
                                        Manifest.permission.CAMERA)
                                        == PackageManager.PERMISSION_GRANTED;
                                if (hasCAMERAPermission) {
                                    takePhotoFromCamera();
                                } else {
                                    requestPermissionCamera(view);
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
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
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
                    String path = saveImage(eventPic);
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
            saveImage(eventPic);
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
        byte [] byte_arr = bytes.toByteArray();
        eventPicBytes = Base64.encodeToString(byte_arr,0);
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

    private void requestPermission(View view) {
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

    private void requestPermissionCamera(View view) {
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
                    choosePhotoFromGallery();
                } else {
                    Toast.makeText(CreateInvitationPic.this,
                            (getString(R.string.SMS_faild_please_try_again)),
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhotoFromCamera();
                } else {
                    Toast.makeText(CreateInvitationPic.this,
                            (getString(R.string.SMS_faild_please_try_again)),
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }

    private void updateDB() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("UserID", String.valueOf(user.getID()));
            if(eventPic!=null && eventPicBytes!=null && !eventPicBytes.isEmpty()) {
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
            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(CreateInvitationPic.this,
                    (getString(R.string.error_saving_contact_in_db)),
                    Toast.LENGTH_LONG).show();

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
                            userInvitationPic =new InvitationPic(jsonObject.getString("Picture"));

                            showInvitationPic();
                        }
                        else{
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

    private void showInvitationPic(){

        if(userInvitationPic.getPicture()!=null && !userInvitationPic.getPicture().isEmpty() ) {
            eventPicBytes = String.valueOf(userInvitationPic.getPicture());
            //set the base64 bitmap image to bitmap into ImageView:
            if (eventPicBytes != null && !eventPicBytes.isEmpty()) {
                byte[] decodedString = Base64.decode(eventPicBytes, Base64.DEFAULT);
                eventPic = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgEventPic.setImageBitmap(eventPic);
            }

        }
        progressDialog.dismiss();
    }


    public Bitmap addEffect(Bitmap src, int depth, double red, double green, double blue) {

        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap finalBitmap = Bitmap.createBitmap(width, height, src.getConfig());

        final double grayScale_Red = 0.3;
        final double grayScale_Green = 0.59;
        final double grayScale_Blue = 0.11;

        int channel_aplha, channel_red, channel_green, channel_blue;
        int pixel;

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {

                pixel = src.getPixel(x, y);
                channel_aplha = Color.alpha(pixel);
                channel_red = Color.red(pixel);
                channel_green = Color.green(pixel);
                channel_blue = Color.blue(pixel);

                channel_blue = channel_green = channel_red = (int)(grayScale_Red * channel_red + grayScale_Green * channel_green + grayScale_Blue * channel_blue);

                channel_red += (depth * red);
                if(channel_red > 255)
                {
                    channel_red = 255;
                }
                channel_green += (depth * green);
                if(channel_green > 255)
                {
                    channel_green = 255;
                }
                channel_blue += (depth * blue);
                if(channel_blue > 255)
                {
                    channel_blue = 255;
                }

                finalBitmap.setPixel(x, y, Color.argb(channel_aplha, channel_red, channel_green, channel_blue));
            }
        }
        return finalBitmap;
    }


    }
