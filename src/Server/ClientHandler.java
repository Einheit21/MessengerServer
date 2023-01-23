package Server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.Random;

/**
 * Für die allgemeine Kommunikation mit Clients
 * extends Thread weil jeder Client mit einem eigenen Thread verbunden ist
 */
public class ClientHandler extends Thread {
    final Socket client;
    final DataInputStream dis;
    final DataOutputStream dos;
    private final Userlist User;

    /**
     * ClientHandler constructor für neue Clients.
     * @param client der Client Socket
     * @param dis der InputStream
     * @param dos der OutputStream
     * @param User die Userlist
     */
    public ClientHandler(Socket client, DataInputStream dis, DataOutputStream dos, Userlist User) {
        this.client = client;
        this.dis = dis;
        this.dos = dos;
        this.User = User;
    }

    /**
     * Die allgemeine run Methode
     * Nimmt Nachrichten vom Client entgegen und sendet sie an alle anderen Clients bzw an diesen zurück
     * Gibt die Nachrichten auch am Server aus
     * Überprüft außerdem ob es sich um einen Server-Command handelt und leitet an die entsprechende Methode weiter
     */
    @Override
    public void run() {
        try {
            //incoming data
            InputStreamReader inputRead = new InputStreamReader(dis);
            BufferedReader br = new BufferedReader(inputRead);
            LogHandler toLog = new LogHandler();

            while (true) {
                String bufferInput = br.readLine();
                //wenn sich Client disconnected wird der Input null
                if (bufferInput != null) {
                    System.out.printf(currentThread().getName() + ": ");
                    System.out.println(bufferInput);
                    toLog.writeToFile(bufferInput, this.getName());
                    //hier werden Server Befehle abgefangen (erweiterbar)
                    if (bufferInput.startsWith("/")) {
                        bufferInput = ServerCommands(bufferInput);
                        if (Objects.equals(bufferInput, "/logout")) {
                            break;
                        }
                        if (bufferInput != null) {
                            byte[] bmessage = (bufferInput + "\n").getBytes();
                            dos.write(bmessage);
                        }
                    } else {
                        //Nachricht wird an alle die Clients geschickt

                        for (ClientHandler ch : TCPServer.ar) {
                            byte[] bmessage;
                            if (Objects.equals(ch.getName(), this.getName())) {
                                bmessage = ("You: " + bufferInput + "\n").getBytes();
                            } else {
                                bmessage = (currentThread().getName() + ": " + bufferInput + "\n").getBytes();
                            }
                            ch.dos.write(bmessage);
                        }
                    }

                } else {
                    break;
                }
            }
            this.dis.close();
            this.dos.close();
        } catch (SocketException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                this.dis.close();
                this.dos.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Hier werden Server Commands bearbeitet
     * Erst wird der Input zerlegt um nur den Befehl zu überprüfen
     * Dann je nach Befehl abgearbeitet
     * @param input die gesamte Nachricht, beginnend mit /Serverbefehl
     * @return gibt meistens einen String zurück um den jeweiligen Befehl zu bestätigen bzw auf Fehler hinzuweisen
     * @throws IOException wenn es einen Fehler beim Zugriff auf Threads oder Streams gibt
     */
    public String ServerCommands(String input) throws IOException {

        String[] splitinput;
        splitinput = input.split(" ");

        if (Objects.equals(splitinput[0], "/login")) {
            try {
                String address = String.valueOf(client.getInetAddress());
                if (User.getName(address) == null) {
                    boolean acceptable = false;
                    int i = 0;
                    Random a = new Random();
                    int b = a.nextInt() % 10000;
                    while (!acceptable) {
                        for (ClientHandler ch : TCPServer.ar) {
                            if (Objects.equals(splitinput[1], ch.getName())) {
                                splitinput[1] = ("User" + b);
                                acceptable = false;
                                i++;
                                break;
                            } else {
                                acceptable = true;
                            }
                        }
                    }

                    User.addUser(splitinput[1], address, client);
                    currentThread().setName(splitinput[1]);
                    this.setName(splitinput[1]);
                    String userlist = User.toString();
                    byte[] bmessage = ("/Users:" + userlist + "\n").getBytes();

                    for (ClientHandler ch : TCPServer.ar) {
                        ch.dos.write(bmessage);
                    }
                    if (i > 0) {
                        input = ("Your Username was already in use, changed to: " + splitinput[1]);
                    } else {
                        input = "User " + splitinput[1] + " registered successfully";
                    }
                }else{
                    return ("You are already logged in");
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else if (Objects.equals(splitinput[0], "/rename")) {
            String address = String.valueOf(client.getInetAddress());
            try {
                switch (splitinput.length) {
                    case 1 -> {
                        return ("Your username can't be blank");
                    }
                    case 2 -> {
                        String newName = splitinput[1];
                        if (User.getName(address).equals(newName)) {
                            input = splitinput[1] + " is already your current username";
                        } else {
                            boolean acceptable = false;
                            for (ClientHandler ch : TCPServer.ar) {
                                if (Objects.equals(newName, ch.getName())) {
                                    acceptable = false;
                                    break;
                                } else {
                                    acceptable = true;
                                }
                            }
                            if (acceptable) {
                                String oldName = currentThread().getName();
                                User.updateName(address, newName);
                                currentThread().setName(newName);
                                this.setName(newName);

                                String userlist = User.toString();

                                for (ClientHandler ch : TCPServer.ar) {
                                    byte[] bmessage = ("/Users:" + userlist + "\n").getBytes();
                                    ch.dos.write(bmessage);
                                    bmessage = (oldName + " changed their name to: " + newName + "\n").getBytes();
                                    ch.dos.write(bmessage);
                                }
                                input = null;
                            } else {
                                input = "sorry, this name is already taken, please choose another one";
                            }
                        }
                    }
                    default -> input = "please dont use whitespaces in username";
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        } else if (Objects.equals(splitinput[0], "/w")) {
            if (splitinput.length == 1) {
                return ("please name a user you want to whisper to");
            }

            if (Objects.equals(splitinput[1], currentThread().getName())) {
                return ("you dont need this chat to whisper to yourself");
            }

            int offset = splitinput[0].length() + splitinput[1].length() + 2;

            if (splitinput.length == 2) {
                input = " ";
            } else {
                input = (input.substring(offset));
            }

            for (ClientHandler ch : TCPServer.ar) {
                if (Objects.equals(ch.getName(), splitinput[1])) {
                    byte[] bmessage = (currentThread().getName() + " whispers: " + input + "\n").getBytes();
                    ch.dos.write(bmessage);
                    return ("You to " + splitinput[1] + ": " + input);
                }
            }
            return ("no user with name " + splitinput[1] + " found");

        } else if (input.startsWith("/user")) {
            return User.toString();

        } else if (Objects.equals(splitinput[0], "/logout")) {
            User.delUser(currentThread().getName());

            if (User.getNumber() == 0) {
                return null;
            }

            String userlist = User.toString();
            byte[] bmessage = ("/Users:" + userlist + "\n").getBytes();

            for (ClientHandler ch : TCPServer.ar) {
                ch.dos.write(bmessage);
            }
            return "/logout";
        } else if (Objects.equals(splitinput[0], "/log")){
            LogHandler log = new LogHandler();
            input = log.sendLog();
            return input;
        } else if (Objects.equals(splitinput[0], "/?")) {
            input = ("""
                    possible server commands are:
                    /rename to change your name
                    /w [username] to send a private Message
                    /? to show this help""");
            return input;
        } else {
            input = input + " is not a server command, try /? for a list";
        }
        return input;
    }
}