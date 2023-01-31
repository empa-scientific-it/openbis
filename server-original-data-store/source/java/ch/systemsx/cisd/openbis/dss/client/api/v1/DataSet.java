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

package ch.systemsx.cisd.openbis.dss.client.api.v1;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.api.retry.Retry;
import ch.systemsx.cisd.common.api.retry.RetryCaller;
import ch.systemsx.cisd.common.api.retry.RetryProxyFactory;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;

/**
 * A class that provides uniform access to data set metadata (from the openBIS AS) and data (from the openBIS DSS).
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSet
{
    private static final int MINIMAL_MINOR_VERSION_DELIVERING_CONTAINER = 20;

    private final IOpenbisServiceFacade facade;

    private final IDssComponent dssComponent;

    private ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet metadata;

    private List<DataSet> containedDataSets;

    private List<DataSet> containerDataSets;

    private IDataSetDss dataSetDss;

    /* Default constructor needed to create a retry-proxy */
    protected DataSet()
    {
        facade = null;
        dssComponent = null;
    }

    /**
     * Constructor.
     * 
     * @param facade The facade used to get access to the server.
     * @param metadata The metadata. May be null if not available at construction time.
     * @param dataSetDss The data. May be null if not available at construction time.
     */
    public DataSet(IOpenbisServiceFacade facade, IDssComponent dssComponent,
            ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet metadata,
            IDataSetDss dataSetDss)
    {
        this.facade = facade;
        this.dssComponent = dssComponent;
        this.metadata = metadata;
        this.dataSetDss = dataSetDss;

        // One of the ivars must be non-null;
        assert metadata != null || dataSetDss != null;
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getCode()
     */
    @Retry
    public String getCode()
    {
        return (metadata == null) ? dataSetDss.getCode() : metadata.getCode();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getExperimentIdentifier()
     */
    @Retry
    public String getExperimentIdentifier()
    {
        return getMetadata().getExperimentIdentifier();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getSampleIdentifierOrNull()
     */
    @Retry
    public String getSampleIdentifierOrNull()
    {
        return getMetadata().getSampleIdentifierOrNull();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getDataSetTypeCode()
     */
    @Retry
    public String getDataSetTypeCode()
    {
        return getMetadata().getDataSetTypeCode();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getRegistrationDate()
     */
    @Retry
    public Date getRegistrationDate()
    {
        return getMetadata().getRegistrationDate();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getProperties()
     */
    @Retry
    public HashMap<String, String> getProperties()
    {
        return getMetadata().getProperties();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getRetrievedConnections()
     */
    @Retry
    public EnumSet<Connections> getRetrievedConnections()
    {
        return getMetadata().getRetrievedConnections();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getParentCodes()
     */
    @Retry
    public List<String> getParentCodes()
    {
        return getMetadata().getParentCodes();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getChildrenCodes()
     */
    @Retry
    public List<String> getChildrenCodes()
    {
        return getMetadata().getChildrenCodes();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#isContainerDataSet()
     */
    @Retry
    public boolean isContainerDataSet()
    {
        return getMetadata().isContainerDataSet();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#isLinkDataSet()
     */
    @Retry
    public boolean isLinkDataSet()
    {
        return getMetadata().isLinkDataSet();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getExternalDataSetCode()
     */
    @Retry
    public String getExternalDataSetCode()
    {
        return getMetadata().getExternalDataSetCode();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getExternalDataSetLink()
     */
    @Retry
    public String getExternalDataSetLink()
    {
        return getMetadata().getExternalDataSetLink();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getExternalDataManagementSystem()
     */
    @Retry
    public ExternalDataManagementSystem getExternalDataManagementSystem()
    {
        return getMetadata().getExternalDataManagementSystem();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getContainerOrNull()
     * @deprecated Use {@link #getContainerDataSets()}
     */
    @Retry
    @Deprecated
    public DataSet getContainerOrNull()
    {
        final DataSet containerOrNull =
                (getMetadata().getContainerOrNull() != null) ? new DataSet(facade, dssComponent,
                        getMetadata().getContainerOrNull(), null) : null;
        return containerOrNull;
    }

    /**
     * Returns <code>true</code>, if result of {@link #getContainerOrNull()} can be trusted and <code>false</code>, if it cannot be trusted because
     * the server is too old to deliver this information.
     */
    public boolean knowsContainer()
    {
        return facade.getMinorVersionInformationService() >= MINIMAL_MINOR_VERSION_DELIVERING_CONTAINER;
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getContainedDataSets()
     */
    @Retry
    public List<DataSet> getContainedDataSets()
    {
        if (null == containedDataSets)
        {
            containedDataSets = new ArrayList<DataSet>();
            List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> primitiveContainedDataSets =
                    getMetadata().getContainedDataSets();
            for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet primitiveContainedDataSet : primitiveContainedDataSets)
            {
                DataSet containedDataSet = new DataSet(facade, dssComponent, primitiveContainedDataSet, null);
                containedDataSets.add(containedDataSet);
            }

        }
        return containedDataSets;
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getContainerDataSets()
     */
    @Retry
    public List<DataSet> getContainerDataSets()
    {
        if (null == containerDataSets)
        {
            containerDataSets = new ArrayList<DataSet>();
            List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> primitiveContainerDataSets =
                    getMetadata().getContainerDataSets();
            for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet primitiveContainerDataSet : primitiveContainerDataSets)
            {
                DataSet containerDataSet = new DataSet(facade, dssComponent, primitiveContainerDataSet, null);
                containerDataSets.add(containerDataSet);
            }
        }
        return containerDataSets;
    }

    /**
     * Returns the primary data set. For a non-container data set, this is itself. For a container data set, this is the one contained data set that
     * is considered primary.
     * 
     * @return The data set that is considered primary, or null if the primary data set cannot be determined.
     */
    @Retry
    public DataSet getPrimaryDataSetOrNull()
    {
        if (false == isContainerDataSet())
        {
            return this;
        }

        // Find the contained data set that follows the specified naming convention
        Pattern containerDataSetTypePattern = Pattern.compile("(.*)_CONTAINER(.*)");
        // See if the container follows the pattern
        Matcher matcher = containerDataSetTypePattern.matcher(getDataSetTypeCode());
        if (false == matcher.matches())
        {
            // We do not know how to figure out what the primary data set might be
            return null;
        }

        // The primary data set type is the same as the container with the "_CONTAINER" removed
        String primaryDataSetType = matcher.group(1);
        if (null == primaryDataSetType)
        {
            primaryDataSetType = matcher.group(2);
        } else
        {
            if (null != matcher.group(2))
            {
                primaryDataSetType = primaryDataSetType + matcher.group(2);
            }
        }
        if (null == primaryDataSetType)
        {
            return null;
        }

        List<DataSet> contained = getContainedDataSets();
        List<DataSet> matchedDataSets = new ArrayList<DataSet>();
        for (DataSet ds : contained)
        {
            if (primaryDataSetType.equals(ds.getDataSetTypeCode()))
            {
                matchedDataSets.add(ds);
            }
        }

        // Return the single match, or null otherwise
        if (1 == matchedDataSets.size())
        {
            return matchedDataSets.get(0);
        }
        return null;
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#equals(java.lang.Object)
     */
    @Override
    @Retry
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof DataSet == false)
        {
            return false;
        }

        DataSet other = (DataSet) obj;
        return getMetadata().equals(other.getMetadata());
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#hashCode()
     */
    @Override
    @Retry
    public int hashCode()
    {
        return getMetadata().hashCode();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#toString()
     */
    @Override
    @Retry
    public String toString()
    {
        return getMetadata().toString();
    }

    /**
     * @param startPath
     * @param isRecursive
     * @throws IllegalArgumentException
     * @throws InvalidSessionException
     * @see ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss#listFiles(java.lang.String, boolean)
     */
    @Retry
    public FileInfoDssDTO[] listFiles(String startPath, boolean isRecursive)
            throws IllegalArgumentException, InvalidSessionException
    {
        return getDataSetDss().listFiles(startPath, isRecursive);
    }

    /**
     * @param path
     * @throws IllegalArgumentException
     * @throws InvalidSessionException
     * @see ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss#getFile(java.lang.String)
     */
    @Retry
    public InputStream getFile(String path) throws IllegalArgumentException,
            InvalidSessionException
    {
        return getDataSetDss().getFile(path);
    }

    /**
     * @param overrideStoreRootPathOrNull
     * @throws IllegalArgumentException
     * @throws InvalidSessionException
     * @see ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss#tryLinkToContents(java.lang.String)
     */
    @Retry
    public File tryLinkToContents(String overrideStoreRootPathOrNull)
            throws IllegalArgumentException, InvalidSessionException
    {
        return getDataSetDss().tryLinkToContents(overrideStoreRootPathOrNull);
    }

    /**
     * @param overrideStoreRootPathOrNull
     * @param downloadDir
     * @throws IllegalArgumentException
     * @throws InvalidSessionException
     * @see ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss#getLinkOrCopyOfContents(java.lang.String, java.io.File)
     */
    @Retry
    public File getLinkOrCopyOfContents(String overrideStoreRootPathOrNull, File downloadDir)
            throws IllegalArgumentException, InvalidSessionException
    {
        return getDataSetDss().getLinkOrCopyOfContents(overrideStoreRootPathOrNull, downloadDir);
    }

    /**
     * @param overrideStoreRootPathOrNull
     * @param downloadDir
     * @param pathInDataSet
     * @throws IllegalArgumentException
     * @throws InvalidSessionException
     * @see ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss#getLinkOrCopyOfContent(java.lang.String, java.io.File, java.lang.String)
     */
    @Retry
    public File getLinkOrCopyOfContent(String overrideStoreRootPathOrNull, File downloadDir,
            String pathInDataSet) throws IllegalArgumentException, InvalidSessionException
    {
        return getDataSetDss().getLinkOrCopyOfContent(overrideStoreRootPathOrNull, downloadDir,
                pathInDataSet);
    }

    /**
     * @see ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss#getSessionURLForFile(java.lang.String)
     */
    @Retry
    public String getSessionURLForFile(String path) throws IllegalArgumentException,
            InvalidSessionException
    {
        return getDataSetDss().getSessionURLForFile(path);
    }

    /**
     * @see ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss#getURLForFileWithTimeout(String, long)
     */
    @Retry
    public String getURLForFileWithTimeout(String path, long validityDurationInSeconds)
            throws IllegalArgumentException, InvalidSessionException
    {
        return getDataSetDss().getURLForFileWithTimeout(path, validityDurationInSeconds);
    }

    public String tryGetInternalPathInDataStore()
    {
        return getDataSetDss().tryGetInternalPathInDataStore();
    }

    /**
     * Internal accessor, made public for testing, but clients should not need it.
     */
    public IDataSetDss getDataSetDss()
    {
        // lazily initialize the ivar.
        if (null == dataSetDss)
        {
            RetryCaller<IDataSetDss, RuntimeException> caller =
                    new RetryCaller<IDataSetDss, RuntimeException>()
                        {
                            @Override
                            protected IDataSetDss call()
                            {
                                return dssComponent.getDataSet(getMetadata().getCode());
                            }
                        };
            dataSetDss = RetryProxyFactory.createProxy(caller.callWithRetry());
        }
        return dataSetDss;
    }

    /**
     * Internal accessor
     */
    private ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet getMetadata()
    {
        if (null == metadata)
        {
            RetryCaller<DataSet, RuntimeException> caller =
                    new RetryCaller<DataSet, RuntimeException>()
                        {
                            @Override
                            protected DataSet call()
                            {
                                return facade.getDataSet(dataSetDss.getCode());
                            }
                        };

            DataSet dataSetWithMetaData = caller.callWithRetry();
            if (dataSetWithMetaData != null)
            {
                metadata = dataSetWithMetaData.getMetadata();
            }
        }
        return metadata;
    }
}
