package commands;

import database.PostgreSQLJDBC;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class ChangePrediction extends ListenerAdapter {

    PostgreSQLJDBC database;

    public ChangePrediction(PostgreSQLJDBC _database) {
        database = _database;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if(message[0].equalsIgnoreCase(",cp")) {
            if(database.inDatabase(Objects.requireNonNull(event.getMember()).getUser().getName())) {
                if (message.length == 2) {
                    int gameNumber = Integer.parseInt(message[1]);
                    String username = event.getMember().getUser().getName();

                    if (!MakePrediction.madePrediction(gameNumber, username)) {
                        event.getChannel().sendMessage("You need to make a prediction first before changing it.").queue();
                    } else {
                        int predictionID = database.getPrediction("2021-06-02", database.getUserID(username), gameNumber);
                        String predictedTeam = database.getPredictionString("2021-06-02", database.getUserID(username), gameNumber);
                        String[] gameInfo = database.getGames("2021-06-02").get(gameNumber-1);

                        String newTeam;

                        if(predictedTeam.equalsIgnoreCase(gameInfo[0])) {
                            newTeam = gameInfo[2];
                        } else {
                            newTeam = gameInfo[0];
                        }

                        database.updatePrediction(predictionID, newTeam);
                        event.getChannel().sendMessage("Your prediction has been changed for game " + gameNumber + " from *" + predictedTeam + "* to *" + newTeam + "*.").queue();
                    }
                } else {
                    event.getChannel().sendMessage("Please enter a valid input of the form: **,cp <game number>**.").queue();
                }
            } else {
                event.getChannel().sendMessage("Please join the bot first using the **,join** command.").queue();
            }
        }
    }
}
