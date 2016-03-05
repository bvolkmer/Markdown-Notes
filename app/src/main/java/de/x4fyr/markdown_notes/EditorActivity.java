package de.x4fyr.markdown_notes;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity {

    private Note note;
    private File folder;

    private final Context mainContext = this;
    private Toolbar actionbar;
    private EditText filenameEditText;

    private ViewPager mPager;

    private Fragment viewFragment;
    private Fragment editorFragment;
    public final EditorWatcher editorWatcher = new EditorWatcher();

    private boolean filenameChangeEditorAction (TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO){// && event.getAction() == KeyEvent.ACTION_DOWN) {
            File newFile = new File(folder.getAbsolutePath() + "/" + filenameEditText.getText().toString().trim());
            if (note.file.compareTo(newFile) != 0 && newFile.exists() ) {
                Toast.makeText(mainContext, "File or Folder with that name already exists.", Toast.LENGTH_SHORT).show();
                filenameEditText.setText(note.file.getName());
            } else if (! newFile.getParentFile().exists()) {
                Toast.makeText(mainContext, "Folder " + newFile.getAbsolutePath() + " does not exist.", Toast.LENGTH_SHORT).show();
                filenameEditText.setText(note.file.getName());
            } else { // File is valid
                if ( note.file.exists()) {
                    if (note.file.compareTo(newFile) == 0) {
                        Toast.makeText(mainContext, "Same filename entered.", Toast.LENGTH_SHORT).show();
                    } else if (note.file.renameTo(newFile)) {
                        Toast.makeText(mainContext, "File successfully moved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mainContext, "File could not be moved", Toast.LENGTH_SHORT).show();
                        filenameEditText.setText(note.file.getName());
                    }
                } else {  // This is a new note, and must be created.
                    try {
                        if (newFile.createNewFile()) {
                            note.file = newFile;
                            Toast.makeText(mainContext, "File successfully created", Toast.LENGTH_SHORT).show();
                            actionbar.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                            actionbar.requestLayout();
                        } else {
                            Toast.makeText(mainContext, "File could not be created", Toast.LENGTH_SHORT).show();
                            filenameEditText.setText(note.file.getName());
                        }
                    } catch (IOException e) {
                        Toast.makeText(mainContext, "File could not be created", Toast.LENGTH_SHORT).show();
                        filenameEditText.setText(note.file.getName());
                    }
                }
            }
            //TODO: Kick out of EditText
        }
        return true;

    }
    private final EditText.OnEditorActionListener filenameEditTextListener = this::filenameChangeEditorAction;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        actionbar = (Toolbar) findViewById(R.id.actionbar_toolbar);
        filenameEditText = (EditText) findViewById(R.id.filename_editText);

        setSupportActionBar(actionbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Handle Intents
        Intent intent = getIntent();
        File noteFile = (File) intent.getSerializableExtra("de.x4fyr.markdown_notes.CURRENT_NOTE");
        folder = new File(noteFile.getAbsolutePath());

        //Handle new file
        if (noteFile.isFile()) {
            note = new Note(noteFile);
            //TODO: Show fragments
        } else if (noteFile.isDirectory()) {
            //TODO: Do transitions
            note = new Note();
            actionbar.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
            actionbar.requestLayout();
        }

        //Populate ViewPager
        viewFragment = new ViewFragment();
        editorFragment = new EditorFragment();

        mPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        filenameEditText.setText(note.filename);
        filenameEditText.setOnEditorActionListener(filenameEditTextListener);

        Bundle editorArgumentBundle = new Bundle();
        editorArgumentBundle.putString("note_content", note.content);
        editorFragment.setArguments(editorArgumentBundle);

        mPager.addOnPageChangeListener(new onPageChangeListener());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_settings) {
            return true;
        } else if (item.getItemId() == android.R.id.home){
            supportFinishAfterTransition();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        if (note.saveNote()){
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Could not safe note", Toast.LENGTH_SHORT).show();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            supportFinishAfterTransition();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem()-1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter{

        public ScreenSlidePagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return viewFragment;
                case 1:
                    return editorFragment;
                default:
                    throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private class onPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (position == 0) {
                viewFragment.onResume();
            } else if (position == 1){
                editorFragment.onResume();
            }

        }
    }

    public Note getNote() {
        return note;
    }

    private class EditorWatcher extends SimpleTextWatcher{
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            note.changeNoteContent(s.toString());
            //Toast.makeText(mainContext, note.content, Toast.LENGTH_SHORT).show();
        }
    }
}
