package de.x4fyr.markdownnotes;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import in.uncod.android.bypass.Bypass;

/**
 * A placeholder fragment containing a simple view.
 */
public class ViewFragment extends Fragment {

    private TextView preview;
    private EditorActivity editorActivity;
    private Bypass bypass;

    @Override
    public void onAttach(Context context) {
        this.editorActivity = (EditorActivity) context;
        bypass = new Bypass(editorActivity);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view, container, false);
    }

    @Override
    public void onStart() {
        //noinspection ConstantConditions
        preview = (TextView) getView().findViewById(R.id.editor_preview);
        preview.setText(bypass.markdownToSpannable(editorActivity.getNote().getContent()));

        super.onStart();
    }

    @Override
    public void onResume() {
        preview.setText(bypass.markdownToSpannable(editorActivity.getNote().getContent()));
        super.onResume();
    }
}
