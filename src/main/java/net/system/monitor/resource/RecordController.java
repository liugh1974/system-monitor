package net.system.monitor.resource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.system.monitor.entity.Record;
import net.system.monitor.entity.RecordRepository;
import net.system.monitor.service.ScheduleService;

@RestController
@RequestMapping(value = "/api/v1/records")
public class RecordController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordController.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping()
    public List<Record> search(@RequestParam(value = "processName") String processName,
            @RequestParam(value = "starttime") String starttime, @RequestParam(value = "endtime") String endtime) {
        LocalDateTime startTime = LocalDateTime.parse(starttime, FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(endtime, FORMATTER);
        LOGGER.info("Search records with processName: {} , starttime: {}, endtime: {}", processName, startTime,
                endTime);

        List<Record> records = recordRepository.findByProcessNameAndCreateTimeBetween(processName.trim(), startTime,
                endTime);
        return records;
    }

    @GetMapping("/collect")
    public Record cmd(@RequestParam("process") String process) {
        LOGGER.info("Collect process: {}", process);
        return scheduleService.collect(process);
    }

    @PostMapping("/params")
    public void reset(@RequestParam("processName") String processName,
            @RequestParam("intervalSeconds") int intervalSeconds) {
        LOGGER.info("Reset schedule parameters, process name: {}, interval seconds: {}", processName, intervalSeconds);
        scheduleService.reset(processName, intervalSeconds);
    }

}
