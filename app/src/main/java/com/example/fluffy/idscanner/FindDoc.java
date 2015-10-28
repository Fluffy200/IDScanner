package com.example.fluffy.idscanner;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FindDoc extends AsyncTask<String, Integer, String> {
    private GridView grid;
    TextView current;
    private String accountName;
    private String scope;
    private Context myContext;
    private ArrayList<String> docs;
    private ArrayAdapter<String> myAdapter;
    private ArrayList<String> keys;
    private String query;
    private String currentTitle;

    public FindDoc(Context myContext, GridView grid, TextView current, String accountName, String scope, ArrayList<String> keys, String query)
    {
        this.myContext = myContext;
        this.grid = grid;
        this.current = current;
        this.accountName = accountName;
        this.scope = scope;
        this.query = query;
        this.keys = keys;

        docs = new ArrayList<String>();
    }
    @Override
    protected void onPreExecute() {


    }

    @Override
    protected String doInBackground(String... params) {

        SpreadsheetService service = new SpreadsheetService("spreadsheettest");
        service.setProtocolVersion(SpreadsheetService.Versions.V3);

        try {
            Log.d("sheettest", "acountName: " + accountName + " scope: " + scope);
            String accessToken = GoogleAuthUtil.getTokenWithNotification(myContext, accountName, scope, null);

            service.setHeader("Authorization", "Bearer " + accessToken);
            service.setProtocolVersion(SpreadsheetService.Versions.V3);

            URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
            SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);

            List<SpreadsheetEntry> spreadsheets = feed.getEntries();
            Log.d("sheettest", "size " + spreadsheets.size());

            keys.clear();

            for(int i=0; i < spreadsheets.size(); i++)
            {
                if(query == null || query.equals("") || query.equals(" ")){
                    Log.d("sheettest", "checking for spreadsheets");
                    docs.add(spreadsheets.get(i).getTitle().getPlainText());
                    keys.add(spreadsheets.get(i).getKey());
                    System.out.println(spreadsheets.get(i).getTitle().getPlainText());
                }
                else if(spreadsheets.get(i).getTitle().getPlainText().toLowerCase().contains(query.toLowerCase())){
                    Log.d("sheettest", "checking for spreadsheets");
                    docs.add(spreadsheets.get(i).getTitle().getPlainText());
                    keys.add(spreadsheets.get(i).getKey());
                    System.out.println(spreadsheets.get(i).getTitle().getPlainText());
                }

                if(spreadsheets.get(i).getKey().equals(MainActivity.sheetKey)){
                    currentTitle = spreadsheets.get(i).getTitle().getPlainText();
                }
            }

        }
        catch(Exception e){
            Log.d("sheettest", "spreadsheet service error: " + e.getStackTrace());
            e.printStackTrace();
        };

        return "All Done!";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

    }

    @Override
    protected void onPostExecute(String result) {
        myAdapter = new ArrayAdapter<String>(myContext, android.R.layout.simple_list_item_1, docs);
        grid.setAdapter(myAdapter);
        current.setText("You are currently using: " + currentTitle);
    }
}
