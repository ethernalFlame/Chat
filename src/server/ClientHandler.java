package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataOutputStream out;
    private DataInputStream in;
    private String name = "";

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            name = "undefined";
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            while (true) {
                try {
                    String tmp = in.readUTF();
                    if (tmp.startsWith("/auth")) {
                        String[] loginAndPass = tmp.split(" ");
                        String nick = this.server.dataBase.getNick(loginAndPass[1]);
                        if (!server.isNickBusy(nick)) {
                            server.dataBase.auth(loginAndPass[1], loginAndPass[2]);
                            name = nick;
                            sendMessage("/authok");
                            break;
                        } else System.out.println("Учетная запись используется");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("неверный логин/пароль");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            while (true) {
                String tmp = null;
                try {
                    tmp = in.readUTF();
                    if (tmp.equals("/end")) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server.broadcast(name + ": " + tmp);
            }

        }).start();
    }

    public String getName() {
        return name;
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
