package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLJDBC {

    public Connection getConnection() {
        Connection c = null;
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
        return c;
    }

    public void createTables() {
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            String remove1 = "DROP TABLE IF EXISTS guild_points cascade";
            stmt.executeUpdate(remove1);

            String remove2 = "DROP TABLE IF EXISTS guild_predictions cascade";
            stmt.executeUpdate(remove2);

            String createGuildPoints = "CREATE TABLE guild_points (" +
                    "id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "guildName VARCHAR(20) NOT NULL," +
                    "points INT NOT NULL)";
            stmt.executeUpdate(createGuildPoints);

            String createGuildPredictions = "CREATE TABLE guild_predictions (" +
                    "predictionID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "gameNumber INT NOT NULL," +
                    "teamName VARCHAR(5) NOT NULL," +
                    "predictionDate VARCHAR(20) NOT NULL," +
                    "betterID INT NOT NULL," +
                    "FOREIGN KEY(betterID) REFERENCES guild_points(id))";
            stmt.executeUpdate(createGuildPredictions);

            stmt.close();
            c.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void addMember(String username) {
        try {
            Connection c = this.getConnection();
            c.setAutoCommit(false);
            Statement stmt = c.createStatement();

            String addMember = String.format("INSERT INTO guild_points(guildName, points) VALUES ('%s', %d)", username, 0);
            stmt.executeUpdate(addMember);

            stmt.close();
            c.commit();
            c.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPrediction(int gameNumber, String teamName, String predictionDate, int betterID) {
        try {
            Connection c = this.getConnection();
            c.setAutoCommit(false);
            Statement stmt = c.createStatement();

            String addMember = String.format("INSERT INTO guild_predictions(gameNumber, teamName, predictionDate, betterID) " +
                    "VALUES (%d, '%s', '%s', %d)", gameNumber, teamName, predictionDate, betterID);
            stmt.executeUpdate(addMember);

            stmt.close();
            c.commit();
            c.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

}
