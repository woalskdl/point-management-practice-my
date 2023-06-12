package me.jay.fcp.point;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public class ExpiredPointSummary {
    String userId;
    BigInteger amount;  // 만료 금액

    @QueryProjection
    public ExpiredPointSummary(String userId, BigInteger amount) {
        this.userId = userId;
        this.amount = amount;
    }
}
