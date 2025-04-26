package petterim1.votifier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class Vote {

    private final String serviceName;
    private final String username;
    private final String address;
    private final long timestamp;
    private final long voteReceivedMs;
}
