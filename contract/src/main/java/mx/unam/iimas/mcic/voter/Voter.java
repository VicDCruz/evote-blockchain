package mx.unam.iimas.mcic.voter;

import com.owlike.genson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mx.unam.iimas.mcic.voting_type.VotingType;
import mx.unam.iimas.mcic.ballot.Ballot;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@DataType
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Voter {
    @JsonProperty("id")
    @Property
    private String id;
    @JsonProperty("registerId")
    private String registerId;
    @JsonProperty("name")
    @Property
    private VoterName name;
    @JsonProperty("ballot")
    private Ballot ballot;
    @JsonProperty("isBallotCasted")
    private boolean isBallotCasted;
    @JsonProperty("type")
    private VotingType type;
    @JsonProperty("picked")
    private String picked;

    public Voter(String id, String registerId, String firstName, String lastName) {
        if (this.validateVoter(id) && this.validateRegister(registerId)) {
            this.id = id;
            this.registerId = registerId;
            this.name = new VoterName(firstName, lastName);
            this.isBallotCasted = false;
            this.type = VotingType.VOTER;
        } else if (!this.validateVoter(id)) {
            throw new RuntimeException("The voter ID '" + id + "' is not valid");
        } else {
            throw new RuntimeException("The register ID '" + registerId + "' is not valid");
        }
    }

    private boolean validateVoter(String id) {
        return isNotBlank(id);
    }

    private boolean validateRegister(String registerId) {
        return isNotBlank(registerId);
    }
}
