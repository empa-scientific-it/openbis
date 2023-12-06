/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;

/**
 * Immplementation of {@link IShareIdManager} based on {@link CountDownLatch}.
 *
 * @author Franz-Josef Elmer
 */
public class ShareIdManager implements IShareIdManager
{
    private static final class GuardedShareID
    {
        private final int lockingTimeOut;

        private final String dataSetCode;

        private CountDownLatch countDownLatch;

        private String shareId;

        GuardedShareID(String dataSetCode, String shareId, int lockingTimeOut)
        {
            this.dataSetCode = dataSetCode;
            this.shareId = shareId == null ? Constants.DEFAULT_SHARE_ID : shareId;
            this.lockingTimeOut = lockingTimeOut;
        }

        String getShareId()
        {
            return shareId;
        }

        void setShareId(String shareId)
        {
            this.shareId = shareId;
        }

        void lock()
        {
            countDownLatch = new CountDownLatch(1);
        }

        void unlock()
        {
            if (countDownLatch != null)
            {
                countDownLatch.countDown();
            }
        }

        void await()
        {
            if (countDownLatch != null)
            {
                try
                {
                    boolean successful = countDownLatch.await(lockingTimeOut, TimeUnit.SECONDS);
                    if (successful == false)
                    {
                        throw new EnvironmentFailureException("Lock for data set " + dataSetCode
                                + " hasn't been released after time out of " + lockingTimeOut
                                + " seconds.");
                    }
                } catch (InterruptedException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }
    }

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ShareIdManager.class);

    private final IEncapsulatedOpenBISService service;

    private final int lockingTimeOut;

    private final Map<String, Set<Thread>> lockedDataSets = new HashMap<String, Set<Thread>>();

    private final Object dataSetCodeToShareIdMapMonitor = new Object();

    private Map<String, GuardedShareID> dataSetCodeToShareIdMap;

    public ShareIdManager(IEncapsulatedOpenBISService service, int lockingTimeOutInSeconds)
    {
        this.service = service;
        this.lockingTimeOut = lockingTimeOutInSeconds;
    }

    private void addShareId(Map<String, GuardedShareID> map, String dataSetCode, String shareId)
    {
        GuardedShareID guardedShareId = new GuardedShareID(dataSetCode, shareId, lockingTimeOut);
        map.put(dataSetCode, guardedShareId);
    }

    @Override
    public boolean isKnown(String dataSetCode)
    {
        tryGetGuardedShareId(dataSetCode);
        return getDataSetCodeToShareIdMap().containsKey(dataSetCode);
    }

    @Override
    public String getShareId(String dataSetCode)
    {
        return getGuardedShareId(dataSetCode).getShareId();
    }

    @Override
    public void setShareId(String dataSetCode, String shareId)
    {
        Map<String, GuardedShareID> map = getDataSetCodeToShareIdMap();
        GuardedShareID guardedShareId = map.get(dataSetCode);
        if (guardedShareId != null)
        {
            guardedShareId.setShareId(shareId);
            operationLog.info("New share of data set " + dataSetCode + " is " + shareId);
        } else
        {
            addShareId(map, dataSetCode, shareId);
            operationLog.info("Register data set " + dataSetCode + " for share " + shareId);
        }
    }

    @Override
    public void lock(String dataSetCode)
    {
        synchronized (lockedDataSets)
        {
            Set<Thread> set = lockedDataSets.get(dataSetCode);
            if (set == null)
            {
                set = new LinkedHashSet<Thread>();
                GuardedShareID guardedShareId = getGuardedShareId(dataSetCode);
                lockedDataSets.put(dataSetCode, set);
                guardedShareId.lock();
                if (operationLog.isDebugEnabled())
                {
                    final Throwable th = new Throwable();
                    th.fillInStackTrace();
                    operationLog.debug("Data set " + dataSetCode + " has been locked.", th);
                }
            }
            set.add(Thread.currentThread());
            log(dataSetCode, set);
        }
    }

    @Override
    public void lock(List<String> dataSetCodes)
    {
        synchronized (lockedDataSets)
        {
            final List<String> locked = new ArrayList<String>();
            try
            {
                for (String dataSetCode : dataSetCodes)
                {
                    Set<Thread> set = lockedDataSets.get(dataSetCode);
                    if (set == null)
                    {
                        set = new LinkedHashSet<Thread>();
                        GuardedShareID guardedShareId = getGuardedShareId(dataSetCode);
                        lockedDataSets.put(dataSetCode, set);
                        guardedShareId.lock();
                        if (operationLog.isDebugEnabled())
                        {
                            final Throwable th = new Throwable();
                            th.fillInStackTrace();
                            operationLog.debug("Data set " + dataSetCode + " has been locked.", th);
                        }
                    }
                    set.add(Thread.currentThread());
                    locked.add(dataSetCode);
                    log(dataSetCode, set);
                }
            } catch (Throwable th)
            {
                for (String dataSetCode : locked)
                {
                    releaseLock(dataSetCode);
                }
                throw CheckedExceptionTunnel.wrapIfNecessary(th);
            }
        }
    }

