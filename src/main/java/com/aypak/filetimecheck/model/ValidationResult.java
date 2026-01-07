package com.aypak.filetimecheck.model;

/**
 * 文件时间校验结果枚举
 */
public enum ValidationResult {
    NORMAL("正常", true),
    MODIFIED_BEFORE_CREATED("修改时间早于创建时间", false),
    ACCESSED_BEFORE_MODIFIED("访问时间早于修改时间", false),
    FUTURE_TIME("检测到未来时间", false),
    PRE_1970("检测到1970年之前的时间", false);

    private final String displayName;
    private final boolean valid;

    ValidationResult(String displayName, boolean valid) {
        this.displayName = displayName;
        this.valid = valid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isValid() {
        return valid;
    }
}
