package com.example.guardiannewssearch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String SEARCH = "SearchActivity";
    private final List<Result> results = new ArrayList<>();
    ResultListAdapter resultListAdapter = new ResultListAdapter();
    SQLiteDatabase saved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadSearch();
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        setListeners();
        MyOpener dbOpener = new MyOpener(this);
        saved = dbOpener.getWritableDatabase();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSearch();
    }

    /**
     * Saves the text entered in #email field in the shared preferences.
     */
    private void saveSearch() {
        EditText search_box = findViewById(R.id.search_box);
        getSharedPreferences(SEARCH, Context.MODE_PRIVATE).edit()
                .putString("lastSearch", search_box.getText().toString())
                .apply();
    }

    /**
     * Loads the text in the email field of shared preferences into the #email field.
     */
    private void loadSearch() {
        EditText email = findViewById(R.id.search_box);
        email.setText(
                getSharedPreferences(SEARCH, Context.MODE_PRIVATE)
                        .getString("lastSearch", "")
        );
    }

    private void setListeners() {
        //set listAdapter
        ListView resultList = findViewById(R.id.list);
        resultList.setAdapter(resultListAdapter);

        //set action for the search button
        findViewById(R.id.search_button).setOnClickListener(v -> {
            results.clear();
            EditText text = findViewById(R.id.search_box);
            String searchTerm = text.getText().toString();
            searchTerm = searchTerm.trim();
            searchTerm = searchTerm.replaceAll("\\s", "+");
            GuardianSearch searcher = new GuardianSearch();
            searcher.execute("https://content.guardianapis.com/search?api-key=1fb36b70-1588-4259-b703-2570ea1fac6a&q=" + searchTerm);
            resultListAdapter.notifyDataSetChanged();
        });
        // Set click listener for each item in the row.
        resultList.setOnItemClickListener(((parent, view, position, id) -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.
                    setTitle(getString(R.string.goToArticle))
                    .setMessage(
                            getString(R.string.title) + results.get(position).getTitle() + "\n" +
                                    getString(R.string.section) + results.get(position).getSection() + "\n"
                    )
                    .setPositiveButton(results.get(position).getUrl(), (click, arg) -> {
                        //open web page here
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(results.get(position).getUrl()));
                        startActivity(browserIntent);
                    })
                    .setNeutralButton(getString(R.string.save), (click, arg) -> {
                        //save to database
                        ContentValues newRowValues = new ContentValues();
                        newRowValues.put(MyOpener.COL_TITLE, results.get(position).getTitle());
                        newRowValues.put(MyOpener.COL_SECTION, results.get(position).getSection());
                        newRowValues.put(MyOpener.COL_URL, results.get(position).getUrl());
                        saved.insert(MyOpener.TABLE_NAME, "NullColumnName", newRowValues);
                    })
                    .setNegativeButton(getString(R.string.abort), (click, arg) -> {
                    })
                    .show();
        }));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choice1:
                Toast.makeText(this, R.string.navMessage, Toast.LENGTH_SHORT).show();
                break;
            case R.id.choice2:
                startActivity(
                        new Intent(MainActivity.this, Favorites.class)
                );
                break;
            case R.id.choice3:
                startActivity(
                        new Intent(MainActivity.this, Recommended.class)
                );
                break;
            case R.id.choice4:
                startActivity(
                        new Intent(MainActivity.this, Trash.class)
                );
                break;
            case R.id.helpChoice:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.
                        setTitle(R.string.helpPage).
                        setMessage(R.string.searchHelp)
                        .setNeutralButton(getString(R.string.ok), (click, arg) -> {
                        })
                        .show();
                break;
        }
        return true;
    }

    private class ResultListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return results.size();
        }

        @Override
        public Object getItem(int position) {
            return results.get(position);
        }

        @Override
        public long getItemId(int position) {
            return results.get(position).getID();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            View newView;
            LayoutInflater inflater = getLayoutInflater();
            newView = inflater.inflate(R.layout.result_layout, parent, false);
            TextView resultName = newView.findViewById(R.id.result);
            resultName.setText(results.get(position).getTitle());
            return newView;
        }
    }

    private class GuardianSearch extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... args) {
            try {//create url object
                ProgressBar bar = findViewById(R.id.progressBar);
                URL url = new URL(args[0]);
                //open the connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                InputStream inStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                publishProgress(50);
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                String result = sb.toString();
                result = result.substring(result.indexOf('['));
                result = result.split("]")[0];
                result = result+"]";
                JSONArray arr = new JSONArray(result);
                bar.setMax(arr.length()*10);
                for (int i = 0; i<arr.length(); i++){
                    JSONObject jObject = arr.getJSONObject(i);
                    String section = jObject.getString("sectionName");
                    String title = jObject.getString("webTitle");
                    String Url = jObject.getString("webUrl");
                    results.add(new Result(title, section, Url, i));
                    if(i%4 == 0){
                        String searchTitle = title.replace("'","''");
                        String Query = "SELECT * FROM " + MyOpener.TABLE_NAME3 + " WHERE " + MyOpener.COL_TITLE + " LIKE '%" + searchTitle + "%'";
                        Cursor cursor = saved.rawQuery(Query,null);
                        if(cursor.getCount() <= 0){
                            ContentValues newRowValues = new ContentValues();
                            newRowValues.put(MyOpener.COL_TITLE, title);
                            newRowValues.put(MyOpener.COL_SECTION, section);
                            newRowValues.put(MyOpener.COL_URL, Url);
                            saved.insert(MyOpener.TABLE_NAME3, "NullColumnName", newRowValues);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
            return null;

        }
        //type 2
        public void onProgressUpdate (Integer ...args)
        {
            ProgressBar bar = findViewById(R.id.progressBar);
            bar.setVisibility(View.VISIBLE);
            bar.setProgress(0);
        }
        public void onPostExecute (String fromDoInBackground){
            ProgressBar bar = findViewById(R.id.progressBar);
            bar.setVisibility(View.INVISIBLE);
            resultListAdapter.notifyDataSetChanged();
        }
    }
}