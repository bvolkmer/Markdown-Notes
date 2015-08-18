package de.x4fyr.markdown_notes;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private List<Note> notes;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView mCardView;

        protected TextView vFilename;
        protected WebView vContent;

        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
            vFilename = (TextView) v.findViewById(R.id.note_card_filename);
            vContent = (WebView) v.findViewById(R.id.note_card_content);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NoteAdapter(List<Note> notes) {
        this.notes = notes;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_card, parent, false);
        // set the view's size, margins, paddings and layout parameters
        
        ViewHolder vh = new ViewHolder((CardView)v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.vFilename.setText(notes.get(position).filename);
        holder.vContent.loadData(notes.get(position).content, "text/html", null);
        holder.vContent.setBackgroundColor(Color.TRANSPARENT);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return notes.size();
    }
}