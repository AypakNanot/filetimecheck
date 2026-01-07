package com.aypak.filetimecheck.service;

import com.aypak.filetimecheck.model.FileInfo;
import com.aypak.filetimecheck.model.ValidationResult;

import java.time.LocalDateTime;

/**
 * 文件时间校验服务
 */
public class TimeValidationService {

    /**
     * 校验文件时间是否合理
     */
    public ValidationResult validate(FileInfo fileInfo) {
        LocalDateTime created = fileInfo.getCreationTime();
        LocalDateTime modified = fileInfo.getLastModifiedTime();
        LocalDateTime accessed = fileInfo.getLastAccessTime();

        // 如果有任何时间为 null，返回正常（某些文件系统可能不支持某些时间属性）
        if (created == null || modified == null || accessed == null) {
            return ValidationResult.NORMAL;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime epoch = LocalDateTime.of(1970, 1, 1, 0, 0);

        // 检查未来时间
        if (created.isAfter(now) || modified.isAfter(now) || accessed.isAfter(now)) {
            return ValidationResult.FUTURE_TIME;
        }

        // 检查 1970 年之前
        if (created.isBefore(epoch) || modified.isBefore(epoch) || accessed.isBefore(epoch)) {
            return ValidationResult.PRE_1970;
        }

        // 检查修改时间是否早于创建时间
        if (modified.isBefore(created)) {
            return ValidationResult.MODIFIED_BEFORE_CREATED;
        }

        // 检查访问时间是否早于修改时间
        if (accessed.isBefore(modified)) {
            return ValidationResult.ACCESSED_BEFORE_MODIFIED;
        }

        return ValidationResult.NORMAL;
    }

    /**
     * 校验并更新 FileInfo 对象的状态
     */
    public void validateAndUpdate(FileInfo fileInfo) {
        ValidationResult result = validate(fileInfo);
        fileInfo.setStatus(result.getDisplayName());
        if (!result.isValid()) {
            fileInfo.setErrorMessage(result.getDisplayName());
        } else {
            fileInfo.setErrorMessage("");
        }
    }
}
