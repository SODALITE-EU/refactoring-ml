package nl.jads.sodalite.dto;

public class BuleprintsData {
    private String[] target;
    private String bptoken;
    private String dpid;

    public String[] getTarget() {
        return target;
    }

    public void setTarget(String[] target) {
        this.target = target;
    }

    public String getBptoken() {
        return bptoken;
    }

    public void setBptoken(String bptoken) {
        this.bptoken = bptoken;
    }

    public String getDpid() {
        return dpid;
    }

    public void setDpid(String dpid) {
        this.dpid = dpid;
    }
}
