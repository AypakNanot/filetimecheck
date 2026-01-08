package com.aypak.filetimecheck.controller;

import com.aypak.filetimecheck.model.RepairConfig;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 修复配置对话框控制器
 */
public class RepairConfigDialogController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ========== 创建时间组件 ==========
    @FXML private ToggleGroup creationModeGroup;
    @FXML private RadioButton creationBasedRadio;
    @FXML private RadioButton creationFixedRadio;
    @FXML private RadioButton creationRandomRadio;
    @FXML private VBox creationFixedPane;
    @FXML private VBox creationRandomPane;
    @FXML private DatePicker creationFixedDatePicker;
    @FXML private TextField creationFixedTimeField;
    @FXML private TextField creationRandomDaysMin;
    @FXML private TextField creationRandomDaysMax;
    @FXML private TextField creationRandomHoursMin;
    @FXML private TextField creationRandomHoursMax;
    @FXML private TextField creationRandomMinutesMin;
    @FXML private TextField creationRandomMinutesMax;
    @FXML private TextField creationRandomSecondsMin;
    @FXML private TextField creationRandomSecondsMax;

    // ========== 修改时间组件 ==========
    @FXML private ToggleGroup modifiedModeGroup;
    @FXML private RadioButton modifiedBasedRadio;
    @FXML private RadioButton modifiedFixedRadio;
    @FXML private RadioButton modifiedRandomRadio;
    @FXML private VBox modifiedBasedPane;
    @FXML private VBox modifiedFixedPane;
    @FXML private VBox modifiedRandomPane;
    @FXML private ChoiceBox<String> modifiedBasedChoice;
    @FXML private Label modifiedRandomBaseLabel;
    @FXML private DatePicker modifiedFixedDatePicker;
    @FXML private TextField modifiedFixedTimeField;
    @FXML private TextField modifiedRandomDaysMin;
    @FXML private TextField modifiedRandomDaysMax;
    @FXML private TextField modifiedRandomHoursMin;
    @FXML private TextField modifiedRandomHoursMax;
    @FXML private TextField modifiedRandomMinutesMin;
    @FXML private TextField modifiedRandomMinutesMax;
    @FXML private TextField modifiedRandomSecondsMin;
    @FXML private TextField modifiedRandomSecondsMax;

    // ========== 访问时间组件 ==========
    @FXML private ToggleGroup accessModeGroup;
    @FXML private RadioButton accessBasedRadio;
    @FXML private RadioButton accessFixedRadio;
    @FXML private RadioButton accessRandomRadio;
    @FXML private VBox accessBasedPane;
    @FXML private VBox accessFixedPane;
    @FXML private VBox accessRandomPane;
    @FXML private ChoiceBox<String> accessBasedChoice;
    @FXML private Label accessRandomBaseLabel;
    @FXML private DatePicker accessFixedDatePicker;
    @FXML private TextField accessFixedTimeField;
    @FXML private TextField accessRandomDaysMin;
    @FXML private TextField accessRandomDaysMax;
    @FXML private TextField accessRandomHoursMin;
    @FXML private TextField accessRandomHoursMax;
    @FXML private TextField accessRandomMinutesMin;
    @FXML private TextField accessRandomMinutesMax;
    @FXML private TextField accessRandomSecondsMin;
    @FXML private TextField accessRandomSecondsMax;

    @FXML private ButtonType resetButton;
    @FXML private DialogPane dialogPane;

    private RepairConfig config;

    @FXML
    public void initialize() {
        setupChoiceBoxes();
        setupBindings();
        setupValidators();
    }

    /**
     * 设置下拉选择框
     */
    private void setupChoiceBoxes() {
        modifiedBasedChoice.getItems().addAll("创建时间", "修改时间", "访问时间");
        modifiedBasedChoice.getSelectionModel().selectFirst();

        accessBasedChoice.getItems().addAll("创建时间", "修改时间", "访问时间");
        accessBasedChoice.getSelectionModel().select(1); // 默认选择修改时间
    }

    /**
     * 设置 UI 绑定
     */
    private void setupBindings() {
        // 创建时间模式绑定
        creationBasedRadio.selectedProperty().addListener((obs, old, newVal) -> {
            creationFixedPane.setVisible(false);
            creationFixedPane.setManaged(false);
            creationRandomPane.setVisible(false);
            creationRandomPane.setManaged(false);
        });

        creationFixedRadio.selectedProperty().addListener((obs, old, newVal) -> {
            creationFixedPane.setVisible(newVal);
            creationFixedPane.setManaged(newVal);
            creationRandomPane.setVisible(false);
            creationRandomPane.setManaged(false);
        });

        creationRandomRadio.selectedProperty().addListener((obs, old, newVal) -> {
            creationFixedPane.setVisible(false);
            creationFixedPane.setManaged(false);
            creationRandomPane.setVisible(newVal);
            creationRandomPane.setManaged(newVal);
        });

        // 修改时间模式绑定
        modifiedBasedRadio.selectedProperty().addListener((obs, old, newVal) -> {
            modifiedBasedPane.setVisible(newVal);
            modifiedBasedPane.setManaged(newVal);
            modifiedFixedPane.setVisible(false);
            modifiedFixedPane.setManaged(false);
            modifiedRandomPane.setVisible(false);
            modifiedRandomPane.setManaged(false);
        });

        modifiedFixedRadio.selectedProperty().addListener((obs, old, newVal) -> {
            modifiedBasedPane.setVisible(false);
            modifiedBasedPane.setManaged(false);
            modifiedFixedPane.setVisible(newVal);
            modifiedFixedPane.setManaged(newVal);
            modifiedRandomPane.setVisible(false);
            modifiedRandomPane.setManaged(false);
        });

        modifiedRandomRadio.selectedProperty().addListener((obs, old, newVal) -> {
            modifiedBasedPane.setVisible(false);
            modifiedBasedPane.setManaged(false);
            modifiedFixedPane.setVisible(false);
            modifiedFixedPane.setManaged(false);
            modifiedRandomPane.setVisible(newVal);
            modifiedRandomPane.setManaged(newVal);
        });

        // 访问时间模式绑定
        accessBasedRadio.selectedProperty().addListener((obs, old, newVal) -> {
            accessBasedPane.setVisible(newVal);
            accessBasedPane.setManaged(newVal);
            accessFixedPane.setVisible(false);
            accessFixedPane.setManaged(false);
            accessRandomPane.setVisible(false);
            accessRandomPane.setManaged(false);
        });

        accessFixedRadio.selectedProperty().addListener((obs, old, newVal) -> {
            accessBasedPane.setVisible(false);
            accessBasedPane.setManaged(false);
            accessFixedPane.setVisible(newVal);
            accessFixedPane.setManaged(newVal);
            accessRandomPane.setVisible(false);
            accessRandomPane.setManaged(false);
        });

        accessRandomRadio.selectedProperty().addListener((obs, old, newVal) -> {
            accessBasedPane.setVisible(false);
            accessBasedPane.setManaged(false);
            accessFixedPane.setVisible(false);
            accessFixedPane.setManaged(false);
            accessRandomPane.setVisible(newVal);
            accessRandomPane.setManaged(newVal);
        });

        // 监听基于选择变化，更新随机偏移标签
        modifiedBasedChoice.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            updateRandomBaseLabel(modifiedRandomBaseLabel, newVal);
        });

        accessBasedChoice.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            updateRandomBaseLabel(accessRandomBaseLabel, newVal);
        });
    }

    private void updateRandomBaseLabel(Label label, String baseName) {
        label.setText("基于: " + baseName);
    }

    /**
     * 设置输入验证
     */
    private void setupValidators() {
        // 设置数字文本框验证
        setupNumericFieldValidation(creationRandomDaysMin, creationRandomDaysMax, 0, 365);
        setupNumericFieldValidation(creationRandomHoursMin, creationRandomHoursMax, 0, 23);
        setupNumericFieldValidation(creationRandomMinutesMin, creationRandomMinutesMax, 0, 59);
        setupNumericFieldValidation(creationRandomSecondsMin, creationRandomSecondsMax, 0, 59);

        setupNumericFieldValidation(modifiedRandomDaysMin, modifiedRandomDaysMax, 0, 365);
        setupNumericFieldValidation(modifiedRandomHoursMin, modifiedRandomHoursMax, 0, 23);
        setupNumericFieldValidation(modifiedRandomMinutesMin, modifiedRandomMinutesMax, 0, 59);
        setupNumericFieldValidation(modifiedRandomSecondsMin, modifiedRandomSecondsMax, 0, 59);

        setupNumericFieldValidation(accessRandomDaysMin, accessRandomDaysMax, 0, 365);
        setupNumericFieldValidation(accessRandomHoursMin, accessRandomHoursMax, 0, 23);
        setupNumericFieldValidation(accessRandomMinutesMin, accessRandomMinutesMax, 0, 59);
        setupNumericFieldValidation(accessRandomSecondsMin, accessRandomSecondsMax, 0, 59);
    }

    private void setupNumericFieldValidation(TextField minField, TextField maxField, int min, int max) {
        minField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*")) {
                minField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        maxField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*")) {
                maxField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    /**
     * 处理重置按钮
     */
    @FXML
    private void handleReset() {
        loadDefaultConfig();
    }

    /**
     * 获取配置
     */
    public RepairConfig getConfig() {
        return config;
    }

    /**
     * 设置配置并加载到 UI
     */
    public void setConfig(RepairConfig config) {
        this.config = config;
        if (config != null) {
            loadConfigToUI();
        }
    }

    /**
     * 加载默认配置
     */
    private void loadDefaultConfig() {
        setConfig(RepairConfig.createDefault());
    }

    /**
     * 从 UI 构建配置
     */
    public boolean buildConfigFromUI() {
        if (config == null) {
            config = new RepairConfig();
        }

        try {
            // 构建创建时间配置
            RepairConfig.TimeConfig creationConfig = new RepairConfig.TimeConfig();
            if (creationBasedRadio.isSelected()) {
                creationConfig.setMode(RepairConfig.TimeMode.BASED_ON_PREVIOUS);
            } else if (creationFixedRadio.isSelected()) {
                creationConfig.setMode(RepairConfig.TimeMode.FIXED);
                creationConfig.setFixedTime(parseFixedTime(creationFixedDatePicker, creationFixedTimeField));
            } else if (creationRandomRadio.isSelected()) {
                creationConfig.setMode(RepairConfig.TimeMode.RANDOM);
                creationConfig.setRandomOffset(parseRandomOffset(
                        creationRandomDaysMin, creationRandomDaysMax,
                        creationRandomHoursMin, creationRandomHoursMax,
                        creationRandomMinutesMin, creationRandomMinutesMax,
                        creationRandomSecondsMin, creationRandomSecondsMax
                ));
            }
            config.setCreationConfig(creationConfig);

            // 构建修改时间配置
            RepairConfig.TimeConfig modifiedConfig = new RepairConfig.TimeConfig();
            if (modifiedBasedRadio.isSelected()) {
                modifiedConfig.setMode(RepairConfig.TimeMode.BASED_ON_PREVIOUS);
                modifiedConfig.setBaseOnPrevious(getSelectedTimeAttribute(modifiedBasedChoice));
            } else if (modifiedFixedRadio.isSelected()) {
                modifiedConfig.setMode(RepairConfig.TimeMode.FIXED);
                modifiedConfig.setFixedTime(parseFixedTime(modifiedFixedDatePicker, modifiedFixedTimeField));
            } else if (modifiedRandomRadio.isSelected()) {
                modifiedConfig.setMode(RepairConfig.TimeMode.RANDOM);
                modifiedConfig.setBaseOnPrevious(RepairConfig.TimeAttribute.CREATION_TIME); // 默认基于创建时间
                modifiedConfig.setRandomOffset(parseRandomOffset(
                        modifiedRandomDaysMin, modifiedRandomDaysMax,
                        modifiedRandomHoursMin, modifiedRandomHoursMax,
                        modifiedRandomMinutesMin, modifiedRandomMinutesMax,
                        modifiedRandomSecondsMin, modifiedRandomSecondsMax
                ));
            }
            config.setModifiedConfig(modifiedConfig);

            // 构建访问时间配置
            RepairConfig.TimeConfig accessConfig = new RepairConfig.TimeConfig();
            if (accessBasedRadio.isSelected()) {
                accessConfig.setMode(RepairConfig.TimeMode.BASED_ON_PREVIOUS);
                accessConfig.setBaseOnPrevious(getSelectedTimeAttribute(accessBasedChoice));
            } else if (accessFixedRadio.isSelected()) {
                accessConfig.setMode(RepairConfig.TimeMode.FIXED);
                accessConfig.setFixedTime(parseFixedTime(accessFixedDatePicker, accessFixedTimeField));
            } else if (accessRandomRadio.isSelected()) {
                accessConfig.setMode(RepairConfig.TimeMode.RANDOM);
                accessConfig.setBaseOnPrevious(RepairConfig.TimeAttribute.MODIFIED_TIME); // 默认基于修改时间
                accessConfig.setRandomOffset(parseRandomOffset(
                        accessRandomDaysMin, accessRandomDaysMax,
                        accessRandomHoursMin, accessRandomHoursMax,
                        accessRandomMinutesMin, accessRandomMinutesMax,
                        accessRandomSecondsMin, accessRandomSecondsMax
                ));
            }
            config.setAccessConfig(accessConfig);

            return true;
        } catch (Exception e) {
            showError("配置错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从配置加载到 UI
     */
    private void loadConfigToUI() {
        // 加载创建时间配置
        loadTimeConfigToUI(
                config.getCreationConfig(),
                creationBasedRadio, creationFixedRadio, creationRandomRadio,
                creationFixedPane, creationRandomPane,
                creationFixedDatePicker, creationFixedTimeField,
                creationRandomDaysMin, creationRandomDaysMax,
                creationRandomHoursMin, creationRandomHoursMax,
                creationRandomMinutesMin, creationRandomMinutesMax,
                creationRandomSecondsMin, creationRandomSecondsMax
        );

        // 加载修改时间配置
        loadTimeConfigToUI(
                config.getModifiedConfig(),
                modifiedBasedRadio, modifiedFixedRadio, modifiedRandomRadio,
                modifiedFixedPane, modifiedRandomPane,
                modifiedFixedDatePicker, modifiedFixedTimeField,
                modifiedRandomDaysMin, modifiedRandomDaysMax,
                modifiedRandomHoursMin, modifiedRandomHoursMax,
                modifiedRandomMinutesMin, modifiedRandomMinutesMax,
                modifiedRandomSecondsMin, modifiedRandomSecondsMax
        );

        // 访问时间需要额外处理基于选择
        if (config.getModifiedConfig() != null && config.getModifiedConfig().getBaseOnPrevious() != null) {
            modifiedBasedChoice.getSelectionModel().select(getTimeAttributeName(config.getModifiedConfig().getBaseOnPrevious()));
        }

        // 加载访问时间配置
        loadTimeConfigToUI(
                config.getAccessConfig(),
                accessBasedRadio, accessFixedRadio, accessRandomRadio,
                accessFixedPane, accessRandomPane,
                accessFixedDatePicker, accessFixedTimeField,
                accessRandomDaysMin, accessRandomDaysMax,
                accessRandomHoursMin, accessRandomHoursMax,
                accessRandomMinutesMin, accessRandomMinutesMax,
                accessRandomSecondsMin, accessRandomSecondsMax
        );

        if (config.getAccessConfig() != null && config.getAccessConfig().getBaseOnPrevious() != null) {
            accessBasedChoice.getSelectionModel().select(getTimeAttributeName(config.getAccessConfig().getBaseOnPrevious()));
        }
    }

    private void loadTimeConfigToUI(
            RepairConfig.TimeConfig timeConfig,
            RadioButton basedRadio, RadioButton fixedRadio, RadioButton randomRadio,
            VBox fixedPane, VBox randomPane,
            DatePicker datePicker, TextField timeField,
            TextField daysMin, TextField daysMax,
            TextField hoursMin, TextField hoursMax,
            TextField minutesMin, TextField minutesMax,
            TextField secondsMin, TextField secondsMax) {

        if (timeConfig == null) {
            basedRadio.setSelected(true);
            return;
        }

        switch (timeConfig.getMode()) {
            case BASED_ON_PREVIOUS:
                basedRadio.setSelected(true);
                break;
            case FIXED:
                fixedRadio.setSelected(true);
                if (timeConfig.getFixedTime() != null) {
                    datePicker.setValue(timeConfig.getFixedTime().toLocalDate());
                    timeField.setText(timeConfig.getFixedTime().toLocalTime().format(TIME_FORMATTER));
                }
                break;
            case RANDOM:
                randomRadio.setSelected(true);
                if (timeConfig.getRandomOffset() != null) {
                    RepairConfig.RandomOffset offset = timeConfig.getRandomOffset();
                    daysMin.setText(String.valueOf(offset.getDaysMin()));
                    daysMax.setText(String.valueOf(offset.getDaysMax()));
                    hoursMin.setText(String.valueOf(offset.getHoursMin()));
                    hoursMax.setText(String.valueOf(offset.getHoursMax()));
                    minutesMin.setText(String.valueOf(offset.getMinutesMin()));
                    minutesMax.setText(String.valueOf(offset.getMinutesMax()));
                    secondsMin.setText(String.valueOf(offset.getSecondsMin()));
                    secondsMax.setText(String.valueOf(offset.getSecondsMax()));
                }
                break;
        }
    }

    private LocalDateTime parseFixedTime(DatePicker datePicker, TextField timeField) throws Exception {
        if (datePicker.getValue() == null) {
            throw new Exception("请选择日期");
        }

        LocalDate date = datePicker.getValue();
        LocalTime time = LocalTime.MIDNIGHT;

        if (timeField.getText() != null && !timeField.getText().trim().isEmpty()) {
            try {
                time = LocalTime.parse(timeField.getText().trim(), TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new Exception("时间格式错误，请使用 HH:mm:ss 格式");
            }
        }

        return LocalDateTime.of(date, time);
    }

    private RepairConfig.RandomOffset parseRandomOffset(
            TextField daysMin, TextField daysMax,
            TextField hoursMin, TextField hoursMax,
            TextField minutesMin, TextField minutesMax,
            TextField secondsMin, TextField secondsMax) throws Exception {

        int dMin = parseIntWithDefault(daysMin, 0);
        int dMax = parseIntWithDefault(daysMax, 0);
        int hMin = parseIntWithDefault(hoursMin, 0);
        int hMax = parseIntWithDefault(hoursMax, 0);
        int minMin = parseIntWithDefault(minutesMin, 0);
        int minMax = parseIntWithDefault(minutesMax, 0);
        int sMin = parseIntWithDefault(secondsMin, 0);
        int sMax = parseIntWithDefault(secondsMax, 0);

        // 验证范围
        if (dMin > dMax) throw new Exception("天数最小值不能大于最大值");
        if (hMin > hMax) throw new Exception("小时最小值不能大于最大值");
        if (minMin > minMax) throw new Exception("分钟最小值不能大于最大值");
        if (sMin > sMax) throw new Exception("秒数最小值不能大于最大值");

        return new RepairConfig.RandomOffset(dMin, dMax, hMin, hMax, minMin, minMax, sMin, sMax);
    }

    private int parseIntWithDefault(TextField field, int defaultVal) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private RepairConfig.TimeAttribute getSelectedTimeAttribute(ChoiceBox<String> choiceBox) {
        String selected = choiceBox.getSelectionModel().getSelectedItem();
        if ("创建时间".equals(selected)) return RepairConfig.TimeAttribute.CREATION_TIME;
        if ("修改时间".equals(selected)) return RepairConfig.TimeAttribute.MODIFIED_TIME;
        if ("访问时间".equals(selected)) return RepairConfig.TimeAttribute.ACCESS_TIME;
        return RepairConfig.TimeAttribute.CREATION_TIME;
    }

    private String getTimeAttributeName(RepairConfig.TimeAttribute attribute) {
        switch (attribute) {
            case CREATION_TIME: return "创建时间";
            case MODIFIED_TIME: return "修改时间";
            case ACCESS_TIME: return "访问时间";
            default: return "创建时间";
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("配置错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
