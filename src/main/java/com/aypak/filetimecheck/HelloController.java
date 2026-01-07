package com.aypak.filetimecheck;

import com.aypak.filetimecheck.model.FileInfo;
import com.aypak.filetimecheck.service.FileScannerTask;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 文件时间校验工具控制器
 */
public class HelloController {

    @FXML private StackPane dropZone;
    @FXML private TableView<FileInfo> fileTable;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Label totalLabel;
    @FXML private Label normalLabel;
    @FXML private Label abnormalLabel;
    @FXML private Button clearButton;

    @FXML private TableColumn<FileInfo, String> pathColumn;
    @FXML private TableColumn<FileInfo, String> creationTimeColumn;
    @FXML private TableColumn<FileInfo, String> modifiedTimeColumn;
    @FXML private TableColumn<FileInfo, String> accessTimeColumn;
    @FXML private TableColumn<FileInfo, String> statusColumn;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Task<ObservableList<FileInfo>> currentTask;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupDragAndDrop();
    }

    /**
     * 设置表格列的数据绑定
     */
    private void setupTableColumns() {
        pathColumn.setCellValueFactory(cellData -> cellData.getValue().filePathProperty());

        creationTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime time = cellData.getValue().getCreationTime();
            return new javafx.beans.property.SimpleStringProperty(
                    time != null ? time.format(DATE_FORMATTER) : "N/A"
            );
        });

        modifiedTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime time = cellData.getValue().getLastModifiedTime();
            return new javafx.beans.property.SimpleStringProperty(
                    time != null ? time.format(DATE_FORMATTER) : "N/A"
            );
        });

        accessTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime time = cellData.getValue().getLastAccessTime();
            return new javafx.beans.property.SimpleStringProperty(
                    time != null ? time.format(DATE_FORMATTER) : "N/A"
            );
        });

        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // 设置状态列的样式工厂
        statusColumn.setCellFactory(column -> new TableCell<FileInfo, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("正常".equals(status)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    /**
     * 设置拖拽功能
     */
    private void setupDragAndDrop() {
        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropZone.setOnDragEntered(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                dropZone.getStyleClass().add("drop-zone-drag-over");
            }
            event.consume();
        });

        dropZone.setOnDragExited(event -> {
            dropZone.getStyleClass().remove("drop-zone-drag-over");
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                handleFiles(db.getFiles());
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * 处理选择文件按钮点击
     */
    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择要校验的文件");
        File file = fileChooser.showOpenDialog(dropZone.getScene().getWindow());
        if (file != null) {
            handleFiles(java.util.List.of(file));
        }
    }

    /**
     * 处理选择文件夹按钮点击
     */
    @FXML
    private void handleSelectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择要校验的文件夹");
        File directory = directoryChooser.showDialog(dropZone.getScene().getWindow());
        if (directory != null) {
            handleFiles(java.util.List.of(directory));
        }
    }

    /**
     * 处理文件列表（拖拽或选择）
     */
    private void handleFiles(java.util.List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        // 如果有正在运行的任务，先取消
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
        }

        // 清空当前表格
        fileTable.getItems().clear();
        resetStatistics();

        // 只处理第一个文件/文件夹
        File firstFile = files.get(0);
        Path path = Paths.get(firstFile.getAbsolutePath());
        startScan(path);
    }

    /**
     * 开始扫描文件
     */
    private void startScan(Path path) {
        FileScannerTask task = new FileScannerTask(path);
        currentTask = task;

        // 绑定进度条
        progressBar.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());

        // 扫描完成后的处理
        task.setOnSucceeded(e -> {
            javafx.collections.ObservableList<FileInfo> result = task.getValue();
            Platform.runLater(() -> {
                fileTable.setItems(result);
                updateStatistics(result);
                clearButton.setDisable(false);
                progressLabel.textProperty().unbind();
                progressLabel.setText("扫描完成");
                progressBar.progressProperty().unbind();
                progressBar.setProgress(1.0);
            });
        });

        // 扫描失败的处理
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressLabel.textProperty().unbind();
                progressLabel.setText("扫描失败: " + task.getException().getMessage());
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
                showAlert("扫描失败", task.getException().getMessage());
            });
        });

        // 扫描取消的处理
        task.setOnCancelled(e -> {
            Platform.runLater(() -> {
                progressLabel.textProperty().unbind();
                progressLabel.setText("扫描已取消");
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
            });
        });

        // 在后台线程执行
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics(javafx.collections.ObservableList<FileInfo> files) {
        int total = files.size();
        long normal = files.stream().filter(FileInfo::isNormal).count();
        long abnormal = total - normal;

        totalLabel.setText("总文件数: " + total);
        normalLabel.setText("正常: " + normal);
        abnormalLabel.setText("异常: " + abnormal);
    }

    /**
     * 重置统计信息
     */
    private void resetStatistics() {
        totalLabel.setText("总文件数: 0");
        normalLabel.setText("正常: 0");
        abnormalLabel.setText("异常: 0");
        clearButton.setDisable(true);
    }

    /**
     * 处理清空按钮点击
     */
    @FXML
    private void handleClear() {
        // 取消正在运行的任务
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
        }

        fileTable.getItems().clear();
        resetStatistics();
        progressLabel.setText("就绪");
        progressBar.setProgress(0);
    }

    /**
     * 显示警告对话框
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
