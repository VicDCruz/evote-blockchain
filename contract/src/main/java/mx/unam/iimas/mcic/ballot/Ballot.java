package mx.unam.iimas.mcic.ballot;

import com.owlike.genson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mx.unam.iimas.mcic.voting_type.VotingType;
import mx.unam.iimas.mcic.election.Election;
import mx.unam.iimas.mcic.vote.Votable;
import mx.unam.iimas.mcic.voter.Voter;
import org.hyperledger.fabric.contract.Context;

import java.util.ArrayList;
import java.util.UUID;

import static mx.unam.iimas.mcic.utils.JsonMapper.fromJSONString;

@Getter
@NoArgsConstructor
public class Ballot {
    @JsonProperty("id")
    private String id;
    @JsonProperty("votes")
    private ArrayList<Votable> votables;
    @JsonProperty("voterId")
    private String voterId;
    @JsonProperty("election")
    private Election election;
    @JsonProperty("ballotCast")
    private boolean ballotCast;
    @JsonProperty("type")
    private VotingType type;

    public Ballot(Context context, Election election, ArrayList<Votable> votableItems , String voterId) {
        this.election = election;
        this.votables = votableItems;
        this.voterId = voterId;
        this.ballotCast = false;
        this.id = UUID.randomUUID().toString();
        this.type = VotingType.BALLOT;
    }

    private boolean validateBallot(Context context, String voterId) {
        byte[] buffer = context.getStub().getState(voterId);
        if (this.voteExists(buffer)) {
            Voter voter = fromJSONString(new String(buffer), Voter.class);
            if (voter.isBallotCasted()) {
                System.out.println("Ballot has already been casted for this voter");
                return false;
            }
            return true;
        } else {
            System.out.println("Voter ID '"+ voterId + "' is not registered to vote");
            return false;
        }
    }

    private boolean voteExists(byte[] buffer) {
        return buffer != null && buffer.length > 0;
    }
}
