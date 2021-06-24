package ch.ethz.sis.benchmark;

import java.util.HashMap;

public class BenchmarkConfig
{
    private String className;
    private String user;
    private String password;
    private String openbisURL;
    private int openbisTimeout;
    private String datastoreURL;
    private int datastoreTimeout;
    private HashMap<String, String> parameters;
    private int threads;

    public BenchmarkConfig() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOpenbisURL() {
        return openbisURL;
    }

    public void setOpenbisURL(String openbisURL) {
        this.openbisURL = openbisURL;
    }

    public int getOpenbisTimeout() {
        return openbisTimeout;
    }

    public void setOpenbisTimeout(int openbisTimeout) {
        this.openbisTimeout = openbisTimeout;
    }

    public String getDatastoreURL() {
        return datastoreURL;
    }

    public void setDatastoreURL(String datastoreURL) {
        this.datastoreURL = datastoreURL;
    }

    public int getDatastoreTimeout() {
        return datastoreTimeout;
    }

    public void setDatastoreTimeout(int datastoreTimeout) {
        this.datastoreTimeout = datastoreTimeout;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    @Override
    public String toString() {
        return "BenchmarkConfig{" +
                "className='" + className + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", openbisURL='" + openbisURL + '\'' +
                ", openbisTimeout=" + openbisTimeout +
                ", datastoreURL='" + datastoreURL + '\'' +
                ", datastoreTimeout=" + datastoreTimeout +
                ", parameters=" + parameters +
                ", threads=" + threads +
                '}';
    }
}
