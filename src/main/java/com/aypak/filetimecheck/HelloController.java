package com.aypak.filetimecheck;

import com.aypak.filetimecheck.controller.RepairConfigDialogController;
import com.aypak.filetimecheck.model.FileInfo;
import com.aypak.filetimecheck.model.RepairConfig;
import com.aypak.filetimecheck.service.FileScannerTask;
import com.aypak.filetimecheck.service.TimeRepairService;
import com.aypak.filetimecheck.service.TimeValidationService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
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
    @FXML private Button repairSelectedButton;
    @FXML private Button repairAllButton;

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
    private TimeRepairService repairService;
    private RepairConfig currentRepairConfig;  // 保存当前修复配置

    @FXML
    public void initialize() {
        validationService = new TimeValidationService();
        repairService = new TimeRepairService();
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
            updateButtonStates();
        });
    }

    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        boolean hasSelection = !fileTable.getSelectionModel().getSelectedItems().isEmpty();
        boolean hasItems = !fileTable.getItems().isEmpty();

        deleteButton.setDisable(!hasSelection);
        repairSelectedButton.setDisable(!hasSelection);
        repairAllButton.setDisable(!hasItems);
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
                updateButtonStates();
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
     * 处理修复选中按钮点击
     */
    @FXML
    private void handleRepairSelected() {
        ObservableList<FileInfo> selectedItems = fileTable.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            return;
        }

        // 显示配置对话框
        RepairConfig config = showRepairConfigDialog();
        if (config == null) {
            return;  // 用户取消
        }

        currentRepairConfig = config;

        int count = selectedItems.size();
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认修复");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("确定要修复选中的 " + count + " 个文件的时间吗？\n\n" +
                getConfigDescription(config));

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            repairFiles(selectedItems, false);
        }
    }

    /**
     * 处理一键修复按钮点击
     */
    @FXML
    private void handleRepairAll() {
        ObservableList<FileInfo> items = fileTable.getItems();
        if (items.isEmpty()) {
            showAlert("提示", "列表中没有可修复的文件");
            return;
        }

        // 显示配置对话框
        RepairConfig config = showRepairConfigDialog();
        if (config == null) {
            return;  // 用户取消
        }

        currentRepairConfig = config;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认修复");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("确定要修复全部 " + items.size() + " 个文件的时间吗？\n\n" +
                getConfigDescription(config));

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            repairFiles(items, true);
        }
    }

    /**
     * 修复文件时间
     */
    private void repairFiles(ObservableList<FileInfo> files, boolean repairAll) {
        progressLabel.setText("正在修复文件时间...");
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        Task<RepairSummary> repairTask = new Task<RepairSummary>() {
            @Override
            protected RepairSummary call() throws Exception {
                int successCount = 0;
                int failCount = 0;
                List<String> errors = new ArrayList<>();

                int total = files.size();
                for (int i = 0; i < total; i++) {
                    if (isCancelled()) {
                        break;
                    }

                    FileInfo fileInfo = files.get(i);
                    Path filePath = Paths.get(fileInfo.getFilePath());

                    try {
                        // 使用配置修复文件
                        repairService.repairFile(filePath, currentRepairConfig);

                        // 更新 FileInfo 中的时间显示
                        repairService.repairFileInfo(fileInfo, currentRepairConfig);
                        validationService.validateAndUpdate(fileInfo);

                        successCount++;
                        updateMessage(String.format("修复中: %d/%d", i + 1, total));
                    } catch (IOException e) {
                        failCount++;
                        errors.add(filePath.getFileName() + ": " + e.getMessage());
                    }
                }

                return new RepairSummary(successCount, failCount, errors);
            }

            @Override
            protected void succeeded() {
                RepairSummary summary = getValue();
                Platform.runLater(() -> {
                    progressLabel.setText("修复完成");
                    progressBar.setProgress(1.0);

                    // 显示修复结果
                    String message = String.format("修复完成！\n成功: %d 个\n失败: %d 个",
                            summary.successCount, summary.failCount);

                    if (!summary.errors.isEmpty()) {
                        message += "\n\n失败详情:\n" + String.join("\n", summary.errors.subList(0, Math.min(5, summary.errors.size())));
                        if (summary.errors.size() > 5) {
                            message += "\n... 还有 " + (summary.errors.size() - 5) + " 个文件";
                        }
                    }

                    showAlert("修复完成", message);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressLabel.setText("修复失败");
                    progressBar.setProgress(0);
                    showAlert("修复失败", getException().getMessage());
                });
            }
        };

        new Thread(repairTask).start();
    }

    /**
     * 修复结果摘要
     */
    private static class RepairSummary {
        final int successCount;
        final int failCount;
        final List<String> errors;

        RepairSummary(int successCount, int failCount, List<String> errors) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.errors = errors;
        }
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
        updateButtonStates();
    }

    /**
     * 显示警告对话框
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 显示修复配置对话框
     */
    private RepairConfig showRepairConfigDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aypak/filetimecheck/repair-config-dialog.fxml"));
            DialogPane dialogPane = loader.load();

            RepairConfigDialogController controller = loader.getController();

            // 如果有上次配置，加载它；否则使用默认配置
            RepairConfig config = currentRepairConfig != null
                    ? currentRepairConfig
                    : RepairConfig.createDefault();
            controller.setConfig(config);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("时间修复配置");
            dialog.initOwner(dropZone.getScene().getWindow());

            // 处理重置按钮 - 通过查找按钮文本
            for (ButtonType bt : dialogPane.getButtonTypes()) {
                Button button = (Button) dialogPane.lookupButton(bt);
                if ("重置默认".equals(bt.getText())) {
                    button.setOnAction(e -> {
                        controller.setConfig(RepairConfig.createDefault());
                    });
                    break;
                }
            }

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (controller.buildConfigFromUI()) {
                    return controller.getConfig();
                }
            }

        } catch (IOException e) {
            showAlert("错误", "无法加载配置对话框: " + e.getMessage());
        }

        return null;
    }

    /**
     * 获取配置描述
     */
    private String getConfigDescription(RepairConfig config) {
        if (config == null) {
            return "修复规则: 使用默认配置";
        }

        StringBuilder sb = new StringBuilder("修复规则:\n");
        sb.append("• 创建时间: ").append(getTimeConfigDesc(config.getCreationConfig())).append("\n");
        sb.append("• 修改时间: ").append(getTimeConfigDesc(config.getModifiedConfig())).append("\n");
        sb.append("• 访问时间: ").append(getTimeConfigDesc(config.getAccessConfig()));
        return sb.toString();
    }

    /**
     * 获取时间配置描述
     */
    private String getTimeConfigDesc(RepairConfig.TimeConfig timeConfig) {
        if (timeConfig == null) {
            return "保持原始";
        }

        switch (timeConfig.getMode()) {
            case FIXED:
                if (timeConfig.getFixedTime() != null) {
                    return "固定时间 " + timeConfig.getFixedTime().format(DATE_FORMATTER);
                }
                return "保持原始";
            case RANDOM:
                RepairConfig.RandomOffset offset = timeConfig.getRandomOffset();
                String baseDesc = "";
                if (timeConfig.getBaseOnPrevious() != null) {
                    baseDesc = "基于" + getTimeAttributeName(timeConfig.getBaseOnPrevious()) + " + ";
                }
                return String.format("%s随机偏移 %d-%d天 %d-%d小时 %d-%d分钟",
                        baseDesc,
                        offset.getDaysMin(), offset.getDaysMax(),
                        offset.getHoursMin(), offset.getHoursMax(),
                        offset.getMinutesMin(), offset.getMinutesMax());
            case BASED_ON_PREVIOUS:
                if (timeConfig.getBaseOnPrevious() != null) {
                    return "基于" + getTimeAttributeName(timeConfig.getBaseOnPrevious());
                }
                return "保持原始";
            default:
                return "保持原始";
        }
    }

    /**
     * 获取时间属性名称
     */
    private String getTimeAttributeName(RepairConfig.TimeAttribute attribute) {
        switch (attribute) {
            case CREATION_TIME: return "创建时间";
            case MODIFIED_TIME: return "修改时间";
            case ACCESS_TIME: return "访问时间";
            default: return "创建时间";
        }
    }
}
