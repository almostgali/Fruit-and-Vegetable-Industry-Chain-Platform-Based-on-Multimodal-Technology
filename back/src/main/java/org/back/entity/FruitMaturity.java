package org.back.entity;

import java.time.LocalDateTime;

/**
 * 水果新鲜度检测结果实体类
 */
public class FruitMaturity {
    private Long id;
    private Long userId;
    private String imageUrl;
    private String maturity;
    private String result;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 构造函数
    public FruitMaturity() {}

    public FruitMaturity(Long userId, String imageUrl, String maturity, String result) {
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.maturity = maturity;
        this.result = result;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMaturity() {
        return maturity;
    }

    public void setMaturity(String maturity) {
        this.maturity = maturity;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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