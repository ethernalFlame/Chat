package client;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientWindow extends JFrame {

    private static final String SERVER_IP = "localhost";
    private static int PORT = 8189;
    private JPanel bottomPanel, authPanel;
    private JTextField messageField, authField;
    private JPasswordField passwordField;
    private JTextArea messageHist;
    private JButton sendMessage, authButton;
    private JScrollPane messageHistWrapper;
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private boolean isAuthorized;

    public ClientWindow() {

        try {
            socket = new Socket(SERVER_IP, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        initView();
        initListeners();
        start();

    }

    private void sendMessage() {
        String tmp = messageField.getText();
        messageField.setText("");
        try {
            out.writeUTF(tmp);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("chat");
        setSize(400, 400);
        setResizable(false);

        authPanel = new JPanel(new GridLayout(1, 3));
        authField = new JTextField();
        passwordField = new JPasswordField();
        authButton = new JButton("Auth");

        bottomPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageHist = new JTextArea();
        sendMessage = new JButton("send");

        messageHist.setEditable(false);
        messageHist.setLineWrap(true);
        messageHistWrapper = new JScrollPane(messageHist);

        authPanel.add(authField);
        authPanel.add(passwordField);
        authPanel.add(authButton);

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendMessage, BorderLayout.EAST);

        add(authPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);
        add(messageHistWrapper, BorderLayout.CENTER);

        setAuthorized(false);
        setVisible(true);

    }

    private void start() {
        new Thread(() -> {
            try {
                while (true) {
                    String tmp = in.readUTF();
                    if (tmp.startsWith("/authok")) {
                        setAuthorized(true);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while (true)
                    messageHist.append(in.readUTF() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
        authPanel.setVisible(!authorized);
        bottomPanel.setVisible(authorized);
        System.out.println("авторизирован");
    }

    private void auth() {
        try {
            out.writeUTF("/auth " + authField.getText() + " " + passwordField.getText());
            authField.setText("");
            passwordField.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initListeners() {
        sendMessage.addActionListener((ActionEvent e) -> sendMessage());
        messageField.addActionListener((ActionEvent e) -> sendMessage());
        authField.addActionListener((ActionEvent e) -> auth());
        passwordField.addActionListener((ActionEvent e) -> auth());
        authButton.addActionListener(e -> auth());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    out.writeUTF("/end");
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    setAuthorized(false);
                }
            }

        });
    }

}
