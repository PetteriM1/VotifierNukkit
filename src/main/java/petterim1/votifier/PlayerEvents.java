package petterim1.votifier;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PlayerEvents implements cn.nukkit.event.Listener {

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e) {
        List<Vote> votes = Main.instance.storage.readPendingVotes(e.getPlayer().getName());

        if (votes != null && !Main.instance.onlineCommands.isEmpty()) {
            long expirationThreshold = Main.instance.getVoteExpirationThreshold();

            for (Vote vote : votes) {
                if (vote.getVoteReceivedMs() < expirationThreshold) {
                    continue;
                }

                String name = '"' + vote.getUsername() + '"';

                for (String cmd : Main.instance.onlineCommands) {
                    Main.instance.getServer().dispatchCommand(Main.instance.getServer().getConsoleSender(), cmd.replace("%p%", name));
                }
            }
        }
    }
}
