package com.example.fluffy.idscanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GetURLActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_url);

        Intent intent = getIntent();

        String url = intent.getStringExtra("url");
        TextView curURL = (TextView) findViewById(R.id.curURL);
        curURL.setText(url);
        MainActivity.mainAct = false;

        setTitle("Change Source URL");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_get_url, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void typeURL(View v){
        EditText url = (EditText) findViewById(R.id.url);
        if(url.getText().toString().indexOf("docs.google.com/spreadsheets/d/") != -1) {
            writeURL(url.getText().toString());
            finish();
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(), "Invalid URL", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void scan(View v){
        if(v.getId()==R.id.scan){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK) {
            if (scanningResult != null) {
                String scanContent = scanningResult.getContents();
                if (scanContent.indexOf("docs.google.com/spreadsheets/d/") != -1) {
                    writeURL(scanContent);
                    //System.out.println("done");
                    MainActivity.mainAct = true;
                    finish();
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Invalid URL", Toast.LENGTH_SHORT);
                    toast.show();
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "No scan data received!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    public void writeURL(String url){
        String useURL = url.replace("https://docs.google.com/spreadsheets/d/","");
        useURL = useURL.substring(0, useURL.lastIndexOf("/"));
        //System.out.println(useURL);
        try {
            FileWriter out = new FileWriter(new File(this.getFilesDir(), "url.txt"));
            //System.out.println(this.getFilesDir());
            out.write(useURL);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
//docs.google.com/spreadsheets/d/","spreadsheets.google.com/feeds/list/1jSrd4MfxVkHjb8chwOy4ovBZHnttb8TRW3sb_h2R1i8/default/public/values