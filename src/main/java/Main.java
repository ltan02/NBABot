import commands.*;
import database.PostgreSQLJDBC;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Main extends Thread {

    static PostgreSQLJDBC database;
    static APIMain api;

    public static void main(String[] args) throws Exception {

        Main thread = new Main();
        thread.start();

        JDA jda = JDABuilder.createDefault(System.getenv("discord_token"))
                .setActivity(Activity.playing(",help"))
                .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .setCompression(Compression.NONE)
                .setBulkDeleteSplittingEnabled(false)
                .build();

        database = new PostgreSQLJDBC();

        String todayDate = getTodayDate();
        String yesterdayDate = getYesterdayDate();

        jda.addEventListener(new Join(database));
        jda.addEventListener(new Leaderboard(database));
        jda.addEventListener(new Help());
        jda.addEventListener(new Stats(database));
        jda.addEventListener(new Games(database, todayDate));
        jda.addEventListener(new MakePrediction(database, todayDate));
        jda.addEventListener(new ChangePrediction(database, todayDate));
        jda.addEventListener(new Results(database, yesterdayDate));
        jda.addEventListener(new ListPredictions(database, todayDate));
        jda.addEventListener(new Wins(database, yesterdayDate));

        api = new APIMain();

        setupGames();
    }

    public void run() {
        while(true) {
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH-mm-ss");
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Canada/Pacific"));
            String time = now.format(timeFormat);

            if(time.equals("00-00-01")) {
                updatePoints();
                try {
                    setupGames();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updatePoints() {
        ArrayList<Integer> betterIDs = database.getUserIDs();
        for(Integer betterID : betterIDs) {
            ArrayList<Integer> predictionIDs = database.getPredictions("2021-06-23", betterID);
            int points = 0;
            for(Integer predictionID : predictionIDs) {
                String[] predictionInfo = database.getPredictionInformation(predictionID);
                int gameNumber = Integer.parseInt(predictionInfo[0]);
                String predictedTeam = predictionInfo[1];

                String[] gameInformation = database.getGame(gameNumber);
                int score1 = Integer.parseInt(gameInformation[4]);
                int score2 = Integer.parseInt(gameInformation[5]);
                if(score1 > score2 && predictedTeam.equalsIgnoreCase(gameInformation[0])) {
                    points++;
                } else if(score1 < score2 && predictedTeam.equalsIgnoreCase(gameInformation[2])) {
                    points++;
                }
            }
            database.updatePoints(betterID, database.getPoints(betterID) + points);
        }
    }

    public static String getTodayDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Canada/Pacific"));
        return now.format(dtf);
    }

    public static String getYesterdayDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZonedDateTime yesterday = ZonedDateTime.now(ZoneId.of("Canada/Pacific")).minusDays(1);
        return yesterday.format(dtf);
    }

    public static String getTomorrowDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZonedDateTime tomorrow = ZonedDateTime.now(ZoneId.of("Canada/Pacific")).plusDays(1);
        return tomorrow.format(dtf);
    }

    public static void setupGames() throws IOException, InterruptedException {
        String todayDate = getTodayDate();
        String yesterdayDate = getYesterdayDate();
        String tomorrowDate = getTomorrowDate();
        database.createGamesTable();

        ArrayList<String[]> games = api.getGames(yesterdayDate, todayDate);
        for(int i = 0; i < games.size(); i++) {
            String[] current = games.get(i);
            database.addGameInformation(yesterdayDate, current[0], current[1], current[2], current[3], Integer.parseInt(current[4]), Integer.parseInt(current[5]));
        }
        games = api.getGames(yesterdayDate, tomorrowDate);
        for(int i = 0; i < games.size(); i++) {
            String[] current = games.get(i);
            database.addGameInformation(todayDate, current[0], current[1], current[2], current[3], Integer.parseInt(current[4]), Integer.parseInt(current[5]));
        }
    }

}
