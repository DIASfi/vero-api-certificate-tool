package fi.dias.tools.vero;

import picocli.CommandLine;
import java.security.Security;

public class App {
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        System.setProperty("com.sun.org.apache.xml.internal.security.ignoreLineBreaks", "true");
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new fi.dias.tools.vero.cli.CertificateCommands()).execute(args);
        System.exit(exitCode);
    }
}
