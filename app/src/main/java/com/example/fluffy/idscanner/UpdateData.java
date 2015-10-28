package com.example.fluffy.idscanner;

/**
 * Created by Fluffy on 9/1/2015.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UpdateData extends AsyncTask<String, Integer, String> {
    private ArrayList<Student> data;
    private String accountName;
    private String scope;

    public UpdateData(String accountName, String scope, ArrayList<Student> data)
    {
        this.accountName = accountName;
        this.scope = scope;
        this.data = data;
        MainActivity.dlFail = 0;
        MainActivity.ulFail = false;
    }

    @Override
    protected void onPreExecute() {


    }

    @Override
    protected String doInBackground(String... params) {
        int index = 0;
        ArrayList<Student> tempData = new ArrayList<Student>();

        SpreadsheetService service = new SpreadsheetService("spreadsheettest");
        service.setProtocolVersion(SpreadsheetService.Versions.V3);

        try {
            Log.d("sheettest", "acountName: " + accountName + " scope: " + scope);
            String accessToken = GoogleAuthUtil.getTokenWithNotification(MainActivity.myContext, accountName, scope, null);

            service.setHeader("Authorization", "Bearer " + accessToken);
            service.setProtocolVersion(SpreadsheetService.Versions.V3);

            URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
            SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);

            List<SpreadsheetEntry> spreadsheets = feed.getEntries();
            Log.d("sheettest", "DL - size " + spreadsheets.size() + ", " + MainActivity.sheetKey);

            for(SpreadsheetEntry entry: spreadsheets){
                if(entry.getKey().equals(MainActivity.sheetKey)){
                    break;
                }
                index++;
            }

            WorksheetFeed worksheetFeed = service.getFeed(spreadsheets.get(index).getWorksheetFeedUrl(), WorksheetFeed.class);
            WorksheetEntry worksheet = worksheetFeed.getEntries().get(0);

            URL listFeedUrl = worksheet.getListFeedUrl();
            ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);

            int i = 0;

            for (ListEntry row : listFeed.getEntries()) {

                CustomElementCollection elements = row.getCustomElements();

                String checkIn = elements.getValue("checkedin");

                if (checkIn != null && checkIn.equals("Y")) {
                    data.get(i).setChecked(true);
                }
                else if (data.size() != 0 && checkIn != null && checkIn.equals("N") && data.get(i).getChecked()) {
                    elements.setValueLocal("checkedin", "Y");
                    row.update();
                }

                i++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            MainActivity.dlFail = 1;
        };


        return "All Done!";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

    }

    @Override
    protected void onPostExecute(String result) {

    }
}
