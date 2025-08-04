package org.example.javafx.dto;

import java.time.LocalDateTime;

public class CurrentPercentageDTO {

    private LocalDateTime hour;

    private double communityPool;

    private double gridPortion;

    public CurrentPercentageDTO() {
    }

    public CurrentPercentageDTO(LocalDateTime hour, double communityPool, double gridPortion) {
        this.hour = hour;
        this.communityPool = communityPool;
        this.gridPortion = gridPortion;
    }

    public LocalDateTime getHour() {
        return hour;
    }

    public void setHour(LocalDateTime hour) {
        this.hour = hour;
    }

    public double getCommunityPool() {
        return communityPool;
    }

    public void setCommunityPool(double communityPool) {
        this.communityPool = communityPool;
    }

    public double getGridPortion() {
        return gridPortion;
    }

    public void setGridPortion(double gridPortion) {
        this.gridPortion = gridPortion;
    }
}
