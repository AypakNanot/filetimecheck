package com.aypak.filetimecheck.service;

import com.aypak.filetimecheck.model.FileInfo;
import com.aypak.filetimecheck.model.RepairConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

/**
 * 文件时间修复服务
 * 以创建时间为基准，生成合理的修改时间和访问时间
 */
public class TimeRepairService {

    private static final Random RANDOM = new Random();

    /**
     * 修复单个文件的时间
     * 以创建时间为基准，生成合理的修改时间和访问时间
     */
    public RepairResult repairFile(Path filePath) throws IOException {
        // 读取当前文件时间
        var attrs = Files.readAttributes(filePath, java.nio.file.attribute.BasicFileAttributes.class);
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDateTime creationTime = LocalDateTime.ofInstant(
                attrs.creationTime().toInstant(),
                zoneId
        );

        // 生成合理的时间
        LocalDateTime modifiedTime = generateModifiedTime(creationTime);
        LocalDateTime accessTime = generateAccessTime(modifiedTime);

        // 设置新的时间
        FileTime modifiedFileTime = FileTime.from(modifiedTime.atZone(zoneId).toInstant());
        FileTime accessFileTime = FileTime.from(accessTime.atZone(zoneId).toInstant());

        Files.setAttribute(filePath, "lastModifiedTime", modifiedFileTime);
        Files.setAttribute(filePath, "lastAccessTime", accessFileTime);

        return new RepairResult(creationTime, modifiedTime, accessTime);
    }

    /**
     * 修复 FileInfo 对象记录的时间（不修改实际文件）
     */
    public void repairFileInfo(FileInfo fileInfo) {
        LocalDateTime creationTime = fileInfo.getCreationTime();
        if (creationTime == null) {
            return;
        }

        LocalDateTime modifiedTime = generateModifiedTime(creationTime);
        LocalDateTime accessTime = generateAccessTime(modifiedTime);

        fileInfo.setLastModifiedTime(modifiedTime);
        fileInfo.setLastAccessTime(accessTime);
    }

    /**
     * 使用配置修复单个文件的时间
     */
    public RepairResult repairFile(Path filePath, RepairConfig config) throws IOException {
        var attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDateTime originalCreationTime = LocalDateTime.ofInstant(
                attrs.creationTime().toInstant(),
                zoneId
        );
        LocalDateTime originalModifiedTime = LocalDateTime.ofInstant(
                attrs.lastModifiedTime().toInstant(),
                zoneId
        );
        LocalDateTime originalAccessTime = LocalDateTime.ofInstant(
                attrs.lastAccessTime().toInstant(),
                zoneId
        );

        // 顺序计算新时间：创建 -> 修改 -> 访问
        // 这样修改时间可以基于新的创建时间，访问时间可以基于新的修改时间
        LocalDateTime newCreationTime = calculateTime(
                originalCreationTime, originalModifiedTime, originalAccessTime,
                config.getCreationConfig(), RepairConfig.TimeAttribute.CREATION_TIME,
                null, null, null  // 还没有计算过新时间
        );
        LocalDateTime newModifiedTime = calculateTime(
                originalCreationTime, originalModifiedTime, originalAccessTime,
                config.getModifiedConfig(), RepairConfig.TimeAttribute.MODIFIED_TIME,
                newCreationTime, null, null  // 基于新的创建时间
        );
        LocalDateTime newAccessTime = calculateTime(
                originalCreationTime, originalModifiedTime, originalAccessTime,
                config.getAccessConfig(), RepairConfig.TimeAttribute.ACCESS_TIME,
                newCreationTime, newModifiedTime, null  // 基于新的创建时间和修改时间
        );

        // 设置新的时间
        FileTime modifiedFileTime = FileTime.from(newModifiedTime.atZone(zoneId).toInstant());
        FileTime accessFileTime = FileTime.from(newAccessTime.atZone(zoneId).toInstant());

        Files.setAttribute(filePath, "lastModifiedTime", modifiedFileTime);
        Files.setAttribute(filePath, "lastAccessTime", accessFileTime);

        return new RepairResult(newCreationTime, newModifiedTime, newAccessTime);
    }

