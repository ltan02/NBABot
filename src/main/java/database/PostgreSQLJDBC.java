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
            c = DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));
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

            String createGuildPoints = "CREATE TABLE IF NOT EXISTS guild_points (" +
                    "id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "guildName VARCHAR(20) NOT NULL," +
                    "points INT NOT NULL)";
            stmt.executeUpdate(createGuildPoints);

            String createGuildPredictions = "CREATE TABLE IF NOT EXISTS guild_predictions (" +
                    "predictionID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "gameNumber INT NOT NULL," +
                    "teamName VARCHAR(5) NOT NULL," +
                    "predictionDate VARCHAR(20) NOT NULL," +
                    "betterID INT NOT NULL," +
                    "gameID INT NOT NULL," +
                    "FOREIGN KEY(betterID) REFERENCES guild_points(id) ON DELETE CASCADE)";
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

    public void createGamesTable() {
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            String remove3 = "DROP TABLE IF EXISTS games";
            stmt.executeUpdate(remove3);

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

            String addMember = String.format("INSERT INTO guild_predictions(gameNumber, teamName, predictionDate, betterID, gameID) " +
                    "VALUES (%d, '%s', '%s', %d, %d)", gameNumber, teamName, predictionDate, betterID, this.getGameID(predictionDate, teamName));
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
                String score1 = rs.getString("score1");
                String score2 = rs.getString("score2");
                String[] gameInfo = {team1ShortName, team1FullName, team2ShortName, team2FullName, score1, score2};
                games.add(gameInfo);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return games;
    }

    public int getGameID(String date, String teamName) {
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            String getGame = String.format("SELECT * FROM games WHERE date = '%s' AND (team1ShortName = '%s' OR team2ShortName = '%s')", date, teamName, teamName);
            ResultSet rs = stmt.executeQuery(getGame);

            if(rs.next()) {
                return rs.getInt("gameID");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String[] getGame(int gameNumber) {
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            String getGame = String.format("SELECT * FROM games WHERE gameID = %d", gameNumber);
            ResultSet rs = stmt.executeQuery(getGame);

            if(rs.next()) {
                String team1ShortName = rs.getString("team1ShortName");
                String team1FullName = rs.getString("team1FullName");
                String team2ShortName = rs.getString("team2ShortName");
                String team2FullName = rs.getString("team2FullName");
                String score1 = rs.getString("score1");
                String score2 = rs.getString("score2");
                return new String[]{team1ShortName, team1FullName, team2ShortName, team2FullName, score1, score2};
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return new String[1];
    }

    public String getUsername(int betterID) {
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            String getID = String.format("SELECT * FROM guild_points WHERE id = %d", betterID);
            ResultSet rs = stmt.executeQuery(getID);

            if(rs.next()) {
                return rs.getString("guildName");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return "";
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

    public ArrayList<Integer> getUserIDs() {
        ArrayList<Integer> output = new ArrayList<>();
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            String getID = String.format("SELECT * FROM guild_points");
            ResultSet rs = stmt.executeQuery(getID);

            while(rs.next()) {
                output.add(rs.getInt("id"));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return output;
    }

    //Date (betterID) -> predictionIDs
    //betterID is optional
    public ArrayList<Integer> getPredictions(String date, int betterID) {
        ArrayList<Integer> predictions = new ArrayList<>();

        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            if(betterID != -1) {
                String getPrediction = String.format("SELECT * FROM guild_predictions WHERE predictionDate = '%s' AND betterID = %d ORDER BY gameNumber ASC", date, betterID);
                ResultSet rs = stmt.executeQuery(getPrediction);

                while (rs.next()) {
                    predictions.add(rs.getInt("predictionID"));
                }
            } else {
                String getPrediction = String.format("SELECT * FROM guild_predictions WHERE predictionDate = '%s' ORDER BY gameNumber ASC", date);
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

    public String[] getPredictionInformation(int predictionID) {
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            String getPrediction = String.format("SELECT * FROM guild_predictions WHERE predictionID = %d", predictionID);
            ResultSet rs = stmt.executeQuery(getPrediction);

            if (rs.next()) {
                String gameNumber = rs.getString("gameID");
                String teamName = rs.getString("teamName");
                String predictionDate = rs.getString("predictionDate");
                return new String[]{gameNumber, teamName, predictionDate};
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return new String[1];
    }

    public Integer getPrediction(String date, int betterID, int gameNumber) {
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            String getPrediction = String.format("SELECT * FROM guild_predictions WHERE predictionDate = '%s' AND betterID = %d AND gameNumber = %d", date, betterID, gameNumber);
            ResultSet rs = stmt.executeQuery(getPrediction);

            if (rs.next()) {
                return rs.getInt("predictionID");
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getPredictionString(String date, int betterID, int gameNumber) {
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            String getPrediction = String.format("SELECT * FROM guild_predictions WHERE predictionDate = '%s' AND betterID = %d AND gameNumber = %d", date, betterID, gameNumber);
            ResultSet rs = stmt.executeQuery(getPrediction);

            if (rs.next()) {
                return rs.getString("teamName");
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public int getPoints(int betterID) {
        try {
            Connection c = this.getConnection();
            Statement stmt = c.createStatement();

            if(this.inDatabase(this.getUsername(betterID))) {
                String getPoints = String.format("SELECT * FROM guild_points WHERE id = %d", betterID);
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

    public void updatePoints(int betterID, int newPoints) {
        try {
            Connection c = this.getConnection();
            c.setAutoCommit(false);
            Statement stmt = c.createStatement();

            if(this.inDatabase(this.getUsername(betterID))) {
                String update = String.format("UPDATE guild_points set points = %d WHERE id = %d", newPoints, betterID);
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

    public void updatePrediction(int predictionID, String newPrediction) {
        try {
            Connection c = this.getConnection();
            c.setAutoCommit(false);
            Statement stmt = c.createStatement();

            String update = String.format("UPDATE guild_predictions set teamName = '%s' WHERE predictionID = %d", newPrediction, predictionID);
            stmt.executeUpdate(update);

            stmt.close();
            c.commit();
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
