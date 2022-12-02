package ch.ethz.sis.afsserver.worker;

import ch.ethz.sis.afsserver.server.Worker;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.afsserver.worker.providers.AuthenticationInfoProvider;
import ch.ethz.sis.afsserver.worker.providers.AuthorizationInfoProvider;
import ch.ethz.sis.afsserver.worker.proxy.*;
import ch.ethz.sis.shared.pool.AbstractFactory;
import ch.ethz.sis.shared.startup.Configuration;

public class WorkerFactory extends AbstractFactory<Configuration, Configuration, Worker> {

    @Override
    public Worker create(Configuration configuration) throws Exception {

        // 5. Execute the operation
        AuditorProxy executorProxy = new AuditorProxy(new ExecutorProxy());

        // 4. Check that the user have rights to do the operation
        AuthorizationInfoProvider authorizationInfoProvider = configuration.getSharableInstance(AtomicFileSystemServerParameter.authorizationInfoProviderClass);
        authorizationInfoProvider.init(configuration);
        AuditorProxy authorizationProxy = new AuditorProxy(new AuthorizationProxy(executorProxy,
                authorizationInfoProvider));

        // 3. Pre/Post check correctness of the call and modifications to avoid things that make little sense
        int maxReadSizeInBytes = configuration.getIntegerProperty(AtomicFileSystemServerParameter.maxReadSizeInBytes);
        AuditorProxy correctnessProxy = new AuditorProxy(new ValidationProxy(authorizationProxy,
                maxReadSizeInBytes));

        // 2. Authenticate user and check that have a valid session
        AuthenticationInfoProvider authenticationInfoProvider = configuration.getSharableInstance(AtomicFileSystemServerParameter.authenticationInfoProviderClass);
        authenticationInfoProvider.init(configuration);
        AuditorProxy authenticationProxy = new AuditorProxy(new AuthenticationProxy(correctnessProxy,
                authenticationInfoProvider));

        // 1. Log call
        AuditorProxy logProxy = new AuditorProxy(new LogProxy(authenticationProxy));

        return logProxy;
    }
}