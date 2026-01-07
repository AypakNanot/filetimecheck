package com.aypak.filetimecheck.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * 文件信息数据模型
 * 封装文件的路径、时间属性和校验结果
 */
public class FileInfo {
    private final StringProperty filePath;
    private final ObjectProperty<LocalDateTime> creationTime;
    private final ObjectProperty<LocalDateTime> lastModifiedTime;
    private final ObjectProperty<LocalDateTime> lastAccessTime;
    private final StringProperty status;
    private final StringProperty errorMessage;

    public FileInfo() {
        this.filePath = new SimpleStringProperty();
        this.creationTime = new SimpleObjectProperty<>();
        this.lastModifiedTime = new SimpleObjectProperty<>();
        this.lastAccessTime = new SimpleObjectProperty<>();
        this.status = new SimpleStringProperty("未校验");
        this.errorMessage = new SimpleStringProperty("");
    }

    public FileInfo(String filePath, LocalDateTime creationTime, LocalDateTime lastModifiedTime, LocalDateTime lastAccessTime) {
        this();
        this.filePath.set(filePath);
        this.creationTime.set(creationTime);
        this.lastModifiedTime.set(lastModifiedTime);
        this.lastAccessTime.set(lastAccessTime);
    }

    // Property getters
    public StringProperty filePathProperty() { return filePath; }
    public ObjectProperty<LocalDateTime> creationTimeProperty() { return creationTime; }
    public ObjectProperty<LocalDateTime> lastModifiedTimeProperty() { return lastModifiedTime; }
    public ObjectProperty<LocalDateTime> lastAccessTimeProperty() { return lastAccessTime; }
    public StringProperty statusProperty() { return status; }
    public StringProperty errorMessageProperty() { return errorMessage; }

    // Getters and Setters
    public String getFilePath() { return filePath.get(); }
    public void setFilePath(String filePath) { this.filePath.set(filePath); }

    public LocalDateTime getCreationTime() { return creationTime.get(); }
    public void setCreationTime(LocalDateTime creationTime) { this.creationTime.set(creationTime); }

    public LocalDateTime getLastModifiedTime() { return lastModifiedTime.get(); }
    public void setLastModifiedTime(LocalDateTime lastModifiedTime) { this.lastModifiedTime.set(lastModifiedTime); }

    public LocalDateTime getLastAccessTime() { return lastAccessTime.get(); }
    public void setLastAccessTime(LocalDateTime lastAccessTime) { this.lastAccessTime.set(lastAccessTime); }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    public String getErrorMessage() { return errorMessage.get(); }
    public void setErrorMessage(String errorMessage) { this.errorMessage.set(errorMessage); }

    public boolean isNormal() {
        return "正常".equals(status.get());
    }
}
