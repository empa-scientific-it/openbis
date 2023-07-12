/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.sis.afsserver.server;

import ch.ethz.sis.afs.api.TwoPhaseTransactionAPI;
import ch.ethz.sis.afs.api.dto.ExceptionReason;
import ch.ethz.sis.afsserver.exception.APIExceptions;
import ch.ethz.sis.afsserver.server.observer.APIServerObserver;
import ch.ethz.sis.afsserver.server.performance.PerformanceAuditor;
import ch.ethz.sis.shared.exception.ThrowableReason;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import ch.ethz.sis.shared.pool.Pool;
import ch.ethz.sis.shared.reflect.Reflect;
import ch.ethz.sis.shared.startup.Configuration;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ch.ethz.sis.afsserver.server.APIServerErrorType.IncorrectParameters;
import static ch.ethz.sis.afsserver.server.APIServerErrorType.MethodNotFound;

/**
 * This class should be used as delegate by specific server transport classes
 *
 * The API Server allows the following modes of operation:
 * | Mode                | Authorization Keys    | Description                                                                                                                                                                                                                                                                                                                                                                                               |
 * |---------------------|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
 * | Non Interactive     | sessionToken          | Use cases: Standard mode of operation to develop command line applications and user interfaces. Batch call requests are executed on a single transaction. The system starts automatically every Batch call with a begin and ends it with a commit or rollback if exceptions happen. If the user tries to use transaction methods manually on a Batch call the server will reject the complete Batch call. |
 * | Interactive         | interactiveSessionKey | Use cases: Non-standard use cases that require to leave a transaction opened, attached to a particular sessionToken between Batch calls. The system will not execute the transaction control methods automatically, standard transaction methods begin and commit should be used manually. Rollback can be used manually but the server will still use it automatically if errors happen.                 |
 * | Transaction Manager | transactionManagerKey | Use cases: Implementing a two phase transaction manager to execute transactions between two systems. Is meant to be used together in conjunction with Interactive mode. Allows the usage of the two phase transaction methods prepare and recover.                                                                                                                                                        |
 */
public class APIServer<CONNECTION, INPUT extends Request, OUTPUT extends Response, API> {

    private static final Logger logger = LogManager.getLogger(APIServer.class);
    private static final long IDLE_WORKER_TIMEOUT_CHECK_INTERVAL_IN_MILLIS = 1000;
    private final Pool<Configuration, CONNECTION> connectionsPool;
    private final Pool<Configuration, Worker<CONNECTION>> workersPool;

    private final Map<Worker<CONNECTION>, Long> workersLastAccessed = new ConcurrentHashMap<>();
    private final Map<String, Worker<CONNECTION>> workersInUse = new ConcurrentHashMap<>();

    private final Map<String, Method> apiMethods = new ConcurrentHashMap<>();
    private final Map<Method, Parameter[]> apiMethodParameters = new ConcurrentHashMap<>();

    private final String interactiveSessionKey;
    private final String transactionManagerKey;
    private final int apiServerWorkerTimeout; // Maximum amount of time allowed for a request to do a piece of work, when exceeded, the server cancels the request.

    private Timer idleWorkerCleanupTask;
    private boolean shutdown;
    private APIServerObserver<CONNECTION> observer;

    public APIServer(
            @NonNull Pool<Configuration, CONNECTION> connectionsPool,
            @NonNull Pool<Configuration, Worker<CONNECTION>> workersPool,
            @NonNull Class<API> apiClassDefinition,
            @NonNull String interactiveSessionKey,
            @NonNull String transactionManagerKey,
            int apiServerWorkerTimeout,
            @NonNull APIServerObserver observer) {
        this.shutdown = false;
        this.connectionsPool = connectionsPool;
        this.workersPool = workersPool;

        for (Method method : apiClassDefinition.getMethods()) {
            apiMethods.put(method.getName(), method);
            apiMethodParameters.put(method, method.getParameters());
        }

        this.apiServerWorkerTimeout = apiServerWorkerTimeout;
        this.interactiveSessionKey = interactiveSessionKey;
        this.transactionManagerKey = transactionManagerKey;
        this.observer = observer;

        scheduleIdleWorkerCleanupTask();
    }

