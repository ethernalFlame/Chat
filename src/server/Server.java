package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final int PORT = 8189;
    DBAuthService dataBase;
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private CopyOnWriteArrayList<ClientHandler> clients;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            clients = new CopyOnWriteArrayList<>();
            dataBase = new DBAuthService();
            dataBase.connect();
            while (true) {
                try {
                    socket = serverSocket.accept();
                    clients.add(new ClientHandler(this, socket));
                    System.out.println("Клиент подключен");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        } finally {
            try {
                dataBase.disconnect();
                serverSocket.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    public void broadcast(String msg) {
        for (ClientHandler c :
                clients) {
            c.sendMessage(msg);
        }
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler c :
                clients) {
            if (c.getName().equals(nick)) return true;
        }
        return false;
    }
}
