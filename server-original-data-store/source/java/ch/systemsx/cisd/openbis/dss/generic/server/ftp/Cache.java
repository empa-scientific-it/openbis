/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;

/**
 * Helper class to cache objects retrieved from remote services. Used by {@link FtpPathResolverContext}.
 * 
 * @author Franz-Josef Elmer
 */
public class Cache
{
    static final long LIVE_TIME = 1000;

    private static final class TimeStampedObject<T>
    {
        private final long timestamp;

        private final T object;

        public TimeStampedObject(T object, long timestamp)
        {
            this.object = object;
            this.timestamp = timestamp;
        }
    }

    private final Map<String, Node> nodesByPath = new HashMap<>();

    private final Map<String, TimeStampedObject<FtpFile>> filesByPath = new HashMap<>();

    private final Map<String, TimeStampedObject<DataSet>> dataSetsByCode = new HashMap<String, Cache.TimeStampedObject<DataSet>>();

    private final Map<String, TimeStampedObject<List<AbstractExternalData>>> dataSetsByExperiment = new HashMap<>();

    private final Map<String, TimeStampedObject<AbstractExternalData>> externalData =
            new HashMap<String, Cache.TimeStampedObject<AbstractExternalData>>();

    private final Map<String, TimeStampedObject<Experiment>> experiments = new HashMap<String, Cache.TimeStampedObject<Experiment>>();

    private final ITimeProvider timeProvider;

    private final Map<String, TimeStampedObject<FtpFile>> v3Responses = new HashMap<>();

    private final Map<String, TimeStampedObject<IHierarchicalContent>> contents = new HashMap<>();

    private final Map<String, TimeStampedObject<Boolean>> accessData = new HashMap<>();

    public Cache(ITimeProvider timeProvider)
    {
        this.timeProvider = timeProvider;
    }

    public void putNode(Node node, String path)
    {
        nodesByPath.put(path, node);
    }

    public Node getNode(String path)
    {
        return nodesByPath.get(path);
    }

    public void putFile(FtpFile file, String path)
    {
        filesByPath.put(path, timestamp(file));
    }

    public FtpFile getFile(String path)
    {
        return getObject(filesByPath, path);
    }

    public void putDataSetsForExperiment(List<AbstractExternalData> dataSets, String experimentPermId)
    {
        dataSetsByExperiment.put(experimentPermId, timestamp(dataSets));
    }

    public List<AbstractExternalData> getDataSetsByExperiment(String experimentPermId)
    {
        return getObject(dataSetsByExperiment, experimentPermId);
    }

    public void putDataSet(DataSet dataSet)
    {
        dataSetsByCode.put(dataSet.getCode(), timestamp(dataSet));
    }

    public DataSet getDataSet(String dataSetCode)
    {
        return getObject(dataSetsByCode, dataSetCode);
    }

    public AbstractExternalData getExternalData(String code)
    {
        return getObject(externalData, code);
    }

    public void putExternalData(AbstractExternalData dataSet)
    {
        externalData.put(dataSet.getCode(), timestamp(dataSet));
    }

    public Experiment getExperiment(String experimentId)
    {
        return getObject(experiments, experimentId);
    }

    public void putExperiment(Experiment experiment)
    {
        experiments.put(experiment.getIdentifier(), timestamp(experiment));
    }

    public FtpFile getResponse(String key)
    {
        return getObject(v3Responses, key);
    }

    public void putResponse(String key, FtpFile file)
    {
        v3Responses.put(key, timestamp(file));
    }

    public IHierarchicalContent getContent(String key)
    {
        return getObject(contents, key);
    }

    public void putContent(String key, IHierarchicalContent content)
    {
        contents.put(key, timestamp(content));
    }

    public Boolean getAccess(String dataSetCode)
    {
        return getObject(accessData, dataSetCode);
    }

    public void putAccess(String dataSetCode, Boolean access)
    {
        accessData.put(dataSetCode, timestamp(access));
    }

    private <T> TimeStampedObject<T> timestamp(T object)
    {
        return new TimeStampedObject<T>(object, timeProvider.getTimeInMilliseconds());
    }

    private <T> T getObject(Map<String, TimeStampedObject<T>> map, String key)
    {
        TimeStampedObject<T> timeStampedObject = map.get(key);
        if (timeStampedObject == null)
        {
            return null;
        }
        if (timeProvider.getTimeInMilliseconds() - timeStampedObject.timestamp > LIVE_TIME)
        {
            map.remove(key);
            return null;
        }
        return timeStampedObject.object;
    }

}