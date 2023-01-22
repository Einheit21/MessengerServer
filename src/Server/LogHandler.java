package Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
/**
 * Erstellt und verwaltet das Log-File
 */
public class LogHandler {

    private final Date dt = new Date();
    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY);
    private final File file = new File ("log_" + dateFormat.format(dt)+".txt");
    private final FileWriter out = new FileWriter(file, true);
    private final BufferedWriter writer = new BufferedWriter(out);

    /**
     * Schreibt die Logs in ein Tagesaktuelles file
     * "Flush" um direkt in die Datei zu schreiben
     * andernfalls würde der BufferedWriter erst beim beenden schreiben.
     * @param message die zu schreibende Nachricht
     * @param username der User der die Nachricht schreibt
     * @throws IOException exceptions beim File-Handling
     */
    public void writeToFile(String message, String username) throws IOException {
        writer.write(username + ": " + message+"\n");
        writer.flush();
    }

    /**
     * erstellt einen neuen LogHandler
     * @throws IOException für Exceptions beim File-Zugriff
     */
    public LogHandler() throws IOException {
    }

    /**
     * close Funktion um die Streams zu schließen
     * @throws IOException wenn der Stream nicht geschlossen werden kann
     */
    public void close() throws IOException {
        writer.close();
        out.close();
    }
}
