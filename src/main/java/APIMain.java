import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class APIMain {
    //dateFormat = yyyy-MM-dd
    //[team1ShortName, team1FullName, team2ShortName, team2FullName, score1, score2]
    public ArrayList<String[]> getGames(String todayDate, String tomorrowDate) throws IOException, InterruptedException {
        ArrayList<String[]> todayGamesUTC = getInformation(todayDate, false);
        ArrayList<String[]> tomorrowGamesUTC = getInformation(tomorrowDate, true);

        tomorrowGamesUTC.addAll(todayGamesUTC);
        return tomorrowGamesUTC;
    }

    //yesterday == 1 means we look at the games after 7am UTC and yesterday == 0 means we look at the games before 7am UTC
    public ArrayList<String[]> getInformation(String date, boolean tomorrow) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api-nba-v1.p.rapidapi.com/games/date/" + date)
                .get()
                .addHeader("x-rapidapi-key", System.getenv("rapidapi_token"))
                .addHeader("x-rapidapi-host", "api-nba-v1.p.rapidapi.com")
                .build();

        Response response = client.newCall(request).execute();

        String information = Objects.requireNonNull(response.body()).string();
        String[] arrayInformation = information.split(",");

        //Retrieving information about each team [teamShortName, teamFullName, score]
        ArrayList<String[]> gameInfo = new ArrayList<>();
        for(int i = 0; i < arrayInformation.length; i++) {
            String current = arrayInformation[i];
            if(current.length() >= 29 && current.substring(1, 13).equals("startTimeUTC")) {
                if((tomorrow && Integer.parseInt(current.substring(27,29)) < 7) ||
                        (!tomorrow && Integer.parseInt(current.substring(27,29)) >= 7)) {
                    String team1ShortNameLine = arrayInformation[i + 14];
                    String team1FullNameLine = arrayInformation[i + 15];
                    String team1ScoreLine = arrayInformation[i + 18];

                    String team1ShortName = team1ShortNameLine.substring(13, team1ShortNameLine.length() - 1);
                    String team1TeamName = team1FullNameLine.substring(12, team1FullNameLine.length() - 1);
                    String team1Score = team1ScoreLine.substring(19, team1ScoreLine.substring(19).indexOf("\"") + 19);
                    if(team1Score.equals("")) {
                        team1Score = "0";
                    }

                    String team2ShortNameLine = arrayInformation[i + 20];
                    String team2FullNameLine = arrayInformation[i + 21];
                    String team2ScoreLine = arrayInformation[i + 24];

                    String team2ShortName = team2ShortNameLine.substring(13, team2ShortNameLine.length() - 1);
                    String team2TeamName = team2FullNameLine.substring(12, team2FullNameLine.length() - 1);
                    String team2Score = team2ScoreLine.substring(19, team2ScoreLine.substring(19).indexOf("\"") + 19);
                    if(team2Score.equals("")) {
                        team2Score = "0";
                    }

                    String[] match = {team1ShortName, team1TeamName, team2ShortName, team2TeamName, team1Score, team2Score};
                    gameInfo.add(match);
                }
            }
        }

        return gameInfo;
    }

}