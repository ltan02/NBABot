package commands;

import database.PostgreSQLJDBC;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class Join extends ListenerAdapter {

    PostgreSQLJDBC database;

    public Join(PostgreSQLJDBC _database) {
        database = _database;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if(message.length == 1 && message[0].equalsIgnoreCase(",join")) {
            String username = Objects.requireNonNull(event.getMember()).getUser().getName();
            if(!database.inDatabase(username)) {
                event.getChannel().sendMessage("You have joined the BettingBot, here are the commands you can now use:").queue();
                event.getChannel().sendMessage(",help").queue();
                database.addMember(username);
            } else {
                event.getChannel().sendMessage("You have already joined the BettingBot.").queue();
            }
        }
    }

}
