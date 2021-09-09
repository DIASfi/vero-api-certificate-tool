package fi.dias.tools.vero.cli;

import fi.dias.tools.vero.tasks.TaskBuilder;
import picocli.CommandLine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@CommandLine.Command(synopsisHeading = "%nUsage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n")
public class CommonCommandOptions {
    @CommandLine.Option(names = {"-e", "--env"}, description = "Supported values: ${COMPLETION-CANDIDATES}")
    protected TaskBuilder.Environment environment;

    @CommandLine.Option(names = "--customer-id", description = "Customer ID")
    protected String customerId;

    @CommandLine.Option(names = "--customer-name", description = "Customer name")
    protected String customerName = null;

    @CommandLine.Option(names = "--key-store-password", description = "Password for key store")
    protected String keyStorePassword = null;

    @CommandLine.Option(names = "--key-store-alias", description = "Alias for entries in created KeyStore")
    protected String keyStoreAlias = DateTimeFormatter.ofPattern("yyyy-MM-dd-Hms").format(LocalDateTime.now());
}
