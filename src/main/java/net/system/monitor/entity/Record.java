package net.system.monitor.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import net.system.monitor.websocket.WebsocketMessage;

@Entity
@Table(name = "record")
public class Record implements Serializable, WebsocketMessage {
    private static final long serialVersionUID = 2899167184167058706L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "create_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalDateTime createTime;

    @Column(name = "process_name")
    private String processName;

    @Column(name = "processor_number")
    private int processorNumber;
    @Column(name = "cpu_percent")
    private double cpuPercent;

    @Column(name = "memory_total")
    private long memoryTotal;
    @Column(name = "memory_free")
    private long memoryFree;
    @Column(name = "memory_used")
    private long memoryUsed;
    @Column(name = "memory_cache")
    private long memoryCache;
    @Column(name = "memory_process_percent")
    private double memoryProcessPercent;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public int getProcessorNumber() {
        return processorNumber;
    }

    public void setProcessorNumber(int processorNumber) {
        this.processorNumber = processorNumber;
    }

    public double getCpuPercent() {
        return cpuPercent;
    }

    public void setCpuPercent(double cpuPercent) {
        this.cpuPercent = cpuPercent;
    }

    public long getMemoryTotal() {
        return memoryTotal;
    }

    public void setMemoryTotal(long memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public long getMemoryFree() {
        return memoryFree;
    }

    public void setMemoryFree(long memoryfree) {
        this.memoryFree = memoryfree;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public long getMemoryCache() {
        return memoryCache;
    }

    public void setMemoryCache(long memoryCache) {
        this.memoryCache = memoryCache;
    }

    public double getMemoryProcessPercent() {
        return memoryProcessPercent;
    }

    public void setMemoryProcessPercent(double memoryProcessPercent) {
        this.memoryProcessPercent = memoryProcessPercent;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
