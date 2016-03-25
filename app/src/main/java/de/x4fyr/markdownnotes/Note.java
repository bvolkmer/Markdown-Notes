package de.x4fyr.markdownnotes;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class representing a note.
 *
 * <p>Actually this doesn't have to be a real note, but can be a folder.
 * This changes in future.</p>
 */
public class Note {
    final String filename;
    File file;
    String content;
    boolean folderDummy;

    /**
     * Constructor for existing notes.
     *
     * @param file file or folder of this note.
     */
    public Note(File file) {
        filename = file.getName();
        this.file = file;
        if (file.isDirectory()) {
            folderDummy = true;
        } else {
            folderDummy = false;
            loadNoteContent(file);
        }
    }

    /**
     * Constructor for new notes.
     */
    public Note() {
        //TODO: Remove me. I'm just for unfinished implementations
        filename = "";
        this.file = new File("");
        content = "";
    }

    private void loadNoteContent(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb2 = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb2.append(line);
                sb2.append(System.lineSeparator());
                line = br.readLine();
            }
            content = sb2.toString();

        } catch (FileNotFoundException exception) {
            content = "";
        } catch (IOException exception) {
            //noinspection HardCodedStringLiteral
            Log.e(this.getClass().toString(), "While reading note: ", exception);
        }
    }

    /**
     * Change the content of the note.
     *
     * @param newContent new content of the note. The old content is going to replaced by this.
     */
    public void changeNoteContent(String newContent) {
        content = newContent;
    }

    /**
     * Save this note to the persistent memory.
     *
     * @return true if successful.
     */
    public boolean saveNote() {
        try (FileWriter fw = new FileWriter(this.file)) {
            fw.write(this.content);
            fw.close();
            return true;
        } catch (IOException exception) {
            //noinspection HardCodedStringLiteral
            Log.e(this.getClass().toString(), "While saving note: ", exception);
            return false;
        }
    }

}
