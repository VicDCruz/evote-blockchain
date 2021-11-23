package mx.unam.iimas.mcic;

import lombok.NoArgsConstructor;
import mx.unam.iimas.mcic.ballot.Ballot;
import mx.unam.iimas.mcic.election.Election;
import mx.unam.iimas.mcic.query.QueryString;
import mx.unam.iimas.mcic.query.SelectorString;
import mx.unam.iimas.mcic.vote.Vote;
import mx.unam.iimas.mcic.voter.Voter;
import mx.unam.iimas.mcic.voting_type.VotingType;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.shim.ledger.KeyValue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;

import static java.nio.charset.StandardCharsets.*;
import static mx.unam.iimas.mcic.utils.JsonMapper.*;

@Contract(name = "VoteContract",
        info = @Info(title = "Vote contract",
                description = "Smart Contract to emit a vote",
                version = "0.0.1",
                license = @License(name = "Apache-2.0"),
                contact = @Contact(email = "vicdan1@comunidad.unam.mx", name = "e-vote")))
@Default
@NoArgsConstructor
public class VoteContract implements ContractInterface {
    private <T> ArrayList<T> queryWithQueryString(Context context, String query, Class<T> tClass) {
        System.out.println("Query String - " + query);
        Iterator<KeyValue> resultsIterator = context.getStub().getQueryResult(query).iterator();
        ArrayList<T> allResults = new ArrayList();
        while (resultsIterator.hasNext()) {
            KeyValue res = resultsIterator.next();
            if (StringUtils.isNotBlank(res.getStringValue())) {
                String value = res.getStringValue();
                System.out.println(value);
                T tmp = fromJSONString(res.getStringValue(), tClass);
                allResults.add(tmp);
            }
        }
        System.out.println("End of data.");
        System.out.println(allResults);
        return allResults;
    }

    private <T> ArrayList<T> queryAll(Context context, Class<T> tClass) {
        return this.queryWithQueryString(context, "{selector:{}}", tClass);
    }

    private <T> ArrayList<T> queryByObjectType(Context context, VotingType type, Class<T> tClass) {
        QueryString query = QueryString.builder()
                .selector(SelectorString.builder()
                        .type(type)
                        .build())
                .build();
        ArrayList<T> queryResults = this.queryWithQueryString(context, toJSONString(query), tClass);
        return queryResults;
    }

    @Transaction
    public void generateBallot(Context context, ArrayList<Vote> votes, Election election, Voter voter) {
        Ballot ballot = new Ballot(context, election, votes, voter.getId());
        voter.setBallot(ballot);
        voter.setBallotCast(true);

        context.getStub().putState(ballot.getId(), toJSONString(ballot).getBytes(UTF_8));
        context.getStub().putState(voter.getId(), toJSONString(voter).getBytes(UTF_8));
    }

    @Transaction
    public Response createVoter(Context context, String args) {
        Voter newVoter = fromJSONString(args, Voter.class);
        context.getStub().putState(newVoter.getId(), toJSONString(newVoter).getBytes(UTF_8));
        ArrayList<Election> elections = this.queryByObjectType(context, VotingType.ELECTION, Election.class);
        if (elections.isEmpty()) {
            ResponseUtils.newErrorResponse(new RuntimeException("No elections! Run the init() function first"));
        }
        Election currentElection = elections.get(0);
        ArrayList<Vote> votes = this.queryByObjectType(context, VotingType.VOTE, Vote.class);
        this.generateBallot(context, votes, currentElection, newVoter);

        return ResponseUtils.newSuccessResponse("Voter with voterId " + newVoter.getId() + " is updated in the world state");
    }

    @Transaction
    public boolean voteAssetExists(Context context, String assetId) {
        byte[] buffer = context.getStub().getState(assetId);
        return buffer != null && buffer.length > 0;
    }

    @Transaction
    public Response readVoteAsset(Context context, String assetId) {
        boolean exists = this.voteAssetExists(context, assetId);
        if (!exists) {
            return ResponseUtils.newErrorResponse(new RuntimeException("The asset " + assetId + " does not exist"));
        }
        String json = new String(context.getStub().getState(assetId), UTF_8);
        return ResponseUtils.newSuccessResponse(json);
    }

    @Transaction
    public Response updateVoteAsset(Context context, String id, String newValue) {
        boolean exists = this.voteAssetExists(context, id);
        if (!exists) {
            return ResponseUtils.newErrorResponse(new RuntimeException("The asset " + id + " does not exist"));
        }
        Vote vote = new Vote(newValue);
        context.getStub().putState(id, toJSONString(vote).getBytes(UTF_8));
        return ResponseUtils.newSuccessResponse();
    }

    @Transaction
    public Response deleteVoteAsset(Context context, String id) {
        boolean exists = this.voteAssetExists(context, id);
        if (!exists) {
            return ResponseUtils.newErrorResponse(new RuntimeException("The asset " + id + " does not exist"));
        }
        context.getStub().delState(id);
        return ResponseUtils.newSuccessResponse();
    }

    @Transaction
    public Voter castVote(Context context, String args) {
        VoteRequest request = fromJSONString(args, VoteRequest.class);
        String votableId = request.getPicked();
        boolean electionExists = this.voteAssetExists(context, request.getElectionId());
        if (electionExists) {
            byte[] electionAsBytes = context.getStub().getState(request.getElectionId());
            Election election = fromJSONString(new String(electionAsBytes, UTF_8), Election.class);
            byte[] voterAsBytes = context.getStub().getState(request.getVoterId());
            Voter voter = fromJSONString(new String(voterAsBytes, UTF_8), Voter.class);

            if (voter.isBallotCast()) {
                System.out.println("This voter has already cast this ballot!");
                return null;
            }
            LocalDate currentTime = LocalDate.now();
            if (election.getStartDate().compareTo(currentTime) <= 0 && election.getEndDate().compareTo(currentTime) > 0) {
                boolean votableExists = this.voteAssetExists(context, votableId);
                if (!votableExists) {
                    System.out.println("VotableId does not exists!");
                }
                byte[] votableAsBytes = context.getStub().getState(votableId);
                Vote vote = fromJSONString(new String(votableAsBytes), Vote.class);
                vote.setCount(vote.getCount() + 1);
                context.getStub().putState(votableId, toJSONString(vote).getBytes(UTF_8));

                voter.setBallotCast(true);
                voter.setPicked(request.getPicked());

                context.getStub().putState(voter.getId(), toJSONString(voter).getBytes(UTF_8));
                return voter;
            } else {
                System.out.println("The election is not open now");
            }
        } else {
            System.out.println("The election is not open now");
            return null;
        }
    }
}
