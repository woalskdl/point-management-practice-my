package me.jay.fcp.point;

import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class PointCustomRepositoryImpl extends QuerydslRepositorySupport implements PointCustomRepository {
    public PointCustomRepositoryImpl() {
        super(Point.class);
    }

    /**
     * select w.user_id
     *      , sum(p.amount)
     *   from point p
     *  inner join point_wallet w
     *     on p.point_wallet_id = w.id
     *  where p.is_expired = 1
     *    and p.is_used = 0
     *    and p.expired_date = '2023-06-13'
     *  group by p.point_wallet_id;
     */
    @Override
    public Page<ExpiredPointSummary> sumByExpireDate(LocalDate alarmCriteriaDate, Pageable pageable) {
        JPQLQuery<ExpiredPointSummary> query =
                from(QPoint.point)
                .select(
                        new QExpiredPointSummary(
                            QPoint.point.pointWallet.userId,
                            QPoint.point.amount.sum().coalesce(BigInteger.ZERO)
                        )
                )
                .where(QPoint.point.expired.eq(true))
                .where(QPoint.point.used.eq(false))
                .where(QPoint.point.expireDate.eq(alarmCriteriaDate))
                .groupBy(QPoint.point.pointWallet);

        List<ExpiredPointSummary> expiredPointList = Objects.requireNonNull(getQuerydsl()).applyPagination(pageable, query).fetch();
        long elementCount = query.fetchCount();
        return new PageImpl<>(
                expiredPointList,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
                elementCount
        );
    }

    @Override
    public Page<ExpiredPointSummary> sumBeforeCriteriaDate(LocalDate alarmCriteriaDate, Pageable pageable) {
        JPQLQuery<ExpiredPointSummary> query = from(QPoint.point)
                .select(
                        new QExpiredPointSummary(
                                QPoint.point.pointWallet.userId,
                                QPoint.point.amount.sum().coalesce(BigInteger.ZERO)
                        )
                )
                .where(QPoint.point.expired.eq(false))
                .where(QPoint.point.used.eq(false))
                .where(QPoint.point.expireDate.lt(alarmCriteriaDate))
                .groupBy(QPoint.point.pointWallet);

        List<ExpiredPointSummary> expireSoonPointList = Objects.requireNonNull(getQuerydsl()).applyPagination(pageable, query).fetch();
        long elementCount = query.fetchCount();
        return new PageImpl<>(
                expireSoonPointList,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
                elementCount
        );
    }

    // select p from Point p where p.expireDate < :today and used = false and expired = false
    @Override
    public Page<Point> findPointToExpire(LocalDate today, Pageable pageable) {
        QPoint point = QPoint.point;
        JPQLQuery<Point> query = from(point)
                .select(point)
                .where(point.expireDate.lt(today))
                .where(point.used.eq(false))
                .where(point.expired.eq(false));

        List<Point> points = getQuerydsl().applyPagination(pageable, query).fetch();
        long elementCount = query.fetchCount();
        return new PageImpl<>(
                points,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
                elementCount
        );
    }
}
