package de.x4fyr.markdownnotes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.x4fyr.markdownnotes.utils.Note;
import de.x4fyr.markdownnotes.utils.SimpleTextWatcher;

import java.io.File;
import java.io.IOException;

/**
 * Activity handling view and edit of a single note.
 */
public class EditorActivity extends AppCompatActivity {

    private Note note;
    private File folder;

    private final Context mainContext = this;
    private Toolbar actionbar;
    private EditText filenameEditText;

    private ViewPager pager;

    private Fragment viewFragment;
    private Fragment editorFragment;

    final EditorWatcher editorWatcher = new EditorWatcher();

    private final EditText.OnEditorActionListener filenameEditTextListener = this::filenameChangeEditorAction;

    private boolean filenameChangeEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO
            && event.getAction() == KeyEvent.ACTION_DOWN && !event.isCanceled()) {
            File newFile = new File(folder.getAbsolutePath() + "/" + filenameEditText.getText().toString().trim());
            if (note.getFile().compareTo(newFile) != 0 && newFile.exists() ) {
                Toast.makeText(mainContext, R.string.toast_file_folder_exists, Toast.LENGTH_SHORT).show();
                filenameEditText.setText(note.getFile().getName());
            } else if (! newFile.getParentFile().exists()) {
                Toast.makeText(mainContext, String.format(getString(R.string.toast_folder_does_not_exists),
                               newFile.getAbsolutePath()), Toast.LENGTH_SHORT).show();
                filenameEditText.setText(note.getFile().getName());
            } else { // File is valid
                if ( note.getFile().exists()) {
                    if (note.getFile().compareTo(newFile) == 0) {
                        Toast.makeText(mainContext, R.string.toast_same_filename_entered, Toast.LENGTH_SHORT).show();
                    } else if (note.getFile().renameTo(newFile)) {
                        Toast.makeText(mainContext, R.string.toast_file_move_successful, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mainContext, R.string.toast_file_move_unsuccessful, Toast.LENGTH_SHORT).show();
                        filenameEditText.setText(note.getFile().getName());
                    }
                } else {  // This is a new note, and must be created.
                    try {
                        if (newFile.createNewFile()) {
                            note.setFile(newFile);
                            Toast.makeText(mainContext, R.string.toast_file_create_successful,
                                           Toast.LENGTH_SHORT).show();
                            actionbar.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                            actionbar.requestLayout();
                        } else {
                            Toast.makeText(mainContext, R.string.toast_file_create_unsuccessful,
                                           Toast.LENGTH_SHORT).show();
                            filenameEditText.setText(note.getFile().getName());
                        }
                    } catch (IOException exception) {
                        Toast.makeText(mainContext, R.string.toast_file_create_unsuccessful, Toast.LENGTH_SHORT).show();
                        filenameEditText.setText(note.getFile().getName());
                    }
                }
            }
            //TODO: Kick out of EditText
        }
        return true;

    }

    @Override
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
        //noinspection HardCodedStringLiteral
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

        pager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        filenameEditText.setText(note.getFilename());
        filenameEditText.setOnEditorActionListener(filenameEditTextListener);

        Bundle editorArgumentBundle = new Bundle();
        //noinspection HardCodedStringLiteral
        editorArgumentBundle.putString("note_content", note.getContent());
        editorFragment.setArguments(editorArgumentBundle);

        pager.addOnPageChangeListener(new EditorOnPageChangeListener());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_settings) {
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            supportFinishAfterTransition();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        if (note.saveNote()) {
            Toast.makeText(this, R.string.toast_note_saved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.toast_note_not_saved, Toast.LENGTH_SHORT).show();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == 0) {
            supportFinishAfterTransition();
        } else {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
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

    private class EditorOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (position == 0) {
                viewFragment.onResume();
            } else if (position == 1) {
                editorFragment.onResume();
            }

        }
    }

    public Note getNote() {
        return note;
    }

    private class EditorWatcher extends SimpleTextWatcher {
        @Override
        public void onTextChanged(CharSequence string, int start, int before, int count) {
            note.changeNoteContent(string.toString());
            //Toast.makeText(mainContext, note.content, Toast.LENGTH_SHORT).show();
        }
    }
}
