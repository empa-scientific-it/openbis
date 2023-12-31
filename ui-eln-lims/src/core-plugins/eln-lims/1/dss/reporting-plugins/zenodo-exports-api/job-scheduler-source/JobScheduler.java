/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class JobScheduler
{

    public static void scheduleRepeatedRequest(final long period, final int repCount, final BooleanCallable callable)
    {
        final Timer timer = new Timer(false);

        final AtomicInteger remainingRepCount = new AtomicInteger(repCount);
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                final boolean returnedValue = callable.call();
                if (returnedValue || remainingRepCount.decrementAndGet() <= 0)
                {
                    timer.cancel();
                }
            }

        }, period, period);
    }

    public interface BooleanCallable
    {
        boolean call();
    }

}