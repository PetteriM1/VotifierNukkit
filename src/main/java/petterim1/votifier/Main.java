package petterim1.votifier;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;

import java.io.File;
import java.security.KeyPair;
import java.util.List;

public class Main extends PluginBase {

    static Main instance;

    private long votesExpireMs;

    KeyPair keyPair;
    Storage storage;
    VotifierServer votifier;

    List<String> onlineCommands;
    List<String> offlineCommands;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        File rsaDirectory = new File(getDataFolder(), "keys");
        try {
            if (!rsaDirectory.exists()) {
                getLogger().info("Generating new key pair");
                keyPair = RSA.generateKeys(rsaDirectory);
            } else {
                keyPair = RSA.loadKeys(rsaDirectory);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        instance = this;

        votesExpireMs = getConfig().getInt("VotesExpireAfter", 168) * 3600000L;

        storage = new Storage(new File(getDataFolder(), "votes.json"));

        onlineCommands = getConfig().getStringList("OnlineCommands");
        offlineCommands = getConfig().getStringList("OfflineCommands");

        String voteMsg = getConfig().getString("VoteCommandMessage");
        if (!voteMsg.isEmpty()) {
            getServer().getCommandMap().register("vote", new VoteCommand(voteMsg));
        }

        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);

        votifier = new VotifierServer(getConfig().getString("Address", "0.0.0.0"), getConfig().getInt("Port", 8192));
    }

    @Override
    public void onDisable() {
        if (votifier != null) {
            votifier.close();
        }
    }

    void onVoteReceived(Vote vote) {
        getServer().getScheduler().scheduleTask(instance, () -> {
            VoteReceivedEvent event = new VoteReceivedEvent(vote);
            getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            String name = '"' + vote.getUsername() + '"';

            if (!offlineCommands.isEmpty()) {
                for (String cmd : offlineCommands) {
                    getServer().dispatchCommand(getServer().getConsoleSender(), cmd.replace("%p%", name));
                }
            }

            Player p = getServer().getPlayerExact(vote.getUsername());

            if (p == null) {
                storage.writePendingVote(vote);
            } else if (!onlineCommands.isEmpty()) {
                for (String cmd : onlineCommands) {
                    getServer().dispatchCommand(getServer().getConsoleSender(), cmd.replace("%p%", name));
                }
            }
        });
    }

    long getVoteExpirationThreshold() {
        return System.currentTimeMillis() - votesExpireMs;
    }
}
