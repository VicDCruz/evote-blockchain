package mx.unam.iimas.mcic.vote;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import mx.unam.iimas.mcic.voting_type.VotingType;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.time.LocalDateTime;

@DataType
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Votable {

    @JsonProperty("id")
    @Property
    private String id;
    @JsonProperty("description")
    @Property
    private String description;
    @JsonProperty("count")
    @Property
    private Integer count;
    @JsonProperty("type")
    private VotingType type;

    public Votable(String id, String description) {
        this.id = id;
        this.description = description;
        this.count = 0;
        this.type = VotingType.VOTABLE;
    }
}
