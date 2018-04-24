package sample;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class CpuProcess {
    private int processId;
    private int size;
    private String state = "";




    public CpuProcess(int processId, int size) {
        this.processId = processId;
        this.size = size;
    }

    public CpuProcess(int processId) {
        this.processId = processId;
        size=0;
    }


    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public void setState(String state) {

        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setSize(int size) {

        this.size = size;
    }

    public int getProcessId() {

        return processId;
    }

    public int getSize() {
        return size;
    }
}
