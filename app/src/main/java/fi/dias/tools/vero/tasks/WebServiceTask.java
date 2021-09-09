package fi.dias.tools.vero.tasks;

import fi.vero.certificates._2017._10.certificateservices.*;
import jakarta.xml.ws.BindingProvider;
import org.bouncycastle.crypto.modes.kgcm.Tables4kKGCMMultiplier_128;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WebServiceTask {
    private static final Logger LOGGER = Logger.getLogger(WebServiceTask.class.getName());
    private final CertificateServices certificateServices;
    protected final CertificateServicesPortType port;
    protected final EnvironmentTypes environmentType;
    private final BindingProvider bindingProvider;

    public WebServiceTask(TaskBuilder builder) {
        this.certificateServices = new CertificateServices();
        this.port = certificateServices.getCertificateServicesPort();
        this.bindingProvider = (BindingProvider)port;
        this.environmentType = builder.environmentType;

        LOGGER.log(Level.FINE, "Using  endpoint: " + builder.serviceURL);
        this.bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, builder.serviceURL);
    }

    public void handleErrorResult(Result result, String errorMessage) {
        if (result.getStatus().equals(ResultTypes.FAIL)) {
            throw new RuntimeException(errorMessage + ", service returned errors: " + errorToString(result));
        }
    }

    public String errorToString(Result result) {
        String errors = "";
        for (ErrorType error: result.getErrorInfo()) {
            errors += error.getErrorMessage();
        }

        return errors;
    }
}
