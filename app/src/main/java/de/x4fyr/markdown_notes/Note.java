package de.x4fyr.markdown_notes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by x4fyr on 8/4/15.
 */
public class Note {
    protected String filename;
    protected String content;

    public Note(File file) {
        filename = file.getName();
        loadNoteContent(file);
    }

    public Note() {
        filename = "";
        content = "";
    }

    private void loadNoteContent(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }

            content = sb.toString();
        } catch (FileNotFoundException e) { content = "";
        } catch (IOException e) { e.printStackTrace();}
    }
}
