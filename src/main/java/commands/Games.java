package commands;

import database.PostgreSQLJDBC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Games extends ListenerAdapter {

    PostgreSQLJDBC database;

    public Games(PostgreSQLJDBC _database) {
        database = _database;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if(message[0].equalsIgnoreCase(",games")) {
            if (database.inDatabase(Objects.requireNonNull(event.getMember()).getUser().getName())) {
                ArrayList<String[]> games = database.getGames("2021-06-24");
                if(games.size() == 0) {
                    event.getChannel().sendMessage("There are no games scheduled for " + "2021-06-24").queue();
                } else {
                    EmbedBuilder eb = displayInformation("2021-06-24", games);
                    event.getChannel().sendMessage(eb.build()).queue();
                }
            } else {
                event.getChannel().sendMessage("Please join the bot first using the **,join** command.").queue();
            }
        }
    }

    public EmbedBuilder displayInformation(String date, ArrayList<String[]> games) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Games for " + date, null);
        for(int i = 0; i < games.size(); i++) {
            String[] currentGame = games.get(i);
            eb.addField((i+1) + ". " + currentGame[1] + " v " + currentGame[3], "[" + currentGame[0] + "] v [" + currentGame[2] + "]", false);
        }
        return eb;
    }

}
