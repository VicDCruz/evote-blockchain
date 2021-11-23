package mx.unam.iimas.mcic.voter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mx.unam.iimas.mcic.voting_type.VotingType;
import mx.unam.iimas.mcic.ballot.Ballot;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Setter
@ToString
public class Voter {
    private String id;
    private String registerId;
    private VoterName name;
    private Ballot ballot;
    private boolean isBallotCast;
    private VotingType type;
    private String picked;

    public Voter(String id, String registerId, VoterName name) {
        if (this.validateVoter(id) && this.validateRegister(registerId)) {
            this.id = id;
            this.registerId = registerId;
            this.name = name;
            this.isBallotCast = false;
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
