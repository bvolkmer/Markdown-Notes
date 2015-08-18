package de.x4fyr.markdown_notes;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Note> notes = new ArrayList<Note>();
    private File folder = new File("/sdcard");

    protected TextView locationTextView;

    protected ArrayList<Note> getNotesFromFolder(File folder){
        ArrayList<Note> notes = new ArrayList<Note>();

        class MarkdownFileFilter implements FileFilter {
            public MarkdownFileFilter() {};
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
        }

        return notes;
    }

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    protected void createCards(ArrayList<Note> notes){


        mRecyclerView = (RecyclerView) findViewById(R.id.note_card_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new NoteAdapter(notes);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationTextView = (TextView) findViewById(R.id.location);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationTextView.setText("Location: " + folder.getAbsolutePath());
        notes = getNotesFromFolder(folder);
        createCards(notes);

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

    public void addItem(View view){

    }
}
