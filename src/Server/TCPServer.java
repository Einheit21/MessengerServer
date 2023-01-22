package Server;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;
import java.util.Vector;

/**
 * Ein Server Programm über das mehrere Clients chatten können
 */
public class TCPServer {
    //Vector weil synchronized - mehrere Threads können nicht gleichzeitig darauf zugreifen.
    static Vector<ClientHandler> ar = new Vector<>();

    /**
     * Hauptprogramm, kümmert sich um direkte Eingaben am Server
     * erstellt einen ConnectionHandler
     * und beinhaltet die Möglichkeit den Server zu beenden
     * @param args Kommandozeilenparameter
     * @throws IOException das starten und schließen des connection Handlers sowie nextLine können exceptions erzeugen
     * */
    public static void main(String[] args) throws IOException {

        Userlist User = new Userlist();
        ConnectionHandler ConnH = new ConnectionHandler(User);
        ConnH.start();

        Scanner s = new Scanner(System.in);
        boolean shutdown = false;
        while(!shutdown){
            System.out.println("input: ");
            String input = s.nextLine();
            System.out.println(input);

            if (Objects.equals(input, "kill")){
                for (ClientHandler ch : TCPServer.ar) {
                    byte[] message= ("Server shuts down, thank you all for chatting \n").getBytes();
                    try {
                        ch.dos.write(message);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                shutdown = true;
            }

        }
        ConnH.end();
    }
}
