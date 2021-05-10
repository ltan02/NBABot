import database.PostgreSQLJDBC;

public class Main {

    public static void main(String[] args) {
        PostgreSQLJDBC database = new PostgreSQLJDBC();

        database.getConnection();
        database.createTables();
    }

}
