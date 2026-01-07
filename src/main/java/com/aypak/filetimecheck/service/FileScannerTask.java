package com.aypak.filetimecheck.service;

import com.aypak.filetimecheck.model.FileInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件扫描异步任务
 * 在后台线程中递归扫描文件夹，收集所有文件的时间信息
 */
public class FileScannerTask extends Task<ObservableList<FileInfo>> {

    private final List<Path> paths;
    private final TimeValidationService validationService;

    /**
     * 单路径构造函数
     */
    public FileScannerTask(Path path) {
        this.paths = java.util.List.of(path);
        this.validationService = new TimeValidationService();
    }

    /**
     * 多路径构造函数
     */
    public FileScannerTask(List<Path> paths) {
        this.paths = new ArrayList<>(paths);
        this.validationService = new TimeValidationService();
    }

    @Override
    protected ObservableList<FileInfo> call() throws Exception {
        ObservableList<FileInfo> result = FXCollections.observableArrayList();
        List<Path> filesToScan = new ArrayList<>();

        // 收集所有需要扫描的文件
        for (Path path : paths) {
            if (isCancelled()) {
                break;
            }

            if (Files.isDirectory(path)) {
                try (Stream<Path> stream = Files.walk(path)) {
                    List<Path> dirFiles = stream
                            .filter(Files::isRegularFile)
                            .collect(Collectors.toList());
                    filesToScan.addAll(dirFiles);
                }
            } else if (Files.isRegularFile(path)) {
                filesToScan.add(path);
            } else {
                System.err.println("无效的文件路径: " + path);
            }
        }

        final int total = filesToScan.size();

        // 扫描每个文件
        for (int i = 0; i < total; i++) {
            if (isCancelled()) {
                updateMessage("扫描已取消");
                break;
            }

            Path filePath = filesToScan.get(i);
            try {
                FileInfo fileInfo = readFileInfo(filePath);
                validationService.validateAndUpdate(fileInfo);
                result.add(fileInfo);
            } catch (IOException e) {
                // 跳过无法读取的文件
                System.err.println("无法读取文件: " + filePath + " - " + e.getMessage());
            }

            updateProgress(i + 1, total);
            updateMessage(String.format("扫描中: %d/%d", i + 1, total));
        }

        return result;
    }

    /**
     * 读取文件的时间信息
     */
    private FileInfo readFileInfo(Path filePath) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(
                filePath,
                BasicFileAttributes.class
        );

        ZoneId zoneId = ZoneId.systemDefault();

        LocalDateTime creationTime = LocalDateTime.ofInstant(
                attrs.creationTime().toInstant(),
                zoneId
        );

        LocalDateTime lastModifiedTime = LocalDateTime.ofInstant(
                attrs.lastModifiedTime().toInstant(),
                zoneId
        );

        LocalDateTime lastAccessTime = LocalDateTime.ofInstant(
                attrs.lastAccessTime().toInstant(),
                zoneId
        );

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilePath(filePath.toString());
        fileInfo.setCreationTime(creationTime);
        fileInfo.setLastModifiedTime(lastModifiedTime);
        fileInfo.setLastAccessTime(lastAccessTime);

        return fileInfo;
    }
}