    /**
     * 使用配置修复 FileInfo 对象记录的时间（不修改实际文件）
     */
    public void repairFileInfo(FileInfo fileInfo, RepairConfig config) {
        LocalDateTime originalCreationTime = fileInfo.getCreationTime();
        LocalDateTime originalModifiedTime = fileInfo.getLastModifiedTime();
        LocalDateTime originalAccessTime = fileInfo.getLastAccessTime();

        if (originalCreationTime == null || originalModifiedTime == null || originalAccessTime == null) {
            return;
        }

        // 顺序计算新时间：创建 -> 修改 -> 访问
        LocalDateTime newCreationTime = calculateTime(
                originalCreationTime, originalModifiedTime, originalAccessTime,
                config.getCreationConfig(), RepairConfig.TimeAttribute.CREATION_TIME,
                null, null, null
        );
        LocalDateTime newModifiedTime = calculateTime(
                originalCreationTime, originalModifiedTime, originalAccessTime,
                config.getModifiedConfig(), RepairConfig.TimeAttribute.MODIFIED_TIME,
                newCreationTime, null, null
        );
        LocalDateTime newAccessTime = calculateTime(
                originalCreationTime, originalModifiedTime, originalAccessTime,
                config.getAccessConfig(), RepairConfig.TimeAttribute.ACCESS_TIME,
                newCreationTime, newModifiedTime, null
        );

        fileInfo.setCreationTime(newCreationTime);
        fileInfo.setLastModifiedTime(newModifiedTime);
        fileInfo.setLastAccessTime(newAccessTime);
    }

    /**
     * 根据配置计算时间
     * @param newCreationTime 已计算的新创建时间（可为null）
     * @param newModifiedTime 已计算的新修改时间（可为null）
     * @param newAccessTime 已计算的新访问时间（可为null）
     */
    private LocalDateTime calculateTime(
            LocalDateTime originalCreation, LocalDateTime originalModified,
            LocalDateTime originalAccess,
            RepairConfig.TimeConfig timeConfig,
            RepairConfig.TimeAttribute currentAttribute,
            LocalDateTime newCreationTime,
            LocalDateTime newModifiedTime,
            LocalDateTime newAccessTime) {

        if (timeConfig == null) {
            return getOriginalTime(currentAttribute, originalCreation, originalModified, originalAccess);
        }

        switch (timeConfig.getMode()) {
            case FIXED:
                if (timeConfig.getFixedTime() != null) {
                    return timeConfig.getFixedTime();
                }
                return getOriginalTime(currentAttribute, originalCreation, originalModified, originalAccess);

            case RANDOM:
                LocalDateTime baseTime = determineBaseTime(
                        timeConfig.getBaseOnPrevious(),
                        originalCreation, originalModified, originalAccess,
                        newCreationTime, newModifiedTime, newAccessTime
                );
                return applyRandomOffset(baseTime, timeConfig.getRandomOffset());

            case BASED_ON_PREVIOUS:
                return getOriginalTime(currentAttribute, originalCreation, originalModified, originalAccess);

            default:
                return getOriginalTime(currentAttribute, originalCreation, originalModified, originalAccess);
        }
    }

    /**
     * 确定基准时间
     * 优先使用新计算的时间，如果没有则使用原始时间
     */
    private LocalDateTime determineBaseTime(
            RepairConfig.TimeAttribute baseOn,
            LocalDateTime originalCreation, LocalDateTime originalModified, LocalDateTime originalAccess,
            LocalDateTime newCreationTime, LocalDateTime newModifiedTime, LocalDateTime newAccessTime) {

        if (baseOn == null) {
            return newCreationTime != null ? newCreationTime : originalCreation;
        }

        switch (baseOn) {
            case CREATION_TIME:
                return newCreationTime != null ? newCreationTime : originalCreation;
            case MODIFIED_TIME:
                return newModifiedTime != null ? newModifiedTime : originalModified;
            case ACCESS_TIME:
                return newAccessTime != null ? newAccessTime : originalAccess;
            default:
                return newCreationTime != null ? newCreationTime : originalCreation;
        }
    }

