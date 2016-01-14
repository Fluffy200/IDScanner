package com.example.fluffy.idscanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class DisplayListActivity extends ActionBarActivity {
    ArrayList<String> data;
    ArrayAdapter<String> myAdapter;
    ListView myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_list);

        Intent intent = getIntent();

        data = intent.getStringArrayListExtra("data");
        myAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        myList=(ListView) findViewById(R.id.rslts);
        myList.setAdapter(myAdapter);

        setTitle("List");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_display_list, menu);
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

    public void updateList(View v){
        Intent intent = this.getIntent();
        ArrayList<String> results = new ArrayList<String>();
        for(Student each: MainActivity.data){
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
        startActivity(this.getIntent());
        this.finish();
    }
}
