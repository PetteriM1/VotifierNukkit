package petterim1.votifier;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Called when vote is received from voting service
 */
@RequiredArgsConstructor
public class VoteReceivedEvent extends Event implements Cancellable {

    @Getter
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Vote vote;
}
