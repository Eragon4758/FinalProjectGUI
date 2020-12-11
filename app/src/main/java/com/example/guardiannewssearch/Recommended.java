package com.example.guardiannewssearch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Recommended extends AppCompatActivity {
    SQLiteDatabase saved;
    private final static String TAG = "Recommended Activity";
    private final List<Result> results = new ArrayList<>();
    ResultListAdapter recListAdapter = new ResultListAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommended);
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        setListeners();
        loadDataFromDatabase();
        recListAdapter.notifyDataSetChanged();
    }

    private void setListeners(){
        ListView faveList = findViewById(R.id.recommendedList);
        faveList.setAdapter(recListAdapter);
        faveList.setOnItemClickListener(((parent, view, position, id) -> {
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
                    .setNegativeButton(getString(R.string.abort), (click, arg) ->{
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
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()) {
            case R.id.choice1:
                startActivity(
                        new Intent(Recommended.this, MainActivity.class)
                );
                break;
            case R.id.choice2:
                startActivity(
                        new Intent(Recommended.this, Favorites.class)
                );
                break;
            case R.id.choice3:
                Toast.makeText(this, R.string.navMessage, Toast.LENGTH_SHORT).show();
                break;
            case R.id.choice4:
                startActivity(
                        new Intent(Recommended.this, Trash.class)
                );
                break;
            case R.id.helpChoice:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.
                        setTitle(R.string.helpPage).
                        setMessage(R.string.recHelp)
                        .setNeutralButton(getString(R.string.ok), (click, arg) -> {
                        })
                        .show();
                break;
        }
        return true;
    }
    protected void loadDataFromDatabase(){
        MyOpener dbOpener = new MyOpener(this);
        saved = dbOpener.getWritableDatabase();
        String [] columns = {MyOpener.COL_ID, MyOpener.COL_TITLE, MyOpener.COL_SECTION, MyOpener.COL_URL};
        Cursor finder = saved.query(false, MyOpener.TABLE_NAME3, columns, null, null, null, null, null, null);
        int IDIndex = finder.getColumnIndex(MyOpener.COL_ID);
        int titleIndex = finder.getColumnIndex(MyOpener.COL_TITLE);
        int sectionIndex = finder.getColumnIndex(MyOpener.COL_SECTION);
        int urlIndex = finder.getColumnIndex(MyOpener.COL_URL);
        while(finder.moveToNext())
        {
            String title = finder.getString(titleIndex);
            String section = finder.getString(sectionIndex);
            String Url = finder.getString(urlIndex);
            long id = finder.getLong(IDIndex);
            results.add(new Result(title, section, Url, id));
        }
        printCursor(finder);
    }
    protected void printCursor( Cursor c ){
        c.moveToFirst();
        Log.v(TAG,"Database version number: " + saved.getVersion());
        Log.v(TAG,"Number of columns in cursor: " + c.getColumnCount());
        Log.v(TAG,"Columns in cursor" + Arrays.toString(c.getColumnNames()));
        Log.v(TAG,"Number of results: " + c.getCount());
        Log.v(TAG,"rows: ");
        while(c.moveToNext()){
            Log.v(TAG,c.getString(0) + " | " + c.getString(1)+" | " + c.getString(2));
        }
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
}