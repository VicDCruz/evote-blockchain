package mx.unam.iimas.mcic.vote;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VoteHelper {
    public static ArrayList<Vote> filterVotesByDates(ArrayList<Vote> votes, LocalDateTime fromDate, LocalDateTime toDate) {
        return (ArrayList<Vote>) votes.stream().filter(vote -> fromDate.isBefore(vote.getTimestamp()) && toDate.isAfter(vote.getTimestamp())).collect(Collectors.toList());
    }
}
