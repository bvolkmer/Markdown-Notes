package de.x4fyr.markdown_notes;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.logging.Logger;

/**
 * A placeholder fragment containing a simple view.
 */
public class ViewFragment extends Fragment {

    WebView preview;
    EditorActivity editorActivity;

    public ViewFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        this.editorActivity = (EditorActivity) context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view, container, false);
    }

    @Override
    public void onStart() {
        preview = (WebView) getView().findViewById(R.id.editor_preview);
        preview.loadData(editorActivity.getNote().formatedContent, "text/html", null);
        WebSettings previewWebSettings = preview.getSettings();
        previewWebSettings.setJavaScriptEnabled(true);

        super.onStart();
    }

    @Override
    public void onResume() {
        preview.loadData(editorActivity.getNote().formatedContent, "text/html", null);
        WebSettings previewWebSettings = preview.getSettings();
        previewWebSettings.setJavaScriptEnabled(true);
        super.onResume();
    }
}
