package mx.unam.iimas.mcic.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.unam.iimas.mcic.key.KeyType;

import javax.persistence.*;

@Entity
@Table(name = "UserKey")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Lob
    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    @Lob
    @Column(name = "modulus", columnDefinition = "TEXT")
    private String modulus;

    @Lob
    @Column(name = "exponent", columnDefinition = "TEXT")
    private String exponent;

    @Column(name = "keyType", columnDefinition = "TEXT")
    @Enumerated(EnumType.STRING)
    private KeyType keyType;
}
