package commands;

import database.PostgreSQLJDBC;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MakePrediction extends ListenerAdapter {

    private static String todayDate;
    private static PostgreSQLJDBC database;

    public MakePrediction(PostgreSQLJDBC _database, String _todayDate) {
        database = _database;
        todayDate = _todayDate;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if(message[0].equalsIgnoreCase(",mp")) {
            if(database.inDatabase(Objects.requireNonNull(event.getMember()).getUser().getName())) {
                if (message.length == 3) {
                    String username = event.getMember().getUser().getName();

                    int gameNumber = 0;

                    try {
                        gameNumber = Integer.parseInt(message[1]);
                    } catch (NumberFormatException e) {
                        event.getChannel().sendMessage("Please enter a valid input of the form: **,mp <game number> <team name>**. " +
                                "For example, if the Golden State Warriors is playing in game 1, then input: **,mp 1 GSW**").queue();
                        return;
                    }

                    String teamName = message[2].toUpperCase(Locale.ROOT);

                    ArrayList<String[]> games = database.getGames(todayDate);

                    if (checkValidTeam(gameNumber-1, teamName, games)) {
                        if (madePrediction(gameNumber, username)) {
                            database.addPrediction(gameNumber, teamName, todayDate, database.getUserID(username));
                            //Message saying that prediction went through (command for changing prediction)
                            event.getChannel().sendMessage("Your prediction for " + teamName + " in game " + gameNumber + " has been processed. " +
                                    "If you wish to change your prediction, please use the **,cp** command.").queue();
                        } else {
                            event.getChannel().sendMessage("You already made a prediction for game " + gameNumber + ". " +
                                    "If you wish to change your bet, please use the **,cp** command.").queue();
                        }
                    } else {
                        event.getChannel().sendMessage("The teams playing in game " + gameNumber + " are **" +
                                games.get(gameNumber-1)[1] + "** and **" + games.get(gameNumber-1)[3] + "**").queue();
                    }

                } else {
                    event.getChannel().sendMessage("Please enter a valid input of the form: **,mp <game number> <team name>**. " +
                            "For example, if the Golden State Warriors is playing in game 1, then input: **,mp 1 GSW**").queue();
                }
            } else {
                event.getChannel().sendMessage("Please join the bot first using the **,join** command.").queue();
            }
        }
    }

    public boolean checkValidTeam(int gameNumber, String teamName, ArrayList<String[]> games) {
        String team1 = games.get(gameNumber)[0];
        String team2 = games.get(gameNumber)[2];
        return teamName.equalsIgnoreCase(team1) || teamName.equalsIgnoreCase(team2);
    }

    public static boolean madePrediction(int gameNumber, String username) {
        int id = database.getPrediction(todayDate, database.getUserID(username), gameNumber);
        return id == -1;
    }

}
