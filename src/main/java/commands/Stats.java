package commands;

import database.PostgreSQLJDBC;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class Stats extends ListenerAdapter {

    PostgreSQLJDBC database;

    public Stats(PostgreSQLJDBC _database) {
        database = _database;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if(message[0].equalsIgnoreCase(",stats")) {
            if (database.inDatabase(Objects.requireNonNull(event.getMember()).getUser().getName())) {
                int points = database.getPoints(event.getMember().getUser().getName());
                event.getChannel().sendMessage("You currently have **" + points + "** points.").queue();
            } else {
                event.getChannel().sendMessage("Please join the bot first using the **,join** command.").queue();
            }
        }
    }

}