    /**
     * 应用随机偏移
     */
    private LocalDateTime applyRandomOffset(LocalDateTime baseTime, RepairConfig.RandomOffset offset) {
        if (offset == null) {
            return baseTime;
        }

        int days = offset.getDaysMin() + RANDOM.nextInt(Math.max(1, offset.getDaysMax() - offset.getDaysMin() + 1));
        int hours = offset.getHoursMin() + RANDOM.nextInt(Math.max(1, offset.getHoursMax() - offset.getHoursMin() + 1));
        int minutes = offset.getMinutesMin() + RANDOM.nextInt(Math.max(1, offset.getMinutesMax() - offset.getMinutesMin() + 1));
        int seconds = offset.getSecondsMin() + RANDOM.nextInt(Math.max(1, offset.getSecondsMax() - offset.getSecondsMin() + 1));

        LocalDateTime newTime = baseTime
                .plusDays(days)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);

        // 确保不超过当前时间
        LocalDateTime now = LocalDateTime.now();
        if (newTime.isAfter(now)) {
            newTime = now.minusSeconds(RANDOM.nextInt(300));
        }

        return newTime;
    }

    /**
     * 获取原始时间
     */
    private LocalDateTime getOriginalTime(
            RepairConfig.TimeAttribute attribute,
            LocalDateTime creation, LocalDateTime modified, LocalDateTime access) {

        switch (attribute) {
            case CREATION_TIME: return creation;
            case MODIFIED_TIME: return modified;
            case ACCESS_TIME: return access;
            default: return creation;
        }
    }

    /**
     * 生成修改时间：创建时间 + 随机 0-30 天
     */
    private LocalDateTime generateModifiedTime(LocalDateTime creationTime) {
        int daysToAdd = RANDOM.nextInt(31); // 0-30 天
        int hoursToAdd = RANDOM.nextInt(25); // 0-24 小时
        int minutesToAdd = RANDOM.nextInt(61); // 0-60 分钟

        LocalDateTime modifiedTime = creationTime
                .plusDays(daysToAdd)
                .plusHours(hoursToAdd)
                .plusMinutes(minutesToAdd);

        // 确保不超过当前时间
        LocalDateTime now = LocalDateTime.now();
        if (modifiedTime.isAfter(now)) {
            modifiedTime = now.minusSeconds(RANDOM.nextInt(3600)); // 当前时间之前 1 小时内
        }

        return modifiedTime;
    }

    /**
     * 生成访问时间：修改时间 + 随机 0-7 天
     */
    private LocalDateTime generateAccessTime(LocalDateTime modifiedTime) {
        int daysToAdd = RANDOM.nextInt(8); // 0-7 天
        int hoursToAdd = RANDOM.nextInt(25); // 0-24 小时
        int minutesToAdd = RANDOM.nextInt(61); // 0-60 分钟

        LocalDateTime accessTime = modifiedTime
                .plusDays(daysToAdd)
                .plusHours(hoursToAdd)
                .plusMinutes(minutesToAdd);

        // 确保不超过当前时间
        LocalDateTime now = LocalDateTime.now();
        if (accessTime.isAfter(now)) {
            accessTime = now.minusSeconds(RANDOM.nextInt(300)); // 当前时间之前 5 分钟内
        }

        return accessTime;
    }

    /**
     * 修复结果
     */
    public static class RepairResult {
        private final LocalDateTime creationTime;
        private final LocalDateTime modifiedTime;
        private final LocalDateTime accessTime;

        public RepairResult(LocalDateTime creationTime, LocalDateTime modifiedTime, LocalDateTime accessTime) {
            this.creationTime = creationTime;
            this.modifiedTime = modifiedTime;
            this.accessTime = accessTime;
        }

        public LocalDateTime getCreationTime() {
            return creationTime;
        }

        public LocalDateTime getModifiedTime() {
            return modifiedTime;
        }

        public LocalDateTime getAccessTime() {
            return accessTime;
        }
    }
}
