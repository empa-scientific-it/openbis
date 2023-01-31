/*
 * Copyright 2016 ETH Zuerich, SIS
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
package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common;

import static ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.Edge.CHILD;
import static ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.Edge.COMPONENT;
import static ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.Edge.CONNECTION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.Edge;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.EntityGraph;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.IEntityRetriever;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.INode;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.NodeIdentifier;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.SkinnyNode;
//import ch.ethz.sis.openbis.generic.shared.entitygraph.Edge;
//import ch.ethz.sis.openbis.generic.shared.entitygraph.EntityGraph;
//import ch.ethz.sis.openbis.generic.shared.entitygraph.Node;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;

public class SkinnyEntityRetriever implements IEntityRetriever
{
    private EntityGraph<INode> graph = new EntityGraph<INode>();

    private final IApplicationServerApi v3Api;

    private final IMasterDataRegistrationTransaction masterDataRegistrationTransaction;

    private final String sessionToken;

    private SkinnyEntityRetriever(IApplicationServerApi v3Api, String sessionToken, IMasterDataRegistrationTransaction masterDataRegistrationTransaction)
    {
        this.v3Api = v3Api;
        this.sessionToken = sessionToken;
        this.masterDataRegistrationTransaction = masterDataRegistrationTransaction;
    }
    
    public static SkinnyEntityRetriever createWithMasterDataRegistationTransaction(IApplicationServerApi v3Api, String sessionToken,
            IMasterDataRegistrationTransaction masterDataRegistrationTransaction)
    {
        return new SkinnyEntityRetriever(v3Api, sessionToken, masterDataRegistrationTransaction);
    }

    public static SkinnyEntityRetriever createWithSessionToken(IApplicationServerApi v3Api, String sessionToken)
    {
        return new SkinnyEntityRetriever(v3Api, sessionToken, null);
    }

    @Override
    public EntityGraph<INode> getEntityGraph(String spaceId)
    {
        buildEntityGraph(spaceId);
        return graph;
    }

    public boolean spaceExists(String spaceId)
    {
        SpacePermId spacePermId = new SpacePermId(spaceId);
        Map<ISpaceId, Space> map =
                v3Api.getSpaces(sessionToken,
                        Arrays.asList(spacePermId),
                        new SpaceFetchOptions());
        return map.get(spacePermId) != null;
    }

    /**
     * Returns spaces the logged in user is allowed to see
     */
    public List<Space> getSpaces()
    {
        return v3Api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();
    }

    public void buildEntityGraph(String spaceId)
    {
        graph = new EntityGraph<INode>();

        // add shared samples
        /*
         * adding shared samples to the entity graph for a space means if we are synching from multiple spaces, each entity graph (for each space)
         * will have the shared entity. When we add them to the RL (Resource List) in the data source servlet, any duplicate will throw an error when
         * using the Resync library. To work around this we catch the duplicate exceptions where a shared sample is involved.
         */
        findSharedSamples();

        // build the graph for the space from top-down starting from projects
        ProjectSearchCriteria prjCriteria = new ProjectSearchCriteria();
        prjCriteria.withSpace().withCode().thatEquals(spaceId);

        ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
        projectFetchOptions.withSpace();

        List<Project> projects = v3Api.searchProjects(sessionToken, prjCriteria, projectFetchOptions).getObjects();
        for (Project project : projects)
        {
            SkinnyNode prjNode = new SkinnyNode(project.getPermId().toString(), 
                    NodeIdentifier.asNodeIdentifier(project), null,
                    project.getSpace(),
                    project.getCode());
            graph.addNode(prjNode);
            findExperiments(prjNode);
        }

        // add space samples
        findSpaceSamples(spaceId);


        // TODO logout?
        // v3.logout(sessionToken);
    }

    // TODO temporary solution until V3 API SampleSearchCriteria.withoutSpace() is implemented
    private void findSharedSamples()
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withSpace();

        List<Sample> samples = v3Api.searchSamples(sessionToken, new SampleSearchCriteria(), fetchOptions).getObjects();
        for (Sample sample : samples)
        {
            if (sample.getSpace() == null)
            {
                INode sampleNode = new SkinnyNode(sample.getPermId().toString(),
                        NodeIdentifier.asNodeIdentifier(sample), sample.getType().getCode(),
                        null,
                        sample.getCode());
                graph.addNode(sampleNode);
                findChildAndComponentSamples(sampleNode);
                findAndAttachDataSets(sampleNode);
            }
        }
    }

    private void findExperiments(INode prjNode)
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withCode().thatEquals(prjNode.getCode());
        criteria.withProject().withSpace().withCode().thatEquals(prjNode.getSpace().getCode());
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProject().withSpace();
        fetchOptions.withType();

        List<Experiment> experiments = v3Api.searchExperiments(sessionToken, criteria, fetchOptions).getObjects();
        for (Experiment exp : experiments)
        {
            INode expNode = new SkinnyNode(exp.getPermId().toString(),
                    NodeIdentifier.asNodeIdentifier(exp), exp.getType().getCode(),
                    exp.getProject().getSpace(),
                    exp.getCode());
            graph.addEdge(prjNode, expNode, new Edge(CONNECTION));
            findSamplesForExperiment(expNode);
            findAndAttachDataSetsForExperiment(expNode);
        }
    }

    private void findSpaceSamples(String spaceCode)
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withCode().thatEquals(spaceCode);
        criteria.withoutExperiment();
        criteria.withAndOperator();

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withSpace();

        List<Sample> samples = v3Api.searchSamples(sessionToken, criteria, fetchOptions).getObjects();
        for (Sample sample : samples)
        {
            INode sampleNode = new SkinnyNode(sample.getPermId().toString(),
                    NodeIdentifier.asNodeIdentifier(sample), sample.getType().getCode(),
                    sample.getSpace(),
                    sample.getCode());
            graph.addNode(sampleNode);
            findChildAndComponentSamples(sampleNode);
            findAndAttachDataSets(sampleNode);
        }
    }

    private void findSamplesForExperiment(INode expNode)
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withId().thatEquals(new ExperimentPermId(expNode.getPermId()));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withDataSets();
        fetchOptions.withType();
        fetchOptions.withExperiment();
        fetchOptions.withSpace();

        List<Sample> samples = v3Api.searchSamples(sessionToken, criteria, fetchOptions).getObjects();
        for (Sample sample : samples)
        {
            INode sampleNode = new SkinnyNode(sample.getPermId().toString(),
                    NodeIdentifier.asNodeIdentifier(sample), sample.getType().getCode(),
                    sample.getSpace(),
                    sample.getCode());
            graph.addEdge(expNode, sampleNode, new Edge(CONNECTION));

            findAndAttachDataSets(sampleNode);
            findChildAndComponentSamples(sampleNode);
        }
    }

    private void findAndAttachDataSetsForExperiment(INode expNode)
    {
        DataSetSearchCriteria dsCriteria = new DataSetSearchCriteria();
        dsCriteria.withExperiment().withId().thatEquals(new ExperimentIdentifier(expNode.getIdentifier().getEntityIdentifier()));

        DataSetFetchOptions dsFetchOptions = new DataSetFetchOptions();
        dsFetchOptions.withType();
        dsFetchOptions.withSample();
        dsFetchOptions.withExperiment();
        // dsFetchOptions.withProperties();

        List<DataSet> dataSets = v3Api.searchDataSets(sessionToken, dsCriteria, dsFetchOptions).getObjects();
        for (DataSet dataSet : dataSets)
        {
            INode dataSetNode = new SkinnyNode(dataSet.getPermId().toString(),
                    NodeIdentifier.asNodeIdentifier(dataSet), dataSet.getType().getCode(),
                    null,
                    dataSet.getCode());
            graph.addEdge(expNode, dataSetNode, new Edge(CONNECTION));
            findChildAndContainedDataSets(dataSetNode);
        }
    }

    private void findAndAttachDataSets(INode sampleNode)
    {
        DataSetSearchCriteria dsCriteria = new DataSetSearchCriteria();
        dsCriteria.withSample().withId().thatEquals(new SampleIdentifier(sampleNode.getIdentifier().getEntityIdentifier()));

        DataSetFetchOptions dsFetchOptions = new DataSetFetchOptions();
        dsFetchOptions.withType();
        dsFetchOptions.withSample();
        dsFetchOptions.withExperiment();

        List<DataSet> dataSets = v3Api.searchDataSets(sessionToken, dsCriteria, dsFetchOptions).getObjects();
        for (DataSet dataSet : dataSets)
        {
            INode dataSetNode = new SkinnyNode(dataSet.getPermId().toString(),
                    NodeIdentifier.asNodeIdentifier(dataSet), dataSet.getType().getCode(),
                    null,
                    dataSet.getCode());
            graph.addEdge(sampleNode, dataSetNode, new Edge(CONNECTION));
            findChildAndContainedDataSets(dataSetNode);
        }
    }

    private void findChildAndComponentSamples(INode sampleNode)
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withDataSets();
        fetchOptions.withExperiment();
        fetchOptions.withSpace();

        // first find the children
        if (graph.isVisitedAsParent(sampleNode.getIdentifier()) == false)
        {
            graph.markAsVisitedAsParent(sampleNode.getIdentifier());
            findChildSamples(sampleNode, fetchOptions);
        }

        // then find contained samples
        if (graph.isVisitedAsContainer(sampleNode.getIdentifier()) == false)
        {
            graph.markAsVisitedAsContainer(sampleNode.getIdentifier());
            findComponentSamples(sampleNode, fetchOptions);
        }
    }

    private void findComponentSamples(INode sampleNode, SampleFetchOptions fetchOptions)
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withId().thatEquals(new SamplePermId(sampleNode.getPermId()));
        List<Sample> components = v3Api.searchSamples(sessionToken, criteria, fetchOptions).getObjects();
        for (Sample sample : components)
        {
            INode subSampleNode = new SkinnyNode(sample.getPermId().toString(),
                    NodeIdentifier.asNodeIdentifier(sample), sample.getType().getCode(),
                    sample.getSpace(),
                    sample.getCode());
            graph.addEdge(sampleNode, subSampleNode, new Edge(COMPONENT));

            findAndAttachDataSets(subSampleNode);
            findChildAndComponentSamples(subSampleNode);
        }
    }

    private void findChildSamples(INode sampleNode, SampleFetchOptions fetchOptions)
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withId().thatEquals(new SamplePermId(sampleNode.getPermId()));
        List<Sample> children = v3Api.searchSamples(sessionToken, criteria, fetchOptions).getObjects();
        for (Sample sample : children)
        {
            INode subSampleNode = new SkinnyNode(sample.getPermId().toString(),
                    NodeIdentifier.asNodeIdentifier(sample), sample.getType().getCode(),
                    sample.getSpace(),
                    sample.getCode());
            graph.addEdge(sampleNode, subSampleNode, new Edge(CHILD));
   
            findAndAttachDataSets(subSampleNode);
            findChildAndComponentSamples(subSampleNode);
        }
    }

    private void findChildAndContainedDataSets(INode dsNode)
    {
        DataSetFetchOptions dsFetchOptions = new DataSetFetchOptions();
        dsFetchOptions.withType();
        dsFetchOptions.withSample();
        dsFetchOptions.withExperiment();

        // first find the children
        if (graph.isVisitedAsParent(dsNode.getIdentifier()) == false)
        {
            graph.markAsVisitedAsParent(dsNode.getIdentifier());
            findChildDataSets(dsNode, dsFetchOptions);
        }

        // then find contained data sets
        if (graph.isVisitedAsContainer(dsNode.getIdentifier()) == false)
        {
            graph.markAsVisitedAsContainer(dsNode.getIdentifier());
            findComponentDataSets(dsNode, dsFetchOptions);
        }
    }

    private void findComponentDataSets(INode dsNode, DataSetFetchOptions dsFetchOptions)
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withContainer().withId().thatEquals(new DataSetPermId(dsNode.getPermId()));
        List<DataSet> components = v3Api.searchDataSets(sessionToken, criteria, dsFetchOptions).getObjects();
        for (DataSet ds : components)
        {
            INode containedDsNode = new SkinnyNode(ds.getPermId().toString(),
                    NodeIdentifier.asNodeIdentifier(ds), ds.getType().getCode(),
                    null,
                    ds.getCode());
            graph.addEdge(dsNode, containedDsNode, new Edge(COMPONENT));
            findChildAndContainedDataSets(containedDsNode);
        }
    }

    private void findChildDataSets(INode dsNode, DataSetFetchOptions dsFetchOptions)
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withParents().withId().thatEquals(new DataSetPermId(dsNode.getPermId()));
        List<DataSet> children = v3Api.searchDataSets(sessionToken, criteria, dsFetchOptions).getObjects();
        for (DataSet ds : children)
        {
            INode childDsNode = new SkinnyNode(ds.getPermId().toString(),
                    NodeIdentifier.asNodeIdentifier(ds), ds.getType().getCode(),
                    null,
                    ds.getCode());
            graph.addEdge(dsNode, childDsNode, new Edge(CHILD));

            findChildAndContainedDataSets(childDsNode);
        }
    }

    @Override
    public List<Material> fetchMaterials()
    {
        MaterialSearchCriteria criteria = new MaterialSearchCriteria();

        final MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withType();
        
        SearchResult<Material> searchResult =
                v3Api.searchMaterials(sessionToken, criteria, fetchOptions);

        return searchResult.getObjects();
    }

    public String fetchMasterDataAsXML() throws ParserConfigurationException, TransformerException
    {
        MasterDataExtractor masterDataExtractor = new MasterDataExtractor(v3Api, sessionToken, masterDataRegistrationTransaction);
        return masterDataExtractor.fetchAsXmlString();
    }

    protected List<String> extractCodes(List<? extends ICodeHolder> codeHolders)
    {
        List<String> codes = new ArrayList<>();
        for (ICodeHolder codeHolder : codeHolders)
        {
            codes.add(codeHolder.getCode());
        }
        return codes;
    }
}