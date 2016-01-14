package com.example.fluffy.idscanner;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadSheets extends AsyncTask<String, Integer, String> {
    private ArrayList<Student> data;
    private String accountName;
    private String scope;

    public DownloadSheets(String accountName, String scope, ArrayList<Student> data)
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
            boolean docFormat = false;

            URL listFeedUrl = worksheet.getListFeedUrl();
            ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);

            for(String tag: listFeed.getEntries().get(0).getCustomElements().getTags()){
                if(tag.equalsIgnoreCase("customername")){
                    docFormat = true;
                }
            }

            if(docFormat) {
                int i = 0;

                for (ListEntry row : listFeed.getEntries()) {
                    boolean checkedIn = false;

                    CustomElementCollection elements = row.getCustomElements();
                    String name = elements.getValue("customername");
                    String fName = name.substring(name.indexOf(',') + 2, name.length());
                    //System.out.println(fName);
                    String lName = name.substring(0, name.indexOf(','));
                    //System.out.println(lName);
                    String id = elements.getValue("custno");
                    //System.out.println(id);
                    String note = elements.getValue("quantity");
                    //System.out.println(note);
                    String checkIn = elements.getValue("checkedin");
                    //System.out.println(checkI

                    if (checkIn != null && checkIn.equals("Y")) {
                        checkedIn = true;
                        System.out.println(fName);
                    } else if (data.size() != 0 && checkIn != null && checkIn.equals("N") && data.get(i).getChecked()) {
                        elements.setValueLocal("checkedin", "Y");
                        row.update();
                        checkedIn = true;
                        System.out.println(fName);
                    } else if (checkIn == null) {
                        elements.setValueLocal("checkedin", "N");
                        row.update();
                    }

                    tempData.add(new Student(id, fName, lName, note, checkedIn));
                    i++;
                }

                data.clear();
                for (Student each : tempData) {
                    data.add(each);
                }
            }
            else{
                MainActivity.dlFail = 2;
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
        //for (int i = 0; i < data.size(); i++) {
        //    System.out.println(data.get(i).getId());
        //}
        if(MainActivity.dlFail == 0) {
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
        }
    }
}