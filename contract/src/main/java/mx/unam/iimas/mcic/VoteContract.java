package mx.unam.iimas.mcic;

import lombok.NoArgsConstructor;
import mx.unam.iimas.mcic.ballot.Ballot;
import mx.unam.iimas.mcic.configuration.DatabaseConfiguration;
import mx.unam.iimas.mcic.election.Election;
import mx.unam.iimas.mcic.models.Receipt;
import mx.unam.iimas.mcic.models.User;
import mx.unam.iimas.mcic.query.QueryString;
import mx.unam.iimas.mcic.query.SelectorString;
import mx.unam.iimas.mcic.vote.Votable;
import mx.unam.iimas.mcic.vote.Vote;
import mx.unam.iimas.mcic.vote.VoteHelper;
import mx.unam.iimas.mcic.voter.Voter;
import mx.unam.iimas.mcic.voting_type.VotingType;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ledger.KeyValue;

import javax.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static mx.unam.iimas.mcic.election.ElectionHelper.isExpired;
import static mx.unam.iimas.mcic.utils.JsonMapper.*;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Contract(name = "VoteContract",
        info = @Info(title = "Vote contract",
                description = "Smart Contract to emit a vote",
                version = "0.0.1",
                license = @License(name = "Apache-2.0"),
                contact = @Contact(email = "vicdan1@comunidad.unam.mx", name = "e-vote")))
@Default
@NoArgsConstructor
public class VoteContract implements ContractInterface {
    private Session session;

    @Transaction
    public String openDatabaseConnection(Context context) {
        try {
            this.session = DatabaseConfiguration.getSession();
            System.out.println("MySQL connection opened");
            String sql = "SELECT VERSION()";
            this.session.beginTransaction();
            String res = (String) this.session.createNativeQuery(sql).getSingleResult();
            this.session.close();
            String output = "Actual MySQL version: " + res;
            System.out.println(output);
            return output;
        } catch (Exception e) {
            this.closeDatabaseConnection(context);
            e.printStackTrace();
        }
        return "Error at execution";
    }

    @Transaction
    public String closeDatabaseConnection(Context context) {
        DatabaseConfiguration.getSessionFactory().close();
        String output = "MySQL connection closed";
        System.out.println(output);
        return output;
    }

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

    private <T> ArrayList<T> queryByObjectType(Context context, VotingType type, Class<T> tClass) {
        QueryString query = QueryString.builder()
                .selector(SelectorString.builder()
                        .type(type)
                        .build())
                .build();
        return this.queryWithQueryString(context, toJSONString(query), tClass);
    }

    @Transaction
    public void generateBallot(Context context, String electionId, String voterId) {
        if (!this.voteAssetExists(context, voterId)) {
            System.out.println("Voter ID '"+ voterId + "' is not registered to vote");
            return ;
        }
        ArrayList<Votable> votables = this.queryByObjectType(context, VotingType.VOTABLE, Votable.class);
        ArrayList<Voter> voters = this.queryByObjectType(context, VotingType.VOTER, Voter.class);
        Voter voter = voters.stream().filter(v -> v.getId().equals(voterId)).collect(Collectors.toList()).get(0);
        ArrayList<Election> elections = this.queryByObjectType(context, VotingType.ELECTION, Election.class);
        Election election = elections.stream().filter(v -> v.getId().equals(electionId)).collect(Collectors.toList()).get(0);
        this.generateBallot(context, votables, election, voter);
    }

    private void generateBallot(Context context, ArrayList<Votable> votables, Election election, Voter voter) {
        if (voter.isBallotCasted()) {
            System.out.println("Ballot has already been casted for this voter");
            return ;
        }
        Ballot ballot = new Ballot(context, election, votables, voter.getId());
        voter.setBallot(ballot);

        context.getStub().putState(ballot.getId(), requireNonNull(toJSONString(ballot)).getBytes(UTF_8));
        context.getStub().putState(voter.getId(), requireNonNull(toJSONString(voter)).getBytes(UTF_8));
    }

    private String getRandomVoterId(Context context) {
        ArrayList<Voter> voters = this.queryByObjectType(context, VotingType.VOTER, Voter.class);
        List<String> ids = voters.stream().map(voter -> voter.getId()).collect(Collectors.toList());
        String output;
        do {
            output = RandomStringUtils.randomNumeric(5);
        } while(!ids.contains(output.toString()));
        return output;
    }

    private Integer saveUser(String registerId, String firstName, String lastName) {
        User user = User.builder()
                .registerId(registerId)
                .firstName(firstName)
                .lastName(lastName)
                .build();
        Session session = DatabaseConfiguration.getSession();
        session.beginTransaction();
        Integer id = (Integer) session.save(user);
        session.close();
        return id;
    }

