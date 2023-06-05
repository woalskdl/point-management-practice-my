package me.jay.fcp.point.wallet;

import lombok.*;
import me.jay.fcp.point.IdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigInteger;

@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PointWallet extends IdEntity {
    @Column(name = "user_id", unique = true, nullable = false)
    String userId;

    @Setter
    @Column(name = "amount", columnDefinition = "BIGINT")
    BigInteger amount;
}
