package fi.dias.tools.vero.cli;

import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class CommandLogger {
    final int SILENT = 0;
    final int INFO = 1;
    final int DEBUG = 2;
    final int SCREAMING = 3;

    @CommandLine.Option(names = {"-v", "--verbose"},
            description = "Increase verbosity. Specify multiple times to increase (-vvv).")
    boolean[] verbosity = new boolean[0];

    public void enableDebug() {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        switch (verbosity.length) {
            case DEBUG:
                rootLogger.setLevel(Level.INFO);
                for (Handler h : rootLogger.getHandlers()) {
                    h.setLevel(Level.INFO);
                }
                break;
            case SCREAMING:
                rootLogger.setLevel(Level.ALL);
                for (Handler h : rootLogger.getHandlers()) {
                    h.setLevel(Level.ALL);
                }
                break;
        }

        if (verbosity.length >= DEBUG) {
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
        if (verbosity.length >= level) {
            System.err.printf(pattern, params);
            System.err.println("\n");
        }
    }

    public void toFile(String test, String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.append(test);
            writer.append(System.getProperty("line.separator"));
            writer.append(System.getProperty("line.separator"));
            writer.close();
        } catch (IOException e) {
            info("Failed to write special log file", e.getMessage());
        }
    }

    public void certificateToFile(X509Certificate certificate, String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.append("Certificate:");
            writer.append(System.getProperty("line.separator"));
            writer.append("-----BEGIN CERTIFICATE-----");
            writer.append(System.getProperty("line.separator"));
            writer.append(Base64.getEncoder().encodeToString(certificate.getEncoded()));
            writer.append(System.getProperty("line.separator"));
            writer.append("-----END CERTIFICATE-----");
            writer.append(System.getProperty("line.separator"));
            writer.append(System.getProperty("line.separator"));
            writer.close();
        } catch (IOException | CertificateEncodingException e) {
            info("Failed to write special log file", e.getMessage());
        }
    }

    public void certificateRequestToFile(byte[] certificateRequest, String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.append("Certificate Request:");
            writer.append(System.getProperty("line.separator"));
            writer.append("-----BEGIN CERTIFICATE REQUEST-----");
            writer.append(System.getProperty("line.separator"));
            writer.append(Base64.getEncoder().encodeToString(certificateRequest));
            writer.append(System.getProperty("line.separator"));
            writer.append("-----END CERTIFICATE REQUEST-----");
            writer.append(System.getProperty("line.separator"));
            writer.append(System.getProperty("line.separator"));
            writer.close();
        } catch (IOException e) {
            info("Failed to write special log file", e.getMessage());
        }
    }
}
