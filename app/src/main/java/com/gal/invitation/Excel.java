package com.gal.invitation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gal.invitation.Entities.Contact;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Interfaces.GeneralRequestCallbacks;
import com.gal.invitation.Utils.ContactUtil;
import com.gal.invitation.Utils.CustomCheckbox;
import com.gal.invitation.Utils.MyStringRequest;
import com.gal.invitation.Utils.NetworkUtil;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class Excel extends AppCompatActivity {

    private final static String url_get_contacts = "";
    private final static String TAG_SUCCESS = "success";

    private ArrayList<Contact> excelContacts = new ArrayList<>();
    boolean hasGuests = false;
    private static final String FILE_DIRECTORY = "/MyEvent";
    public static final int FILE_SELECT_CODE = 1212;

    private RequestQueue netRequestQueue;
    public static String systemLanguage;
    private int requestsStack = 0;
    private User user = null;
    private String userType = null;

    private ProgressDialog progressDialog;

    private TreeSet<Contact> userContacts = new TreeSet<>(new Comparator<Contact>() {
        @Override
        public int compare(Contact contact, Contact contact2) {
            return contact.getName().compareTo(contact2.getName());
        }
    });

    Button btnExcelRead;
    Button btnExcelWrite;
    Button btnExcelOK;

    private LinearLayout checkBoxesContainer;
    private List<Contact> selectedContacts = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excel);

        Toolbar toolbar = findViewById(R.id.excel_toolbar);
        setSupportActionBar(toolbar);

        netRequestQueue = Volley.newRequestQueue(this);

        user = (User) getIntent().getSerializableExtra("user");
        userType = getIntent().getStringExtra("userType");

        checkBoxesContainer = findViewById(R.id.excel_checkboxes_container);


        btnExcelRead = findViewById(R.id.excel_import);
        btnExcelRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent();
                intent.setType("*/*");
                if (Build.VERSION.SDK_INT < 19) {
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent = Intent.createChooser(intent, "Select file");
                } else {
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                startActivityForResult(Intent.createChooser(intent, "Select Excel File"), FILE_SELECT_CODE);
            }
        });

        btnExcelWrite = findViewById(R.id.excel_export);

        if (hasGuests) {
            btnExcelWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExcelWrite();
                }
            });
        }

        btnExcelOK = findViewById(R.id.excel_import_ok);
        btnExcelOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // UploadExcelContacts();
                if(selectedContacts.isEmpty());
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILE_SELECT_CODE: {
                if (resultCode == RESULT_OK && data != null) {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle(getString(R.string.update_data));
                    progressDialog.setCancelable(false);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage(getString(R.string.please_wait));
                    progressDialog.show();
                    ExcelRead(data);
                }
                break;
            }
            default:
                break;
        }
    }

    private void ExcelRead(Intent data){
        try {
            InputStream inputStream = getContentResolver().openInputStream(data.getData());
            if (inputStream==null) {
                System.out.println("Can't get file");
                return;
            }

            System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
            System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
            System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

            Workbook wb = null;
            try {
                wb = new XSSFWorkbook(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Sheet datatypeSheet = wb.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            excelContacts.clear();

            while (iterator.hasNext()) {

                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();

                Contact contact = new Contact();
                int curCell = 0;
                while (cellIterator.hasNext()) {

                    curCell++;

                    Cell currentCell = cellIterator.next();
                    //getCellTypeEnum shown as deprecated for version 3.15
                    //getCellTypeEnum ill be renamed to getCellType starting from version 4.0
                    if (currentCell.getCellTypeEnum() == CellType.STRING) {
                        System.out.print(currentCell.getStringCellValue() + "--");
                    } else if (currentCell.getCellTypeEnum() == CellType.NUMERIC) {
                        System.out.print(currentCell.getNumericCellValue() + "--");
                    }
                    if (curCell == 1){
                        if (!currentCell.getStringCellValue().isEmpty()){
                            contact.setName(currentCell.getStringCellValue());
                        }
                    }
                    if (curCell == 2){
                        if (!currentCell.getStringCellValue().isEmpty()){
                            contact.setPhone(currentCell.getStringCellValue());
                        }
                    }



                }
                System.out.println();

                if ((!contact.getName().isEmpty())&&(!contact.getPhone().isEmpty())) {
                    excelContacts.add(contact);
                }

            }

            selectedContacts.clear();
            selectedContacts.addAll(excelContacts);
            checkBoxesContainer.setVisibility(View.VISIBLE);
            btnExcelOK.setVisibility(View.VISIBLE);
            btnExcelRead.setVisibility(View.GONE);
            btnExcelWrite.setVisibility(View.GONE);

            for (final Contact contact : excelContacts) {


                CustomCheckbox checkBox = new CustomCheckbox(Excel.this);
                checkBox.setText(contact.getName()+" \n"+contact.getPhone());
                checkBox.setChecked(true);

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        if (checked)
                            selectedContacts.add(contact);
                        else
                            selectedContacts.remove(contact);
                    }
                });
                checkBoxesContainer.addView(checkBox);
                progressDialog.dismiss();
            }
//                        excelContacts.size();

        } catch (Exception ex) {
            ex.printStackTrace();
            progressDialog.dismiss();
        }
    }

    private void ExcelWrite(){

        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");



        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook();
        } catch (Exception e) {
            e.printStackTrace();
        }
        XSSFSheet sheet = workbook.createSheet(getString(R.string.guest_list_excel_name));

        int rowNum = 0;
        System.out.println("Creating excel");

        //Enter guest-list to excel

        for (Contact contact : userContacts){
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            Cell cell = row.createCell(colNum++);
            cell.setCellValue((String)contact.getName());

            cell = row.createCell(colNum++);
            cell.setCellValue((String)contact.getPhone());

            cell = row.createCell(colNum++);
            cell.setCellValue((Integer)contact.getStatus());

        }


        try {
            File fileDirectory = new File(
                    Environment.getExternalStorageDirectory() + FILE_DIRECTORY);
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs();
            }
            File file = new File(fileDirectory,getString(R.string.guest_list_excel_name) + Calendar.getInstance()
                    .getTimeInMillis()+ ".xlsx");
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done");
    }

    private void UploadExcelContacts(){
        if (selectedContacts.isEmpty())
            checkFinished();
        else {
            for (Contact contact : selectedContacts) {
                requestsStack++;
                NetworkUtil.updateDB(this, user, contact, new GeneralRequestCallbacks() {
                    @Override
                    public void onSuccess() {
                        requestsStack--;
                        checkFinished();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        requestsStack--;
                        checkFinished();
                        Toast.makeText(Excel.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

    }

    private void checkFinished() {
        if (requestsStack == 0) {
            progressDialog.dismiss();
            finish();
            startActivity(getIntent());
//            Intent guestListIntent = new Intent(ManageGuestList.this, Profile.class);
//            guestListIntent.putExtra("user", user);
//            guestListIntent.putExtra("userType", userType);
//            startActivity(guestListIntent);
//            finish();
        }
    }

    private void getUserContacts() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("UserID", String.valueOf(user.getID()));

            MyStringRequest request = new MyStringRequest(Request.Method.POST,
                    url_get_contacts, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt(TAG_SUCCESS) == 1) {
                            JSONArray ja = jsonObject.getJSONArray("Contacts");
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject jo = ja.getJSONObject(i);
                                Contact tempContact = new Contact();
                                tempContact.setName(jo.getString("Name"));
                                tempContact.setPhone(jo.getString("Phone"));
                                tempContact.setCode(jo.getString("Code"));
                                tempContact.setStatus(jo.getInt("Status"));
                                tempContact.setImage(ContactUtil.retrieveContactPhoto(
                                        tempContact.getPhone(), Excel.this));

                                userContacts.add(tempContact);
                            }
                            hasGuests = true;
                            ExcelWrite();
                        }
                        else{
                            progressDialog.dismiss();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(Excel.this,
                                getString(R.string.error_occurred),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                    progressDialog.dismiss();
                    Toast.makeText(Excel.this,
                            getString(R.string.error_occurred),
                            Toast.LENGTH_LONG).show();
                }
            });
            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(Excel.this,
                    getString(R.string.error_occurred),
                    Toast.LENGTH_LONG).show();
        }

    }
}
