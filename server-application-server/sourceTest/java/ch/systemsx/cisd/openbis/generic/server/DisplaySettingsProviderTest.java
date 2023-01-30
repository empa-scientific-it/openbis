/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public class DisplaySettingsProviderTest
{

    @Test(timeOut = 5000)
    public void testExecuteActionWithPersonLockIsThreadSafe()
    {
        final DisplaySettingsProvider provider = new DisplaySettingsProvider();

        final MessageChannel channel = new MessageChannel(5000);
        List<Thread> threads = new ArrayList<Thread>();

        final int NUMBER_OF_PERSONS = 10;
        final int NUMBER_OF_THREADS_PER_PERSON = 10;
        final int NUMBER_OF_ITERATIONS_PER_THREAD = 10;

        for (int p = 0; p < NUMBER_OF_PERSONS; p++)
        {
            final int pFinal = p;

            final PersonPE person = new PersonPE();
            person.setUserId(String.valueOf(p));

            for (int t = 0; t < NUMBER_OF_THREADS_PER_PERSON; t++)
            {
                final int tFinal = t;

                Thread thread = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            for (int i = 0; i < NUMBER_OF_ITERATIONS_PER_THREAD; i++)
                            {
                                provider.executeActionWithPersonLock(person, new IDelegatedActionWithResult<Void>()
                                    {
                                        @Override
                                        @SuppressWarnings("deprecation")
                                        public Void execute(boolean didOperationSucceed)
                                        {
                                            String token = pFinal + "_" + tFinal;

                                            DisplaySettings newSettings = new DisplaySettings();
                                            newSettings.setLastHistoryTokenOrNull(token);
                                            provider.replaceCurrentDisplaySettings(person,
                                                    newSettings);

                                            try
                                            {
                                                Thread.sleep(1);
                                            } catch (Exception e)
                                            {
                                            }

                                            DisplaySettings currentSettings = provider.getCurrentDisplaySettings(person);

                                            Assert.assertEquals(newSettings.getLastHistoryTokenOrNull(), currentSettings.getLastHistoryTokenOrNull());

                                            channel.send("finished");
                                            return null;
                                        }
                                    });
                            }
                        }
                    });
                threads.add(thread);
            }
        }

        for (Thread thread : threads)
        {
            thread.start();
        }

        for (int i = 0; i < NUMBER_OF_PERSONS * NUMBER_OF_THREADS_PER_PERSON * NUMBER_OF_ITERATIONS_PER_THREAD; i++)
        {
            channel.assertNextMessage("finished");
        }

    }

    @Test(timeOut = 5000)
    public void testExecuteActionWithPersonLockAllowsConcurrentActionsForDifferentPersons()
    {
        final MessageChannel sendChannel = new MessageChannel();
        final MessageChannel waitChannel = new MessageChannel();

        final DisplaySettingsProvider provider = new DisplaySettingsProvider();

        Thread sleepingThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    PersonPE person = new PersonPE();
                    person.setUserId("user_1");

                    provider.executeActionWithPersonLock(person, new IDelegatedActionWithResult<Void>()
                        {
                            @Override
                            public Void execute(boolean didOperationSucceed)
                            {
                                waitChannel.send("inside execute");
                                sendChannel.assertNextMessage("wake up");
                                waitChannel.send("woke up");
                                return null;
                            }
                        });
                }
            });

        Thread updatingThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    PersonPE person = new PersonPE();
                    person.setUserId("user_2");

                    provider.executeActionWithPersonLock(person, new IDelegatedActionWithResult<Void>()
                        {
                            @Override
                            public Void execute(boolean didOperationSucceed)
                            {
                                waitChannel.send("update finished");
                                return null;
                            }
                        });
                }
            });

        sleepingThread.start();
        waitChannel.assertNextMessage("inside execute");
        updatingThread.start();
        waitChannel.assertNextMessage("update finished");
        sendChannel.send("wake up");
        waitChannel.assertNextMessage("woke up");
    }

    @Test(timeOut = 5000)
    public void testExecuteActionWithPersonLockBlocksConcurrentActionsForSamePerson()
    {
        final MessageChannel sendChannel = new MessageChannel();
        final MessageChannel waitChannel = new MessageChannel();

        final DisplaySettingsProvider provider = new DisplaySettingsProvider();

        Thread sleepingThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    PersonPE person = new PersonPE();
                    person.setUserId("user_1");

                    provider.executeActionWithPersonLock(person, new IDelegatedActionWithResult<Void>()
                        {
                            @Override
                            public Void execute(boolean didOperationSucceed)
                            {
                                waitChannel.send("inside execute");
                                sendChannel.assertNextMessage("wake up");
                                return null;
                            }
                        });
                }
            });

        Thread updatingThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    PersonPE person = new PersonPE();
                    person.setUserId("user_1");

                    provider.executeActionWithPersonLock(person, new IDelegatedActionWithResult<Void>()
                        {
                            @Override
                            public Void execute(boolean didOperationSucceed)
                            {
                                waitChannel.send("update finished");
                                return null;
                            }
                        });
                }
            });

        sleepingThread.start();
        waitChannel.assertNextMessage("inside execute");
        updatingThread.start();
        waitChannel.assertEmpty();
        sendChannel.send("wake up");
        waitChannel.assertNextMessage("update finished");
    }
}
