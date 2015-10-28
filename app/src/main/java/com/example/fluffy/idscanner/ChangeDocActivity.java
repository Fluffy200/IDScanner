package com.example.fluffy.idscanner;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class ChangeDocActivity extends ActionBarActivity {
    private GridView grid;
    private TextView current;
    private String account;
    private String scope;

    private EditText srchBar;
    private ArrayList<String> keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_doc);
        grid = (GridView)findViewById(R.id.grid);
        current = (TextView) findViewById(R.id.currentDoc);
        srchBar = (EditText)findViewById(R.id.srchBar);
        keys = new ArrayList<String>();

        MainActivity.pauseUpdate = true;

        Intent intent = getIntent();
        account = intent.getStringExtra("account");
        scope = intent.getStringExtra("scope");

        setTitle("Change Document");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(MainActivity.dlFail == 2){
            MainActivity.dlFail = 0;
        }

        FindDoc ds = new FindDoc(this, grid, current, account, scope, keys, null);
        ds.execute("test");

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private View prev;
            private int prevInd = -1;
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                MainActivity.sheetKey = keys.get(position);
                ((TextView)v).setTypeface(Typeface.DEFAULT_BOLD);
                Toast.makeText(getApplicationContext(), ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
                if(prevInd != -1 && prevInd != position){
                    ((TextView)prev).setTypeface(Typeface.DEFAULT);
                    prev = v;
                    prevInd = position;
                }
                if(prev == null && prevInd == -1){
                    prev = v;
                    prevInd = position;
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_change_doc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void searchDocs(View v){
        String query = srchBar.getText().toString();
        FindDoc ds = new FindDoc(this, grid, current, account, scope, keys, query);
        ds.execute("test");
    }
}
