/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.io;

import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * Calculates the estimated transmission speed for a file upload or download.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TransmissionSpeedCalculator
{
    private static final int HISTORY_LENGTH = 3;

    private final ITimeProvider timeProvider;

    private final float[] bytesPerMSecHistory = new float[HISTORY_LENGTH];

    private int historyIndex = 0;

    private boolean initialized;

    private long lastTransmissionTime;

    public TransmissionSpeedCalculator(ITimeProvider timeProvider)
    {
        super();
        this.timeProvider = timeProvider;
        initialized = false;
    }

    /**
     * Takes note that numberOfBytesTransmitted have been transmitted
     */
    public void noteTransmittedBytesSinceLastUpdate(long numberOfBytesTransmitted)
    {
        if (!initialized)
        {
            initializeHistory(numberOfBytesTransmitted);
        }

        long newTransmissionTime = timeProvider.getTimeInMilliseconds();
        float diff = newTransmissionTime - lastTransmissionTime;
        diff = Math.max(diff, 1.f);
        bytesPerMSecHistory[historyIndex] = (numberOfBytesTransmitted / diff);
        historyIndex = (historyIndex + 1) % HISTORY_LENGTH;
        lastTransmissionTime = newTransmissionTime;
    }

    /**
     * Returns the estimated transmission speed in bytes per second based the transmission history.
     */
    public float getEstimatedBytesPerMillisecond()
    {
        float bytesPerMSecAvg = 0.f;
        for (int i = 0; i < HISTORY_LENGTH; ++i)
        {
            bytesPerMSecAvg += bytesPerMSecHistory[i];
        }

        bytesPerMSecAvg = bytesPerMSecAvg / HISTORY_LENGTH;
        return bytesPerMSecAvg;
    }

    private void initializeHistory(long numberOfBytesTransmitted)
    {
        for (int i = 0; i < HISTORY_LENGTH; ++i)
        {
            bytesPerMSecHistory[i] = 0;
        }
        historyIndex = 0;
        lastTransmissionTime = timeProvider.getTimeInMilliseconds();
        initialized = true;
    }

}
