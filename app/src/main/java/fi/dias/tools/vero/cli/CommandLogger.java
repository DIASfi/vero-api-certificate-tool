package fi.dias.tools.vero.cli;

import picocli.CommandLine;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class CommandLogger {
    final int SILENT = 0;
    final int INFO = 1;
    final int DEBUG = 2;

    @CommandLine.Option(names = {"-v", "--verbose"},
            description = "Increase verbosity. Specify multiple times to increase (-vvv).")
    boolean[] verbosity = new boolean[0];

    public void enableDebug() {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        switch (verbosity.length) {
            case SILENT:
                rootLogger.setLevel(Level.OFF);
                for (Handler h : rootLogger.getHandlers()) {
                    h.setLevel(Level.OFF);
                }
                break;
            case INFO:
                rootLogger.setLevel(Level.INFO);
                for (Handler h : rootLogger.getHandlers()) {
                    h.setLevel(Level.INFO);
                }
                break;
            case DEBUG:
                rootLogger.setLevel(Level.ALL);
                for (Handler h : rootLogger.getHandlers()) {
                    h.setLevel(Level.ALL);
                }
                break;
        }

        if (verbosity.length == DEBUG) {
            System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
            System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
            System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "99999");
            System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
            System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
            System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dumpTreshold", "999999");
        }
    }

    public void info(String pattern, Object... params) {
        log(0, pattern, params);
    }

    public void debug(String pattern, Object... params) {
        log(1, pattern, params);
    }

    private void log(int level, String pattern, Object... params) {
        if (verbosity.length > level) {
            System.err.printf(pattern, params);
        }
    }
}
