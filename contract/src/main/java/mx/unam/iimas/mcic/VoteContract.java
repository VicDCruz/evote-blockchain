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
        ArrayList<T> allResults = new ArrayList<>();
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
        System.out.println("Found " + allResults.size() + " results");
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
        voter.setBallotCreated(true);

        context.getStub().putState(ballot.getId(), toJSONString(ballot).getBytes(UTF_8));
        context.getStub().putState(voter.getId(), toJSONString(voter).getBytes(UTF_8));
    }

    @Transaction
    public Voter createVoter(Context context, String id, String registerId, String firstName, String lastName) {
        if (voteAssetExists(context, id)) {
            throw new RuntimeException("The asset " + id + " already exists");
        }
        Voter newVoter = new Voter(id, registerId, firstName, lastName);
        context.getStub().putState(newVoter.getId(), toJSONString(newVoter).getBytes(UTF_8));
        ArrayList<Election> elections = this.queryByObjectType(context, VotingType.ELECTION, Election.class);
        if (elections.isEmpty()) {
            // ResponseUtils.newErrorResponse(new RuntimeException("No elections! Run the init() function first"));
            System.out.println("No elections! Run the init() function first");
            return null;
        }
        Election currentElection = elections.get(0);
        ArrayList<Vote> votes = this.queryByObjectType(context, VotingType.VOTE, Vote.class);
        this.generateBallot(context, votes, currentElection, newVoter);

        return newVoter;
    }

    @Transaction
    public boolean voteAssetExists(Context context, String assetId) {
        byte[] buffer = context.getStub().getState(assetId);
        return buffer != null && buffer.length > 0;
    }

    @Transaction
    public Object readVoteAsset(Context context, String assetId) {
        boolean exists = this.voteAssetExists(context, assetId);
        if (!exists) {
            System.out.println("The asset " + assetId + " does not exist");
            return null;
        }
        byte[] state = context.getStub().getState(assetId);
        String json = new String(state, UTF_8);
        if (json.contains(VotingType.VOTE.toString()))
            return fromJSONString(json, Vote.class);
        if (json.contains(VotingType.ELECTION.toString()))
            return fromJSONString(json, Election.class);
        if (json.contains(VotingType.VOTER.toString()))
            return fromJSONString(json, Voter.class);
        return fromJSONString(json, Ballot.class);
    }

    @Transaction
    public boolean deleteVoteAsset(Context context, String id) {
        boolean exists = this.voteAssetExists(context, id);
        if (!exists) {
            System.out.println("The asset " + id + " does not exist");
            return false;
        }
        context.getStub().delState(id);
        return true;
    }

    @Transaction
    public boolean castVote(Context context, String picked, String electionId, String voterId) {
        VoteRequest request = new VoteRequest(picked, electionId, voterId);
        String votableId = request.getPicked();
        boolean electionExists = this.voteAssetExists(context, request.getElectionId());
        if (electionExists) {
            byte[] electionAsBytes = context.getStub().getState(request.getElectionId());
            Election election = fromJSONString(new String(electionAsBytes, UTF_8), Election.class);
            byte[] voterAsBytes = context.getStub().getState(request.getVoterId());
            Voter voter = fromJSONString(new String(voterAsBytes, UTF_8), Voter.class);

            if (voter.isBallotCreated()) {
                System.out.println("This voter has already cast this ballot!");
                return false;
            }
            LocalDate currentTime = LocalDate.now();
            if (election.getStartDate().compareTo(currentTime) <= 0 && election.getEndDate().compareTo(currentTime) > 0) {
                boolean votableExists = this.voteAssetExists(context, votableId);
                if (!votableExists) {
                    System.out.println("VotableId does not exists!");
                    return false;
                }
                byte[] votableAsBytes = context.getStub().getState(votableId);
                Vote vote = fromJSONString(new String(votableAsBytes), Vote.class);
                vote.setCount(vote.getCount() + 1);
                context.getStub().putState(votableId, toJSONString(vote).getBytes(UTF_8));

                voter.setBallotCreated(true);
                voter.setPicked(request.getPicked());

                context.getStub().putState(voter.getId(), toJSONString(voter).getBytes(UTF_8));
                // return ResponseUtils.newSuccessResponse(toJSONString(voter), toJSONString(voter).getBytes(UTF_8));
                return false;
            } else {
                System.out.println("The election is not open now");
            }
        }
        // return ResponseUtils.newErrorResponse("The election is not open now");
        System.out.println("The election is not open now");
        return false;
    }

    @Transaction
    public Election getLatestElection(Context context) {
        ArrayList<Election> currentElections = this.queryByObjectType(context, VotingType.ELECTION, Election.class);
        if (currentElections.isEmpty()) {
            return null;
        }
        System.out.println("Not null");
        Election output = currentElections.get(currentElections.size() - 1);
        System.out.println("Output: " + output);
        return output;
    }

    @Transaction
    public boolean instantiate(Context context, String example) {
        System.out.println("Instantiate was called!");
        ArrayList<Voter> voters = new ArrayList<>();
        ArrayList<Vote> votes = new ArrayList<>();
        ArrayList<Election> elections = new ArrayList<>();
        Election election;

        // create voters
        Voter voter1 = new Voter("V1", "234", "Horea", "Porutiu");
        Voter voter2 = new Voter("V2", "345", "Duncan", "Conley");
        voters.add(voter1);
        voters.add(voter2);

        // add the voters to world state
        context.getStub().putState(voter1.getId(), toJSONString(voter1).getBytes(UTF_8));
        System.out.println("Voter " + voter1.getId() + " was " + (this.voteAssetExists(context, voter1.getId()) ? "successfully" : "not") + " created");
        context.getStub().putState(voter2.getId(), toJSONString(voter2).getBytes(UTF_8));
        System.out.println("Voter " + voter2.getId() + " was " + (this.voteAssetExists(context, voter2.getId()) ? "successfully" : "not") + " created");

        // query for election first before creating one.
        ArrayList<Election> currentElections = this.queryByObjectType(context, VotingType.ELECTION, Election.class);
        if (currentElections.isEmpty()) {
            // Tomorrow is election day
            LocalDate electionStartDate = LocalDate.now().plusDays(1);
            LocalDate electionEndDate = LocalDate.now().plusDays(1).plusDays(1);

            // create the election
            election = new Election("Basic example", "Country", 2021, electionStartDate, electionEndDate);
            elections.add(election);
            context.getStub().putState(election.getId(), toJSONString(election).getBytes(UTF_8));
        } else {
            election = currentElections.get(0);
        }
        System.out.println("Election selected: " + election);

        // populate choices
        Vote repVote = new Vote("Republican", "He is a Republican");
        Vote demVote = new Vote("Democrat", "She is a Democrat");
        votes.add(repVote);
        votes.add(demVote);

        votes.forEach(vote -> context.getStub().putState(vote.getId(), toJSONString(vote).getBytes(UTF_8)));

        // generate ballots for all voters
        voters.stream().forEach(voter -> {
            if (voter.isBallotCreated())
                System.out.println("The voter " + voter.getId() + " already have ballots");
            else
                this.generateBallot(context, votes, election, voter);
        });

        return true;
    }
}
