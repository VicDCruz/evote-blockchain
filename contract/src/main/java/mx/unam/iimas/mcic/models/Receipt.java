package mx.unam.iimas.mcic.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import javax.persistence.*;
import java.time.LocalDateTime;

@DataType
@Entity
@Table(name = "Receipt")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "voterId")
    @Property
    private String voterId;

    @Column(name = "value")
    @Property
    private String value;

    @Column(name = "timestamp")
    @Property
    private LocalDateTime timestamp;
}
