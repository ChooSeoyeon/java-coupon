package coupon.quiz;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.restassured.RestAssured;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexPerformanceTest {
    private static final Logger logger = LoggerFactory.getLogger(IndexPerformanceTest.class);

    // 테스트할 API의 기본 URL
    private static final String BASE_URI = "http://localhost:8080";

    // 랜덤한 쿠폰 ID 생성할 범위
    private static final Long MIN_COUPON_ID = 1L;
    private static final Long MAX_COUPON_ID = 351160L;

    // 랜덤한 회원 ID 생성할 범위
    private static final Long MIN_MEMBER_ID = 1L;
    private static final Long MAX_MEMBER_ID = 250000L;

    // 스레드 관련 설정
    private static final int THREAD_COUNT = 10;                 // 동시 요청을 보낼 스레드의 개수
    private static final int TEST_DURATION_SECONDS = 10;        // 테스트가 실행될 시간(초 단위) -> 10초
    private static final long MILLISECONDS_IN_SECOND = 1000L;   // 1초를 밀리초로 변환해줄 값

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    void 쿠폰의_발급_수량_조회() throws InterruptedException {  // 쿠폰의 발급 수량을 조회하는 API의 성능 테스트
        AtomicBoolean running = new AtomicBoolean(false);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicLong totalElapsedTime = new AtomicLong(0);

        // API 호출이 정상적으로 이루어지는지 확인
        int statusCode = RestAssured.get("/coupons/" + ThreadLocalRandom.current()
                        .nextLong(MIN_COUPON_ID, MAX_COUPON_ID + 1) + "/issued-count").statusCode();
        assertThat(statusCode).withFailMessage("쿠폰의 발급 수량 조회 API 호출에 실패했습니다. 테스트 대상 서버가 실행중인지 확인해 주세요.").isEqualTo(200);

        // 다중 요청 실행하여, 요청 횟수와 총 소요 시간 측정
        executeMultipleRequests(running, requestCount, totalElapsedTime,
                () -> RestAssured.get("/coupons/" + ThreadLocalRandom.current()
                        .nextLong(MIN_COUPON_ID, MAX_COUPON_ID + 1) + "/issued-count"));

        // 측정 결과 출력 및 검증
        System.out.println("Total request count: " + requestCount.get()); // 총 요청 수
        System.out.println("Total elapsed time: " + totalElapsedTime.get() + "ms"); // 총 경과 시간

        long averageElapsedTime = totalElapsedTime.get() / requestCount.get();
        System.out.println("Average elapsed time: " + averageElapsedTime + "ms"); // 평균 응답 시간

        assertThat(averageElapsedTime).isLessThanOrEqualTo(100L); // 평균 응답 시간이 100ms 이하인지 검증
    }

    @Test
    void 쿠폰의_사용_수량_조회() throws InterruptedException {
        AtomicBoolean running = new AtomicBoolean(false);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicLong totalElapsedTime = new AtomicLong(0);

        int statusCode = RestAssured.get("/coupons/" + ThreadLocalRandom.current()
                        .nextLong(MIN_COUPON_ID, MAX_COUPON_ID + 1) + "/used-count").statusCode();
        assertThat(statusCode).withFailMessage("쿠폰의 사용 수량 조회 API 호출에 실패했습니다. 테스트 대상 서버가 실행중인지 확인해 주세요.").isEqualTo(200);

        executeMultipleRequests(running, requestCount, totalElapsedTime,
                () -> RestAssured.get("/coupons/" + ThreadLocalRandom.current()
                        .nextLong(MIN_COUPON_ID, MAX_COUPON_ID + 1) + "/used-count"));

        System.out.println("Total request count: " + requestCount.get());
        System.out.println("Total elapsed time: " + totalElapsedTime.get() + "ms");

        long averageElapsedTime = totalElapsedTime.get() / requestCount.get();
        System.out.println("Average elapsed time: " + totalElapsedTime.get() / requestCount.get() + "ms");

        assertThat(averageElapsedTime).isLessThanOrEqualTo(100L);
    }

    @Test
    void 현재_발급_가능한_쿠폰_조회() throws InterruptedException {
        AtomicBoolean running = new AtomicBoolean(false);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicLong totalElapsedTime = new AtomicLong(0);

        int statusCode = RestAssured.get("/coupons/issuable").statusCode();
        assertThat(statusCode).withFailMessage("발급 가능한 쿠폰 조회 API 호출에 실패했습니다. 테스트 대상 서버가 실행중인지 확인해 주세요.").isEqualTo(200);

        executeMultipleRequests(running, requestCount, totalElapsedTime, () -> RestAssured.get("/coupons/issuable"));

        System.out.println("Total request count: " + requestCount.get());
        System.out.println("Total elapsed time: " + totalElapsedTime.get() + "ms");

        long averageElapsedTime = totalElapsedTime.get() / requestCount.get();
        System.out.println("Average elapsed time: " + totalElapsedTime.get() / requestCount.get() + "ms");

        assertThat(averageElapsedTime).isLessThanOrEqualTo(500L);
    }

    @Test
    void 회원이_가지고_있는_사용_가능한_쿠폰_조회() throws InterruptedException {
        AtomicBoolean running = new AtomicBoolean(false);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicLong totalElapsedTime = new AtomicLong(0);

        int statusCode = RestAssured.get("/member-coupons/by-member-id?memberId=" + ThreadLocalRandom.current()
                .nextLong(MIN_MEMBER_ID, MAX_MEMBER_ID + 1)).statusCode();
        assertThat(statusCode).withFailMessage("회원이 가지고 있는 쿠폰 조회 API 호출에 실패했습니다. 테스트 대상 서버가 실행중인지 확인해 주세요.").isEqualTo(200);

        executeMultipleRequests(running, requestCount, totalElapsedTime,
                () -> RestAssured.get("/member-coupons/by-member-id?memberId=" + ThreadLocalRandom.current()
                        .nextLong(MIN_MEMBER_ID, MAX_MEMBER_ID + 1)));

        System.out.println("Total request count: " + requestCount.get());
        System.out.println("Total elapsed time: " + totalElapsedTime.get() + "ms");

        long averageElapsedTime = totalElapsedTime.get() / requestCount.get();
        System.out.println("Average elapsed time: " + totalElapsedTime.get() / requestCount.get() + "ms");

        assertThat(averageElapsedTime).isLessThanOrEqualTo(100L);
    }

    @Test
    void 월별_쿠폰_할인을_가장_많이_받은_회원_조회() throws InterruptedException {
        AtomicBoolean running = new AtomicBoolean(false);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicLong totalElapsedTime = new AtomicLong(0);

        int statusCode = RestAssured.get("/marketing/max-coupon-discount-member?year=2019&month=1").statusCode();
        assertThat(statusCode).withFailMessage("월별 쿠폰 할인을 가장 많이 받은 회원 조회 API 호출에 실패했습니다. 테스트 대상 서버가 실행중인지 확인해 주세요.").isEqualTo(200);

        executeMultipleRequests(running, requestCount, totalElapsedTime, () -> {
            RestAssured.get(
                    "/marketing/max-coupon-discount-member?year=2019&month=" + ThreadLocalRandom.current().nextInt(1, 6));
        });

        System.out.println("Total request count: " + requestCount.get());
        System.out.println("Total elapsed time: " + totalElapsedTime.get() + "ms");

        long averageElapsedTime = totalElapsedTime.get() / requestCount.get();
        System.out.println("Average elapsed time: " + totalElapsedTime.get() / requestCount.get() + "ms");

        assertThat(averageElapsedTime).isLessThanOrEqualTo(100L);
    }

    // 다중 요청을 실행하는 메서드 (여러 스레드에서 동시에 요청(ex. api 요청) 보내도록함)
    private void executeMultipleRequests(AtomicBoolean running, AtomicInteger requestCount, AtomicLong totalElapsedTime,
                                         Runnable runnable)
            throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT); // 동시 요청 보낼 스레드 개수(10개)만큼 스레드 생성
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> executeRequest(running, requestCount, totalElapsedTime, runnable)); // 각 스레드에서 executeRequest 메서드 실행
        }

        Thread.sleep(MILLISECONDS_IN_SECOND);    // 스레드에 실행 요청 후 1초간 대기한 후 요청을 시작하도록 변경함
        running.set(true); // 요청 시작
        Thread.sleep(TEST_DURATION_SECONDS * MILLISECONDS_IN_SECOND); // 요청 실행할 시간 (10초)
        running.set(false); // 요청 중지

        executorService.shutdown(); // 더이상 새로운 작업 받지 않겠단 신호 보냄 (이미 제출된 작업은 계속 실행됨)
        executorService.awaitTermination(10, TimeUnit.SECONDS); // 모든 스레드가 작업 완료할 때까지 최대 10초동안 대기
