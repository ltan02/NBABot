import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

public class JDAConnection {

    public JDAConnection() throws Exception {
        JDA jda = JDABuilder.createDefault(System.getenv("discord_token"))
                .setActivity(Activity.playing(",help"))
                .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .setCompression(Compression.NONE)
                .setBulkDeleteSplittingEnabled(false)
                .build();
    }

}
