package me.jay.fcp.point;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PointCustomRepository {
    Page<ExpiredPointSummary> sumByExpireDate(LocalDate alarmCriteriaDate, Pageable pageable);

    Page<ExpiredPointSummary> sumBeforeCriteriaDate(LocalDate alarmCriteriaDate, Pageable pageable);

    Page<Point> findPointToExpire(LocalDate today, Pageable pageable);

}
