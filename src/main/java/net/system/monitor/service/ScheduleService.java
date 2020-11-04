package net.system.monitor.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.system.monitor.entity.Record;
import net.system.monitor.entity.RecordRepository;
import net.system.monitor.websocket.WebSocketService;

@Service
public class ScheduleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleService.class);

    @Value(value = "${schedule.interval.seconds:15}")
    private int intervalSeconds;

    @Value(value = "${process.name:java}")
    private String processName;

    @Autowired
    private RecordRepository recordRepository;

    private ScheduledExecutorService executor;

    @PostConstruct
    public void init() {
        executor = Executors.newScheduledThreadPool(1);
        start();
    }

    public void reset(String processName, int intervalSeconds) {
        this.processName = processName;
        if (intervalSeconds < 10) {
            intervalSeconds = 10;
        } else if (intervalSeconds > 120) {
            intervalSeconds = 3600;
        }
        this.intervalSeconds = intervalSeconds;

        executor.shutdownNow();
        executor = Executors.newScheduledThreadPool(1);
        start();
    }

    private void start() {
        LOGGER.info("Start schedule for monitor system process: {} info with interval seconds: {}", processName,
                intervalSeconds);
        executor.scheduleAtFixedRate(() -> record(), 2, intervalSeconds, TimeUnit.SECONDS);
    }

    private void record() {
        Record record = collect(processName);
        if (record != null) {
            record = recordRepository.save(record);
            WebSocketService.broadcast(record);
        }
    }

    private double scale(double d) {
        return new BigDecimal(d).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private static final String pidCmd = "ps -ef | grep %s | grep -v grep | awk '{print $2}'";
    private static final String topCmd = "top -b -n 1 -p %s";

    public Record collect(String processName) {
        LOGGER.debug("Start to collect process: {} parameters", processName);
        String cmd = String.format(pidCmd, processName);
        String result = exec(cmd);
        if (result == null) {
            return null;
        }

        cmd = "";
        String[] pids = result.split("\n");
        LOGGER.debug("Top pids: {}", result);
        Record record1 = top(pids);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Record record2 = top(pids);
        if (record1 == null && record2 == null) {
            return null;
        } else if (record1 != null && record2 != null) {
            return record1.getCpuPercent() > record2.getCpuPercent() ? record1 : record2;
        } else {
            return record1 == null ? record2 : record1;
        }
    }

    private Record top(String[] pids) {
        String params = StringUtils.join(pids, ",");

        String cmd = String.format(topCmd, params);
        String result = exec(cmd);
        if (result == null) {
            return null;
        }
        Record record1 = new Record();

        record1.setProcessName(processName);
        record1.setProcessorNumber(Runtime.getRuntime().availableProcessors());
        String[] ss = result.split("\n");
        for (String line : ss) {
            line = line.trim();
            if (line.startsWith("KiB Mem")) {
                parseMemInfo(record1, line);
            }
            for (String pid : pids) {
                if (line.startsWith(pid)) {
                    parseDetailInfo(record1, line);
                }
            }
        }
        record1.setCpuPercent(scale(record1.getCpuPercent()));
        record1.setMemoryProcessPercent(scale(record1.getMemoryProcessPercent()));
        record1.setCreateTime(LocalDateTime.now());
        return record1;
    }

    private void parseDetailInfo(Record record, String line) {
        line = replaceMultipleSpaces(line);
        String[] ps = line.split(" ");
        String cpu = ps[8];
        String mem = ps[9];
        record.setCpuPercent(record.getCpuPercent() + Double.parseDouble(cpu.trim()));
        record.setMemoryProcessPercent(record.getMemoryProcessPercent() + Double.parseDouble(mem.trim()));
    }

    private String replaceMultipleSpaces(String s) {
        Pattern p = Pattern.compile("\\s+");
        Matcher m = p.matcher(s);
        return m.replaceAll(" ");
    }

    private void parseMemInfo(Record record, String line) {
        line = line.substring(line.indexOf(":") + 1).trim();
        String[] ps = line.split(",");
        String param = ps[0].trim();
        String[] params = param.split(" ");
        record.setMemoryTotal(Long.parseLong(params[0].trim()));

        param = ps[1].trim();
        params = param.split(" ");
        record.setMemoryFree(Long.parseLong(params[0].trim()));

        param = ps[2].trim();
        params = param.split(" ");
        record.setMemoryUsed(Long.parseLong(params[0].trim()));

        param = ps[3].trim();
        params = param.split(" ");
        record.setMemoryCache(Long.parseLong(params[0].trim()));
    }

    public String exec(String cmd) {
        String[] cmds = new String[] { "/bin/sh", "-c", cmd };
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmds);
            process.waitFor(5, TimeUnit.SECONDS);
            String result = convertInputStream(process.getInputStream());
            LOGGER.debug("CMD: {} return result: {}", cmd, result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    public String convertInputStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    public static void main(String[] args) {
        System.out.println(StringUtils.join(new String[] { "1", "2", "3", "4" }, ","));
    }

}
