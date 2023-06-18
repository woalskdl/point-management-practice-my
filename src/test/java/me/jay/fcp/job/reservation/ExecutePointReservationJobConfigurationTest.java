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

class ExecutePointReservationJobConfigurationTest extends BatchTestSupport {

    @Autowired
    PointWalletRepository pointWalletRepository;
    @Autowired
    PointReservationRepository pointReservationRepository;
    @Autowired
    PointRepository pointRepository;
    @Autowired
    Job executePointReservationJob;

    @Test
    void executePointReservationJob() throws Exception {
        // Given
        // pointReservation 이 있어야 한다.
        LocalDate earnDate = LocalDate.of(2023, 6, 8);
        PointWallet pointWallet = pointWalletRepository.save(
                new PointWallet(
                        "user1",
                        BigInteger.valueOf(3000)
                )
        );

        pointReservationRepository.save(
                new PointReservation(
                        pointWallet,
                        BigInteger.valueOf(1000),
                        earnDate,
                        10
                )
        );

        // When
        // executePointReservationJob 실행
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("today", "2023-06-08")
                .toJobParameters();

        JobExecution jobExecution = launchJob(executePointReservationJob, jobParameters);

        // Then
        // point reservation 은 완료처리되어야 한다.
        // point 적립이 발생한다.
        // point wallet 의 잔액이 증가해야 한다.
        then(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        List<PointReservation> reservations = pointReservationRepository.findAll();
        then(reservations).hasSize(1);
        then(reservations.get(0).isExecuted()).isTrue();

        List<Point> points = pointRepository.findAll();
        then(points).hasSize(1);
        then(points.get(0).getAmount()).isEqualByComparingTo(BigInteger.valueOf(1000));
        then(points.get(0).getEarnedDate()).isEqualTo(LocalDate.of(2023,6,8));
        then(points.get(0).getExpireDate()).isEqualTo(LocalDate.of(2023,6,18));

        List<PointWallet> wallets = pointWalletRepository.findAll();
        then(wallets).hasSize(1);
        then(wallets.get(0).getAmount()).isEqualByComparingTo(BigInteger.valueOf(4000));
    }
}