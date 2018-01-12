package com.example.idarkduck.shopify_backend;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.Map;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements Runnable {

    String bodyResponse1;
    String bodyResponse2;
    String bodyResponse3;

    String address1 = "https://backend-challenge-summer-2018.herokuapp.com/challenges.json?id=1&page=1";
    String address2 = "https://backend-challenge-summer-2018.herokuapp.com/challenges.json?id=1&page=2";
    String address3 = "https://backend-challenge-summer-2018.herokuapp.com/challenges.json?id=1&page=3";


    OkHttpClient client;

    TextView test;

    ArrayList<String> id;
    ArrayList<Menu> menus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bodyResponse1="";
        client =  new OkHttpClient();
        test = (TextView) findViewById(R.id.textView);

        DownloadFilesTask download = new DownloadFilesTask();
        download.execute(address1);

        id = new ArrayList<>();
    }

    // Validates graph on a thread.
    public void run() {

    }


    //https://stackoverflow.com/questions/7646392/convert-string-to-int-array-in-java
    private void readJson(String bodyResponse) {
        try {
            JSONObject jsonObj = new JSONObject(bodyResponse);

            JSONArray arr = jsonObj.getJSONArray("menus");


            for (int i = 0; i < arr.length(); i++) {
                //create entire object of JSON file at index i
                JSONObject o = arr.getJSONObject(i);
                //find the keys associated
                String ID = o.getString("id");
                String data = o.getString("data");
                int parentID = -1;

                if (o.length() == 4)
                    parentID = o.getInt("parent_id");

                ArrayList<Integer> childList = new ArrayList<>();

                // Get ids.
                JSONArray arrChildJson = o.getJSONArray("child_ids");

                // Parse child id data.
                if (arrChildJson != null) {
                    String[] arrID = parseJsonArray(arrChildJson);

                    for (int j = 0; j < arrID.length; j++) {
                        if (!arrID[j].isEmpty())
                            childList.add(Integer.parseInt(arrID[j]));
                    }
                }

                // Update menu.
                Menu newMenu = new Menu(ID, data, parentID, childList);
                menus.add(newMenu);
            }
        }
        catch(JSONException e){
            test.setText("Failed: "+ e);
        }
    }

    private String[] parseJsonArray(JSONArray jsonArray) {
        String arrBefore = jsonArray.toString();
        return arrBefore.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
    }

    private void validateGraphs() {
        Map<String, Integer> visited = new HashMap<>(menus.size());
        Stack<Integer> itemsStack = new Stack<>();
        ArrayList<Integer> roots = new ArrayList<>();
        ArrayList<Integer> childList = null;

        // Find all roots.
        for (int i = 0; i < menus.size(); i++) {
            // Check if root.
            if (menus.get(i).getParentID() == -1)
                roots.add(i);
        }

        System.out.print("roots: " + roots.size());
        System.out.print("roots: " + roots.toString());

        // Validate each graph of the root.
        for (int i = 0; i < roots.size(); i++) {
            // https://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread
            Runnable r = new ValidateGraphTask(roots.get(i));
            new Thread(r).start();
        }
    }

    // Sets the menu of an item id invalid.
    private void setInvalidMenu(int id) {

    }

    // Executes worker thread to validate a graph.
    private class ValidateGraphTask implements Runnable {
        private int key = 0;

        public ValidateGraphTask(int key) {
            this.key = key;
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            // Find first item with child.
            for (int i = 0; i < menus.size(); i++) {
                if (!menus.get(i).childIDList.isEmpty()) {
                    childList = menus.get(i).childIDList;
                    break;
                }
            }

            if (childList != null) {
                int currentID = 0;
                itemsStack.push(currentID);

                // Check children
                do {
                    currentID = itemsStack.pop();

                    // Initialize visited.
                    if (menus.size() < currentID || visited.get(menus.get(currentID)) == null)
                        visited.put(menus.get(currentID).getData(), 0);

                    System.out.println("CID: " + currentID);
                    System.out.println("Visited: " + visited.get(menus.get(currentID).getData()));

                    // Check if child was previously visited.
                    if (visited.get(menus.get(currentID).getData()) > 0) {
                        // Invalid.
                        setInvalidMenu(currentID);
                        System.out.println("Invalid menu: " + currentID);
                        break;
                    }

                    // Push all children.
                    childList = menus.get(currentID).childIDList;
                    System.out.println("CL: " + menus.get(currentID).toString());
                    System.out.println("CL: " + childList.toString());
                    for (int i = 0; i < childList.size(); i++) {

                        System.out.println("CL: " + (childList.get(i)));
                        itemsStack.push(childList.get(i) - 1);
                    }

                    System.out.println("stack size: " + itemsStack.size());
                    visited.put(menus.get(currentID).getData(), 1);
                    System.out.println("Visited1: " + visited.get(menus.get(currentID).getData()));
                } while (!itemsStack.empty());
            }

            for (int i = 0; i < menus.size(); i++) {
                System.out.println("item: " + menus.get(i).getData());
                System.out.println("visited: " + visited.get(menus.get(i)));
            }
        }
    }

    private class DownloadFilesTask extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... urls) {
            Downloader d1 = new Downloader(address1);
            Downloader d2 = new Downloader(address2);
            Downloader d3 = new Downloader(address3);

            //added for progress bar
            for (int i = 0; i < 100; i++) {
                publishProgress(i);
            }
            //
            bodyResponse1 = d1.getBodyRespose();
            bodyResponse2 = d2.getBodyRespose();
            bodyResponse3 = d3.getBodyRespose();

            return d1.getBodyRespose();
        }

        //Maybe need to be implemented to avoid crashes
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            //test.setText(" Loading ... ");
            //progressBar.setProgress(progress[0]);
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //showDialog("Downloaded " + result + " bytes");
            if (result != null) {
                menus = new ArrayList<Menu>();
                readJson(bodyResponse1);
                readJson(bodyResponse2);
                readJson(bodyResponse3);
                test.setText(menus.toString());
                validateGraphs();
            }
        }

        //not needed actually but who knows
        protected void onPreExcecute() {
            super.onPreExecute();
            //progressBar.setMax(100);
        }
    }
}


