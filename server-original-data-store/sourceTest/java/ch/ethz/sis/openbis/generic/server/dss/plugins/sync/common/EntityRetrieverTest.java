/*
 * Copyright 2018 ETH Zuerich, SIS
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

import static org.testng.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.EdgeNodePair;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.EntityGraph;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.INode;
import ch.systemsx.cisd.common.collection.SimpleComparator;

/**
 * @author Franz-Josef Elmer
 */
public class EntityRetrieverTest
{
    private static final String SPACE = "S";

    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private IApplicationServerApi v3api;

    private EntityRetriever entityRetriever;

    private Sequence sequence;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        v3api = context.mock(IApplicationServerApi.class);
        entityRetriever = EntityRetriever.createWithSessionToken(v3api, SESSION_TOKEN);
        sequence = context.sequence("sequence");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExampleWithDataSetContainer()
    {
        // Given
        prepareSharedSamples();
        prepareSpaceSamples();
        DataSetBuilder ds11 = d("ds1:1");
        DataSetBuilder ds12 = d("ds1:2");
        Builder<Sample> s1 = s("s1").with(d("ds1").with(ds11, ds12));
        Builder<Experiment> e1 = e("e1").with(ds11, ds12).with(s1);
        prepareProjects(p("p1").with(e1));

        // When
        EntityGraph<INode> graph = entityRetriever.getEntityGraph(SPACE);

        // Then
        assertEquals(render(graph), "D:DS1 -> [Component] D:DS1:1 [Component] D:DS1:2\n"
                + "D:DS1:1 ->\n"
                + "D:DS1:2 ->\n"
                + "E:E1 -> [Connection] S:S1\n"
                + "P:P1 -> [Connection] E:E1\n"
                + "S:S1 -> [Connection] D:DS1\n");

        context.assertIsSatisfied();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExampleWithDataSetParentChildWeb()
    {
        // Given
        prepareSharedSamples();
        prepareSpaceSamples();
        DataSetBuilder ds1 = d("ds1");
        DataSetBuilder ds2 = d("ds2");
        ds2.with(ds1);
        DataSetBuilder ds3 = d("ds3");
        ds3.with(ds1, ds2);
        Builder<Sample> s1 = s("s1").with(ds1, ds2);
        Builder<Experiment> e1 = e("e1").with(ds3);
        prepareProjects(p("p1").with(e1, s1));

        // When
        EntityGraph<INode> graph = entityRetriever.getEntityGraph(SPACE);

        // Then
        assertEquals(render(graph), "D:DS1 ->\n"
                + "D:DS2 -> [Child] D:DS1\n"
                + "D:DS3 -> [Child] D:DS1 [Child] D:DS2\n"
                + "E:E1 -> [Connection] D:DS3\n"
                + "P:P1 -> [Connection] E:E1 [Connection] S:S1\n"
                + "S:S1 -> [Connection] D:DS1 [Connection] D:DS2\n");

        context.assertIsSatisfied();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExampleWithProjectSamplAndExperimentWithSameIdentifier()
    {
        // Given
        prepareSharedSamples();
        prepareSpaceSamples();
        Builder<Sample> sample = s("C").with(d("ds1"));
        Builder<Experiment> experiment = e("C").with(d("ds2"));
        prepareProjects(p("B").with(experiment, sample));

        // When
        EntityGraph<INode> graph = entityRetriever.getEntityGraph(SPACE);

        // Then
        assertEquals(render(graph), "D:DS1 ->\n"
                + "D:DS2 ->\n"
                + "E:C -> [Connection] D:DS2\n"
                + "P:B -> [Connection] E:C [Connection] S:C\n"
                + "S:C -> [Connection] D:DS1\n");

        context.assertIsSatisfied();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExampleWithSharedSamples()
    {
        // Given
        prepareSharedSamples(s("S1"), s("S2"));
        prepareSpaceSamples();
        prepareProjects();

        // When
        EntityGraph<INode> graph = entityRetriever.getEntityGraph(SPACE);

        // Then
        assertEquals(render(graph), "S:S1 ->\n"
                + "S:S2 ->\n");

        context.assertIsSatisfied();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExampleWithSharedAndSpaceSamples()
    {
        // Given
        prepareSharedSamples(s("S1"));
        prepareSpaceSamples(s("S2").with(s("S3")));
        prepareProjects();

        // When
        EntityGraph<INode> graph = entityRetriever.getEntityGraph(SPACE);

        // Then
        assertEquals(render(graph), "S:S1 ->\n"
                + "S:S2 -> [Child] S:S3\n"
                + "S:S3 ->\n");

        context.assertIsSatisfied();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExampleWithContainerSamples()
    {
        // Given
        prepareSharedSamples();
        prepareSpaceSamples(s("S2").with(s("S2:1"), s("S2:2").with(s("S3").with(d("DS1")))));
        prepareProjects();

        // When
        EntityGraph<INode> graph = entityRetriever.getEntityGraph(SPACE);

        // Then
        assertEquals(render(graph), "D:DS1 ->\n"
                + "S:S2 -> [Component] S:S2:1 [Component] S:S2:2\n"
                + "S:S2:1 ->\n"
                + "S:S2:2 -> [Child] S:S3\n"
                + "S:S3 -> [Connection] D:DS1\n");

        context.assertIsSatisfied();
    }

    private String render(EntityGraph<INode> graph)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter out = new PrintWriter(stringWriter);
        List<INode> nodes = graph.getNodes();
        Collections.sort(nodes, new SimpleComparator<INode, String>()
            {
                @Override
                public String evaluate(INode node)
                {
                    return node.getIdentifier().toString();
                }
            });
        for (INode node : nodes)
        {
            out.print(node.getIdentifier() + " ->");
            List<EdgeNodePair> connections = node.getConnections();
            for (EdgeNodePair connection : connections)
            {
                out.print(" [" + connection.getEdge().getType() + "] " + connection.getNode().getIdentifier());
            }
            out.println();
        }
        return stringWriter.toString();
    }

    @SuppressWarnings("unchecked")
    private void prepareSharedSamples(Builder<Sample>... sampleBuilders)
    {
        List<Sample> samples = new ArrayList<>();
        for (Builder<Sample> sampleBuilder : sampleBuilders)
        {
            samples.add(sampleBuilder.get());
        }
        context.checking(new Expectations()
            {
                {
                    one(v3api).searchSamples(with(SESSION_TOKEN), with(any(SampleSearchCriteria.class)), with(any(SampleFetchOptions.class)));
                    will(returnValue(new SearchResult<>(samples, samples.size())));
                    inSequence(sequence);
                }
            });
    }

    @SuppressWarnings("unchecked")
    private void prepareSpaceSamples(Builder<Sample>... sampleBuilders)
    {
        List<Sample> samples = new ArrayList<>();
        for (Builder<Sample> sampleBuilder : sampleBuilders)
        {
            samples.add(sampleBuilder.get());
        }
        context.checking(new Expectations()
            {
                {
                    one(v3api).searchSamples(with(SESSION_TOKEN), with(any(SampleSearchCriteria.class)), with(any(SampleFetchOptions.class)));
                    will(returnValue(new SearchResult<>(samples, samples.size())));
                    inSequence(sequence);
                }
            });
    }

    @SuppressWarnings("unchecked")
    private void prepareProjects(Builder<Project>... projectBuilders)
    {
        List<Project> projects = new ArrayList<>();
        for (Builder<Project> projectBuilder : projectBuilders)
        {
            projects.add(projectBuilder.get());
        }
        context.checking(new Expectations()
            {
                {
                    one(v3api).searchProjects(with(SESSION_TOKEN), with(any(ProjectSearchCriteria.class)), with(any(ProjectFetchOptions.class)));
                    will(returnValue(new SearchResult<>(projects, projects.size())));
                }
            });

    }

    private Builder<Project> p(String projectIdentifier)
    {
        return new ProjectBuilder(projectIdentifier);
    }

    private Builder<Experiment> e(String experimentIdentifier)
    {
        return new ExperimentBuilder(experimentIdentifier);
    }

    private Builder<Sample> s(String sampleIdentifier)
    {
        return new SampleBuilder(sampleIdentifier);
    }

    private DataSetBuilder d(String dataSetCode)
    {
        return new DataSetBuilder(dataSetCode);
    }

    private static abstract class Builder<T>
    {
        protected String identifier;

        protected Builder<?>[] builders = new Builder<?>[0];

        Builder(String identifier)
        {
            this.identifier = identifier;

        }

        Builder<T> with(Builder<?>... builders)
        {
            this.builders = builders;
            return this;
        }

        abstract T get();
    }

    private static final class ProjectBuilder extends Builder<Project>
    {
        ProjectBuilder(String identifier)
        {
            super(identifier);
        }

        @Override
        public Project get()
        {
            Project project = new Project();
            project.setIdentifier(new ProjectIdentifier(identifier));
            ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
            fetchOptions.withExperiments();
            fetchOptions.withSamples();
            project.setFetchOptions(fetchOptions);
            List<Experiment> experiments = new ArrayList<>();
            List<Sample> samples = new ArrayList<>();
            for (Builder<?> builder : builders)
            {
                Object object = builder.get();
                if (object instanceof Experiment)
                {
                    Experiment experiment = (Experiment) object;
                    experiment.setProject(project);
                    experiments.add(experiment);
                }
                if (object instanceof Sample)
                {
                    Sample sample = (Sample) object;
                    sample.setProject(project);
                    samples.add(sample);
                }
            }
            project.setExperiments(experiments);
            project.setSamples(samples);
            return project;
        }
    }

    private static final class ExperimentBuilder extends Builder<Experiment>
    {
        ExperimentBuilder(String identifier)
        {
            super(identifier);
        }

        @Override
        public Experiment get()
        {
            Experiment experiment = new Experiment();
            experiment.setIdentifier(new ExperimentIdentifier(identifier));
            ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
            fetchOptions.withSamples();
            fetchOptions.withDataSets();
            experiment.setFetchOptions(fetchOptions);
            List<Sample> samples = new ArrayList<>();
            List<DataSet> dataSets = new ArrayList<>();
            for (Builder<?> builder : builders)
            {
                Object object = builder.get();
                if (object instanceof Sample)
                {
                    Sample sample = (Sample) object;
                    sample.setExperiment(experiment);
                    samples.add(sample);
                }
                if (object instanceof DataSet)
                {
                    DataSet dataSet = (DataSet) object;
                    dataSet.setExperiment(experiment);
                    dataSets.add(dataSet);
                }
            }
            experiment.setSamples(samples);
            experiment.setDataSets(dataSets);
            return experiment;
        }
    }

    private static final class SampleBuilder extends Builder<Sample>
    {
        SampleBuilder(String identifier)
        {
            super(identifier);
        }

        @Override
        Sample get()
        {
            Sample sample = new Sample();
            sample.setIdentifier(new SampleIdentifier(identifier));
            SampleFetchOptions fetchOptions = new SampleFetchOptions();
            fetchOptions.withChildren();
            fetchOptions.withParents();
            fetchOptions.withExperiment();
            fetchOptions.withProject();
            fetchOptions.withSpace();
            fetchOptions.withComponents();
            fetchOptions.withContainer();
            fetchOptions.withDataSets();
            sample.setFetchOptions(fetchOptions);
            List<Sample> children = new ArrayList<>();
            List<Sample> components = new ArrayList<>();
            List<DataSet> dataSets = new ArrayList<>();
            for (Builder<?> builder : builders)
            {
                Object object = builder.get();
                if (object instanceof Sample)
                {
                    Sample sample2 = (Sample) object;
                    if (sample2.getIdentifier().getIdentifier().contains(":"))
                    {
                        components.add(sample2);
                        sample2.setContainer(sample);
                    } else
                    {
                        children.add(sample2);
                        List<Sample> parents = sample2.getParents();
                        if (parents == null)
                        {
                            parents = new ArrayList<>();
                            sample2.setParents(parents);
                        }
                        parents.add(sample);
                    }
                }
                if (object instanceof DataSet)
                {
                    DataSet dataSet = (DataSet) object;
                    dataSet.setSample(sample);
                    dataSets.add(dataSet);
                }
            }
            sample.setChildren(children);
            sample.setComponents(components);
            sample.setDataSets(dataSets);
            return sample;
        }
    }

    private static final class DataSetBuilder extends Builder<DataSet>
    {
        DataSetBuilder(String identifier)
        {
            super(identifier);
        }

        @Override
        DataSet get()
        {
            DataSet dataSet = new DataSet();
            dataSet.setCode(identifier);
            dataSet.setPermId(new DataSetPermId(identifier));
            DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
            fetchOptions.withChildren();
            fetchOptions.withParents();
            fetchOptions.withComponents();
            fetchOptions.withContainers();
            dataSet.setFetchOptions(fetchOptions);
            List<DataSet> children = new ArrayList<>();
            List<DataSet> components = new ArrayList<>();
            for (Builder<?> builder : builders)
            {
                Object object = builder.get();
                if (object instanceof DataSet)
                {
                    DataSet dataSet2 = (DataSet) object;
                    if (dataSet2.getCode().contains(":"))
                    {
                        components.add(dataSet2);
                        List<DataSet> containers = dataSet2.getContainers();
                        if (containers == null)
                        {
                            containers = new ArrayList<>();
                            dataSet2.setContainers(containers);
                        }
                        containers.add(dataSet);
                    } else
                    {
                        children.add(dataSet2);
                        List<DataSet> parents = dataSet2.getParents();
                        if (parents == null)
                        {
                            parents = new ArrayList<>();
                            dataSet2.setParents(parents);
                        }
                        parents.add(dataSet);
                    }
                }
            }
            dataSet.setChildren(children);
            dataSet.setComponents(components);
            return dataSet;
        }

    }
}
