package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

public class Server {
    public static final int PORT = 8191;
    public Vector<ClientHandler> allClients;

    public synchronized boolean isLoginBusy(String s) {
        for (ClientHandler client : allClients)
            if (client.getUserName().equalsIgnoreCase(s)) return true;
        return false;
    }



    public HashMap<String, String> getUserMap() {
        return userMap;
    }

    private HashMap<String, String> userMap;

    public static void main(String[] args) {
        new Server().start();
    }

    private void start() {
        userMap = new HashMap<>();
        defaultUser();
        allClients = new Vector<>();

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.err.println("Сервер запущен, ожидаем подключение");
            while (true) {
                Socket socket = server.accept();
                allClients.add(new ClientHandler(Server.this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Сервер отключен");
        }


    }

    public synchronized void sentAllUsersNotMe(String msg) throws IOException {
        for (ClientHandler client : allClients) {
            if (!msg.contains(client.getUserName()))client.sentMessage(msg);
        }
    }

    public synchronized void sentAllUsers(String msg) throws IOException {
        for (ClientHandler client : allClients) {
            client.sentMessage(msg);
        }
    }

    public synchronized void sentUser(String nameTo, String msg) throws IOException {
        for (ClientHandler client : allClients) {
            if (nameTo.equalsIgnoreCase(client.getUserName())) {
                client.sentMessage(msg);
            }
        }
    }

    private void defaultUser() {
        userMap.put("Vasia", "123");
        userMap.put("Dima", "123");
        userMap.put("Kolia", "123");
        userMap.put("Petia", "123");
        userMap.put("Vova", "123");
        userMap.put("Olia", "123");
        userMap.put("Katia", "123");
        userMap.put("Marina", "123");
    }


}
