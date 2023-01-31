/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.security.TokenGenerator;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;

/**
 * Repository of {@link InputStream} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class StreamRepository implements IStreamRepository
{
    private static final class InputStreamWithValidityDuration
    {
        final long validityInMs;

        final Date timestamp;

        final InputStreamWithPath inputStreamWithPath;

        InputStreamWithValidityDuration(InputStreamWithPath inputStreamWithPath, Date timestamp,
                long validityDuration)
        {
            this.inputStreamWithPath = inputStreamWithPath;
            this.timestamp = timestamp;
            this.validityInMs = validityDuration;
        }
    }

    static interface IUniqueIdGenerator
    {
        public String createUniqueID();
    }

    private static final class IdGenerator implements IUniqueIdGenerator
    {
        private final TokenGenerator tokenGenerator = new TokenGenerator();

        @Override
        public String createUniqueID()
        {
            return tokenGenerator.getNewToken(System.currentTimeMillis());
        }
    }

    private final Map<String, InputStreamWithValidityDuration> streams =

            new HashMap<String, InputStreamWithValidityDuration>();

    private final IUniqueIdGenerator inputStreamIDGenerator;

    private final ITimeProvider timeProvider;

    private final long minimumTime;

    private final long maximumTime;

    public StreamRepository(IConfigProvider configProvider)
    {
        this(configProvider.getDataStreamTimeout(), configProvider.getDataStreamMaxTimeout(),
                new IdGenerator(), SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    @Private
    StreamRepository(int minimumTimeInSecondsToKeepStreams, int maximumTimeInSecondsToKeepStreams,
            IUniqueIdGenerator inputStreamIDGenerator, ITimeProvider timeProvider)
    {
        if (minimumTimeInSecondsToKeepStreams <= 0)
        {
            throw new IllegalArgumentException(
                    "Minimum time to keep streams is not a positive number: "
                            + minimumTimeInSecondsToKeepStreams);
        }
        minimumTime = minimumTimeInSecondsToKeepStreams * 1000L;

        if (maximumTimeInSecondsToKeepStreams <= 0)
        {
            throw new IllegalArgumentException(
                    "Maximum time to keep streams is not a positive number: "
                            + maximumTimeInSecondsToKeepStreams);
        }
        maximumTime = maximumTimeInSecondsToKeepStreams * 1000L;
        this.inputStreamIDGenerator = inputStreamIDGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public synchronized String addStream(InputStream inputStream, String path,
            long validityInSeconds)
    {
        removeStaleInputStreams();
        String id = inputStreamIDGenerator.createUniqueID();
        Date timestamp = new Date(timeProvider.getTimeInMilliseconds());

        long validityInMs = validityInSeconds * 1000L;
        long validity = (validityInMs < minimumTime) ? minimumTime : validityInMs;
        validity = (validityInMs > maximumTime) ? maximumTime : validity;
        streams.put(id, new InputStreamWithValidityDuration(new InputStreamWithPath(inputStream,
                path), timestamp, validity));
        return id;
    }

    @Override
    public synchronized InputStreamWithPath getStream(String inputStreamID)
    {
        removeStaleInputStreams();
        InputStreamWithValidityDuration inputStreamWithTimeStamp = streams.remove(inputStreamID);
        if (inputStreamWithTimeStamp == null)
        {
            throw new IllegalArgumentException("Stream " + inputStreamID
                    + " is no longer available.");
        }
        return inputStreamWithTimeStamp.inputStreamWithPath;
    }

    private void removeStaleInputStreams()
    {
        long currentTime = timeProvider.getTimeInMilliseconds();
        Set<Entry<String, InputStreamWithValidityDuration>> entrySet = streams.entrySet();
        for (Iterator<Entry<String, InputStreamWithValidityDuration>> iterator =
                entrySet.iterator(); iterator.hasNext();)
        {
            InputStreamWithValidityDuration stream = iterator.next().getValue();
            if (stream.timestamp.getTime() < currentTime - stream.validityInMs)
            {
                iterator.remove();
            }
        }
    }

}