//        if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) { // 더 긴 대기 시간 설정
//            executorService.shutdownNow(); // 강제 종료
//        }
    }

    // 주어진 시간동안 실제 요청을 보내고 응답 시간을 측정하는 메서드
    private void executeRequest(AtomicBoolean running, AtomicInteger requestCount, AtomicLong totalElapsedTime,
                                Runnable runnable) {
        while (!running.get()) { // 10개 스레드가 실행한 메서드들이 전부 이 상태에 걸려 있음. 1초 후 요청 다같이 시작함
            // 요청을 시작할 때까지 대기함
        }
        /*
        TODO: 질문: 모든 스레드가 이 상태에 걸리기까지 1초면 충분한지 (혹은 다같이 시작하도록 의도한 게 맞는지)
        - 모든 스레드가 동시에 시작하지 않으면,
             - 일부 요청은 서버가 덜 바쁜 상태에서 처리되고, 일부 요청은 서버가 더 많은 부하 받는 상태에서 처리될 수 있어 성능 데이터 왜곡될 수 있음
             - 성능 테스트의 재현성이 떨어질 수도 있음
        */

        // 1. 원래 코드
        long elapsedTime = 0;
        while (running.get()) { // 요청이 가능한 상태에서 (요청 실행 시간인 10초동안)
            long startTime = System.currentTimeMillis();
            runnable.run(); // 요청 보냄 (스레드의 작업)
            long endTime = System.currentTimeMillis();

            elapsedTime += endTime - startTime; // 각 요청에 걸린 시간을 총 경과 시간에 더함
            requestCount.incrementAndGet(); // 요청 횟수 증가시킴
        }
        totalElapsedTime.addAndGet(elapsedTime);

        // 1-1. 원래 코드 문제점 분석
//        long elapsedTime = 0;
//        int count = 1;
//        while (running.get()) { // 요청이 가능한 상태에서 (요청 실행 시간인 10초동안)
//            logger.info("요청 시작 " + count++);
//            long startTime = System.currentTimeMillis();
//            try {
//                runnable.run(); // 요청 보냄 (스레드의 작업)
//            } catch (Exception e) {
//                logger.error("실행 에러 발생");
//                e.printStackTrace(); // 예외 발생 시 스택 트레이스 출력
//                break; // 예외가 발생하면 루프 종료
//            }
//            long endTime = System.currentTimeMillis();
//
//            elapsedTime += endTime - startTime; // 각 요청에 걸린 시간을 총 경과 시간에 더함
//            requestCount.incrementAndGet(); // 요청 횟수 증가시킴
//        }
//        totalElapsedTime.addAndGet(elapsedTime);

            // 2. 개선한 코드
//        while (running.get()) { // 요청이 가능한 상태에서 (요청 실행 시간인 10초동안)
//            long startTime = System.currentTimeMillis();
//            runnable.run(); // 요청 보냄 (스레드의 작업)
//            long endTime = System.currentTimeMillis();
//
//            requestCount.incrementAndGet(); // 요청 횟수 증가시킴
//            totalElapsedTime.addAndGet(endTime - startTime);
//        }
    }
}
