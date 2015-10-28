package com.example.fluffy.idscanner;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;

import org.apache.http.auth.AuthenticationException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UpdateSheets extends AsyncTask<String, Integer, String> {
    private int index;
    private String accountName;
    private String scope;

    public UpdateSheets(String accountName, String scope, int index)
    {
        this.accountName = accountName;
        this.scope = scope;
        this.index = index;
        MainActivity.ulFail = false;
        MainActivity.dlFail = 0;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected String doInBackground(String... params) {
        int i = 0;
        SpreadsheetService service = new SpreadsheetService("spreadsheettest");
        // service.setProtocolVersion(SpreadsheetService.Versions.V3);
        try {
            //String urlString = "https://spreadsheets.google.com/feeds/list/0AsaDhyyXNaFSdDJ2VUxtVGVWN1Yza1loU1RPVVU3OFE/default/public/values";
            // turn the string into a URL

            Log.d("sheettest", "acountName: " + accountName + " scope: " + scope);
            String accessToken = GoogleAuthUtil.getTokenWithNotification(MainActivity.myContext, accountName, scope, null);

            service.setHeader("Authorization", "Bearer " + accessToken);
            service.setProtocolVersion(SpreadsheetService.Versions.V3);

            URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
            SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);

            List<SpreadsheetEntry> spreadsheets = feed.getEntries();
            Log.d("sheettest", "size " + spreadsheets.size());

            for(SpreadsheetEntry entry: spreadsheets){
                if(entry.getKey().equals(MainActivity.sheetKey)){
                    break;
                }
                i++;
            }

            WorksheetFeed worksheetFeed = service.getFeed(spreadsheets.get(i).getWorksheetFeedUrl(), WorksheetFeed.class);
            WorksheetEntry worksheet = worksheetFeed.getEntries().get(0);

            URL listFeedUrl = worksheet.getListFeedUrl();
            ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);

            ListEntry row = listFeed.getEntries().get(index);

            row.getCustomElements().setValueLocal("checkedin", "Y");
            row.update();

        } catch (Exception e) {
            MainActivity.ulFail = true;
            e.printStackTrace();
        }


        return "All Done!";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

    }

    @Override
    protected void onPostExecute(String result) {

    }
}