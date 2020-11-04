package net.system.monitor.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {

    List<Record> findByProcessNameAndCreateTimeBetween(String processName, LocalDateTime startTime,
            LocalDateTime endTime);

    List<Record> findByProcessName(String processName);

    List<Record> findByCreateTimeAfter(LocalDateTime startTime);

}
