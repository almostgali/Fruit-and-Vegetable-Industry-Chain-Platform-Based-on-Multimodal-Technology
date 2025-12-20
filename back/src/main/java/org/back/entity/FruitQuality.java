package org.back.entity;

import java.time.LocalDateTime;

/**
 * 水果品质检测结果实体类
 */
public class FruitQuality {
    private Long id;
    private Long userId;
    private String imageUrl;
    private String category;
    private String maturity;
    private String sweetness;
    private String result;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 构造函数
    public FruitQuality() {}

    public FruitQuality(Long userId, String imageUrl, String category, String maturity, String sweetness, String result) {
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.category = category;
        this.maturity = maturity;
        this.sweetness = sweetness;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMaturity() {
        return maturity;
    }

    public void setMaturity(String maturity) {
        this.maturity = maturity;
    }

    public String getSweetness() {
        return sweetness;
    }

    public void setSweetness(String sweetness) {
        this.sweetness = sweetness;
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