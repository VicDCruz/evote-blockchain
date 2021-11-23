package mx.unam.iimas.mcic.query;

import lombok.Builder;
import mx.unam.iimas.mcic.voting_type.VotingType;

@Builder(toBuilder = true)
public class SelectorString {

    VotingType type;
}
