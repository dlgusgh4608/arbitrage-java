import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ScheduleThreadTest {
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final ScheduledExecutorService scheduler2 = Executors.newSingleThreadScheduledExecutor();

  //   예상결과
  //   start (0초 시작)
  //   delaySchedule (동일 시간) (2초)
  //   defaultOnce (동일 시간) (2초)
  //   defaultOnce (동일 시간) (2초)
  //   defaultOnce (동일 시간) (2초)
  //   delaySchedule (5초)
  //   end (6초)
  @Test
  public void test() {
    try {
      log.info("start");
      defaultOnce();
      delaySchedule();
      defaultOnce();
      defaultOnce();

      Thread.sleep(6000);
      log.info("end");
      scheduler.shutdownNow();
    } catch (Exception e) {
      log.info("main Thread Error");
    }
  }

  // start (0초 시작)
  // defaultSchedule(0초)
  // delayOnce(1초)
  // delayOnce(2초)
  // delayOnce(3초)
  // delayOnce(4초)
  // defaultSchedule(4초) <-- 3초에 실행 되야하지만 Queue대기열에 밀려 5초에 실행
  // end (5초)
  @Test
  public void test2() {
    try {
      log.info("start");
      defaultSchedule();
      delayOnce();
      delayOnce();
      delayOnce();
      delayOnce();

      Thread.sleep(5000);
      log.info("end");
      scheduler2.shutdownNow();
    } catch (Exception e) {
      log.info("main Thread Error");
    }
  }

  private void defaultOnce() {
    scheduler.schedule(
        () -> {
          log.info("defaultOnce");
        },
        1000,
        TimeUnit.MILLISECONDS);
  }

  private void delayOnce() {
    scheduler2.schedule(
        () -> {
          try {
            Thread.sleep(1000);
            log.info("delayOnce");
          } catch (Exception e) {
            log.info("delayOnce Thread Error");
          }
        },
        0,
        TimeUnit.MILLISECONDS);
  }

  private void defaultSchedule() {
    scheduler2.scheduleAtFixedRate(
        () -> {
          log.info("defaultSchedule");
        },
        0,
        3000,
        TimeUnit.MILLISECONDS);
  }

  private void delaySchedule() {
    scheduler.scheduleAtFixedRate(
        () -> {
          try {
            Thread.sleep(2000);
            log.info("delaySchedule");
          } catch (Exception e) {
            log.info("delaySchedule Thread Error");
          }
        },
        0,
        3000,
        TimeUnit.MILLISECONDS);
  }
}