    @Override
    public void await(String dataSetCode)
    {
        Map<String, GuardedShareID> map = getDataSetCodeToShareIdMap();
        GuardedShareID guardedShareId = map.get(dataSetCode);
        if (guardedShareId != null)
        {
            try
            {
                guardedShareId.await();
            } catch (EnvironmentFailureException ex)
            {
                if (ex.getMessage().contains("time out"))
                {
                    final Set<Thread> set = lockedDataSets.get(dataSetCode);
                    if (set != null)
                    {
                        final StringBuilder b = new StringBuilder();
                        for (Thread t : set)
                        {
                            b.append(t.getName());
                            b.append(',');
                        }
                        if (b.length() > 0)
                        {
                            b.setLength(b.length() - 1);
                        }
                        operationLog.error("Timeout: Lock for data set " + dataSetCode
                                + " is held by threads '" + b.toString() + "' for "
                                + lockingTimeOut + " seconds.");
                    }
                }
                throw ex;
            }
        }
    }

    @Override
    public void releaseLock(String dataSetCode)
    {
        synchronized (lockedDataSets)
        {
            Set<Thread> set = lockedDataSets.get(dataSetCode);
            if (set == null)
            {
                return;
            }
            set.remove(Thread.currentThread());
            if (set.isEmpty())
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Unlock data set " + dataSetCode);
                }
                lockedDataSets.remove(dataSetCode);
                getGuardedShareId(dataSetCode).unlock();
            }
            log(dataSetCode, set);
        }
    }

    @Override
    public void releaseLocks()
    {
        synchronized (lockedDataSets)
        {
            List<String> dataSets = new ArrayList<String>(lockedDataSets.keySet());
            for (String dataSet : dataSets)
            {
                releaseLock(dataSet);
            }
        }
    }

    @Override public void cleanupLocks()
    {
        synchronized (lockedDataSets)
        {
            Collection<String> dataSetsCodes = new ArrayList<>(lockedDataSets.keySet());

            for (String dataSetCode : dataSetsCodes)
            {
                try
                {
                    Set<Thread> threads = lockedDataSets.get(dataSetCode);

                    if (threads == null)
                    {
                        continue;
                    }

                    Set<Thread> removedThreads = new HashSet<>(threads);
                    threads.removeIf(thread -> !thread.isAlive());
                    removedThreads.removeAll(threads);

                    if (threads.isEmpty())
                    {
                        if (operationLog.isDebugEnabled())
                        {
                            operationLog.debug("Unlock data set " + dataSetCode);
                        }

                        lockedDataSets.remove(dataSetCode);
                        getGuardedShareId(dataSetCode).unlock();
                    }

                    log(dataSetCode, threads);

                    if (!removedThreads.isEmpty())
                    {
                        if (operationLog.isDebugEnabled())
                        {
                            operationLog.debug("Cleaned up dataset " + dataSetCode + " locks. Removed locks held by dead threads: " + removedThreads);
                        }
                    }
                } catch (Exception e)
                {
                    operationLog.warn("Could not clean up dataset " + dataSetCode + " locks", e);
                }
            }
        }
    }

    private void log(String dataSetCode, Set<Thread> set)
    {
        if (operationLog.isDebugEnabled() && set.isEmpty() == false)
        {
            StringBuilder builder = new StringBuilder();
            for (Thread thread : set)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(thread.getName());
            }
            operationLog.debug("Data set " + dataSetCode + " is locked by the following threads: "
                    + builder);
        }
    }

    private GuardedShareID tryGetGuardedShareId(String dataSetCode)
    {
        GuardedShareID shareId = getDataSetCodeToShareIdMap().get(dataSetCode);
        if (shareId == null)
        {
            updateValueForDataSet(dataSetCode);
            shareId = getDataSetCodeToShareIdMap().get(dataSetCode);
        }
        return shareId;
    }

    private GuardedShareID getGuardedShareId(String dataSetCode)
    {
        GuardedShareID shareId = tryGetGuardedShareId(dataSetCode);
        if (shareId == null)
        {
            throw new IllegalArgumentException("Unknown data set: " + dataSetCode);
        }
        return shareId;
    }

    private Map<String, GuardedShareID> getDataSetCodeToShareIdMap()
    {
        synchronized (dataSetCodeToShareIdMapMonitor)
        {
            if (dataSetCodeToShareIdMap == null)
            {
                List<DataSetShareId> dataSets = service.listDataSetShareIds();
                dataSetCodeToShareIdMap = new HashMap<String, GuardedShareID>();
                for (DataSetShareId dataSet : dataSets)
                {
                    String dataSetCode = dataSet.getDataSetCode();
                    String shareId = dataSet.getShareId();
                    addShareId(dataSetCodeToShareIdMap, dataSetCode, shareId);
                }
                operationLog.info("Share id manager initialized with " + dataSets.size()
                        + " data sets.");
            }
            return dataSetCodeToShareIdMap;
        }
    }

    private void updateValueForDataSet(String dataSetCode)
    {
        // We assume that the dataSetCodeToShareIdMap is already initialized -- otherwise we
        // wouldn't be here.
        AbstractExternalData abstractDataSet = service.tryGetLocalDataSet(dataSetCode);
        if (null == abstractDataSet)
        {
            return;
        }
        String shareId;
        if (abstractDataSet instanceof PhysicalDataSet)
        {
            PhysicalDataSet dataSet = (PhysicalDataSet) abstractDataSet;
            shareId = dataSet.getShareId();
        } else
        {
            /*
             * If dataset is not physical we assume it has default share id. see {@link
             * ch.systemsx.cisd.openbis.dss.generic.server.EncapsulatedOpenBISService# listDataSetShareIds}
             */
            shareId = ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID;
        }
        addShareId(dataSetCodeToShareIdMap, dataSetCode, shareId);
    }
}
