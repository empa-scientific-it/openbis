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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Context for perfoming archiving/unarchiving.
 * 
 * @author Franz-Josef Elmer
 */
public class ArchiverTaskContext
{
    private final IDataSetDirectoryProvider directoryProvider;

    private IUnarchivingPreparation unarchivingPreparation = new IUnarchivingPreparation()
        {
            @Override
            public void prepareForUnarchiving(List<DatasetDescription> dataSet)
            {
            }
        };

    private final IHierarchicalContentProvider hierarchicalContentProvider;

    private String userId;

    private String userEmail;

    private String userSessionToken;

    private boolean forceUnarchiving;

    private Map<String, String> options;

    public ArchiverTaskContext(IDataSetDirectoryProvider directoryProvider,
            IHierarchicalContentProvider hierarchicalContentProvider)
    {
        this.directoryProvider = directoryProvider;
        this.hierarchicalContentProvider = hierarchicalContentProvider;
    }

    public IDataSetDirectoryProvider getDirectoryProvider()
    {
        return directoryProvider;
    }

    public IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        return hierarchicalContentProvider;
    }

    public void setUnarchivingPreparation(IUnarchivingPreparation unarchivingPreparation)
    {
        if (unarchivingPreparation == null)
        {
            throw new IllegalArgumentException("Unspecified unarchiving preparation object.");
        }
        this.unarchivingPreparation = unarchivingPreparation;
    }

    public IUnarchivingPreparation getUnarchivingPreparation()
    {
        return unarchivingPreparation;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

    public void setUserSessionToken(String userSessionToken)
    {
        this.userSessionToken = userSessionToken;
    }

    public String getUserSessionToken()
    {
        return userSessionToken;
    }

    public boolean isForceUnarchiving()
    {
        return forceUnarchiving;
    }

    public void setForceUnarchiving(boolean delayedUnarchiving)
    {
        this.forceUnarchiving = delayedUnarchiving;
    }

    public Map<String, String> getOptions()
    {
        return options;
    }

    public void setOptions(Map<String, String> options)
    {
        this.options = options;
    }

}
