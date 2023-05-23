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
    private final Map<String, Node> nodesByPath = new HashMap<>();

    private final Map<String, FtpFile> filesByPath = new HashMap<>();

    private final Map<String, DataSet> dataSetsByCode = new HashMap<>();

    private final Map<String, List<AbstractExternalData>> dataSetsByExperiment = new HashMap<>();

    private final Map<String, AbstractExternalData> externalData = new HashMap<>();

    private final Map<String, Experiment> experiments = new HashMap<>();

    private final Map<String, FtpFile> v3Responses = new HashMap<>();

    private final Map<String, IHierarchicalContent> contents = new HashMap<>();

    private final Map<String, Boolean> accessData = new HashMap<>();

    private final Map<String, Object> objects = new HashMap<>();

    // TODO Remove this constructor
    public Cache(ITimeProvider timeProvider)
    {
        this();
    }

    public Cache()
    {
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
        filesByPath.put(path, file);
    }

    public FtpFile getFile(String path)
    {
        return filesByPath.get(path);
    }

    public void putDataSetsForExperiment(List<AbstractExternalData> dataSets, String experimentPermId)
    {
        dataSetsByExperiment.put(experimentPermId, dataSets);
    }

    public List<AbstractExternalData> getDataSetsByExperiment(String experimentPermId)
    {
        return dataSetsByExperiment.get(experimentPermId);
    }

    public void putDataSet(DataSet dataSet)
    {
        dataSetsByCode.put(dataSet.getCode(), dataSet);
    }

    public DataSet getDataSet(String dataSetCode)
    {
        return dataSetsByCode.get(dataSetCode);
    }

    public AbstractExternalData getExternalData(String code)
    {
        return externalData.get(code);
    }

    public void putExternalData(AbstractExternalData dataSet)
    {
        externalData.put(dataSet.getCode(), dataSet);
    }

    public Experiment getExperiment(String experimentId)
    {
        return experiments.get(experimentId);
    }

    public void putExperiment(Experiment experiment)
    {
        experiments.put(experiment.getIdentifier(), experiment);
    }

    public FtpFile getResponse(String key)
    {
        return v3Responses.get(key);
    }

    public void putResponse(String key, FtpFile file)
    {
        v3Responses.put(key, file);
    }

    public IHierarchicalContent getContent(String key)
    {
        return contents.get(key);
    }

    public void putContent(String key, IHierarchicalContent content)
    {
        contents.put(key, content);
    }

    public Boolean getAccess(String dataSetCode)
    {
        return accessData.get(dataSetCode);
    }

    public void putAccess(String dataSetCode, Boolean access)
    {
        accessData.put(dataSetCode, access);
    }

    public Object getObject(String key)
    {
        return objects.get(key);
    }

    public void putObject(String key, Object object)
    {
        objects.put(key, object);
    }

    @Override
    public String toString()
    {
        StringBuilder statsSummary = new StringBuilder();
        addStats(statsSummary, nodesByPath, "nodes");
        addStats(statsSummary, filesByPath, "files");
        addStats(statsSummary, dataSetsByCode, "data sets by code");
        addStats(statsSummary, dataSetsByExperiment, "data sets by experiment");
        addStats(statsSummary, externalData, "data sets");
        addStats(statsSummary, experiments, "experiments");
        addStats(statsSummary, v3Responses, "responses");
        addStats(statsSummary, contents, "contents");
        addStats(statsSummary, accessData, "accesses");
        addStats(statsSummary, objects, "objects");
        return statsSummary.length() > 0 ? "Cache with " + statsSummary : "Cache is empty";
    }

    private void addStats(StringBuilder builder, Map<?, ?> map, String name)
    {
        if (map.isEmpty() == false)
        {
            if (builder.length() > 0)
            {
                builder.append(", ");
            }
            builder.append(map.size()).append(" ").append(name);
        }
    }
}