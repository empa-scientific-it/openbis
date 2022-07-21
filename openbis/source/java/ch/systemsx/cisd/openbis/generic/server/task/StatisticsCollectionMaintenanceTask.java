/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.task;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.Logger;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME;

public class StatisticsCollectionMaintenanceTask implements IMaintenanceTask
{

    public static final String DEFAULT_MAINTENANCE_TASK_NAME = "statistics-collection-task";

    /* One day interval in seconds. */
    public static final int DEFAULT_MAINTENANCE_TASK_INTERVAL = 86400;

    /** ID of this openBIS server. */
    private static final String SERVER_ID_DOCUMENT_VERSION_FILE_PATH = "etc/instance-id";

    private final IApplicationServerInternalApi applicationServerApi;

    /** Whether this task has been called for the first time. */
    private boolean firstCall = true;

    protected final Logger operationLog;

    protected final Logger notificationLog;

    public StatisticsCollectionMaintenanceTask()
    {
        this(CommonServiceProvider.getApplicationServerApi());
    }

    StatisticsCollectionMaintenanceTask(final IApplicationServerInternalApi applicationServerApi)
    {
        this.applicationServerApi = applicationServerApi;
        operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());
        notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, getClass());
    }

    @Override
    public void setUp(String pluginName, Properties properties) {

    }

    @Override
    public void execute()
    {
        String sessionToken = null;
        try {
            sessionToken = applicationServerApi.loginAsSystem();
            execute(sessionToken);
        } catch (Exception exception) {
            if (sessionToken != null) {
                applicationServerApi.logout(sessionToken);
            }
            throw exception;
        }
    }

    public void execute(String sessionToken)
    {
        if (shouldExecute())
        {
            notificationLog.info("Statistics collection execution started.");

            final long personsCount = getPersonsCount(sessionToken);

            final Map<StatisticsKeys, String> statisticsMap = new EnumMap<>(StatisticsKeys.class);
            statisticsMap.put(StatisticsKeys.SERVER_ID, getThisServerId());
            statisticsMap.put(StatisticsKeys.USERS_COUNT, String.valueOf(personsCount));
            statisticsMap.put(StatisticsKeys.OPENBIS_VERSION, BuildAndEnvironmentInfo.INSTANCE.getVersion());
            statisticsMap.put(StatisticsKeys.SUBMISSION_TIMESTAMP, Instant.now().toString());

            final byte[] body;
            try
            {
                body = JacksonObjectMapper.getInstance().writeValue(statisticsMap);
            } catch (final JsonProcessingException e)
            {
                throw new RuntimeException("Error mapping JSON object.", e);
            }

            final Request request = JettyHttpClientFactory.getHttpClient()
                    .POST("http://statistics.openbis.ch/statistics")
                    .content(new BytesContentProvider(body));
            try
            {
                final ContentResponse contentResponse = request.send();
                contentResponse.getContent();
                final int statusCode = contentResponse.getStatus();
                if (statusCode >= 400)
                {
                    notificationLog.warn(String.format("Error sending statistics collection request. " +
                            "Error code received: %d (%s)", statusCode, contentResponse.getReason()));
                }
            } catch (final InterruptedException | TimeoutException | ExecutionException e)
            {
                notificationLog.warn("Error sending statistics collection request.", e);
            }

            notificationLog.info("Statistics collection execution finished.");
        }
    }

    /**
     * Whether this task should be executed.
     *
     * @return {@code true} if this is the first execution or first day of the month.
     */
    private boolean shouldExecute()
    {
        final String disableStatistics = System.getenv().get("DISABLE_OPENBIS_STATISTICS");
        if (!"true".equals(disableStatistics))
        {
            final String collectStatistics = ((ExposablePropertyPlaceholderConfigurer) CommonServiceProvider
                    .tryToGetBean(PROPERTY_CONFIGURER_BEAN_NAME))
                    .getResolvedProps().getProperty("collect-statistics");
            final boolean propertyIsMissing = collectStatistics == null || collectStatistics.isEmpty();
            if ("true".equals(collectStatistics) || propertyIsMissing)
            {
                if (propertyIsMissing)
                {
                    operationLog.warn("The collect-statistics property is missing. " +
                            "Statistics data is sent by default.");
                }

                if (firstCall)
                {
                    firstCall = false;
                    return true;
                } else
                {
                    return Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1;
                }
            }
        }
        return false;
    }

    private String getThisServerId()
    {
        final File file = new File(SERVER_ID_DOCUMENT_VERSION_FILE_PATH);
        final String idFromFile = readIdFromFile(file);

        if (idFromFile != null)
        {
            return idFromFile;
        } else
        {
            final String generatedId = UUID.randomUUID().toString();
            writeVersionToFile(file, generatedId);
            return generatedId;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeVersionToFile(final File file, final String version)
    {
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        try (final FileOutputStream fileOutputStream = new FileOutputStream(file))
        {
            fileOutputStream.write(version.getBytes());
        } catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String readIdFromFile(final File file)
    {
        try
        {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (final NoSuchFileException e)
        {
            operationLog.debug(String.format("File '%s' not found", file.getAbsolutePath()));
            return null;
        } catch (final IOException e)
        {
            operationLog.error(String.format("Error reading from file '%s'", file.getAbsolutePath()), e);
            throw new RuntimeException(e);
        }
    }

    private long getPersonsCount(final String sessionToken)
    {
        final PersonSearchCriteria personSearchCriteria = new PersonSearchCriteria();
        final SearchResult<Person> personSearchResult = applicationServerApi.searchPersons(sessionToken,
                personSearchCriteria, new PersonFetchOptions());
        final long personsCount = personSearchResult.getObjects().stream().filter(Person::isActive).count();
        return personsCount;
    }

    private static class JacksonObjectMapper
    {
        //
        // Singleton
        //
        private static final JacksonObjectMapper jacksonObjectMapper;

        static
        {
            jacksonObjectMapper = new JacksonObjectMapper();
        }

        public static JacksonObjectMapper getInstance()
        {
            return jacksonObjectMapper;
        }

        //
        // Class implementation
        //

        private final ObjectMapper objectMapper;

        private JacksonObjectMapper()
        {
            objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.enableDefaultTyping();
        }

        public <T> T readValue(final InputStream src, final Class<T> valueType) throws IOException
        {
            return objectMapper.readValue(src, valueType);
        }

        public <T> T readValue(final InputStream src, final TypeReference<T> typeRef) throws Exception
        {
            return objectMapper.readValue(src, typeRef);
        }

        public byte[] writeValue(final Object value) throws JsonProcessingException
        {
            return objectMapper.writeValueAsBytes(value);
        }
    }

    private enum StatisticsKeys
    {

        SERVER_ID,

        SUBMISSION_TIMESTAMP,

        USERS_COUNT,

        OPENBIS_VERSION

    }

}
