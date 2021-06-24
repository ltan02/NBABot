package commands;

import database.PostgreSQLJDBC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class Results extends ListenerAdapter {

    PostgreSQLJDBC database;

    public Results(PostgreSQLJDBC _database) {
        database = _database;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if(message[0].equalsIgnoreCase(",results")) {
            if (database.inDatabase(Objects.requireNonNull(event.getMember()).getUser().getName())) {
                EmbedBuilder eb = displayInformation();
                event.getChannel().sendMessage(eb.build()).queue();
            } else {
                event.getChannel().sendMessage("Please join the bot first using the **,join** command.").queue();
            }
        }
    }

    public EmbedBuilder displayInformation() {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Results for " + "2021-06-23", null);
        ArrayList<String[]> yesterdayScores = database.getGames("2021-06-23");
        int counter = 1;
        for(int i = 0; i < yesterdayScores.size(); i++) {
            String score1 = yesterdayScores.get(i)[1] + " **" + yesterdayScores.get(i)[4] + "**";
            String score2 = yesterdayScores.get(i)[3] + " **" + yesterdayScores.get(i)[5] + "**";
            eb.addField("", counter + ". " + score1 + " - " + score2, false);
            counter++;
        }
        return eb;
    }

}
