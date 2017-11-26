package server;

import java.sql.*;

/*
CREATE TABLE logins (
    login    TEXT         PRIMARY KEY
                          UNIQUE
                          NOT NULL,
    password TEXT         NOT NULL,
    nick     TEXT (4, 14) UNIQUE
                          NOT NULL
);

 */
public class DBAuthService {
    private Statement statement;
    private Connection connection;

    public void register(String login, String pass, String nick) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO userData(login, password, nick) VALUES (?,?,?);");
            ps.setString(1, login);
            ps.setString(2, pass);
            ps.setString(3, nick);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getNick(String login) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT nick FROM userData WHERE login = '" + login + "';");
        String s = rs.getString("nick");
        return s;
    }

    public void auth(String login, String pass) throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT password FROM userData WHERE login = '" + login + "';");
        String s = rs.getString("password");
        if (!s.equals(pass))
            System.out.println("Неверный логин/пароль");
        else System.out.println("vse ok");
    }

    public void connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:users.db");
        statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS userData (\n" +
                "    login    TEXT         PRIMARY KEY\n" +
                "                          UNIQUE\n" +
                "                          NOT NULL,\n" +
                "    password TEXT         NOT NULL,\n" +
                "    nick     TEXT (4, 14) UNIQUE\n" +
                "                          NOT NULL\n" +
                ");");
    }

    public void disconnect() throws SQLException {
        connection.close();
        statement.close();
    }

}
