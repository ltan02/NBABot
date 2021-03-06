package commands;

import database.PostgreSQLJDBC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;

public class Leaderboard extends ListenerAdapter {

    PostgreSQLJDBC database;

    public Leaderboard(PostgreSQLJDBC _database) {
        database = _database;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if(message[0].equalsIgnoreCase(",top")) {
            ArrayList<String[]> leaderboard = database.getLeaderboard(); //[guildName, points]

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Leaderboard");
            for(int i = 0; i < leaderboard.size(); i++) {
                String guildName = leaderboard.get(i)[0];
                String points = leaderboard.get(i)[1];
                eb.addField((i+1) + ". " + guildName, guildName + " has scored a total of " + points + " points.", false);
            }
            event.getChannel().sendMessage(eb.build()).queue();
        }
    }

}
