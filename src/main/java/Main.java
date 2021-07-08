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

    static Join join;
    static Leaderboard leaderboard;
    static Help help;
    static Stats stats;
    static Games games;
    static MakePrediction makePrediction;
    static ChangePrediction changePrediction;
    static Results results;
    static ListPredictions listPredictions;
    static Wins wins;

    static JDA jda;

    public static void main(String[] args) throws Exception {

        Main thread = new Main();
        thread.start();

        jda = JDABuilder.createDefault(System.getenv("discord_token"))
                .setActivity(Activity.playing(",help"))
                .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .setCompression(Compression.NONE)
                .setBulkDeleteSplittingEnabled(false)
                .build();

        database = new PostgreSQLJDBC();

        String todayDate = getTodayDate();
        String yesterdayDate = getYesterdayDate();

        setup(true, todayDate, yesterdayDate);

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
                setup(false, getTodayDate(), getYesterdayDate());
            }
        }
    }

    public static void setup(boolean first, String todayDate, String yesterdayDate) {
        if(!first) {
            jda.removeEventListener(join);
            jda.removeEventListener(games);
            jda.removeEventListener(help);
            jda.removeEventListener(makePrediction);
            jda.removeEventListener(results);
            jda.removeEventListener(changePrediction);
            jda.removeEventListener(listPredictions);
            jda.removeEventListener(wins);
            jda.removeEventListener(leaderboard);
            jda.removeEventListener(stats);
        }

        join = new Join(database);
        leaderboard = new Leaderboard(database);
        help = new Help();
        stats = new Stats(database);
        games = new Games(database, todayDate);
        makePrediction = new MakePrediction(database, todayDate);
        changePrediction = new ChangePrediction(database, todayDate);
        results = new Results(database, yesterdayDate);
        listPredictions = new ListPredictions(database, todayDate);
        wins = new Wins(database, yesterdayDate);

        jda.addEventListener(join);
        jda.addEventListener(leaderboard);
        jda.addEventListener(help);
        jda.addEventListener(stats);
        jda.addEventListener(games);
        jda.addEventListener(makePrediction);
        jda.addEventListener(changePrediction);
        jda.addEventListener(results);
        jda.addEventListener(listPredictions);
        jda.addEventListener(wins);
    }

    public void updatePoints() {
        ArrayList<Integer> betterIDs = database.getUserIDs();
        for(Integer betterID : betterIDs) {
            ArrayList<Integer> predictionIDs = database.getPredictions(getYesterdayDate(), betterID);
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
