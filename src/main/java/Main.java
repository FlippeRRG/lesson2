import java.sql.*;
import java.util.Scanner;

public class Main {

    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement pstmt;
    private static boolean close = true;

    public static void main(String[] args) {
        Scanner scanner = new Scanner( System.in );

        try {
            connect();
            connection.setAutoCommit(false);
            stmt.executeUpdate( "INSERT INTO products ( prodid, title, cost) VALUES ('id товара', 'товар', 1)" );
            pstmt = connection.prepareStatement("INSERT INTO products ( prodid, title, cost)\n" +
                    "VALUES  (?,?,?)");
            for (int i = 1; i <= 10000; i++) {
                pstmt.setString(1, "id_товара " + i);
                pstmt.setString(2, "товар" + i);
                pstmt.setInt(3, i * 10);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            connection.setAutoCommit( true );

            while(close){
                System.out.println("Для выхода из приложения введите /1");
                String scan = scanner.nextLine();
                consoleCommandParser( scan );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
        stmt.executeUpdate( "CREATE TABLE products (\n" +
                "    id     INTEGER PRIMARY KEY,\n" +
                "    prodid TEXT    UNIQUE,\n" +
                "    title  TEXT,\n" +
                "    cost   INTEGER\n" +
                ");" );
    }

    public static void disconnect() throws SQLException {
        connection.close();
    }

    public static void consoleCommandParser (String s){
        String recognizer = s.substring( 1,2 ).toLowerCase();
        if (recognizer.compareTo( "ц" )==0){
                costFinder(s);
        } else if (recognizer.compareTo( "с" )==0){
                costChanger(s);
        } else if (recognizer.compareTo( "т" )==0){
                titlePrinter(s);
        } else if(recognizer.compareTo( "1" )==0){
            close = false;
        } else {
            System.out.println("Неверная команда!");
        }
    }

    private static void titlePrinter(String s) {
        String prices = s.substring( 14 );
        int delete = prices.lastIndexOf( " " );
        int lowPrice = Integer.parseInt( prices.substring( 0, delete ) );
        int highPrice = Integer.parseInt( prices.substring( delete+1 ) );

        try {
            PreparedStatement ps = connection.prepareStatement( "SELECT title FROM products WHERE cost <= ? and cost >= ?" );
            ps.setInt( 1, highPrice );
            ps.setInt(2, lowPrice );
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String result = rs.getString( 1 );
                System.out.println(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void costChanger(String s) {
        String title = s.substring( 13 );
        int delete = title.lastIndexOf( " " );
        int newCost = Integer.parseInt( title.substring( delete+1 ) );
        title = title.substring( 0, delete );

        try {
            PreparedStatement ps = connection.prepareStatement( "UPDATE products SET cost = ? WHERE title = ?" );
            ps.setInt( 1, newCost );
            ps.setString(2, title );
            int result = ps.executeUpdate();
            System.out.println(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void costFinder(String s) {
        String title = s.substring( 6 );
        try {
            PreparedStatement ps = connection.prepareStatement( "SELECT cost FROM products WHERE title = ?" );
            ps.setString(1, title );
            ResultSet rs = ps.executeQuery();
            String result = rs.getString( 1 );
            System.out.println(result);
        } catch (SQLException e) {
            System.out.println("Такого товара нет");
        }
    }
}



