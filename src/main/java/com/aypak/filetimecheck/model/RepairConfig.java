package com.aypak.filetimecheck.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 时间修复配置模型
 * 用于配置文件时间修复的规则
 */
public class RepairConfig {

    /**
     * 时间模式枚举
     */
    public enum TimeMode {
        FIXED,              // 固定时间模式
        RANDOM,             // 随机时间偏移模式
        BASED_ON_PREVIOUS   // 基于前一个时间/保持原始
    }

    /**
     * 时间属性枚举
     */
    public enum TimeAttribute {
        CREATION_TIME,   // 创建时间
        MODIFIED_TIME,   // 修改时间
        ACCESS_TIME      // 访问时间
    }

    /**
     * 单个时间属性的配置
     */
    public static class TimeConfig {
        private TimeMode mode;
        private LocalDateTime fixedTime;
        private RandomOffset randomOffset;
        private TimeAttribute baseOnPrevious;

        public TimeConfig() {
            this.mode = TimeMode.BASED_ON_PREVIOUS;
        }

        public TimeConfig(TimeMode mode, LocalDateTime fixedTime, RandomOffset randomOffset) {
            this.mode = mode;
            this.fixedTime = fixedTime;
            this.randomOffset = randomOffset;
        }

        public TimeMode getMode() {
            return mode;
        }

        public void setMode(TimeMode mode) {
            this.mode = mode;
        }

        public LocalDateTime getFixedTime() {
            return fixedTime;
        }

        public void setFixedTime(LocalDateTime fixedTime) {
            this.fixedTime = fixedTime;
        }

        public RandomOffset getRandomOffset() {
            return randomOffset;
        }

        public void setRandomOffset(RandomOffset randomOffset) {
            this.randomOffset = randomOffset;
        }

        public TimeAttribute getBaseOnPrevious() {
            return baseOnPrevious;
        }

        public void setBaseOnPrevious(TimeAttribute baseOnPrevious) {
            this.baseOnPrevious = baseOnPrevious;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TimeConfig timeConfig = (TimeConfig) o;
            return mode == timeConfig.mode &&
                    Objects.equals(fixedTime, timeConfig.fixedTime) &&
                    Objects.equals(randomOffset, timeConfig.randomOffset) &&
                    baseOnPrevious == timeConfig.baseOnPrevious;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mode, fixedTime, randomOffset, baseOnPrevious);
        }
    }

    /**
     * 随机偏移配置
     */
    public static class RandomOffset {
        private int daysMin;
        private int daysMax;
        private int hoursMin;
        private int hoursMax;
        private int minutesMin;
        private int minutesMax;
        private int secondsMin;
        private int secondsMax;

        public RandomOffset() {
            this.daysMin = 0;
            this.daysMax = 30;
            this.hoursMin = 0;
            this.hoursMax = 24;
            this.minutesMin = 0;
            this.minutesMax = 60;
            this.secondsMin = 0;
            this.secondsMax = 0;
        }

        public RandomOffset(int daysMin, int daysMax, int hoursMin, int hoursMax,
                           int minutesMin, int minutesMax, int secondsMin, int secondsMax) {
            this.daysMin = daysMin;
            this.daysMax = daysMax;
            this.hoursMin = hoursMin;
            this.hoursMax = hoursMax;
            this.minutesMin = minutesMin;
            this.minutesMax = minutesMax;
            this.secondsMin = secondsMin;
            this.secondsMax = secondsMax;
        }

        public int getDaysMin() {
            return daysMin;
        }

        public void setDaysMin(int daysMin) {
            this.daysMin = daysMin;
        }

        public int getDaysMax() {
            return daysMax;
        }

        public void setDaysMax(int daysMax) {
            this.daysMax = daysMax;
        }

        public int getHoursMin() {
            return hoursMin;
        }

        public void setHoursMin(int hoursMin) {
            this.hoursMin = hoursMin;
        }

        public int getHoursMax() {
            return hoursMax;
        }

        public void setHoursMax(int hoursMax) {
            this.hoursMax = hoursMax;
        }

        public int getMinutesMin() {
            return minutesMin;
        }

        public void setMinutesMin(int minutesMin) {
            this.minutesMin = minutesMin;
        }

        public int getMinutesMax() {
            return minutesMax;
        }

        public void setMinutesMax(int minutesMax) {
            this.minutesMax = minutesMax;
        }

        public int getSecondsMin() {
            return secondsMin;
        }

        public void setSecondsMin(int secondsMin) {
            this.secondsMin = secondsMin;
        }

        public int getSecondsMax() {
            return secondsMax;
        }

        public void setSecondsMax(int secondsMax) {
            this.secondsMax = secondsMax;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RandomOffset that = (RandomOffset) o;
            return daysMin == that.daysMin && daysMax == that.daysMax &&
                    hoursMin == that.hoursMin && hoursMax == that.hoursMax &&
                    minutesMin == that.minutesMin && minutesMax == that.minutesMax &&
                    secondsMin == that.secondsMin && secondsMax == that.secondsMax;
        }

        @Override
        public int hashCode() {
            return Objects.hash(daysMin, daysMax, hoursMin, hoursMax,
                    minutesMin, minutesMax, secondsMin, secondsMax);
        }
    }

    private TimeConfig creationConfig;
    private TimeConfig modifiedConfig;
    private TimeConfig accessConfig;

    public RepairConfig() {
    }

    public TimeConfig getCreationConfig() {
        return creationConfig;
    }

    public void setCreationConfig(TimeConfig creationConfig) {
        this.creationConfig = creationConfig;
    }

    public TimeConfig getModifiedConfig() {
        return modifiedConfig;
    }

    public void setModifiedConfig(TimeConfig modifiedConfig) {
        this.modifiedConfig = modifiedConfig;
    }

    public TimeConfig getAccessConfig() {
        return accessConfig;
    }

    public void setAccessConfig(TimeConfig accessConfig) {
        this.accessConfig = accessConfig;
    }

    /**
     * 创建默认配置
     * - 创建时间: 保持原始
     * - 修改时间: 基于创建时间 + 随机 0-30天 + 0-24小时 + 0-60分钟
     * - 访问时间: 基于修改时间 + 随机 0-7天 + 0-24小时 + 0-60分钟
     */
    public static RepairConfig createDefault() {
        RepairConfig config = new RepairConfig();

        // 创建时间: 保持原始
        TimeConfig creationConfig = new TimeConfig();
        creationConfig.setMode(TimeMode.BASED_ON_PREVIOUS);
        config.setCreationConfig(creationConfig);

        // 修改时间: 基于创建时间 + 随机 0-30天
        TimeConfig modifiedConfig = new TimeConfig();
        modifiedConfig.setMode(TimeMode.RANDOM);
        modifiedConfig.setBaseOnPrevious(TimeAttribute.CREATION_TIME);
        modifiedConfig.setRandomOffset(new RandomOffset(0, 30, 0, 24, 0, 60, 0, 0));
        config.setModifiedConfig(modifiedConfig);

        // 访问时间: 基于修改时间 + 随机 0-7天
        TimeConfig accessConfig = new TimeConfig();
        accessConfig.setMode(TimeMode.RANDOM);
        accessConfig.setBaseOnPrevious(TimeAttribute.MODIFIED_TIME);
        accessConfig.setRandomOffset(new RandomOffset(0, 7, 0, 24, 0, 60, 0, 0));
        config.setAccessConfig(accessConfig);

        return config;
    }
}
