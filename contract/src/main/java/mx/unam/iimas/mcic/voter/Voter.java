package mx.unam.iimas.mcic.voter;

import com.owlike.genson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mx.unam.iimas.mcic.voting_type.VotingType;
import mx.unam.iimas.mcic.ballot.Ballot;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Voter {
    @JsonProperty("id")
    private String id;
    @JsonProperty("registerId")
    private String registerId;
    @JsonProperty("name")
    private VoterName name;
    @JsonProperty("ballot")
    private Ballot ballot;
    @JsonProperty("isBallotCreated")
    private boolean isBallotCreated;
    @JsonProperty("type")
    private VotingType type;
    @JsonProperty("picked")
    private String picked;

    public Voter(String id, String registerId, String firstName, String lastName) {
        if (this.validateVoter(id) && this.validateRegister(registerId)) {
            this.id = id;
            this.registerId = registerId;
            this.name = new VoterName(firstName, lastName);
            this.isBallotCreated = false;
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
