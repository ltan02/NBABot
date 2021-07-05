package commands;

import database.PostgreSQLJDBC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class Wins extends ListenerAdapter {

    PostgreSQLJDBC database;
    String yesterdayDate;

    public Wins(PostgreSQLJDBC _database, String _yesterdayDate) {
        database = _database;
        yesterdayDate = _yesterdayDate;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if(message[0].equalsIgnoreCase(",wins")) {
            if(database.inDatabase(Objects.requireNonNull(event.getMember()).getUser().getName())) {
                int userID = database.getUserID(event.getMember().getUser().getName());
                ArrayList<Integer> yesterdayPredictions = database.getPredictions(yesterdayDate, userID);

                ArrayList<String[]> winnings = new ArrayList<>();
                for(Integer predictionID : yesterdayPredictions) {
                    String[] predictionsInformation = database.getPredictionInformation(predictionID);

                    int gameID = Integer.parseInt(predictionsInformation[0]);
                    String predictedTeam = predictionsInformation[1];

                    String[] gameInformation = database.getGame(gameID);

                    int score1 = Integer.parseInt(gameInformation[4]);
                    int score2 = Integer.parseInt(gameInformation[5]);
                    if(score1 > score2 && predictedTeam.equalsIgnoreCase(gameInformation[0])) {
                        winnings.add(new String[]{gameInformation[1], gameInformation[3]});
                    } else if(score1 < score2 && predictedTeam.equalsIgnoreCase(gameInformation[2])) {
                        winnings.add(new String[]{gameInformation[3], gameInformation[1]});
                    }
                }

                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(event.getMember().getUser().getName() + "'s winning predictions from " + yesterdayDate);
                if (winnings.size() == 1) {
                    eb.setDescription("You won a total of 1 prediction.");
                } else {
                    eb.setDescription("You won a total of " + winnings.size() + " predictions.");
                }


                for (int j = 0; j < winnings.size(); j++) {
                    eb.addField("", (j + 1) + ". **" + winnings.get(j)[0] + "** - " + winnings.get(j)[1], false);
                }

                event.getChannel().sendMessage(eb.build()).queue();
            } else {
                event.getChannel().sendMessage("You have already joined the BettingBot.").queue();
            }
        }
    }
}
