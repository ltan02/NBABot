package database;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLJDBC {

    Connection c;
    Statement stmt;

    public void getConnection() {
        c = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/NBABotDB",
                            "postgres", System.getenv("PASSWORD"));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": " +e.getMessage());
            System.exit(0);
        }
        System.out.println("Works");
    }

    public void createTables() {
        try {
            stmt = c.createStatement();

            String remove1 = "DROP TABLE IF EXISTS guild_points";
            stmt.executeUpdate(remove1);

            String remove2 = "DROP TABLE IF EXISTS guild_predictions";
            stmt.executeUpdate(remove2);

            String createGuildPoints = "CREATE TABLE guild_points (" +
                    "id INT PRIMARY KEY," +
                    "guildName VARCHAR(20) NOT NULL," +
                    "points INT NOT NULL)";
            stmt.executeUpdate(createGuildPoints);

            String createGuildPredictions = "CREATE TABLE guild_predictions (" +
                    "predictionID INT PRIMARY KEY," +
                    "gameNumber INT NOT NULL," +
                    "teamName VARCHAR(5) NOT NULL," +
                    "predictionDate VARCHAR(20) NOT NULL," +
                    "betterID INT NOT NULL," +
                    "FOREIGN KEY(betterID) REFERENCES guild_points(id))";
            stmt.executeUpdate(createGuildPredictions);
        } catch(SQLException e) {
            e.printStackTrace();
        }

        System.out.println("here");
    }

}