    @Transaction
    public boolean userExists(Context context, String registerId) {
        Session session = DatabaseConfiguration.getSession();
        session.beginTransaction();
        Query query = session.createQuery("FROM User WHERE registerId=:registerId").setParameter("registerId", registerId);
        List results = query.getResultList();
        session.close();
        return results.size() > 0;
    }

    @Transaction
    public User getUser(Context context, String registerId) {
        Session session = DatabaseConfiguration.getSession();
        session.beginTransaction();
        Query query = session.createQuery("FROM User WHERE registerId=:registerId").setParameter("registerId", registerId);
        List users = query.getResultList();
        session.close();
        if (users.size() > 0)
            return (User) users.get(users.size() - 1);
        System.out.println("No user found");
        return null;
    }

    private Voter createVoterNoVerification(Context context, String registerId, String firstName, String lastName) {
        String id;
        if (this.userExists(context, registerId)) {
            System.out.println("User with registerId '" + registerId + "' already exists");
            id = this.getUser(context, registerId).getId().toString();
        } else {
            id = this.saveUser(registerId, firstName, lastName).toString();
        }
        Voter newVoter = new Voter(id);
        context.getStub().putState(newVoter.getId(), requireNonNull(toJSONString(newVoter)).getBytes(UTF_8));
        return newVoter;
    }

