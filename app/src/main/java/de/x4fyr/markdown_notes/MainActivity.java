package de.x4fyr.markdown_notes;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public File folder = Environment.getExternalStorageDirectory();

    private final Context mainContext = this;

    private EditText locationEditText;
    private final EditText.OnEditorActionListener locationEditTextListener = this::changeFolderEditorAction;

    public void changeFolder(File newFolder){
        try {
            if (newFolder.exists() && newFolder.isDirectory()) {
                folder = newFolder;
                createCards(getNotesFromFolder(folder));
                locationEditText.setText(folder.getAbsolutePath());
            } else {
                Toast.makeText(mainContext, "Folder not found", Toast.LENGTH_SHORT).show();
                locationEditText.setText(folder.getAbsoluteFile().toString());
            }
        } catch (Exception e) {
            Toast.makeText(mainContext, "Not possible", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean changeFolderEditorAction (TextView v, int actionId, KeyEvent event){
        if (actionId == EditorInfo.IME_ACTION_GO){// && event.getAction() == KeyEvent.ACTION_DOWN) {
            File newFolder = new File(locationEditText.getText().toString().trim());
            changeFolder(newFolder);
        }
        return true;
    }

    private ArrayList<Note> getNotesFromFolder(File folder) {
        ArrayList<Note> notes = new ArrayList<>();

        class MarkdownFileFilter implements FileFilter {
            public boolean accept(File file) {
                try {
                    return file.isFile()
                            && file.getName().substring(file.getName().lastIndexOf(".")).equals(".md");
                } catch (Exception e) {
                    return false;
                }
            }
        }
        File[] files = folder.listFiles(new MarkdownFileFilter());


        if (files.length != 0) {
            for (File file : files) {
                notes.add(new Note(file));
            }
        } else {
            Toast.makeText(mainContext, "No notes found. \nSearching for files with \".md\" ending.", Toast.LENGTH_SHORT).show();
        }

        return notes;
    }

    private void createCards(ArrayList<Note> notes){


        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.note_card_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        RecyclerView.Adapter mAdapter = new NoteAdapter(notes, this);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String strFolder = sharedPref.getString(getString(R.string.pref_startup_folder_key), "");


        if (!"".equals(strFolder)) {
            folder = new File(strFolder);
            if (!folder.exists() || !folder.isDirectory() || !folder.canRead()) {
                folder = Environment.getExternalStorageDirectory();
            }
        }

        //Make toolbar as actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationEditText = (EditText) findViewById(R.id.location_editText);
        locationEditText.setText(folder.getAbsolutePath());
        locationEditText.setOnEditorActionListener(locationEditTextListener);
        findViewById(R.id.add_button).setOnClickListener(this::addItem);
        findViewById(R.id.note_card_recycler_view).setOnClickListener(this::viewItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        createCards(getNotesFromFolder(folder));
    }

    @Override
    public void onBackPressed() {
        if (folder.getParentFile() == null) {
            super.onBackPressed();
        } else {
            changeFolder(folder.getParentFile());
        }
    }

    public void addItem(View view){
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra("de.x4fyr.markdown_notes.CURRENT_NOTE", folder);
        startActivity(intent);
    }

    public void viewItem(View v){
        Intent intent = new Intent(this, EditorActivity.class);
        String filename = ((TextView) v.findViewById(R.id.note_card_filename)).getText().toString().trim();
        File file = new File(folder.getName() + "/" + filename);
        intent.putExtra("de.x4fyr.markdown_notes.CURRENT_NOTE", file);
        WebView wv_title = ((WebView) v.findViewById(R.id.note_card_content));
        ActivityOptions options  = ActivityOptions.makeSceneTransitionAnimation(this, wv_title, "rendered_view");
        startActivity(intent, options.toBundle());
    }

}
