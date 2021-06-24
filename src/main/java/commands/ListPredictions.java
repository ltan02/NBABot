package commands;

import database.PostgreSQLJDBC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class ListPredictions extends ListenerAdapter {

    PostgreSQLJDBC database;

    public ListPredictions(PostgreSQLJDBC _database) {
        database = _database;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if(message[0].equalsIgnoreCase(",lp")) {
            if(database.inDatabase(Objects.requireNonNull(event.getMember()).getUser().getName())) {
                int betterID = database.getUserID(event.getMember().getUser().getName());
                ArrayList<Integer> predictions = database.getPredictions("2021-06-24", betterID);

                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(event.getMember().getUser().getName() + "'s Predictions for Today");

                for (int i = 0; i < predictions.size(); i++) {
                    String[] prediction = database.getPredictionInformation(predictions.get(i));

                    int gameNumber = Integer.parseInt(prediction[0]);
                    String teamName = prediction[1];
                    String predictionDate = prediction[2];

                    String otherTeam = "";
                    if (teamName.equalsIgnoreCase(teams.get((gameNumber * 2) - 2)[1])) {
                        otherTeam = teams.get((gameNumber * 2) - 1)[0];
                        teamName = teams.get((gameNumber * 2) - 2)[0];
                    } else {
                        otherTeam = teams.get((gameNumber * 2) - 2)[0];
                        teamName = teams.get((gameNumber * 2) - 1)[0];
                    }
                    eb.addField("", String.valueOf(gameNumber) + ". **" + teamName + "**", false);
                }
                event.getChannel().sendMessage(eb.build()).queue();
            } else {
                event.getChannel().sendMessage("Please join the bot first using the **,join** command.").queue();
            }
        }
    }

}
