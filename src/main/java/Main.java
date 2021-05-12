import database.PostgreSQLJDBC;

public class Main {

    public static void main(String[] args) {
        PostgreSQLJDBC database = new PostgreSQLJDBC();

        database.createTables();
        database.addMember("Lance");
        database.addPrediction(1, "GSW", "2021-05-21", 1);
    }

}
