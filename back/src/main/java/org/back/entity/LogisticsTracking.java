package org.back.entity;

import java.time.LocalDateTime;

/**
 * 物流追踪实体类
 */
public class LogisticsTracking {
    
    private Long id;
    private String trackingNumber;
    private String fruitType;
    private String originLocation;
    private String destination;
    private String currentLocation;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public LogisticsTracking() {}
    
    public LogisticsTracking(String trackingNumber, String fruitType, String originLocation, 
                           String destination, String currentLocation, String status) {
        this.trackingNumber = trackingNumber;
        this.fruitType = fruitType;
        this.originLocation = originLocation;
        this.destination = destination;
        this.currentLocation = currentLocation;
        this.status = status;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public String getFruitType() {
        return fruitType;
    }
    
    public void setFruitType(String fruitType) {
        this.fruitType = fruitType;
    }
    
    public String getOriginLocation() {
        return originLocation;
    }
    
    public void setOriginLocation(String originLocation) {
        this.originLocation = originLocation;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public String getCurrentLocation() {
        return currentLocation;
    }
    
    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}