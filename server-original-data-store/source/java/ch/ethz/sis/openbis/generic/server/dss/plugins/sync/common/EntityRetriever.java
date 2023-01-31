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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
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
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.Node;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.NodeIdentifier;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
//import ch.ethz.sis.openbis.generic.shared.entitygraph.Edge;
//import ch.ethz.sis.openbis.generic.shared.entitygraph.EntityGraph;
//import ch.ethz.sis.openbis.generic.shared.entitygraph.Node;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;

public class EntityRetriever implements IEntityRetriever
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, EntityRetriever.class);

    private EntityGraph<INode> graph = new EntityGraph<INode>();

    private final IApplicationServerApi v3Api;

    private final IMasterDataRegistrationTransaction masterDataRegistrationTransaction;

    private final String sessionToken;

    private EntityRetriever(IApplicationServerApi v3Api, String sessionToken, IMasterDataRegistrationTransaction masterDataRegistrationTransaction)
    {
        this.v3Api = v3Api;
        this.sessionToken = sessionToken;
        this.masterDataRegistrationTransaction = masterDataRegistrationTransaction;
    }

    public static EntityRetriever createWithMasterDataRegistationTransaction(IApplicationServerApi v3Api, String sessionToken,
            IMasterDataRegistrationTransaction masterDataRegistrationTransaction)
    {
        return new EntityRetriever(v3Api, sessionToken, masterDataRegistrationTransaction);
    }

    public static EntityRetriever createWithSessionToken(IApplicationServerApi v3Api, String sessionToken)
    {
        return new EntityRetriever(v3Api, sessionToken, null);
    }
    
    private boolean ponging;
    
    private boolean pongingFinished;
    
    public void start(PrintWriter writer)
    {
        operationLog.info("Start");
        new Thread(new Runnable()
            {
                
                @Override
                public void run()
                {
                    ponging = true;
                    while (ponging)
                    {
                        try
                        {
                            Thread.sleep(5000);
                            writer.print(" ");
                            writer.flush();
                            operationLog.info("pong");
                        } catch (InterruptedException e)
                        {
                            // silently ignored
                        }
                    }
                    pongingFinished = true;
                    operationLog.info("ponging finished");
                    synchronized (EntityRetriever.this)
                    {
                        EntityRetriever.this.notifyAll();
                    }
                }
            }, "Pong").start();
    }
    
    public void finish()
    {
        operationLog.info("Finish");
        ponging = false;
        
        synchronized (this) {
                try
                {
                    while (pongingFinished == false)
                    {
                        wait();
                    }
                } catch (InterruptedException e)
                {
                    // silently ignored
                }
        }
    
        
    }

    @Override
    public EntityGraph<INode> getEntityGraph(String spaceId)
    {
        long t0 = System.currentTimeMillis();
        try
        {
            buildEntityGraph(spaceId);
            return graph;
        } finally
        {
            logTime(t0, "getEntityGraph(" + spaceId + ")");
        }
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

    private void buildEntityGraph(String spaceId)
    {
        graph = new EntityGraph<INode>();

        // add shared samples
        /*
         * adding shared samples to the entity graph for a space means if we are synching from multiple spaces, each entity graph (for each space)
         * will have the shared entity. When we add them to the RL (Resource List) in the data source servlet, any duplicate will throw an error when
         * using the Resync library. To work around this we catch the duplicate exceptions where a shared sample is involved.
         */
        addSharedSamples();

        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withSpace().withCode().thatEquals(spaceId);

        List<Project> projects = v3Api.searchProjects(sessionToken, criteria, createProjectFetchOptions()).getObjects();

        for (Project project : projects)
        {
            Node<Project> prjNode = new Node<Project>(project);
            graph.addNode(prjNode);
            addExperiments(prjNode);
            addSamplesForProject(prjNode);
        }

        // add space samples
        addSpaceSamples(spaceId);

        // TODO logout?
        // v3.logout(sessionToken);
    }

    private void addSharedSamples()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withoutSpace();

        List<Sample> samples = v3Api.searchSamples(sessionToken, criteria, createSampleFetchOptions()).getObjects();

        for (Sample sample : samples)
        {
            if (sample.getSpace() != null)
            {
                throw new RuntimeException("Expected a shared sample but got " + sample);
            }
            Node<Sample> sampleNode = new Node<Sample>(sample);
            graph.addNode(sampleNode);
            addChildAndComponentSamples(sampleNode);
            addDataSetsForSample(sampleNode);
        }
    }

    private void addSpaceSamples(String spaceCode)
    {
        // Add samples that are connected with a space only (i.e. have project == null and experiment == null).

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withCode().thatEquals(spaceCode);
        criteria.withoutProject();
        criteria.withoutExperiment();
        criteria.withAndOperator();

        List<Sample> samples = v3Api.searchSamples(sessionToken, criteria, createSampleFetchOptions()).getObjects();

        for (Sample sample : samples)
        {
            Node<Sample> sampleNode = new Node<Sample>(sample);
            graph.addNode(sampleNode);
            addChildAndComponentSamples(sampleNode);
            addDataSetsForSample(sampleNode);
        }
    }

    private void addExperiments(Node<Project> prjNode)
    {
        for (Experiment exp : prjNode.getEntity().getExperiments())
        {
            Node<Experiment> expNode = new Node<Experiment>(exp);
            graph.addEdge(prjNode, expNode, new Edge(CONNECTION));
            addSamplesForExperiment(expNode);
            addDataSetsForExperiment(expNode);
        }
    }

    private void addSamplesForExperiment(Node<Experiment> expNode)
    {
        for (Sample sample : expNode.getEntity().getSamples())
        {
            // Add samples that are connected with an experiment and optionally a project.

            Node<Sample> sampleNode = new Node<Sample>(sample);
            graph.addEdge(expNode, sampleNode, new Edge(CONNECTION));

            addDataSetsForSample(sampleNode);
            addChildAndComponentSamples(sampleNode);
        }
    }

    private void addSamplesForProject(Node<Project> prjNode)
    {
        for (Sample sample : prjNode.getEntity().getSamples())
        {
            // Add samples that are connected with a project only (i.e. have experiment == null).

            if (sample.getExperiment() == null)
            {
                Node<Sample> sampleNode = new Node<Sample>(sample);
                graph.addEdge(prjNode, sampleNode, new Edge(CONNECTION));

                addDataSetsForSample(sampleNode);
                addChildAndComponentSamples(sampleNode);
            }
        }
    }

    private void addDataSetsForExperiment(Node<Experiment> expNode)
    {
        for (DataSet dataSet : expNode.getEntity().getDataSets())
        {
            Node<DataSet> dataSetNode = new Node<DataSet>(dataSet);
            graph.addEdge(expNode, dataSetNode, new Edge(CONNECTION));
            addChildAndContainedDataSets(dataSetNode);
        }
    }

    private void addDataSetsForSample(Node<Sample> sampleNode)
    {
        for (DataSet dataSet : sampleNode.getEntity().getDataSets())
        {
            Node<DataSet> dataSetNode = new Node<DataSet>(dataSet);
            graph.addEdge(sampleNode, dataSetNode, new Edge(CONNECTION));
            addChildAndContainedDataSets(dataSetNode);
        }
    }

    private void addChildAndComponentSamples(Node<Sample> sampleNode)
    {
        NodeIdentifier identifier = sampleNode.getIdentifier();
        // first add the children
        if (graph.isVisitedAsParent(identifier) == false)
        {
            graph.markAsVisitedAsParent(identifier);
            addChildSamples(sampleNode);
        }

        // then add contained samples
        if (graph.isVisitedAsContainer(identifier) == false)
        {
            graph.markAsVisitedAsContainer(identifier);
            addComponentSamples(sampleNode);
        }
    }

    private void addComponentSamples(Node<Sample> sampleNode)
    {
        for (Sample sample : sampleNode.getEntity().getComponents())
        {
            Node<Sample> subSampleNode = new Node<Sample>(sample);
            graph.addEdge(sampleNode, subSampleNode, new Edge(COMPONENT));

            addDataSetsForSample(subSampleNode);
            addChildAndComponentSamples(subSampleNode);
        }
    }

    private void addChildSamples(Node<Sample> sampleNode)
    {
        for (Sample sample : sampleNode.getEntity().getChildren())
        {
            Node<Sample> subSampleNode = new Node<Sample>(sample);
            graph.addEdge(sampleNode, subSampleNode, new Edge(CHILD));

            addDataSetsForSample(subSampleNode);
            addChildAndComponentSamples(subSampleNode);
        }
    }

    private void addChildAndContainedDataSets(Node<DataSet> dsNode)
    {
        NodeIdentifier identifier = dsNode.getIdentifier();
        // first add the children
        if (graph.isVisitedAsParent(identifier) == false)
        {
            graph.markAsVisitedAsParent(identifier);
            addChildDataSets(dsNode);
        }

        // then add contained data sets
        if (graph.isVisitedAsContainer(identifier) == false)
        {
            graph.markAsVisitedAsContainer(identifier);
            addComponentDataSets(dsNode);
        }
    }

    private void addComponentDataSets(Node<DataSet> dsNode)
    {
        for (DataSet ds : dsNode.getEntity().getComponents())
        {
            Node<DataSet> containedDsNode = new Node<DataSet>(ds);
            graph.addEdge(dsNode, containedDsNode, new Edge(COMPONENT));
            addChildAndContainedDataSets(containedDsNode);
        }
    }

    private void addChildDataSets(Node<DataSet> dsNode)
    {
        for (DataSet ds : dsNode.getEntity().getChildren())
        {
            Node<DataSet> childDsNode = new Node<DataSet>(ds);
            graph.addEdge(dsNode, childDsNode, new Edge(CHILD));

            addChildAndContainedDataSets(childDsNode);
        }
    }

    @Override
    public List<Material> fetchMaterials()
    {
        long t0 = System.currentTimeMillis();
        try
        {
            MaterialSearchCriteria criteria = new MaterialSearchCriteria();
            
            final MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
            fetchOptions.withRegistrator();
            fetchOptions.withType();
            fetchOptions.withProperties();
            
            SearchResult<Material> searchResult =
                    v3Api.searchMaterials(sessionToken, criteria, fetchOptions);
            
            return searchResult.getObjects();
        } finally
        {
            logTime(t0, "fetchMaterials");
        }
    }
    
    private void logTime(long t0, String method)
    {
        long duration = System.currentTimeMillis() - t0;
        operationLog.info(method + ": " + duration + " msec");
    }

    public String fetchMasterDataAsXML() throws ParserConfigurationException, TransformerException
    {
        long t0 = System.currentTimeMillis();
        try
        {
            MasterDataExtractor masterDataExtractor = new MasterDataExtractor(v3Api, sessionToken, masterDataRegistrationTransaction);
            return masterDataExtractor.fetchAsXmlString();
        } finally
        {
            logTime(t0, "fetchMasterDataAsXML");
        }
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

    private ProjectFetchOptions createProjectFetchOptions()
    {
        ProjectFetchOptions fo = new ProjectFetchOptions();
        fo.withRegistrator();
        fo.withModifier();
        fo.withSpace();
        fo.withAttachments();
        fo.withExperimentsUsing(createExperimentFetchOptions());
        fo.withSamplesUsing(createSampleFetchOptions());
        return fo;
    }

    private ExperimentFetchOptions createExperimentFetchOptions()
    {
        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withRegistrator();
        fo.withModifier();
        fo.withProperties();
        fo.withProject().withSpace();
        fo.withType();
        fo.withAttachments();
        fo.withSamplesUsing(createSampleFetchOptions());
        fo.withDataSetsUsing(createDataSetFetchOptions());
        return fo;
    }

    private SampleFetchOptions createSampleFetchOptions()
    {
        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withRegistrator();
        fo.withModifier();
        fo.withProperties();
        fo.withDataSets();
        fo.withType();
        fo.withExperiment();
        fo.withProject();
        fo.withSpace();
        fo.withAttachments();
        fo.withChildrenUsing(fo);
        fo.withComponentsUsing(fo);
        fo.withDataSetsUsing(createDataSetFetchOptions());
        return fo;
    }

    private DataSetFetchOptions createDataSetFetchOptions()
    {
        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withRegistrator();
        fo.withModifier();
        fo.withType();
        fo.withSample();
        fo.withExperiment();
        fo.withProperties();
        fo.withChildrenUsing(fo);
        fo.withComponentsUsing(fo);
        fo.withLinkedData().withExternalDms();
        return fo;
    }

}