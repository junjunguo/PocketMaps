package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.map.AndroidDownloader;
import com.junjunguo.pocketmaps.model.util.MyMap;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends Activity {
    private volatile boolean prepareInProgress = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


//
//    /**
//     * download files
//     *
//     * @param area
//     */
//    private void initFiles(String area) {
//        prepareInProgress = true;
//        Variable.getVariable().setCountry(area);
//        new DownloadFiles(Variable.getVariable().getMapsFolder(), Variable.getVariable().getCountry(), downloadURL,
//                this);
//    }
//
//    // TODO get user confirmation to download
//    //         if (AndroidHelper.isFastDownload(this))
//    if (isOnline()) {
//        //            chooseAreaFromRemote();
//    } else {//no internet:
//        //            Button btnDownload = (Button) findViewById(R.id.btn_download_map);
//        //            btnDownload.setVisibility(View.INVISIBLE);
//    }
//
//
//    /**
//     * when an Area (country) is chosen
//     *
//     * @param button     btn download map
//     * @param spinner    list of countries
//     * @param nameList   list of names
//     * @param mylistener MySpinnerListener
//     */
//    private void chooseRemoteArea(Button button, final Spinner spinner, List<String> nameList,
//            final MySpinnerListener mylistener) {
//
//        final Map<String, String> nameToFullName = new TreeMap<String, String>();
//        for (String fullName : nameList) {
//            String tmp = Helper.pruneFileEnd(fullName);
//            if (tmp.endsWith("-gh")) tmp = tmp.substring(0, tmp.length() - 3);
//
//            tmp = AndroidHelper.getFileName(tmp);
//            nameToFullName.put(tmp, fullName);
//        }
//        nameList.clear();
//        nameList.addAll(nameToFullName.keySet());
//        ArrayAdapter<String> spinnerArrayAdapter =
//                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, nameList);
//        spinner.setAdapter(spinnerArrayAdapter);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                Object o = spinner.getSelectedItem();
//                if (o != null && o.toString().length() > 0 && !nameToFullName.isEmpty()) {
//                    String area = o.toString();
//                    mylistener.onSelect(area, nameToFullName.get(area));
//                } else {
//                    mylistener.onSelect(null, null);
//                }
//            }
//        });
//    }


    /**
     * download and generate a list of countries from server
     */
    private void downloadList() {
        new AsyncTask<URL, Integer, List<MyMap>>() {

            /**
             * Override this method to perform a computation on a background thread. The specified parameters are the
             * parameters
             * passed to {@link #execute} by the caller of this task.
             * <p>
             * This method can call {@link #publishProgress} to publish updates on the UI thread.
             *
             * @param params The parameters of the task.
             * @return A result, defined by the subclass of this task.
             * @see #onPreExecute()
             * @see #onPostExecute
             * @see #publishProgress
             */
            @Override protected List doInBackground(URL... params) {
                String[] lines = new String[0];
                try {
                    lines = new AndroidDownloader().downloadAsString(Variable.getVariable().getFileListURL())
                            .split("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<MyMap> myMaps = new ArrayList<>();
                for (String str : lines) {
                    int index = str.indexOf("href=\"");
                    if (index >= 0) {
                        index += 6;
                        int lastIndex = str.indexOf(".ghz", index);
                        if (lastIndex >= 0) {
                            int sindex = str.indexOf("right\">", str.length() - 52);
                            int slindex = str.indexOf("M", sindex);
                            myMaps.add(
                                    new MyMap(str.substring(index, lastIndex), str.substring(sindex + 7, slindex + 1)));
                        }
                    }
                }
                return myMaps;
            }
            @Override protected void onPostExecute(List<MyMap> myMaps) {
                //                super.onPostExecute(myMaps);
                listReady(myMaps);
            }
        }.execute();
    }

    /**
     * list of countries are ready
     *
     * @param myMaps
     */
    private void listReady(List<MyMap> myMaps) {
        if (myMaps.isEmpty()) {
            Toast.makeText(this, "There is problem with the server, please report this!", Toast.LENGTH_SHORT).show();
            return;
        }

    }
}
