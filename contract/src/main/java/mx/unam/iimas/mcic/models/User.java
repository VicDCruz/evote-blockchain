package mx.unam.iimas.mcic.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import javax.persistence.*;

@DataType
@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Property
    private Integer id;

    @Column(name = "registerId", nullable = false)
    @Property
    private String registerId;

    @Column(name = "firstName", nullable = false)
    @Property
    private String firstName;

    @Column(name = "lastName")
    @Property
    private String lastName;
}
