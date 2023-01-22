package Server;

import java.net.Socket;
import java.util.Objects;

/**
 * Klasse Userlist.
 * Hält eine Liste aller aktiven User und verwaltet diese
 */
public class Userlist {

    private int nextID = 0;
    private String[][] userList = new String [1][2];
    private Socket[] socketList = new Socket[1];

    /**
     * Funktion um einen neuen User zur Liste hinzuzufügen
     * ruft die Funktion ensureCapacity auf
     * @param username der Name des Users
     * @param ip die ip des Users
     * @param client der Client Socket des Users
     */
    public void addUser(String username, String ip, Socket client){
        if (Objects.equals(this.userList[0][0], null)){
            userList[0] = new String[]{username, ip};
            socketList[0] = client;
        }else{
            ensureCapacity(this.nextID);
            this.userList[nextID] = new String[]{username, ip};
            this.socketList[nextID] = client;
        }
        nextID++;
    }
    /**
     * Funktion um User zu entfernen.
     * Sucht den User anhand des mitgegebenen Namen und löscht ihn.
     * Verkleinert auch das Array bzw entfernt die Lücke.
     * @param username der zu löschende User
     */
    public void delUser (String username){
        int userIndex = 0;
        if (nextID > 1) {
            String[][] newUserList = new String[nextID - 1][];
            Socket[] newSocketList = new Socket[nextID - 1];
            int j = 0;
            for(int i = 0; i < nextID; i++ ) {
                if (!Objects.equals(userList[i][0], username)) {
                    newUserList[j] = userList[i];
                    newSocketList[j] = socketList[i];
                    j++;
                }else{
                    userIndex = i;
                }
            }
            this.userList = newUserList;
            this.socketList = newSocketList;
            this.nextID--;
        }else {
            this.userList = new String[1][2];
            this.socketList = new Socket[1];
            this.nextID = 0;
        }
        TCPServer.ar.remove(userIndex);
    }
    /**
     * Vergrößert die Arrays um eins bevor ein neuer Datensatz geschrieben werden kann
     * @param length die aktuelle größe des Arrays
     * */
    private void ensureCapacity (int length){
        String[][] newUserList = new String[length+1][];
        Socket[] newSocketList = new Socket[length+1];
        for (int i = 0; i < length; i++){
            newUserList[i] = this.userList[i];
            newSocketList[i] = this.socketList[i];
        }

        this.userList = newUserList;
        this.socketList = newSocketList;
    }
    /**
     * Ausgabe aller Usernamen in einem String.
     * @return gibt einen String aus allen aktiven Usern zurück
     * */
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < nextID; i++) {
            output.append(userList[i][0]).append(",");
        }
        return output.toString();
    }
    /**
     * Ausgabe des Namens eines Users anhand seiner ip
     * @param ip die ip des gesuchten users
     * @return gibt entweder den User zurück oder null wenn nicht gefunden
     * */
    public String getName (String ip){
        for (int i = 0; i < nextID; i++){
            if (this.userList[i][1].equals(ip)){
                return this.userList[i][0];
            }
        }
        return null;
    }
    /**
     * Ausgabe des Sockets mit einem bestimmten index
     * @param i index des gesuchten Sockets
     * @return retourniert den gesuchten Socket
     * */
    public Socket getSocket (int i){
        return this.socketList[i];
    }
    /**
     * aktuelle Anzahl an aktiver Chatter
     * @return gibt die aktuelle Anzahl an Chattern zurück
     * */
    public int getNumber(){
        return nextID;
    }

    /**
     * Ändert den Namen in der Userliste der zu einer bestimmten ip gehört
     * @param ip die ip deren zugehöriger Name geändert werden soll
     * @param name der neue Name der eingetragen werden soll
     */
    public void updateName (String ip, String name){
        for (int i = 0; i < nextID; i++){
            if (this.userList[i][1].equals(ip)){
                this.userList[i][0] = name;
            }
        }
    }
}
