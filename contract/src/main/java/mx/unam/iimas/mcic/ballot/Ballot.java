package mx.unam.iimas.mcic.ballot;

import lombok.Getter;
import mx.unam.iimas.mcic.voting_type.VotingType;
import mx.unam.iimas.mcic.election.Election;
import mx.unam.iimas.mcic.vote.Vote;
import mx.unam.iimas.mcic.voter.Voter;
import org.hyperledger.fabric.contract.Context;

import java.util.ArrayList;
import java.util.UUID;

import static mx.unam.iimas.mcic.utils.JsonMapper.fromJSONString;

@Getter
public class Ballot {
    private String id;
    private ArrayList<Vote> votes;
    private String voterId;
    private Election election;
    private boolean ballotCast;
    private VotingType type;

    public Ballot(Context context, Election election, ArrayList<Vote> votableItems , String voterId) {
        if (this.validateBallot(context, voterId)) {
            this.election = election;
            this.votes = votableItems;
            this.voterId = voterId;
            this.ballotCast = false;
            this.id = UUID.randomUUID().toString();
            this.type = VotingType.BALLOT;
        } else {
            System.out.println("A ballot has already been created for this voter.");
            throw new RuntimeException("A ballot has already been created for this voter.");
        }
    }

    private boolean validateBallot(Context context, String voterId) {
        byte[] buffer = context.getStub().getState(voterId);
        if (this.voteExists(context, voterId)) {
            Voter voter = fromJSONString(new String(buffer), Voter.class);
            if (voter.isBallotCast()) {
                System.out.println("Ballot has already been created for this voter");
                return false;
            }
            return true;
        } else {
            System.out.println("This ID is not registered to vote");
            return false;
        }
    }

    private boolean voteExists(Context context, String voterId) {
        byte[] buffer = context.getStub().getState(voterId);
        return buffer != null && buffer.length > 0;
    }
}
