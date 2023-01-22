package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * ConnectionHandler Klasse
 * ist zuständig um neue Verbindungen anzunehmen
 * ist eine Thread extention, da der main Thread im Hauptprogramm für direkte Serverinteraktion bleibt
 */
public class ConnectionHandler extends Thread {

    private boolean running = true;
    private final Userlist User;
    private ServerSocket server;

    /**
     * Constructer für ConnectionHandler
     * @param User die Userliste die beim Serverstart erstellt wird
     */
    public ConnectionHandler(Userlist User){
        this.User = User;
    }

    /**
     * überschreibt die run() funktion aus Thread
     * nimmt die Verbindungen entgegen
     * weist jedem Client einen Thread zu und startet den ClientHandler
     * Socket Exceptions treten auf wenn der Server die Sockets schließt
     */
    @Override
    public void run() {

        System.out.println("Server: waiting for connections");

        try {
            server = new ServerSocket(4711);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (running) {
            Socket client = null;
            try {
                client = server.accept();

                System.out.println("Server: connected to Client " + client.getInetAddress());

                DataInputStream dis = new DataInputStream(client.getInputStream());
                DataOutputStream dos = new DataOutputStream(client.getOutputStream());

                //neuer Client Handler und Thread für User
                ClientHandler clhan = new ClientHandler(client, dis, dos, User);
                Thread t = new Thread(clhan);

                TCPServer.ar.add(clhan);

                t.start();

                //needed cause closing a Socket throws an exception.
            } catch (SocketException f){
                System.out.println("Connection closed by Server");
            }catch (Exception e) {
                if(client != null){
                    try {
                        client.close();
                    } catch (IOException ignored) {
                    }
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * Soll der Server beendet werden, schließt er mit dieser Funktion erst die Sockets
     * @throws IOException wenn Fehler bei der Socket Schließung auftreten
     */
    public void end () throws IOException {
        int i = 0;

        for (ClientHandler ch : TCPServer.ar) {
            User.getSocket(i).close();
            i++;
        }
        running = false;
        server.close();
    }
}
