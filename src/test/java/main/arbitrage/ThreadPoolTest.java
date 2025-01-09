import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ThreadPoolTest {
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  // private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Test
  @Disabled("done")
  public void threadPoolTest() throws InterruptedException {
    try {
      System.out.println(
          "시작 - 현재 스레드: "
              + Thread.currentThread().getName()
              + " "
              + LocalDateTime.now().format(formatter));

      // 2초 후 실행되는 작업
      ScheduledFuture<?> task1 =
          executor.schedule(
              () -> {
                System.out.println(
                    "Task 1 실행 - 스레드: "
                        + Thread.currentThread().getName()
                        + " "
                        + LocalDateTime.now().format(formatter));
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
                System.out.println(
                    "Task 1 완료 - 스레드 풀에 반환됨 " + LocalDateTime.now().format(formatter));
              },
              2,
              TimeUnit.SECONDS);

      // 4초 후 실행되는 작업
      ScheduledFuture<?> task2 =
          executor.schedule(
              () -> {
                System.out.println(
                    "Task 2 실행 - 스레드: "
                        + Thread.currentThread().getName()
                        + " "
                        + LocalDateTime.now().format(formatter));
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
                System.out.println(
                    "Task 2 완료 - 스레드 풀에 반환됨 " + LocalDateTime.now().format(formatter));
              },
              4,
              TimeUnit.SECONDS);

      ScheduledFuture<?> task3 =
          executor.scheduleAtFixedRate(
              () -> {
                System.out.println(
                    "Task 3 실행 (1 초마다) - 스레드: "
                        + Thread.currentThread().getName()
                        + " "
                        + LocalDateTime.now().format(formatter));
              },
              0,
              1,
              TimeUnit.SECONDS);

      Thread.sleep(6000);

      System.out.println("모든 작업 완료 " + LocalDateTime.now().format(formatter));
    } finally {
      executor.shutdown();
    }
  }
}
