package database;

import java.sql.*;
import java.util.ArrayList;

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

            String remove1 = "DROP TABLE IF EXISTS guild_predictions";
            stmt.executeUpdate(remove1);

            String remove2 = "DROP TABLE IF EXISTS guild_points";
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

    public int getUserID(String username) {
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            String getID = String.format("SELECT * FROM guild_points WHERE guildName = '%s'", username);
            ResultSet rs = stmt.executeQuery(getID);

            if(rs.next()) {
                return rs.getInt("id");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //Date (betterID) -> predictionIDs
    //betterID is optional
    public ArrayList<Integer> getPredictions(String date, int betterID) {
        ArrayList<Integer> predictions = new ArrayList<>();

        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            if(betterID != -1) {
                String getPrediction = String.format("SELECT * FROM guild_predictions WHERE predictionDate = '%s' AND betterID = %d", date, betterID);
                ResultSet rs = stmt.executeQuery(getPrediction);

                while (rs.next()) {
                    predictions.add(rs.getInt("predictionID"));
                }
            } else {
                String getPrediction = String.format("SELECT * FROM guild_predictions WHERE predictionDate = '%s'", date);
                ResultSet rs = stmt.executeQuery(getPrediction);

                while(rs.next()) {
                    predictions.add(rs.getInt("predictionID"));
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return predictions;
    }

    public int getPoints(String username) {
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            if(this.inDatabase(username)) {
                String getPoints = String.format("SELECT * FROM guild_points WHERE guildName = '%s'", username);
                ResultSet rs = stmt.executeQuery(getPoints);

                if(rs.next()) {
                    return rs.getInt("points");
                }
            } else {
                System.out.println("User is not in database");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void updatePoints(String username, int newPoints) {
        try {
            Connection c = this.getConnection();
            c.setAutoCommit(false);
            Statement stmt = c.createStatement();

            if(this.inDatabase(username)) {
                String update = String.format("UPDATE guild_points set points = %d WHERE guildName = '%s'", newPoints, username);
                stmt.executeUpdate(update);
                c.commit();
            } else {
                System.out.println("User is not in database");;
            }
            stmt.close();
            c.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean inDatabase(String username) {
        int id = this.getUserID(username);
        return id != -1;
    }

}
