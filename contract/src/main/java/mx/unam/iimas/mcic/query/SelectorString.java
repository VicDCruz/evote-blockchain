package mx.unam.iimas.mcic.query;

import lombok.Builder;
import lombok.Getter;
import mx.unam.iimas.mcic.voting_type.VotingType;

@Getter
@Builder(toBuilder = true)
public class SelectorString {

    VotingType type;
}
