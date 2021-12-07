package mx.unam.iimas.mcic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.owlike.genson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class VoteRequest {
    @JsonProperty("picked")
    private String picked;
    @JsonProperty("electionId")
    private String electionId;
    @JsonProperty("voterId")
    private String voterId;
}
