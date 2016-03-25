package de.x4fyr.markdownnotes;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import in.uncod.android.bypass.Bypass;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for populating a RecyclerView with Note instances.
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private final List<Note> notes = new ArrayList<>();
    private final float scale;
    private final MainActivity superActivity;

    private static final int WEB_VIEW_HEIGHT = 70;

    /**
     * ViewHolder of this for this Adapter.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        Note note;

        public final CardView cardView;
        final TextView filename;
        final TextView content;
        final TextView contentPreview;
        final LinearLayout webViewLayout;
        final LinearLayout toolsLayout;
        final TextView edit;
        final TextView delete;

        private boolean openToolbar(View view) {
            if (!note.folderDummy) {
                if (toolsLayout.getLayoutParams().height == 0) {
                    toolsLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                } else {
                    toolsLayout.getLayoutParams().height = 0;
                }
                toolsLayout.requestLayout();
            }
            return true;
        }

        private void editItem(View view) {
            if (note.folderDummy) {
                superActivity.changeFolder(note.file);
            } else {
                Intent intent = new Intent(superActivity, EditorActivity.class);
                //noinspection HardCodedStringLiteral
                intent.putExtra("de.x4fyr.markdown_notes.CURRENT_NOTE", note.file);
                superActivity.startActivity(intent);
            }
        }

        private void deleteItem(View view) {
            //TODO: make safety dialog
            if (note.file.delete()) {
                superActivity.onStart();
                Toast.makeText(superActivity, R.string.toast_deletion_successful, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(superActivity, R.string.toast_deletion_unsuccessful, Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Constructor.
         *
         * @param view Reference to the view.
         */
        public ViewHolder(CardView view) {
            super(view);
            cardView = view;
            filename = (TextView) view.findViewById(R.id.note_card_filename);
            content = (TextView) view.findViewById(R.id.note_card_content);
            contentPreview = (TextView) view.findViewById(R.id.note_card_content_preview);
            webViewLayout = (LinearLayout) view.findViewById(R.id.webView_Layout);
            toolsLayout = (LinearLayout) view.findViewById(R.id.tools_layout); //TODO: Use on click
            edit = (TextView) view.findViewById(R.id.edit_textView);
            delete = (TextView) view.findViewById(R.id.delete_textView);
            //Set onClickListener
            edit.setOnClickListener(this::editItem);
            delete.setOnClickListener(this::deleteItem);
            cardView.setOnClickListener(this::editItem);
            cardView.setOnLongClickListener(this::openToolbar);
        }
    }

    /**
     * Constructor.
     *
     * @param notes Notes to handle with this Adapter.
     * @param superActivity Reference to the containing Activity.
     */
    public NoteAdapter(List<Note> notes, Activity superActivity) {
        this.superActivity = (de.x4fyr.markdownnotes.MainActivity) superActivity;
        for (File file: this.superActivity.folder.listFiles()) {
            if (file.isDirectory()) {
                this.notes.add(new Note(file));
            }
        }
        Collections.sort(this.notes, (first,second) -> first.file.compareTo(second.file));
        Collections.sort(notes, (first,second) -> first.file.compareTo(second.file));
        this.notes.addAll(notes);
        this.notes.add(0, new Note(this.superActivity.folder.getParentFile()));
        DisplayMetrics metrics = new DisplayMetrics();
        this.superActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        this.scale = metrics.density;
    }

    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_card, parent, false);

        return new ViewHolder((CardView) view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.note = notes.get(position);
        if (position == 0) {
            holder.filename.setText(R.string.toast_one_folder_up);
        } else {
            holder.filename.setText(notes.get(position).filename);
        }
        holder.toolsLayout.getLayoutParams().height = 0;
        holder.toolsLayout.requestLayout();
        if (notes.get(position).folderDummy) {
            holder.webViewLayout.getLayoutParams().height = 0;
        } else {
            // Set style
            holder.webViewLayout.getLayoutParams().height = (int) (WEB_VIEW_HEIGHT * scale);
            holder.webViewLayout.requestLayout();
            // Set formattedContent
            holder.filename.setText(notes.get(position).filename);
            Bypass bypass = new Bypass(superActivity);
            CharSequence spannableContent = bypass.markdownToSpannable(notes.get(position).content);
            holder.content.setText(spannableContent);
            holder.contentPreview.setText(spannableContent);
            holder.contentPreview.setHorizontallyScrolling(true);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
}