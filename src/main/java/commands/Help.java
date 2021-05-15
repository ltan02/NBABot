package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Help extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if(message.length == 1 && message[0].equalsIgnoreCase(",help")) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Bot Help");
            eb.addField(",join", "Needed in order to access all the bot commands.", false);
            eb.addField(",games", "Views the games that will be playing for the next day.", false);
            eb.addField(",results", "Views the results of the games that happened yesterday.", false);
            eb.addField(",mp <game number> <team name>", "Makes a prediction that the given team will win for the given game number. " +
                    "For example, if the Golden State Warriors [GSW] is playing in Game 1 and you think they will win, " +
                    "then you would input *,mp 1 GSW*.", false);
            eb.addField(",cp <game number>", "Changes the current prediction you have for the given game to the other team. " +
                    "For example, if the Golden State Warriors [GSW] is playing against the Miami Heat [MIA] in Game 1 and you predicted " +
                    "that the Miami Heat would win, then *,cp 1* will change the prediction to the Golden State Warriors.", false);
            eb.addField(",lp", "Shows all the predictions that you made for today.", false);
            eb.addField(",wins", "Shows the predictions you won from yesterday", false);
            eb.addField(",top", "Shows a leaderboard of all the people who are participating.", false);
            eb.addField(",stats", "Shows how many points you currently have.", false);
            event.getChannel().sendMessage(eb.build()).queue();
        }
    }
}
