package petterim1.votifier;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Storage {

    private static final Gson GSON = new Gson();

    private final Map<String, List<Vote>> pendingVotes;

    private final File file;

    Storage(File file) {
        this.file = file;

        if (!file.exists()) {
            pendingVotes = new HashMap<>();
            return;
        }

        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            pendingVotes = GSON.fromJson(reader, new VotesTypeToken());

            removeExpiredVotes();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void removeExpiredVotes() {
        if (pendingVotes.isEmpty()) {
            return;
        }

        long expirationThreshold = Main.instance.getVoteExpirationThreshold();

        Iterator<Map.Entry<String, List<Vote>>> votes = pendingVotes.entrySet().iterator();

        while (votes.hasNext()) {
            Map.Entry<String, List<Vote>> entry = votes.next();

            entry.getValue().removeIf(vote -> vote.getVoteReceivedMs() < expirationThreshold);

            if (entry.getValue().isEmpty()) {
                votes.remove();
            }
        }
    }

    private void writeToDisk() {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(pendingVotes, writer);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    void writePendingVote(Vote vote) {
        removeExpiredVotes();

        pendingVotes.computeIfAbsent(vote.getUsername().toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(vote);

        writeToDisk();
    }

    List<Vote> readPendingVotes(String username) {
        List<Vote> votes = pendingVotes.remove(username.toLowerCase(Locale.ROOT));

        if (votes != null) {
            removeExpiredVotes();

            writeToDisk();
        }

        return votes;
    }

    private static class VotesTypeToken extends TypeToken<Map<String, List<Vote>>> {
    }
}
