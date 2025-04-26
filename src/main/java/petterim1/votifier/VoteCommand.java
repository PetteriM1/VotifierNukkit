package petterim1.votifier;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

public class VoteCommand extends Command {

    private final String message;

    public VoteCommand(String message) {
        super("vote");

        setPermission("votifier.vote");
        setDescription("VotifierNukkit");

        this.message = message;
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        sender.sendMessage(message);
        return true;
    }
}
