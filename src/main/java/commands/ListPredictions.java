package commands;

import database.PostgreSQLJDBC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ListPredictions extends ListenerAdapter {

    PostgreSQLJDBC database;
    String todayDate;

    public ListPredictions(PostgreSQLJDBC _database, String _todayDate) {
        database = _database;
        todayDate = _todayDate;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if(message[0].equalsIgnoreCase(",lp")) {
            if(database.inDatabase(Objects.requireNonNull(event.getMember()).getUser().getName())) {
                int betterID = database.getUserID(event.getMember().getUser().getName());
                ArrayList<Integer> predictions = database.getPredictions(todayDate, betterID);

                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(event.getMember().getUser().getName() + "'s Predictions for " + todayDate);

                int counter = 1;
                for (Integer predictionID : predictions) {
                    String[] prediction = database.getPredictionInformation(predictionID);

                    int gameNumber = Integer.parseInt(prediction[0]);
                    String teamName = prediction[1];

                    String[] gameInformation = database.getGame(gameNumber);

                    System.out.println(Arrays.toString(gameInformation));
                    if (teamName.equalsIgnoreCase(gameInformation[0])) {
                        eb.addField("", counter + ". **" + gameInformation[1] + "** v " + gameInformation[3], false);
                    } else if (teamName.equalsIgnoreCase(gameInformation[2])) {
                        eb.addField("", counter + ". " + gameInformation[1] + " v **" + gameInformation[3] + "**", false);
                    }
                    counter++;
                }
                event.getChannel().sendMessage(eb.build()).queue();
            } else {
                event.getChannel().sendMessage("Please join the bot first using the **,join** command.").queue();
            }
        }
    }

}