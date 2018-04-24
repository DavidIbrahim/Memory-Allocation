package sample;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    public GanttChart<Number, String> chart;
    public NumberAxis xAxis;
    public CategoryAxis yAxis;


    public SplitPane splitPaneOutput;
    public AnchorPane anchorPaneRoot;
    public VBox vboxInput;
    public VBox vboxOutput;
    public TableView tableProcessData;
    public TableView tableHolesData;
    public TextField textFieldNoOfProcesses;
    public TextField textFieldNoOfHoles;
    public ChoiceBox choiceBoxAlgorithms;
    public TabPane tabPane;
    public Label labelOutput;
    private VBox vBoxLegend = new VBox();

    private TableColumn<CpuProcess, Integer> columnProcessNo;
    private TableColumn<CpuProcess, Integer> columnProcessSize;
    private TableColumn<CpuProcess, String> columnProcessState;

    private TableColumn<MemoryHole, Integer> columnHoleNo;
    private TableColumn<MemoryHole, Integer> columnHoleStartingIndex;
    private TableColumn<MemoryHole, Integer> columnHoleSize;
    private final ContextMenu contextMenu = new ContextMenu();


    private int numberOfProcess = 0;
    private int numberOfHoles = 0;
    private VBox vBoxChart;
    private static int FIRST_FIT = 0;
    private static int BEST_FIT = 1;
    private static int WORST_FIT = 2;

    private static int currentAlgorithm = 0;


    private ObservableList<CpuProcess> processesList = FXCollections.observableArrayList();
    private ObservableList<MemoryHole> memoryHolesList = FXCollections.observableArrayList();

    String[] colors = new String[]{"status-red", "status-green", "status-blue", "status-tomato",
            "status-violet", "status-purple", "status-yellow", "status-brown", "status-black", "status-purple2", "status-grey"};
    String[] colorsHex = new String[]{"#800000", "#008000", "#000080", "#ff6347", "#ee82ee", "#6a5acd", "#ffff47"
            , "#996600", "#000000", "#993366"};

    ObservableList<String> scheduleAlgoritms = FXCollections.observableArrayList("First fit", "Best fit", "Worst fit");
    private CpuProcess processToBeDeAllocated;

    private void initializechoiceBox() {

        choiceBoxAlgorithms.setItems(scheduleAlgoritms);
        choiceBoxAlgorithms.setValue("First fit");
        choiceBoxAlgorithms.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                currentAlgorithm = newValue.intValue();
                drawChart();
            }
        });


    }


    private void initializeganttChart() {


        xAxis = new NumberAxis();
        yAxis = new CategoryAxis();
        //xAxis.setTickLabelFormatter();
        chart = new GanttChart<Number, String>(xAxis, yAxis);
        xAxis.setLabel("");
        xAxis.setTickLabelFill(Color.CHOCOLATE);
        xAxis.setMinorTickCount(1);

        xAxis.setAutoRanging(true);

        yAxis.setLabel("");
        yAxis.setTickLabelFill(Color.CHOCOLATE);
        yAxis.setTickLabelGap(10);

        //chart.setTitle("Machine Monitoring");
        chart.setLegendVisible(false);


        URL url = this.getClass().getResource("ganttchart.css");
        if (url == null) {
            System.out.println("Resource not found. Aborting.");
            System.exit(-1);
        }
        String css = url.toExternalForm();

        chart.setBlockHeight(50);
        chart.getStylesheets().add(css);
        chart.setAnimated(true);


        vBoxChart = new VBox(chart);

        vboxOutput.getChildren().add(new Group(vBoxChart));


        chart.maxHeightProperty().bind(vboxOutput.widthProperty().divide(3));
        chart.prefWidthProperty().bind(vboxOutput.heightProperty().divide(1.6));
        chart.minHeight(labelOutput.getWidth());


        chart.setLegendVisible(false);
        chart.setRotate(90);

    }


    private void initializeVBoxes() {
        vboxOutput.prefWidthProperty().bind(anchorPaneRoot.widthProperty().divide(2));
        vboxInput.prefWidthProperty().bind(anchorPaneRoot.widthProperty().divide(2));
        vboxOutput.minHeightProperty().bind(anchorPaneRoot.heightProperty());
        vboxInput.prefHeightProperty().bind(anchorPaneRoot.prefHeightProperty());
        VBox.setMargin(vBoxLegend,new Insets(2,5,5,5));

    }

    private void addProcesses(int numberOfRows) {
        if (numberOfRows == 0) {
            return;
        }
        for (int i = 0; i <processesList.size() ; i++) {
            processesList.get(i).setProcessId(i);

        }

        int previousSize = processesList.size();
        for (int i = 0; i < Math.abs(numberOfRows); i++) {
            if (numberOfRows > 0)
                processesList.add(new CpuProcess(i + previousSize));
            else
                processesList.remove(processesList.size() - 1);

        }
        tableProcessData.setItems(processesList);
    }

    private void addHoles(int numberOfRows) {
        if (numberOfRows == 0) {
            return;
        }
        int previousSize = memoryHolesList.size();
        for (int i = 0; i < Math.abs(numberOfRows); i++) {
            if (numberOfRows > 0)
                memoryHolesList.add(new MemoryHole(i + previousSize));
            else
                memoryHolesList.remove(memoryHolesList.size() - 1);

        }
        tableHolesData.setItems(memoryHolesList);
    }

    private void initializeTextFields() {
        textFieldNoOfProcesses.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (oldValue) {
                    int oldNumberOfPrpcess = numberOfProcess;
                    try {
                        numberOfProcess = Integer.valueOf(textFieldNoOfProcesses.getText());
                    } catch (Exception e) {
                        numberOfProcess = 0;
                    }
                    System.out.println(numberOfProcess);
                    addProcesses(numberOfProcess - oldNumberOfPrpcess);
                    drawChart();
                }
            }
        });

        textFieldNoOfHoles.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (oldValue) {
                    int oldNumberOfHoles = numberOfHoles;
                    try {
                        numberOfHoles = Integer.valueOf(textFieldNoOfHoles.getText());
                    } catch (Exception e) {
                        numberOfHoles = 0;
                    }
                    System.out.println(numberOfHoles);
                    addHoles(numberOfHoles - oldNumberOfHoles);
                    drawChart();
                }
            }
        });
    }

    private void initializeTables() {
        /*First Table Process Data */
        tableProcessData.setEditable(true);

        columnProcessNo = new TableColumn<>("Process");
        columnProcessNo.setVisible(true);
        columnProcessNo.prefWidthProperty().bind(tableProcessData.widthProperty().divide(3.01));
        columnProcessNo.setCellValueFactory(new PropertyValueFactory<>("processId"));
        columnProcessNo.setSortable(false);

        columnProcessState = new TableColumn<>("State");
        columnProcessState.setVisible(true);
        columnProcessState.prefWidthProperty().bind(tableProcessData.widthProperty().divide(3.01));
        columnProcessState.setCellValueFactory(new PropertyValueFactory<>("state"));
        columnProcessState.setCellFactory(new Callback<TableColumn<CpuProcess, String>, TableCell<CpuProcess, String>>() {
            @Override
            public TableCell<CpuProcess, String> call(TableColumn<CpuProcess, String> col) {
                final TableCell<CpuProcess, String> cell = new TableCell<>();
                cell.textProperty().bind(cell.itemProperty()); // in general might need to subclass TableCell and override updateItem(...) here
                cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getButton() == MouseButton.SECONDARY) {
                            // handle right click on cell...
                            // access cell data with cell.getItem();
                            // access row data with (Person)cell.getTableRow().getItem();
                            if (cell.getTableRow().getItem()!=null) {
                                cell.setContextMenu(contextMenu);
                                processToBeDeAllocated = cell.getTableRow().getItem();
                            }
                        }
                    }
                });
                return cell;
            }
        });

        columnProcessState.setSortable(false);

        columnProcessSize = new TableColumn<>("Size");
        columnProcessSize.setVisible(true);
        columnProcessSize.prefWidthProperty().bind(tableProcessData.widthProperty().divide(3.01));
        columnProcessSize.setCellValueFactory(new PropertyValueFactory<>("size"));

        columnProcessSize.setSortable(false);
        columnProcessSize.setCellFactory(TextFieldTableCell.<CpuProcess, Integer>forTableColumn(new IntegerStringConverter()));

        columnProcessSize.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<CpuProcess, Integer>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<CpuProcess, Integer> event) {
                event.getRowValue().setSize(event.getNewValue());
                drawChart();

            }
        });

        tableProcessData.getColumns().addAll(columnProcessNo, columnProcessSize, columnProcessState);
        tableProcessData.getSelectionModel().setCellSelectionEnabled(true);


        //Initialize hole table
        tableHolesData.setEditable(true);


        columnHoleNo = new TableColumn<>("Hole");
        columnHoleNo.setVisible(true);
        columnHoleNo.prefWidthProperty().bind(tableHolesData.widthProperty().divide(3.01));
        columnHoleNo.setCellValueFactory(new PropertyValueFactory<>("holeNumber"));
        columnHoleNo.setSortable(false);

        columnHoleStartingIndex = new TableColumn<>("start Index");
        columnHoleStartingIndex.setVisible(true);
        columnHoleStartingIndex.prefWidthProperty().bind(tableHolesData.widthProperty().divide(3));
        columnHoleStartingIndex.setCellValueFactory(new PropertyValueFactory<>("startingIndex"));
        columnHoleStartingIndex.setCellFactory(TextFieldTableCell.<MemoryHole, Integer>forTableColumn(new IntegerStringConverter()));
        columnHoleStartingIndex.setSortable(false);
        columnHoleStartingIndex.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<MemoryHole, Integer>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<MemoryHole, Integer> event) {
                event.getRowValue().setStartingIndex(event.getNewValue());
            }
        });

        columnHoleSize = new TableColumn<>("size");
        columnHoleSize.setVisible(true);
        columnHoleSize.prefWidthProperty().bind(tableHolesData.widthProperty().divide(3));
        columnHoleSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        columnHoleSize.setCellFactory(TextFieldTableCell.<MemoryHole, Integer>forTableColumn(new IntegerStringConverter()));
        columnHoleSize.setSortable(false);
        columnHoleSize.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<MemoryHole, Integer>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<MemoryHole, Integer> event) {
                event.getRowValue().setSize(event.getNewValue());
                drawChart();

            }
        });


        tableHolesData.getColumns().addAll(columnHoleNo, columnHoleStartingIndex, columnHoleSize);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeVBoxes();
        initializeganttChart();
        initializeTables();
        initializeTextFields();

        initializechoiceBox();
        vboxOutput.getChildren().add(vBoxLegend);
        vboxOutput.setAlignment(Pos.TOP_CENTER);


        MenuItem deallocate = new MenuItem("Deallocate");
        contextMenu.getItems().addAll(deallocate);

        deallocate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("D");
                processesList.remove(processToBeDeAllocated);
                textFieldNoOfProcesses.setText(String.valueOf(processesList.size()));
                numberOfProcess = processesList.size();
               /* for (int i = 0; i <processesList.size() ; i++) {
                    processesList.get(i).setProcessId(i);
                }*/
                tableProcessData.refresh();
                drawChart();

            }
        });

        tabPane.minHeightProperty().bind(vboxInput.heightProperty().divide(1.5));


    }

    private void drawChart() {

        chart.getData().clear();
        XYChart.Series series = new XYChart.Series();
        chart.getData().add(series);
        chart.setBlockHeight(chart.getHeight());


        ArrayList<MemoryHole> memoryHoles = new ArrayList<>();

        for (MemoryHole hole : memoryHolesList
                ) {
            memoryHoles.add(new MemoryHole(hole.getHoleNumber(), hole.getStartingIndex(), hole.getSize()));
        }
        Collections.sort(memoryHoles);
        fixHolesArrayList(memoryHoles);
        int lastFinish = 10;
        if (!memoryHoles.isEmpty()) {
            lastFinish = Math.max(memoryHoles.get(memoryHoles.size() - 1).getEndingIndex(),lastFinish);
        }


        xAxis.setUpperBound(lastFinish);
        xAxis.setUpperBound(lastFinish);
        if (lastFinish < 20) {
            xAxis.setTickUnit(1);

        } else if (lastFinish < 30) {
            xAxis.setTickUnit(2);
        } else {
            xAxis.setTickUnit(5);
        }


        series.getData().add(new XYChart.Data<>(0, "", new GanttChart.ExtraData((int) (lastFinish + 1), colors[colors.length - 1])));
        drawHoles(memoryHoles, series);


        ArrayList<CpuProcess> cpuProcesses = new ArrayList<>();

        for (CpuProcess process : processesList) {
            cpuProcesses.add(new CpuProcess(process.getProcessId(), process.getSize()));
        }

        drawProcesses(cpuProcesses, series, memoryHoles);

        buildChartLegend(cpuProcesses);


    }

    private void buildChartLegend(ArrayList<CpuProcess> cpuProcesses) {
        vBoxLegend.getChildren().clear();
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.TOP_CENTER);

        vBoxLegend.setAlignment(Pos.TOP_CENTER);
        vboxOutput.setAlignment(Pos.TOP_CENTER);

        vBoxLegend.getChildren().add(hBox);

        int i = 0;
        while (i < cpuProcesses.size()) {
            CpuProcess process = cpuProcesses.get(i);
            Rectangle rectangle = new Rectangle(20, 20);
            rectangle.setFill(Paint.valueOf(colorsHex[process.getProcessId()]));
            Label label = new Label("P" + process.getProcessId());
            label.setFont(new Font("Arial", 11));
            label.setPadding(new Insets(0, 10, 0, 10));

            if (i % 5 == 0) {
                hBox = new HBox(label, rectangle);

                vBoxLegend.getChildren().add(hBox);
                hBox.setPadding(new Insets(20));
            } else {
                hBox.getChildren().addAll(label, rectangle);

            }


            i++;
        }

    }

    private void drawProcesses(ArrayList<CpuProcess> cpuProcesses, XYChart.Series series, ArrayList<MemoryHole> memoryHoles) {
        for (CpuProcess process : cpuProcesses
                ) {
            int indexOfNextHole = findNextHole(process, memoryHoles);
            if (indexOfNextHole != -1) {
                MemoryHole hole = memoryHoles.get(indexOfNextHole);
                series.getData().add(new XYChart.Data<>(hole.getStartingIndex(), "", new GanttChart.ExtraData((int) (process.getSize()), colors[process.getProcessId()])));
                hole.setStartingIndex(hole.getStartingIndex() + process.getSize());
                processesList.get(cpuProcesses.indexOf(process)).setState("Allocated");
            } else {
                processesList.get(cpuProcesses.indexOf(process)).setState("Waiting");
            }

        }
        tableProcessData.refresh();

    }

    private int findNextHole(CpuProcess process, ArrayList<MemoryHole> memoryHoles) {
        int index = -1;


        int sizeOfBestFit = Integer.MAX_VALUE;

        int sizeOfWorstFit = 0;


        for (int i = 0; i < memoryHoles.size(); i++) {

            MemoryHole currentHole = memoryHoles.get(i);

            if (currentAlgorithm == FIRST_FIT) {
                if (currentHole.getSize() >= process.getSize())
                    return i;

            } else if (currentAlgorithm == BEST_FIT) {
                if (currentHole.getSize() >= process.getSize() && currentHole.getSize() < sizeOfBestFit) {
                    sizeOfBestFit = currentHole.getSize();
                    index = i;
                }

            } else if (currentAlgorithm == WORST_FIT) {
                if (currentHole.getSize() >= process.getSize() && currentHole.getSize() > sizeOfWorstFit) {
                    index = i;
                    sizeOfWorstFit = currentHole.getSize();
                }

            }

        }
        return index;
    }

    private void drawHoles(ArrayList<MemoryHole> memoryHoles, XYChart.Series series) {
        for (MemoryHole hole : memoryHoles) {
            series.getData().add(new XYChart.Data<>(hole.getStartingIndex(), "", new GanttChart.ExtraData((int) (hole.getSize()), "status-white")));
        }
    }

    private void fixHolesArrayList(ArrayList<MemoryHole> memoryHoles) {
        for (int i = 0; i < memoryHoles.size() - 1; i++) {
            MemoryHole currentHole = memoryHoles.get(i);
            MemoryHole nextHole = memoryHoles.get(i + 1);

            if (currentHole.getEndingIndex() >= nextHole.getStartingIndex()) {
                currentHole.setEndingIndex(Math.max(currentHole.getEndingIndex(), nextHole.getEndingIndex()));
                memoryHoles.remove(nextHole);
                i--;
            }
        }

    }
}



