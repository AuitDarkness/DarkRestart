// RestartData.java
package com.dark.restartplugin;

public class RestartData {
    private final long time;
    private final String reason;
    private final boolean technical;

    public RestartData(long time, String reason, boolean technical) {
        this.time = time;
        this.reason = reason;
        this.technical = technical;
    }

    public long getTime() { return time; }
    public String getReason() { return reason; }
    public boolean isTechnical() { return technical; }
    public boolean isValid() { return time > System.currentTimeMillis(); }
}