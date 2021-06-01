import commands.*;
import database.PostgreSQLJDBC;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws Exception {
        JDA jda = JDABuilder.createDefault(System.getenv("discord_token"))
                .setActivity(Activity.playing(",help"))
                .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .setCompression(Compression.NONE)
                .setBulkDeleteSplittingEnabled(false)
                .build();

        PostgreSQLJDBC database = new PostgreSQLJDBC();
        database.addMember("Steven");
        database.updatePoints("Steven", 2);

        jda.addEventListener(new Join(database));
        jda.addEventListener(new Leaderboard(database));
        jda.addEventListener(new Help());
        jda.addEventListener(new Stats(database));
        jda.addEventListener(new Games(database));
        jda.addEventListener(new MakePrediction(database));

        APIMain api = new APIMain();
        //ArrayList<String[]> games = api.getGames("2021-06-02", "2021-06-01");
        /*
        for(int i = 0; i < games.size(); i++) {
            String[] current = games.get(i);
            database.addGameInformation("2021-06-02", current[0], current[1], current[2], current[3], Integer.parseInt(current[4]), Integer.parseInt(current[5]));
        }
        */
    }

    public void testDatabase(PostgreSQLJDBC database) {
        database.addMember("Lance");
        database.addMember("Steven");

        database.addPrediction(1, "GSW", "2021-05-21", 1);
        database.addPrediction(1, "GSW", "2021-05-21", 2);

        ArrayList<Integer> test1 = database.getPredictions("2021-05-21", 1);
        ArrayList<Integer> test2 = database.getPredictions("2021-05-21", -1);

        for(int i = 0; i < test1.size(); i++) {
            System.out.println(test1.get(i));
        }
        for(int j = 0; j < test2.size(); j++) {
            System.out.println(test2.get(j));
        }

        System.out.println(database.getPoints("Lance"));
        database.updatePoints("Lance", 2);
        System.out.println(database.getPoints("Lance"));
    }

}
