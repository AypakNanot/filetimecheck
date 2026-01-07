package com.aypak.filetimecheck;

import com.aypak.filetimecheck.model.FileInfo;
import com.aypak.filetimecheck.service.FileScannerTask;
import com.aypak.filetimecheck.service.TimeValidationService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 文件时间校验工具控制器
 */
public class HelloController {

    @FXML private StackPane dropZone;
    @FXML private ListView<String> sourceListView;
    @FXML private TableView<FileInfo> fileTable;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Label totalLabel;
    @FXML private Label normalLabel;
    @FXML private Label abnormalLabel;
    @FXML private Button clearButton;
    @FXML private Button reloadButton;
    @FXML private Button validateButton;
    @FXML private Button deleteButton;
    @FXML private Button scanAllButton;
    @FXML private Button clearSourceButton;

    @FXML private TableColumn<FileInfo, String> pathColumn;
    @FXML private TableColumn<FileInfo, String> creationTimeColumn;
    @FXML private TableColumn<FileInfo, String> modifiedTimeColumn;
    @FXML private TableColumn<FileInfo, String> accessTimeColumn;
    @FXML private TableColumn<FileInfo, String> statusColumn;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Task<ObservableList<FileInfo>> currentTask;
    private List<Path> sourcePaths;
    private ObservableList<String> sourceListItems;
    private TimeValidationService validationService;

    @FXML
    public void initialize() {
        validationService = new TimeValidationService();
        sourcePaths = new ArrayList<>();
        sourceListItems = FXCollections.observableArrayList();
        sourceListView.setItems(sourceListItems);

        // 设置 TableView 为多选模式
        fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setupTableColumns();
        setupDragAndDrop();
        setupSelectionListener();
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
        // 为 dropZone 及其所有子节点设置拖拽处理
        setupDragAndDropForNode(dropZone);
    }

    /**
     * 递归为节点及其子节点设置拖拽处理
     */
    private void setupDragAndDropForNode(javafx.scene.Node node) {
        node.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        node.setOnDragEntered(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                dropZone.getStyleClass().add("drop-zone-drag-over");
            }
            event.consume();
        });

        node.setOnDragExited(event -> {
            dropZone.getStyleClass().remove("drop-zone-drag-over");
            event.consume();
        });

        node.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                handleFiles(db.getFiles());
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

        // 如果是 Parent，递归处理子节点
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                setupDragAndDropForNode(child);
            }
        }
    }

    /**
     * 设置表格选择监听器
     */
    private void setupSelectionListener() {
        TableView.TableViewSelectionModel<FileInfo> selectionModel = fileTable.getSelectionModel();
        selectionModel.getSelectedItems().addListener((ListChangeListener<FileInfo>) change -> {
            updateDeleteButtonState();
        });
    }

    /**
     * 更新删除按钮状态
     */
    private void updateDeleteButtonState() {
        boolean hasSelection = !fileTable.getSelectionModel().getSelectedItems().isEmpty();
        deleteButton.setDisable(!hasSelection);
    }

    /**
     * 处理选择文件按钮点击（支持多选）
     */
    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择要校验的文件");
        List<File> files = fileChooser.showOpenMultipleDialog(dropZone.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            handleFiles(files);
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
     * 处理文件列表（拖拽或选择）- 添加到源文件列表
     */
    private void handleFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        // 将文件添加到源列表
        for (File file : files) {
            Path path = Paths.get(file.getAbsolutePath());
            String displayPath = getDisplayPath(file);

            // 检查是否已存在
            if (!sourcePaths.contains(path)) {
                sourcePaths.add(path);
                sourceListItems.add(displayPath);
            }
        }

        // 更新按钮状态
        updateSourceButtons();
    }

    /**
     * 获取文件显示路径
     */
    private String getDisplayPath(File file) {
        String path = file.getAbsolutePath();
        boolean isDirectory = file.isDirectory();

        // 如果是文件夹，显示 "文件夹: 路径"
        if (isDirectory) {
            return "[文件夹] " + path;
        } else {
            return "[文件] " + path;
        }
    }

    /**
     * 更新源文件列表按钮状态
     */
    private void updateSourceButtons() {
        boolean hasSources = !sourcePaths.isEmpty();
        scanAllButton.setDisable(!hasSources);
        clearSourceButton.setDisable(!hasSources);
    }

    /**
     * 处理扫描全部按钮点击
     */
    @FXML
    private void handleScanAll() {
        if (sourcePaths.isEmpty()) {
            showAlert("提示", "没有可扫描的文件");
            return;
        }

        // 如果有正在运行的任务，先取消
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
        }

        // 清空当前表格
        fileTable.getItems().clear();
        resetStatistics();

        startScan(sourcePaths);
    }

    /**
     * 处理清空源文件列表按钮点击
     */
    @FXML
    private void handleClearSource() {
        sourcePaths.clear();
        sourceListItems.clear();
        updateSourceButtons();
    }

    /**
     * 开始扫描文件（支持多路径）
     */
    private void startScan(List<Path> paths) {
        FileScannerTask task = new FileScannerTask(paths);
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
                enableActionButtons(true);
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
        enableActionButtons(false);
    }

    /**
     * 启用/禁用操作按钮
     */
    private void enableActionButtons(boolean enable) {
        clearButton.setDisable(!enable);
        reloadButton.setDisable(!enable || sourcePaths.isEmpty());
        validateButton.setDisable(!enable);
    }

    /**
     * 处理重新加载按钮点击
     */
    @FXML
    private void handleReload() {
        if (sourcePaths.isEmpty()) {
            showAlert("提示", "没有可重新加载的文件");
            return;
        }

        // 如果有正在运行的任务，先取消
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
        }

        // 清空当前表格
        fileTable.getItems().clear();
        resetStatistics();

        startScan(sourcePaths);
    }

    /**
     * 处理手动校验按钮点击
     */
    @FXML
    private void handleValidate() {
        ObservableList<FileInfo> items = fileTable.getItems();
        if (items.isEmpty()) {
            showAlert("提示", "列表中没有可校验的文件");
            return;
        }

        progressLabel.setText("正在校验...");
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        // 在后台线程执行校验
        Task<Void> validateTask = new Task<Void>() {
            @Override
            protected Void call() {
                for (FileInfo fileInfo : items) {
                    if (isCancelled()) {
                        break;
                    }
                    validationService.validateAndUpdate(fileInfo);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    updateStatistics(items);
                    progressLabel.setText("校验完成");
                    progressBar.setProgress(1.0);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressLabel.setText("校验失败");
                    progressBar.setProgress(0);
                });
            }
        };

        new Thread(validateTask).start();
    }

    /**
     * 处理删除选中按钮点击
     */
    @FXML
    private void handleDeleteSelected() {
        ObservableList<FileInfo> selectedItems = fileTable.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            return;
        }

        int count = selectedItems.size();
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("确定要删除选中的 " + count + " 项吗？");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 创建新列表避免并发修改异常
            ObservableList<FileInfo> itemsToRemove = FXCollections.observableArrayList(selectedItems);
            fileTable.getItems().removeAll(itemsToRemove);
            updateStatistics(fileTable.getItems());
        }
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
        deleteButton.setDisable(true);
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
