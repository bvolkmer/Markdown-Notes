package de.x4fyr.markdown_notes;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Note> notes = new ArrayList<Note>();
    public File folder = Environment.getExternalStorageDirectory();

    private Context mainContext = this;

    private EditText locationEditText;

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
    protected EditText.OnEditorActionListener locationEditTextListener = this::changeFolderEditorAction;

    protected ArrayList<Note> getNotesFromFolder(File folder) {
        ArrayList<Note> notes = new ArrayList<Note>();

        class MarkdownFileFilter implements FileFilter {
            public MarkdownFileFilter() {
            }

            public boolean accept(File file) {
                try {
                    return (file.isFile()
                            && file.getName().substring(file.getName().lastIndexOf(".")).equals(".md"));
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

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    protected void createCards(ArrayList<Note> notes){


        mRecyclerView = (RecyclerView) findViewById(R.id.note_card_recycler_view);

        // use this setting to improve performance if you know that changes
        // in formatedContent do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new NoteAdapter(notes, this);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        notes = getNotesFromFolder(folder);
        createCards(notes);
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
