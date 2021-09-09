package fi.dias.tools.vero.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "certificate",
subcommands = {NewCertificateCommand.class, RenewCertificateCommand.class},
description = "Manage certificates necessary for communication with Vero-API")
public class CertificateCommands {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;
}
