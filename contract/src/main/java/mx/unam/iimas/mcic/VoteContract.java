package mx.unam.iimas.mcic;

import lombok.NoArgsConstructor;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;

import static java.nio.charset.StandardCharsets.*;

@Contract(name = "VoteContract",
        info = @Info(title = "Vote contract",
                description = "Smart Contract to emit a vote",
                version = "0.0.1",
                license = @License(name = "Apache-2.0"),
                contact =  @Contact(email = "vicdan1@comunidad.unam.mx", name = "e-vote")))
@Default
@NoArgsConstructor
public class VoteContract implements ContractInterface {
    @Transaction
    public boolean voteExists(Context context, String voteId) {
        byte[] buffer = context.getStub().getState(voteId);
        return buffer != null && buffer.length > 0;
    }

    @Transaction
    public void createVote(Context context, String voteId, String value) {
        boolean exists = this.voteExists(context, voteId);
        if (exists) {
            throw new RuntimeException("The asset " + voteId + " already exists");
        }
        Vote vote = new Vote(value);
        context.getStub().putState(voteId, vote.toJSONString().getBytes(UTF_8));
    }

    @Transaction
    public Vote readVote(Context context, String voteId) {
        boolean exists = this.voteExists(context, voteId);
        if (!exists) {
            throw new RuntimeException("The asset " + voteId + " does not exist");
        }
        String json = new String(context.getStub().getState(voteId), UTF_8);
        return Vote.fromJSONString(json);
    }

    @Transaction
    public void updateVote(Context context, String voteId, String newValue) {
        boolean exists = this.voteExists(context, voteId);
        if (!exists) {
            throw new RuntimeException("The asset " + voteId + " does not exist");
        }
        Vote vote = new Vote(newValue);
        context.getStub().putState(voteId, vote.toJSONString().getBytes(UTF_8));
    }

    @Transaction
    public void deleteVote(Context context, String voteId) {
        boolean exists = this.voteExists(context, voteId);
        if (!exists) {
            throw new RuntimeException("The asset " + voteId + " does not exist");
        }
        context.getStub().delState(voteId);
    }
}
