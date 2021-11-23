package mx.unam.iimas.mcic.election;

import lombok.Getter;
import mx.unam.iimas.mcic.voting_type.VotingType;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class Election {
    private String id;
    private String name;
    private String country;
    private Integer year;
    private LocalDate startDate;
    private LocalDate endDate;
    private VotingType type;

    public Election(String name, String country, Integer year, LocalDate startDate, LocalDate endDate) {
        this.id = UUID.randomUUID().toString();
        if (this.validateElection()) {
            this.name = name;
            this.country = country;
            this.year = year;
            this.startDate = startDate;
            this.endDate = endDate;
            this.type = VotingType.ELECTION;
        } else {
            throw new RuntimeException("Not a valid election!");
        }
    }

    private boolean validateElection() {
        return StringUtils.isNotBlank(this.id);
    }
}
