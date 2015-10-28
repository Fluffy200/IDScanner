package com.example.fluffy.idscanner;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.google.gdata.data.spreadsheet.SpreadsheetEntry;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private TextView ID;
    private TextView firstName;
    private TextView lastName;
    private TextView note;
    private EditText firstIn;
    private EditText lastIn;
    private EditText idIn;
    private ImageView found;
    //private String urlKey = "";
    static ArrayList<Student> data = new ArrayList<Student>();
    static boolean multi;
    private MediaPlayer mediaPlayer;
    android.os.Handler customHandler;
    android.os.Handler dataHandler;

    private String accountName;
    private String scope;

    static String sheetKey;
    static int dlFail;
    static boolean ulFail;
    static boolean pauseUpdate;

    static Context myContext;

    private static final int DOC_CHANGE_REQUEST_CODE = 1;
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 2;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        ID = (TextView)findViewById(R.id.id);
        firstName = (TextView)findViewById(R.id.fName);
        lastName = (TextView)findViewById(R.id.lName);
        idIn = (EditText)findViewById(R.id.student_id);
        firstIn = (EditText)findViewById(R.id.first_name);
        lastIn = (EditText)findViewById(R.id.last_name);
        note = (TextView)findViewById(R.id.note);
        found = (ImageView)(ImageView)findViewById(R.id.image);
        multi = true;
        myContext = this;

        setEditTextFocus(firstIn, false);
        setEditTextFocus(lastIn, false);
        setEditTextFocus(idIn, false);

        dataHandler = new android.os.Handler();

        loadData();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    private Runnable connectFail = new Runnable() {
        public void run() {
            if(sheetKey != null) {
                if (ulFail) {
                    Toast.makeText(getApplicationContext(), "Connection Lost. Data may not have been saved", Toast.LENGTH_SHORT).show();
                    syncData();
                }
                if (dlFail == 1) {
                    Toast.makeText(getApplicationContext(), "Connection Lost. Local data may be out of sync", Toast.LENGTH_SHORT).show();
                    if (data == null) {
                        loadData();
                    }
                    try {
                        FileOutputStream fos = MainActivity.myContext.openFileOutput("save.bin", Context.MODE_PRIVATE);
                        ObjectOutputStream os = new ObjectOutputStream(fos);
                        os.writeObject(data);
                        os.close();
                        fos.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    System.out.println("Writing data...");
                    syncData();
                }
                if (dlFail == 2) {
                    Toast.makeText(getApplicationContext(), "Invalid Document. Please choose another", Toast.LENGTH_SHORT).show();
                    Intent changeDoc = new Intent(myContext, ChangeDocActivity.class);
                    changeDoc.putExtra("account", accountName);
                    changeDoc.putExtra("scope", scope);
                    startActivityForResult(changeDoc, DOC_CHANGE_REQUEST_CODE);
                }
                if(dlFail == 0){
                    syncData();
                }
                updateLocal();
            }

            customHandler.postDelayed(this, 30000);
        }
    };

    public void updateLocal() {
        if (!pauseUpdate) {
            UpdateData ud = new UpdateData(accountName, scope, data);
            ud.execute("test");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        idIn.setFocusable(true);
        firstIn.setFocusable(true);
        lastIn.setFocusable(true);
        idIn.setFocusableInTouchMode(true);
        firstIn.setFocusableInTouchMode(true);
        lastIn.setFocusableInTouchMode(true);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_sync:
                syncData();
                return true;
            case R.id.action_load:
                loadData();
                return true;
            case R.id.action_change_account:
                mGoogleApiClient.clearDefaultAccountAndReconnect();
                return true;
            case R.id.action_change_document:
                Intent changeDoc = new Intent(this, ChangeDocActivity.class);
                changeDoc.putExtra("account", accountName);
                changeDoc.putExtra("scope", scope);
                startActivityForResult(changeDoc, DOC_CHANGE_REQUEST_CODE);
                return true;
            case R.id.action_settings:
                Intent setIntent = new Intent(this, SettingsActivity.class);
                startActivity(setIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void syncData(){
        if(sheetKey == null) {
            getURL();
        } else {
            pauseUpdate = false;
            System.out.println(sheetKey);
            DownloadSheets ds = new DownloadSheets(accountName, scope, data);
            ds.execute("test");
        }
    }

    public void loadData(){
        try
        {
            FileInputStream fis = this.openFileInput("save.bin");
            ObjectInputStream is = new ObjectInputStream(fis);
            data = (ArrayList<Student>) is.readObject();
            is.close();
            fis.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        System.out.println("Loading data...");
    }

    public void getURL(){
        String ret = "";

        try {
            InputStream inputStream = this.openFileInput("key.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int size = inputStream.available();
                char[] buffer = new char[size];

                inputStreamReader.read(buffer);

                inputStream.close();
                ret = new String(buffer);

                if(ret != null && !(ret.equals("") || ret.equals(" "))){
                     System.out.println("sheetKey: " + ret);
                     sheetKey = ret;
                     System.out.println(sheetKey);
                }
                else if(accountName != null && !accountName.equals("")){
                    System.out.println("Load Key fail");
                    Intent changeDoc = new Intent(this, ChangeDocActivity.class);
                    changeDoc.putExtra("account", accountName);
                    changeDoc.putExtra("scope", scope);
                    //startActivityForResult(changeDoc, DOC_CHANGE_REQUEST_CODE);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            if(accountName != null && !accountName.equals("")) {
                System.out.println("Load Key fail");
                Intent changeDoc = new Intent(this, ChangeDocActivity.class);
                changeDoc.putExtra("account", accountName);
                changeDoc.putExtra("scope", scope);
                startActivityForResult(changeDoc, DOC_CHANGE_REQUEST_CODE);
            }
        }
    }

    public void searchById(View v) {
        firstName.setText("First Name");
        lastName.setText("Last Name");
        ID.setText("ID");
        note.setText("Note");
        if (!idIn.getText().toString().equals("")){
            String id = idIn.getText().toString();
            try{
                searchID(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                searchID(0);
            }
        }
        else{
            setEditTextFocus(idIn, true);
        }
    }

    public void searchName(View v){
        firstName.setText("First Name");
        lastName.setText("Last Name");
        ID.setText("ID");
        note.setText("Note");
        if (!firstIn.getText().toString().equals("") && !lastIn.getText().toString().equals("")) {
            String first = firstIn.getText().toString();
            String last = lastIn.getText().toString();
            boolean find = false;
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getFirst().equalsIgnoreCase(first) && data.get(i).getLast().equalsIgnoreCase(last)) {
                    if (!data.get(i).getChecked()){
                        find = true;
                        data.get(i).setChecked(true);
                    }
                    firstName.setText(data.get(i).getFirst());
                    lastName.setText(data.get(i).getLast());
                    ID.setText(data.get(i).getId());
                    note.setText(data.get(i).getNote());

                    System.out.println("Accnt: " + accountName +", " + scope);
                    UpdateSheets us = new UpdateSheets(accountName, scope, i);
                    us.execute("test");
                    //view.invalidate();
                }
            }
            checkFound(find);
        }
        else{
            setEditTextFocus(firstIn, true);
        }
    }

    public void scan(View v){
        if(v.getId()==R.id.scan_button){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode) {
            case DOC_CHANGE_REQUEST_CODE:
                customHandler.post(connectFail);
                data.clear();
                try {
                    if(sheetKey != null) {
                        FileWriter out = new FileWriter(new File(this.getFilesDir(), "key.txt"));
                        //System.out.println(this.getFilesDir());
                        out.write(MainActivity.sheetKey);
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                syncData();
                break;
            case IntentIntegrator.REQUEST_CODE:
                if (resultCode == RESULT_OK){
                    firstName.setText("First Name");
                    lastName.setText("Last Name");
                    ID.setText("ID");
                    note.setText("Note");
                    boolean find = false;
                    IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                    if (scanningResult != null) {
                        String scanContent = scanningResult.getContents();
                        System.out.println(scanContent);
                        try {
                            find = searchID(Integer.parseInt(scanContent));
                        } catch (NumberFormatException e) {
                            find = searchID(0);
                        }
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "No scan data received!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    if (multi && find) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                scan(findViewById(R.id.scan_button));
                            }
                        }, 1000);
                    }
                }
                break;
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    public boolean searchID(int idIn) {
        boolean find = false;
        if (idIn != 0) {
            for (int i = 0; i < data.size(); i++) {
                if (Integer.parseInt(data.get(i).getId()) == idIn) {
                    if (!data.get(i).getChecked()){
                        find = true;
                        data.get(i).setChecked(true);
                    }
                    firstName.setText(data.get(i).getFirst());
                    lastName.setText(data.get(i).getLast());
                    ID.setText(data.get(i).getId());
                    note.setText(data.get(i).getNote());

                    System.out.println("Accnt: " + accountName +", " + scope);
                    UpdateSheets us = new UpdateSheets(accountName, scope, i);
                    us.execute("test");
                    //view.invalidate();
                }
            }
            checkFound(find);
        }
        return find;
    }

    public void checkFound(boolean find){
        if(!find){
            found.setImageResource(R.drawable.x);
            mediaPlayer = MediaPlayer.create(this, R.raw.buzz);
            mediaPlayer.start();
           // mediaPlayer.release();
           // mediaPlayer = null;
        }
        else{
            found.setImageResource(R.drawable.check);
            mediaPlayer = MediaPlayer.create(this, R.raw.ding);
            mediaPlayer.start();
          //  mediaPlayer.release();
          //  mediaPlayer = null;
        }
        //UpdateSheets us = new UpdateSheets(urlKey, data, this);
        //us.execute("test");
    }

    public void displaySheet(View v){
        Intent intent = new Intent(this, DisplayListActivity.class);
        ArrayList<String> results = new ArrayList<String>();
        for(Student each: data){
            String dat;
            if(each.getChecked()){
                 dat = "Y - " + each.getId() + " - " + each.getLast() + ", " + each.getFirst();
            }
            else {
                dat = "N - " + each.getId() + " - " + each.getLast() + ", " + each.getFirst();
            }
            if(each.getNote() != null){
                dat += " (" + each.getNote() + ")";
            }
            results.add(dat);
        }
        intent.putExtra("data", results);
        startActivity(intent);
    }

    public void setEditTextFocus(EditText searchEditText, boolean isFocused)
    {
        searchEditText.setCursorVisible(isFocused);
        searchEditText.setFocusable(isFocused);
        searchEditText.setFocusableInTouchMode(isFocused);
        InputMethodManager inputManager = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        if (isFocused) {
            searchEditText.requestFocus();
            inputManager.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        } else {
            inputManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS );
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("sheettest", "GoogleApiClient connection failed: ");
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //Log.i(TAG, "API client connected.");

        accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
        scope = "oauth2:" + Scopes.PLUS_LOGIN + " " + "https://spreadsheets.google.com/feeds"; // add more scopes here

        syncData();

        customHandler = new android.os.Handler();
        customHandler.postDelayed(connectFail, 5000);
    }

    @Override
    public void onConnectionSuspended(int test){
    }
}
