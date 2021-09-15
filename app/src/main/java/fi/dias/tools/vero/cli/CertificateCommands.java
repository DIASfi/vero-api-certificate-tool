package fi.dias.tools.vero.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "certificate",
        version = {"certificate: 1.0.0-beta1",
                   "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                   "OS: ${os.name} ${os.version} ${os.arch}"},
        subcommands = {NewCertificateCommand.class, RenewCertificateCommand.class},
        description = "Manage certificates necessary for communication with Vero-API",
        mixinStandardHelpOptions = true)
public class CertificateCommands {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;
}
