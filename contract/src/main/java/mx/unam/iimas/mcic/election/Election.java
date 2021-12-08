package mx.unam.iimas.mcic.election;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mx.unam.iimas.mcic.voting_type.VotingType;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.time.LocalDate;
import java.util.UUID;

@DataType
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Election {
    @JsonProperty("id")
    @Property
    private String id;
    @JsonProperty("name")
    @Property
    private String name;
    @JsonProperty("country")
    private String country;
    @JsonProperty("year")
    private Integer year;
    @JsonProperty("startDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Property
    private LocalDate startDate;
    @JsonProperty("endDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Property
    private LocalDate endDate;
    @JsonProperty("type")
    private VotingType type;
    @JsonProperty("isClosed")
    @Property
    private boolean isClosed;

    public Election(String name, String country, Integer year, LocalDate startDate, LocalDate endDate) {
        this.id = UUID.randomUUID().toString();
        if (this.validateElection()) {
            this.name = name;
            this.country = country;
            this.year = year;
            this.startDate = startDate;
            this.endDate = endDate;
            this.type = VotingType.ELECTION;
            this.isClosed = false;
        } else {
            throw new RuntimeException("Not a valid election!");
        }
    }

    private boolean validateElection() {
        return StringUtils.isNotBlank(this.id);
    }
}
