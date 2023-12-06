/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.List;

/**
 * Proxy of a {@link IShareIdManager} which delegates calls to wrapped share ID manager. Methods can be overridden to change behavior.
 * 
 * @author Franz-Josef Elmer
 */
public class ProxyShareIdManager implements IShareIdManager
{
    private final IShareIdManager shareIdManager;

    public ProxyShareIdManager(IShareIdManager shareIdManager)
    {
        this.shareIdManager = shareIdManager;
    }

    @Override
    public boolean isKnown(String dataSetCode)
    {
        return shareIdManager.isKnown(dataSetCode);
    }

    @Override
    public String getShareId(String dataSetCode)
    {
        return shareIdManager.getShareId(dataSetCode);
    }

    @Override
    public void setShareId(String dataSetCode, String shareId)
    {
        shareIdManager.setShareId(dataSetCode, shareId);
    }

    @Override
    public void lock(String dataSetCode)
    {
        shareIdManager.lock(dataSetCode);
    }

    @Override
    public void lock(List<String> dataSetCodes)
    {
        shareIdManager.lock(dataSetCodes);
    }

    @Override
    public void await(String dataSetCode)
    {
        shareIdManager.await(dataSetCode);
    }

    @Override
    public void releaseLock(String dataSetCode)
    {
        shareIdManager.releaseLock(dataSetCode);
    }

    @Override
    public void releaseLocks()
    {
        shareIdManager.releaseLocks();
    }

    @Override public void cleanupLocks()
    {
        shareIdManager.cleanupLocks();
    }
}