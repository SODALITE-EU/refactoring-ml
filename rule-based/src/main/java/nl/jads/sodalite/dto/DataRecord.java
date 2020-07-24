package nl.jads.sodalite.dto;

public class DataRecord {
    private String label;
    private int workload;
    private double memory;
    private double cpu;
    private double thermal;
    private long id;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getWorkload() {
        return workload;
    }

    public void setWorkload(int workload) {
        this.workload = workload;
    }

    public double getMemory() {
        return memory;
    }

    public void setMemory(double memory) {
        this.memory = memory;
    }

    public double getCpu() {
        return cpu;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public double getThermal() {
        return thermal;
    }

    public void setThermal(double thermal) {
        this.thermal = thermal;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