    public void shutdown() {
        shutdown = true;
    }

    public boolean hasWorkersInUse() {
        return !workersInUse.isEmpty();
    }

    private void scheduleIdleWorkerCleanupTask() {
        idleWorkerCleanupTask = new Timer();
        idleWorkerCleanupTask.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        for (String sessionToken : workersInUse.keySet()) {
                            try {
                                Worker<CONNECTION> worker = workersInUse.get(sessionToken);
                                if (worker != null) {
                                    boolean isTimeout = workersLastAccessed.get(worker) +
                                            apiServerWorkerTimeout < System.currentTimeMillis();

                                    if (isTimeout) {
                                        checkIn(true,
                                                false,
                                                false,
                                                true,
                                                true,
                                                sessionToken,
                                                worker);
                                    }
                                }
                            } catch (Exception ex) {
                                logger.catching(ex);
                            }
                        }
                    }
                }, 0, IDLE_WORKER_TIMEOUT_CHECK_INTERVAL_IN_MILLIS
        );
    }

    private static final Set<String> twoPhaseTransactionAPIMethods = Reflect.getMethodNames(TwoPhaseTransactionAPI.class);

    private boolean isValidNonInteractiveSession(INPUT request) {
        return !twoPhaseTransactionAPIMethods.contains(request.getMethod());
    }

    private boolean isValidInteractiveSessionFinished(INPUT request) {
        return request.getMethod().equals("commit") || request.getMethod().equals("rollback");
    }

    private boolean sameSessionToken(List<INPUT> requests) {
        String sessionToken = requests.get(0).getSessionToken();
        for (Request request : requests) {
            if ((request.getSessionToken() == null && sessionToken == null) ||
                    (request.getSessionToken() != null && request.getSessionToken().equals(sessionToken))) {
                //Equals
            } else {
                return false;
            }
        }
        return true;
    }

    public OUTPUT processOperation(INPUT request, ResponseBuilder<OUTPUT> responseBuilder, PerformanceAuditor performanceAuditor) throws APIServerException {
        logger.traceAccess(null, request);

        // Shutting down?
        if (shutdown) {
            throw new APIServerException(null, APIServerErrorType.InternalError, APIExceptions.SHUTTING_DOWN.getCause());
        }

        // Requests validation
        // begin/rollback can only be called if the session token is present
        String sessionToken = request.getSessionToken();
        boolean sessionTokenFound = sessionToken != null;
        boolean isValidTransactionManagerMode = transactionManagerKey.equals(request.getTransactionManagerKey());
        boolean isValidInteractiveSession = interactiveSessionKey.equals(request.getInteractiveSessionKey());

        boolean isValidInteractiveSessionFinished = false;
        if (isValidInteractiveSession) {
            isValidInteractiveSessionFinished = isValidInteractiveSessionFinished(request) || !sessionTokenFound;
        }

        boolean isValidNonInteractiveSession = false;
        if (!isValidInteractiveSession) {
            isValidNonInteractiveSession = isValidNonInteractiveSession(request);
        }

        if (!isValidInteractiveSession && !isValidNonInteractiveSession) {
            throw new APIServerException(null, IncorrectParameters, APIExceptions.NON_INTERACTIVE_WITH_TRANSACTION_CONTROL.getCause());
        }

        // Process requests separately
        Worker<CONNECTION> worker = null;
        String currentRequestId = null;
        OUTPUT response = null;
        boolean errorFound = false;

        try {
            worker = checkOut(performanceAuditor,
                                isValidTransactionManagerMode,
                                isValidInteractiveSession,
                                isValidInteractiveSessionFinished,
                                isValidNonInteractiveSession,
                                sessionTokenFound,
                                sessionToken);

            response = dispatcher(worker, request, responseBuilder);
            currentRequestId = request.getId();
            currentRequestId = null;
            errorFound = response.getError() != null;
        } catch (Exception exception) {
            errorFound = true;
            logger.catching(exception);
            APIServerException apiException;
            if (exception instanceof APIServerException) {
                apiException = (APIServerException) exception;
            } else if(exception.getCause() != null && (exception.getCause() instanceof ThrowableReason)) {
                ThrowableReason throwableReason = (ThrowableReason) exception.getCause();
                apiException = new APIServerException(currentRequestId, APIServerErrorType.InternalError, throwableReason.getReason());
            } else if (exception instanceof InvocationTargetException) { // When calling methods using reflection the real cause is wrapped
                Throwable originalException = exception.getCause();
                ExceptionReason reason;
                if ((originalException != null) && (originalException.getCause() instanceof ThrowableReason)) {
                    ThrowableReason throwableReason = (ThrowableReason) originalException.getCause();
                    reason = (ExceptionReason) throwableReason.getReason();
                } else if(originalException != null) {
                    reason = APIExceptions.UNKNOWN.getCause(originalException.getClass().getSimpleName(), originalException.getMessage());
                } else { // This error branch has never been hit during testing
                    reason = APIExceptions.UNKNOWN.getCause(exception.getClass().getSimpleName(), exception.getMessage());
                }
                apiException = new APIServerException(currentRequestId, APIServerErrorType.InternalError, reason);
            } else { // This error branch has never been hit during testing
                ExceptionReason cause = APIExceptions.UNKNOWN.getCause(exception.getClass().getSimpleName(), exception.getMessage());
                apiException = new APIServerException(currentRequestId, APIServerErrorType.InternalError, cause);
            }
            logger.throwing(apiException);
            throw apiException;
        } finally {
            checkIn(isValidInteractiveSession,
                    isValidInteractiveSessionFinished,
                    isValidNonInteractiveSession,
                    errorFound,
                    sessionTokenFound,
                    sessionToken,
                    worker);
        }

        return logger.traceExit(response);
    }

    private Worker<CONNECTION> checkOut(PerformanceAuditor performanceAuditor,
                                        boolean isValidTransactionManagerMode,
                                        boolean isValidInteractiveSession,
                                        boolean isValidInteractiveSessionFinished,
                                        boolean isValidNonInteractiveSession,
                                        boolean sessionTokenFound,
                                        String sessionToken) throws Exception {
        Worker<CONNECTION> worker = null;

        try {
            if (isValidInteractiveSession &&
                    sessionTokenFound &&
                    workersInUse.containsKey(sessionToken)) {
                worker = workersInUse.get(sessionToken);
                workersLastAccessed.put(worker, System.currentTimeMillis());
            } else {
                CONNECTION connection = connectionsPool.checkOut();
                worker = workersPool.checkOut();
                worker.createContext(performanceAuditor);
                worker.setConnection(connection);
                worker.setTransactionManagerMode(isValidTransactionManagerMode);

                if (sessionTokenFound) {
                    worker.setSessionToken(sessionToken);
                    workersInUse.put(sessionToken, worker);
                    workersLastAccessed.put(worker, System.currentTimeMillis());
                    if (isValidNonInteractiveSession) {
                        worker.begin(UUID.randomUUID());
                    }
                }
            }
        } catch (Exception exceptionAtCheckout) {
            checkIn(isValidInteractiveSession,
                    isValidInteractiveSessionFinished,
                    isValidNonInteractiveSession,
                    true,
                    sessionTokenFound,
                    sessionToken,
                    worker);
            throw exceptionAtCheckout;
        }

        return worker;
    }

    private void checkIn(boolean isValidInteractiveSession,
                             boolean isValidInteractiveSessionFinished,
                             boolean isValidNonInteractiveSession,
                             boolean errorFound,
                             boolean sessionTokenFound,
                             String sessionToken,
                             Worker<CONNECTION> worker) {
        try {
            if (sessionTokenFound) {
                if (isValidInteractiveSession) {
                    if (isValidInteractiveSessionFinished || errorFound) {
                        workersInUse.remove(sessionToken);
                        workersLastAccessed.remove(worker);
                    }
                }

                if (isValidNonInteractiveSession) {
                    workersInUse.remove(sessionToken);
                    workersLastAccessed.remove(worker);
                }
            }
        } catch (Exception ex) {
            logger.catching(ex);
        }

        try {
            if (isValidNonInteractiveSession && !errorFound && sessionTokenFound) {
                worker.commit();
            }
        } catch (Exception ex) {
            logger.catching(ex);
        }

        boolean doCleanAndReturnWorker = isValidInteractiveSessionFinished ||
                isValidNonInteractiveSession ||
                errorFound;
        if (doCleanAndReturnWorker) {
            CONNECTION connection = null;

            try {
                connection = worker.getConnection(); // Connection saved before clean it
            } catch (Exception ex) {
                logger.catching(ex);
            }

            try {
                worker.cleanConnection();
            } catch (Exception ex) {
                logger.catching(ex);
            }

            try {
                connectionsPool.checkIn(connection);
            } catch (Exception ex) {
                logger.catching(ex);
            }

            try {
                worker.cleanContext();
            } catch (Exception ex) {
                logger.catching(ex);
            }

            try {
                workersPool.checkIn(worker);
            } catch (Exception ex) {
                logger.catching(ex);
            }
        }
    }

    //
    // Dispatcher, picks the correct handler and executes the method
    //

    private OUTPUT dispatcher(Worker<CONNECTION> api, Request request, ResponseBuilder<OUTPUT> responseBuilder) throws Exception {
        Method apiMethod = apiMethods.get(request.getMethod());
        Object[] requestParamsForApiMethod = null;

        if (apiMethod != null) {
            Parameter[] apiParams = apiMethodParameters.get(apiMethod);

            Map<String, Object> requestParams = request.getParams();

            // Parameters size check
            if ((requestParams == null && apiParams.length != 0) ||
                    (requestParams != null && apiParams.length != requestParams.size())) {
                throw new APIServerException(request.getId(), IncorrectParameters, APIExceptions.WRONG_PARAMETER_LIST_LENGTH.getCause());
            }

            // Parameters present check
            requestParamsForApiMethod = new Object[apiParams.length];
            for (int pIdx = 0; pIdx < apiParams.length; pIdx++) {
                Parameter parameter = apiParams[pIdx];
                Object requestParam = requestParams.get(parameter.getName());

                // Parameter present
                if (requestParam == null) {
                    throw new APIServerException(request.getId(), IncorrectParameters, APIExceptions.MISSING_METHOD_PARAMETER.getCause(parameter.getName(), apiMethod));
                }

                // Parameter of the expected type
                if (!parameter.getType().isInstance(requestParam)) {
                    throw new APIServerException(request.getId(), IncorrectParameters, APIExceptions.METHOD_PARAMETER_WRONG_TYPE.getCause(parameter.getName(), apiMethod));
                }

                requestParamsForApiMethod[pIdx] = requestParam;
            }

            observer.beforeAPICall(api, request.getMethod(), request.getParams());
            Object result = apiMethod.invoke(api, requestParamsForApiMethod);
            observer.afterAPICall(api, request.getMethod(), request.getParams());

            return responseBuilder.build(request.getId(), result);
        } else {
            throw new APIServerException(request.getId(), MethodNotFound, APIExceptions.METHOD_NOT_FOUND.getCause(request.getMethod()));
        }
    }

    //
    // Public API to request workers to the APIServer by extensions
    //

    public Worker<CONNECTION> checkOut() throws Exception {
        PerformanceAuditor performanceAuditor = new PerformanceAuditor();
        performanceAuditor.start();
        return checkOut(performanceAuditor,
                false,
                false,
                false,
                true,
                false,
                null);
    }

    public void checkIn(boolean errorFound,
                        Worker<CONNECTION> worker) {
        checkIn(false,
                false,
                true,
                errorFound,
                worker.getSessionToken() != null,
                worker.getSessionToken(),
                worker);
    }

    private static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}