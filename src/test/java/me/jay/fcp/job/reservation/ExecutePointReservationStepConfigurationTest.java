package me.jay.fcp.job.reservation;

import me.jay.fcp.BatchTestSupport;
import me.jay.fcp.point.Point;
import me.jay.fcp.point.PointRepository;
import me.jay.fcp.point.reservation.PointReservation;
import me.jay.fcp.point.reservation.PointReservationRepository;
import me.jay.fcp.point.wallet.PointWallet;
import me.jay.fcp.point.wallet.PointWalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.*;

class ExecutePointReservationStepConfigurationTest extends BatchTestSupport {

    @Autowired
    PointReservationRepository pointReservationRepository;
    @Autowired
    PointRepository pointRepository;
    @Autowired
    PointWalletRepository pointWalletRepository;
    @Autowired
    Job executePointReservationJob;

    @Test
    void executePointReservationStep() throws Exception {
        LocalDate earnDate = LocalDate.of(2021, 1, 5);
        PointWallet pointWallet1 = pointWalletRepository.save(
                new PointWallet("user1", BigInteger.valueOf(3000))
        );
        pointReservationRepository.save(
                new PointReservation(
                        pointWallet1,
                        BigInteger.valueOf(1000),
                        earnDate,
                        10
                )
        );
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("today", "2021-01-05")
                .toJobParameters();
        JobExecution execution = launchJob(executePointReservationJob, jobParameters);
        // then
        // then point 적립 2개 확인
        then(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        List<Point> points = pointRepository.findAll();
        then(points).hasSize(1);
        Point point1 = points.stream().filter(it -> it.getAmount().compareTo(BigInteger.valueOf(1000)) == 0).findAny().orElse(null);
        then(point1).isNotNull();
        then(point1.getEarnedDate()).isEqualTo(LocalDate.of(2021, 1, 5));
        then(point1.getExpireDate()).isEqualTo(LocalDate.of(2021, 1, 15));
        then(point1.isExpired()).isFalse();
        then(point1.isUsed()).isFalse();
        // PointWallet의 잔액 확인 3000 -> 4500
        List<PointWallet> wallets = pointWalletRepository.findAll();
        then(wallets).hasSize(1);
        then(wallets.get(0).getAmount()).isEqualByComparingTo(BigInteger.valueOf(4000));
        // reservation 2개 완료처리되었는지 확인
        List<PointReservation> reservations = pointReservationRepository.findAll();
        then(reservations).hasSize(1);
        then(reservations.stream().filter(it -> it.isExecuted())).hasSize(1);
    }
}