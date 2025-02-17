package main.arbitrage.application.monitor;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryMonitoringService {

  private final MeterRegistry meterRegistry;

  @Scheduled(cron = "0 */3 * * * *")
  protected void monitorMemory() {
    try {
      System.out.println("===========================================");

      log.info("heapUsed: {} MB", bytesToMB(getMetricValue("jvm.memory.used", "area", "heap")));
      log.info(
          "nonHeapUsed: {} MB", bytesToMB(getMetricValue("jvm.memory.used", "area", "nonheap")));

      ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
      log.info("Currently Loaded Classes: {}", classLoadingBean.getLoadedClassCount());
      log.info("Unloaded Classes: {}", classLoadingBean.getUnloadedClassCount());

      BufferPoolMXBean directPool =
          ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class).stream()
              .filter(pool -> pool.getName().equals("direct"))
              .findFirst()
              .orElse(null);

      if (directPool != null) {
        log.info("Direct Memory Used: {} MB", bytesToMB(directPool.getMemoryUsed()));
        log.info("Direct Memory Total Capacity: {} MB", bytesToMB(directPool.getTotalCapacity()));
        log.info("Direct Memory Count: {}", directPool.getCount());
      }

      MemoryPoolMXBean metaspacePool =
          ManagementFactory.getMemoryPoolMXBeans().stream()
              .filter(pool -> pool.getName().contains("Metaspace"))
              .findFirst()
              .orElse(null);

      if (metaspacePool != null) {
        MemoryUsage usage = metaspacePool.getUsage();
        log.info("Metaspace Used: {} MB", bytesToMB(usage.getUsed()));
        log.info("Metaspace Committed: {} MB", bytesToMB(usage.getCommitted()));
        log.info("Metaspace Max: {} MB", bytesToMB(usage.getMax()));
      }

      System.out.println("===========================================");
    } catch (Exception e) {
      log.error("Error during memory monitoring", e);
    }
  }

  private double getMetricValue(String metricName, String... tags) {
    return Optional.ofNullable(meterRegistry.find(metricName).tags(tags).gauge())
        .map(Gauge::value)
        .orElse(0.0);
  }

  private double bytesToMB(double bytes) {
    return Math.round((bytes / (1024 * 1024)) * 100.0) / 100.0;
  }

  @PostConstruct
  public void init() {
    log.info("Memory Monitoring Service Initialized");
  }

  @PreDestroy
  public void cleanup() {
    log.info("Memory Monitoring Service Shutdown");
  }
}
