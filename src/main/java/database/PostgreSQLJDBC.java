package database;

import java.sql.*;
import java.util.ArrayList;

public class PostgreSQLJDBC {

    public PostgreSQLJDBC() {
        this.createTables();
    }

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

            //String remove3 = "DROP TABLE IF EXISTS games";
            //stmt.executeUpdate(remove3);

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

            String createGames = "CREATE TABLE IF NOT EXISTS games (" +
                    "gameID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "date VARCHAR(20) NOT NULL," +
                    "team1ShortName VARCHAR(5) NOT NULL," +
                    "team1FullName VARCHAR(40) NOT NULL," +
                    "team2ShortName VARCHAR(5) NOT NULL," +
                    "team2FullName VARCHAR(40) NOT NULL," +
                    "score1 INT NOT NULL," +
                    "score2 INT NOT NULL)";
            stmt.executeUpdate(createGames);

            stmt.close();
            c.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void addGameInformation(String date, String team1ShortName, String team1FullName, String team2ShortName, String team2FullName, int score1, int score2) {
        try {
            Connection c = this.getConnection();
            c.setAutoCommit(false);
            Statement stmt = c.createStatement();

            String addGame = String.format("INSERT INTO games(date, team1ShortName, team1FullName, team2ShortName, team2FullName, score1, score2) VALUES " +
                    "('%s', '%s', '%s', '%s', '%s', %d, %d)", date, team1ShortName, team1FullName, team2ShortName, team2FullName, score1, score2);
            stmt.executeUpdate(addGame);

            stmt.close();
            c.commit();
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

    public ArrayList<String[]> getGames(String date) {
        ArrayList<String[]> games = new ArrayList<>();
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            String getGame = String.format("SELECT * FROM games WHERE date = '%s'", date);
            ResultSet rs = stmt.executeQuery(getGame);

            while(rs.next()) {
                String team1ShortName = rs.getString("team1ShortName");
                String team1FullName = rs.getString("team1FullName");
                String team2ShortName = rs.getString("team2ShortName");
                String team2FullName = rs.getString("team2FullName");
                String[] gameInfo = {team1ShortName, team1FullName, team2ShortName, team2FullName};
                games.add(gameInfo);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return games;
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

    //[guildName, points]
    public ArrayList<String[]> getLeaderboard() {
        ArrayList<String[]> leaderboard = new ArrayList<>();
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            String getLeaderboard = "SELECT * FROM guild_points ORDER BY points DESC";
            ResultSet rs = stmt.executeQuery(getLeaderboard);

            while(rs.next()) {
                String guildName = rs.getString("guildName");
                String points = String.valueOf(rs.getInt("points"));
                String[] output = {guildName, points};
                leaderboard.add(output);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return leaderboard;
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
