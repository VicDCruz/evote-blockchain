package mx.unam.iimas.mcic;

import com.owlike.genson.Genson;
import lombok.*;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Vote {

    private static final Genson genson = new Genson();

    @Property
    private String value;

    public String toJSONString() {
        return genson.serialize(this);
    }

    public static Vote fromJSONString(String json) {
        return genson.deserialize(json, Vote.class);
    }
}
