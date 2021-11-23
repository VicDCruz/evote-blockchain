package mx.unam.iimas.mcic.vote;

import lombok.*;
import mx.unam.iimas.mcic.voting_type.VotingType;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Vote {

    @Property
    private String value;
    private String id;
    private String description;
    private Integer count;
    private VotingType type;

    public Vote(String id, String description) {
        this.id = id;
        this.description = description;
        this.count = 0;
        this.type = VotingType.VOTE;
    }
}
