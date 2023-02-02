/*
 * Copyright 2008 ETH Zuerich, CISD
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
package ch.systemsx.cisd.common.concurrent;

/**
 * A role that can provide activity information.
 * 
 * @author Bernd Rinn
 */
public interface IActivitySensor
{
    /**
     * Returns the time of last activity on this sensor in milli-seconds since start of the epoch.
     * <p>
     * Callers should expect that a call of this method may be expensive and thus only call it when necessary. However, the caller can expect the
     * information returned by is to be up-to-date.
     * 
     * @param thresholdMillis The time threshold for activity (in milli-seconds) that qualifies as "recent enough" to terminate the search for even
     *            more recent activity. An implementation can safely ignore this, as it is just meant for the sensor to optimize its search. A value
     *            <code>thresholdMillis=0</code> translates into "The absolutely most recent activity".
     */
    long getLastActivityMillisMoreRecentThan(long thresholdMillis);

    /**
     * Returns <code>true</code>, if this sensor has had activity more recent than <var>thresholdMillis</var> milli-seconds ago.
     * 
     * @param thresholdMillis The time threshold for activity (in milli-seconds) that qualifies as "recent enough" to terminate the search for even
     *            more recent activity.
     */
    boolean hasActivityMoreRecentThan(long thresholdMillis);
}