package de.x4fyr.markdown_notes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private final List<Note> notes = new ArrayList<>();
    private final float scale;
    private final MainActivity superActivity;

    public class ViewHolder extends RecyclerView.ViewHolder {
        Note note;

        public final CardView mCardView;
        final TextView vFilename;
        final WebView vContent;
        final WebView vContentPreview;
        final LinearLayout vWebViewLayout;
        final LinearLayout vToolsLayout;
        final TextView vEdit;
        final TextView vDelete;


        private boolean openToolbar(View v) {
            if (!note.folderDummy) {
                if (vToolsLayout.getLayoutParams().height == 0) {
                    vToolsLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                } else {
                    vToolsLayout.getLayoutParams().height = 0;
                }
                vToolsLayout.requestLayout();
            }
            return true;
        }

        private void editItem(View v) {
            if (note.folderDummy) {
                superActivity.changeFolder(note.file);
            } else {
                Intent intent = new Intent(superActivity, EditorActivity.class);
                intent.putExtra("de.x4fyr.markdown_notes.CURRENT_NOTE", note.file);
                superActivity.startActivity(intent);
            }
        }

        private void deleteItem(View v) {
            //TODO: make safety dialog
            if (note.file.delete()) {
                superActivity.onStart();
                Toast.makeText(superActivity, "Deletion successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(superActivity, "Deletion not successful", Toast.LENGTH_SHORT).show();
            }
        }

        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
            vFilename = (TextView) v.findViewById(R.id.note_card_filename);
            vContent = (WebView) v.findViewById(R.id.note_card_content);
            vContentPreview = (WebView) v.findViewById(R.id.note_card_content_preview);
            vWebViewLayout = (LinearLayout) v.findViewById(R.id.webView_Layout);
            vToolsLayout = (LinearLayout) v.findViewById(R.id.tools_layout); //TODO: Use on click
            vEdit = (TextView) v.findViewById(R.id.edit_textView);
            vDelete = (TextView) v.findViewById(R.id.delete_textView);
            //Set onClickListener
            vEdit.setOnClickListener(this::editItem);
            vDelete.setOnClickListener(this::deleteItem);
            mCardView.setOnClickListener(this::editItem);
            mCardView.setOnLongClickListener(this::openToolbar);
        }
    }

    public NoteAdapter(List<Note> notes, Activity superActivity) {
        this.superActivity = (de.x4fyr.markdown_notes.MainActivity) superActivity;
        for (File file: this.superActivity.folder.listFiles()) {
            if (file.isDirectory()) {
                this.notes.add(new Note(file));
            }
        }
        DisplayMetrics metrics = new DisplayMetrics();
        Collections.sort(this.notes, (a,b) -> a.file.compareTo(b.file));
        Collections.sort(notes, (a,b) -> a.file.compareTo(b.file));
        this.notes.addAll(notes);
        this.notes.add(0, new Note(this.superActivity.folder.getParentFile()));
        this.superActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        this.scale = metrics.density;
    }

    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_card, parent, false);

        return new ViewHolder((CardView) v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.note = notes.get(position);
        if (position == 0){
            holder.vFilename.setText(".. (Go up)");
        } else {
            holder.vFilename.setText(notes.get(position).filename);
        }
        holder.vToolsLayout.getLayoutParams().height = 0;
        holder.vToolsLayout.requestLayout();
        if (notes.get(position).folderDummy) {
            holder.vWebViewLayout.getLayoutParams().height = 0;
        } else {
            // Set style
            holder.vWebViewLayout.getLayoutParams().height = (int) (70 * scale);
            holder.vWebViewLayout.requestLayout();
            holder.vContentPreview.setInitialScale(50); //TODO: Set scale dynamic on line number
            // Set formattedContent
            holder.vFilename.setText(notes.get(position).filename);
            holder.vContent.loadData(notes.get(position).formattedContent, "text/html", null);
            holder.vContentPreview.loadData(notes.get(position).formattedContent, "text/html", null);
            // Set WebView settings
            holder.vContent.setBackgroundColor(Color.TRANSPARENT);
            holder.vContentPreview.setBackgroundColor(Color.TRANSPARENT);
            WebSettings vContentWebSettings = holder.vContent.getSettings();
            WebSettings vContentPreviewWebSettings = holder.vContentPreview.getSettings();
            vContentWebSettings.setJavaScriptEnabled(true);
            vContentPreviewWebSettings.setJavaScriptEnabled(true);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
}