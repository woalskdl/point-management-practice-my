package me.jay.fcp.job.message;

import me.jay.fcp.BatchTestSupport;
import me.jay.fcp.message.Message;
import me.jay.fcp.message.MessageRepository;
import me.jay.fcp.point.Point;
import me.jay.fcp.point.wallet.PointWallet;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class MessageExpiredPointJobConfigurationTest extends BatchTestSupport {

    @Autowired
    Job messageExpiredPointJob;
    @Autowired
    MessageRepository messageRepository;

    @Test
    void messageExpiredPointJob() throws Exception {
        // Given
        // 포인트 지갑 생성
        // 오늘(6월 12일) 만료시킨 포인트 적립 내역 생성 (expireDate = 어제 6월 11)
        LocalDate earnDate = LocalDate.of(2023, 6, 1);
        LocalDate expireDate = LocalDate.of(2023, 6, 11);
        LocalDate notExpireDate = LocalDate.of(2023, 12, 13);
        PointWallet pointWallet1 = pointWalletRepository.save(
                new PointWallet("user1", BigInteger.valueOf(3000))
        );
        PointWallet pointWallet2 = pointWalletRepository.save(
                new PointWallet("user2", BigInteger.ZERO)
        );

        pointRepository.save(new Point(pointWallet2, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        pointRepository.save(new Point(pointWallet2, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, notExpireDate));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, notExpireDate));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, notExpireDate));

        // When
        // messageExpiredPointJob 실행
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("today", "2023-06-12")
                .toJobParameters();

        JobExecution execution = launchJob(messageExpiredPointJob, jobParameters);

        // Then
        // 아래와 같은 메세지가 생성되어 있는지 확인
        // 3000 포인트 만료
        // 2023-06-12 기준 3000포인트가 만료되었습니다.
        // user1 : 3000원 포인트 만료 메세지
        // user2 : 2000원 포인트 만료 메세지
        then(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        List<Message> messages = messageRepository.findAll();
        then(messages).hasSize(2);

        Message message1 = messages.stream().filter(item -> item.getUserId().equals("user1")).findFirst().orElseGet(null);
        then(message1).isNotNull();
        then(message1.getTitle()).isEqualTo("3000 포인트 만료");
        then(message1.getContent()).isEqualTo("2023-06-12 기준 3000 포인트가 만료되었습니다.");

        Message message2 = messages.stream().filter(item -> item.getUserId().equals("user2")).findFirst().orElseGet(null);
        then(message2).isNotNull();
        then(message2.getTitle()).isEqualTo("2000 포인트 만료");
        then(message2.getContent()).isEqualTo("2023-06-12 기준 2000 포인트가 만료되었습니다.");

    }
}