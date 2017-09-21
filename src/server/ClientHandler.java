package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler {

    public static final String MESSAGE = "Message:";
    public static final String MESSAGE_USER = "messageUser";
    public static final String SYSTEM = "System:";
    public static final String ADD_USER = "Вошел: ";
    public static final String DELETE_USER = "Покинул чат: ";


    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String userName = "";

    public String getUserName() {
        return userName;
    }

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    if (userAuth()) objectListener(server); // слушатель сообщений
                }

            } catch (Exception e) {
                e.getMessage();
            } finally {
                try {
                    System.out.println("Клиент " + this.getUserName() + " отключил чат.");
                    server.allClients.remove(this);
                    getOrDeleteAllUserAndSet();
                    server.sentAllUsers(MESSAGE + "/" + DELETE_USER + "/" + userName);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();


    }

    private boolean userAuth() throws IOException {
        boolean result = false;
        System.out.println("Попытка авторизации");

        String loginAndPassword = in.readUTF();
        String[] element = loginAndPassword.split(" ");
        String login = element[0];
        String password = element[1];

        if (server.getUserMap().containsKey(login)) { // Есть ли такой логин
            if (server.getUserMap().get(login).equalsIgnoreCase(password)) { // Проверка пары логин / пароль
                if (!server.isLoginBusy(login)) {
                    userName = login;
                    System.out.println("Юзер: " + userName + " авторизован!");
                    out.writeUTF(userName);
                    out.flush();
                    result = true;
                    getOrDeleteAllUserAndSet(); //получить всех участников чата

                    server.sentAllUsersNotMe(MESSAGE + "/" + ADD_USER + "/" + userName); //Сказать всем участника чата о новом юзере
                } else {
                    sentMessage("Пользователь с таким логном уже авторизован");
                }
            } else {
                sentMessage("Неправильный логин или пароль");
            }
        } else {
            sentMessage("Неправильный логин или пароль");
        }

        return result;
    }

    public synchronized void getOrDeleteAllUserAndSet() throws IOException {
        StringBuilder tmp = new StringBuilder();
        for (ClientHandler client : server.allClients) {
            tmp.append(client.getUserName()).append(" ");
        }
        server.sentAllUsers(ClientHandler.SYSTEM + "/" + tmp);
    }

    public void sentMessage(String msg) throws IOException {
        System.out.println("Отправлено " + msg);
        out.writeUTF(msg);
        out.flush();
    }

    private void objectListener(Server server) throws IOException, ClassNotFoundException {
        while (true) {
            String message = in.readUTF();
            if (message.contains("/w")) {
                String[] element = message.split(" ", 5);
                String data = element[0] + " ";
                String nameFrom = element[1] + " ";
                String nameTo = element[3];
                String messageTo = element[4] + " ";
                server.sentUser(userName, MESSAGE + "/" + ClientHandler.MESSAGE_USER + "/" +
                        data + nameFrom + messageTo + "(Личное сообщение " + nameTo + ")");
                server.sentUser(nameTo, MESSAGE + "/" + ClientHandler.MESSAGE_USER + "/" +
                        data + "Личное сообщение от " + nameFrom + messageTo);
            } else server.sentAllUsers(MESSAGE + "/" + MESSAGE_USER + "/" + message);

            if (message.equals("/end")) break;
        }
    }
}
