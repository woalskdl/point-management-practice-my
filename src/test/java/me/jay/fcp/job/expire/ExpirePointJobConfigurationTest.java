package me.jay.fcp.job.expire;

import me.jay.fcp.BatchTestSupport;
import me.jay.fcp.point.Point;
import me.jay.fcp.point.PointRepository;
import me.jay.fcp.point.wallet.PointWallet;
import me.jay.fcp.point.wallet.PointWalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class ExpirePointJobConfigurationTest extends BatchTestSupport {

    @Autowired
    Job expirePointJob;
    @Autowired
    PointWalletRepository pointWalletRepository;
    @Autowired
    PointRepository pointRepository;

    @Test
    void expirePointJob() throws Exception {
        // Given
        LocalDate earnDate = LocalDate.of(2023, 6, 5);
        LocalDate expireDate = LocalDate.of(2023, 6, 8);
        PointWallet pointWallet = pointWalletRepository.save(
                new PointWallet(
                        "user123",
                        BigInteger.valueOf(6000)
                )
        );

        pointRepository.save(new Point(pointWallet, BigInteger.valueOf(1000), earnDate, expireDate));
        pointRepository.save(new Point(pointWallet, BigInteger.valueOf(1000), earnDate, expireDate));
        pointRepository.save(new Point(pointWallet, BigInteger.valueOf(1000), earnDate, expireDate));

        // When
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("today", "2023-06-09")
                .toJobParameters();

        JobExecution jobExecution = launchJob(expirePointJob, jobParameters);

        // Then
        then(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        List<Point> points = pointRepository.findAll();
        then(points.stream().filter(Point::isExpired)).hasSize(3);

        PointWallet changedPointWallet = pointWalletRepository.findById(pointWallet.getId()).orElseGet(null);
        then(changedPointWallet).isNotNull();
        then(changedPointWallet.getAmount()).isEqualByComparingTo(BigInteger.valueOf(3000));
    }

}