    @Transaction
    public Voter createVoter(Context context, String registerId, String firstName, String lastName) {
        Voter newVoter = createVoterNoVerification(context, registerId, firstName, lastName);
        ArrayList<Election> elections = this.queryByObjectType(context, VotingType.ELECTION, Election.class);
        if (elections.isEmpty()) {
            System.out.println("No elections! Run the init() function first");
            return null;
        }
        System.out.println("Generating new ballot");
        Election currentElection = elections.get(0);
        ArrayList<Votable> votables = this.queryByObjectType(context, VotingType.VOTABLE, Votable.class);
        this.generateBallot(context, votables, currentElection, newVoter);

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
        System.out.println("Raw JSON found: " + json);
        if (json.contains(VotingType.VOTE.name()))
            return fromJSONString(json, Vote.class);
        if (json.contains(VotingType.ELECTION.name()))
            return fromJSONString(json, Election.class);
        if (json.contains(VotingType.VOTER.name()))
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
    public Receipt castVote(Context context, String picked, String electionId, String voterId) {
        VoteRequest request = new VoteRequest(picked, electionId, voterId);
        String votableId = request.getPicked();
        boolean electionExists = this.voteAssetExists(context, request.getElectionId());
        if (electionExists) {
            byte[] electionAsBytes = context.getStub().getState(request.getElectionId());
            Election election = fromJSONString(new String(electionAsBytes, UTF_8), Election.class);
            byte[] voterAsBytes = context.getStub().getState(request.getVoterId());
            Voter voter = fromJSONString(new String(voterAsBytes, UTF_8), Voter.class);

            if (voter.isBallotCasted()) {
                System.out.println("This voter has already cast this ballot!");
                return null;
            }
            if (!isExpired(election)) {
                boolean votableExists = this.voteAssetExists(context, votableId);
                if (!votableExists) {
                    System.out.println("VotableId does not exists!");
                    return null;
                }
                Vote vote = new Vote(votableId);
                context.getStub().putState(vote.getId(), requireNonNull(toJSONString(vote)).getBytes(UTF_8));

                voter.setBallotCasted(true);
                voter.setPicked(request.getPicked());

                context.getStub().putState(voter.getId(), requireNonNull(toJSONString(voter)).getBytes(UTF_8));

                return getReceipt(voterId);
            } else {
                System.out.println("The election is not open now");
                return null;
            }
        }
        // return ResponseUtils.newErrorResponse("The election is not open now");
        System.out.println("The election doesn't exist");
        return null;
    }

    @Transaction
    public Receipt getReceipt(Context context, String voterId) {
        return getReceipt(voterId);
    }

    @Transaction
    public boolean verifyReceipt(Context context, String receiptId) {
        Receipt receipt;
        Session session = DatabaseConfiguration.getSession();
        session.beginTransaction();
        Query query = session.createQuery("FROM Receipt WHERE value=:value").setParameter("value", receiptId);
        List results = query.getResultList();
        session.close();
        return results.size() > 0;
    }

    private Receipt getReceipt(String voterId) {
        Receipt receipt;
        Session session = DatabaseConfiguration.getSession();
        session.beginTransaction();
        Query query = session.createQuery("FROM Receipt WHERE voterId=:voterId").setParameter("voterId", voterId);
        List results = query.getResultList();
        if (results.size() > 0) {
            receipt = (Receipt) results.get(results.size() - 1);
        } else {
            receipt = Receipt.builder()
                    .value(UUID.randomUUID().toString())
                    .voterId(voterId)
                    .timestamp(LocalDateTime.now())
                    .build();
            session.save(receipt);
        }
        session.close();
        return receipt;
    }

    @Transaction
    public Election getLatestElection(Context context) {
        ArrayList<Election> currentElections = this.queryByObjectType(context, VotingType.ELECTION, Election.class);
        if (currentElections.isEmpty()) {
            return null;
        }
        return currentElections.get(currentElections.size() - 1);
    }

    @Transaction
    public boolean instantiate(Context context) {
        System.out.println("Instantiate was called!");
        /*if (!AsymmetricCryptographicHelper.keysExists()) {
            try {
                AsymmetricCryptographicHelper.generateKeyPair();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            System.out.println("RSA keys already exists");
        }*/
        ArrayList<Voter> voters = new ArrayList<>();
        ArrayList<Votable> votables = new ArrayList<>();
        ArrayList<Election> elections = new ArrayList<>();
        Election election;

        // query for election first before creating one.
        ArrayList<Election> currentElections = this.queryByObjectType(context, VotingType.ELECTION, Election.class);
        if (currentElections.isEmpty() || isExpired(currentElections.get(currentElections.size() - 1))) {
            System.out.println("Generating new election...");
            // Tomorrow is election day
            LocalDate electionStartDate = LocalDate.now();
            LocalDate electionEndDate = LocalDate.now().plusDays(1).plusDays(1);

            // create the election
            election = new Election("Basic example", "Country", 2021, electionStartDate, electionEndDate);
            elections.add(election);
            context.getStub().putState(election.getId(), requireNonNull(toJSONString(election)).getBytes(UTF_8));
        } else {
            election = currentElections.get(currentElections.size() - 1);
        }
        System.out.println("Election selected: " + election);

        // create voters
        Voter voter1 = this.createVoterNoVerification(context, "234", "Daniel", "Cruz");
        Voter voter2 = this.createVoterNoVerification(context, "123", "Victor", "Cruz");
        voters.add(voter1);
        voters.add(voter2);

        // populate choices
        System.out.println("Creating vote choices");
        Votable repVotable = new Votable("Republican", "He is a Republican");
        Votable demVotable = new Votable("Democrat", "She is a Democrat");
        votables.add(repVotable);
        votables.add(demVotable);

        votables.forEach(votable -> context.getStub().putState(votable.getId(), requireNonNull(toJSONString(votable)).getBytes(UTF_8)));

        // generate ballots for all voters
        voters.forEach(voter -> {
            if (voter.isBallotCasted())
                System.out.println("The voter " + voter.getId() + " already have ballots");
            else
                this.generateBallot(context, votables, election, voter);
        });

        return true;
    }

    private Election getValidElection(Context context) {
        Election election = this.getLatestElection(context);
        if (isEmpty(election)) {
            System.out.println("No election in the system");
            return null;
        }
        if (isExpired(election)) {
            System.out.println("Latest election is already closed");
            return null;
        }
        return election;
    }

    @Transaction
    public Election closeLatestElection(Context context) {
        Election election = this.getValidElection(context);
        if (isEmpty(election)) {
            return null;
        }
        election.setClosed(true);
        context.getStub().putState(election.getId(), requireNonNull(toJSONString(election)).getBytes(UTF_8));
        return election;
    }

    @Transaction
    public Votable[] countVotes(Context context) {
        Election election = this.getLatestElection(context);
        if (isEmpty(election) || !isExpired(election)) {
            System.out.println("Election is still opened, can't count the votes");
            return null;
        }
        ArrayList<Votable> votables = this.queryByObjectType(context, VotingType.VOTABLE, Votable.class);
        ArrayList<Vote> votes = this.queryByObjectType(context, VotingType.VOTE, Vote.class);
        votes = VoteHelper.filterVotesByDates(votes, election.getStartDate().atStartOfDay(), election.getEndDate().atStartOfDay());
        votes.forEach(vote -> {
            Votable tmpVotable = votables.stream().filter(votable -> votable.getId().equals(vote.getVotableId()))
                    .findFirst().orElse(null);
            tmpVotable.setCount(tmpVotable.getCount() + 1);
        });
        return votables.toArray(new Votable[votables.size()]);
    }
}
