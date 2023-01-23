package Server;

import com.sun.jdi.IntegerValue;

import java.io.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
/**
 * Erstellt und verwaltet das Log-File
 */
public class LogHandler {

    private final Date dt = new Date();
    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY);
    private final File file = new File("log_" + dateFormat.format(dt) + ".txt");

    /**
     * erstellt einen neuen LogHandler
     *
     * @throws IOException für Exceptions beim File-Zugriff
     */
    public LogHandler() throws IOException {
    }

    /**
     * Schreibt die Logs in ein Tagesaktuelles file
     * "Flush" um direkt in die Datei zu schreiben
     * andernfalls würde der BufferedWriter erst beim beenden schreiben.
     *
     * @param message  die zu schreibende Nachricht
     * @param username der User der die Nachricht schreibt
     * @throws IOException exceptions beim File-Handling
     */
    public void writeToFile(String message, String username) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime t = LocalTime.now();
        if (message.startsWith("/")) {
            writer.write("S");
        }
        writer.write(dtf.format(t) + " " + username + ": " + message + "\n");
        writer.flush();

        writer.close();
    }

    /**
     * hier wird das Logfile ausgegeben und an den User geschickt
     * @return den Log als String ohne Server commands
     * @throws IOException wenn es Probleme beim File-Handling gibt
     */
    public String sendLog() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String output = "Last hour log:";
        LocalTime t = LocalTime.now();
        int h = t.getHour();
        int m = t.getMinute();
        int mc;
        String buffer;
        while ((buffer = reader.readLine()) != null) {
            if(buffer.length()>5 && !buffer.startsWith("S")) {
                mc = Integer.parseInt(buffer.substring(3, 5));
                if ((buffer.startsWith(String.valueOf(h - 1)) && mc > m) || buffer.startsWith(String.valueOf(h))) {
                    output = output + ("\n") + (buffer);
                }
            }
        }
        if (output.equals("Last hour log:")){
            output = "There are no entries from the past hour";
        }
        reader.close();
        return output;
    }
}
