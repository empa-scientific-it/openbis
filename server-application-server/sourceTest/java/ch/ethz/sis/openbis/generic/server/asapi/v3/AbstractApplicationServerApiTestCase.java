/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hibernate.SessionFactory;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.testng.annotations.BeforeMethod;

import ch.ethz.sis.openbis.generic.server.asapi.v3.ApplicationServerApi;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.test.RecordingMatcherRepository;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author felmer
 */
public abstract class AbstractApplicationServerApiTestCase extends AbstractServerTestCase
{
    protected static final String SPACE_CODE = "S";

    protected static final String UNKNOWN_ENTITY_TYPE = "UNKNOWN";

    protected static final String ENTITY_TYPE = "TEST";

    protected static final String PROPERTY_TYPE_CODE = "DESCRIPTION";

    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    private SessionFactory sessionFactory;

    private org.hibernate.Session currentHibernateSession;

    protected ApplicationServerApi server;

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;

    protected PersonPE person;

    @BeforeMethod
    public void setUpMocks()
    {
        managedPropertyEvaluatorFactory = context.mock(IManagedPropertyEvaluatorFactory.class);
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
        sessionFactory = context.mock(SessionFactory.class);
        currentHibernateSession = context.mock(org.hibernate.Session.class);
        person = new PersonPE();
        person.setUserId(session.getUserName());
        session.setPerson(person);
        server =
                new ApplicationServerApi(managedPropertyEvaluatorFactory,
                        sessionManager, daoFactory, propertiesBatchManager,
                        sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getEntityTypeDAO(with(any(EntityKind.class)));
                    will(returnValue(entityTypeDAO));
                    allowing(daoFactory).getEntityPropertyTypeDAO(with(any(EntityKind.class)));
                    will(returnValue(entityPropertyTypeDAO));
                    allowing(permIdDAO).createPermId();
                    will(new Action()
                        {
                            private int id = 0;

                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                return "perm-" + ++id;
                            }

                            @Override
                            public void describeTo(Description description)
                            {
                            }
                        });

                    allowing(daoFactory).getSessionFactory();
                    will(returnValue(sessionFactory));

                    allowing(sessionFactory).getCurrentSession();
                    will(returnValue(currentHibernateSession));

                    allowing(currentHibernateSession).clear();
                    allowing(currentHibernateSession).flush();

                    RelationshipTypePE childParentRelationship = new RelationshipTypePE();
                    childParentRelationship.setId(1L);
                    allowing(relationshipTypeDAO).tryFindRelationshipTypeByCode(
                            BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
                    will(returnValue(childParentRelationship));
                }
            });
    }

    protected void prepareFindEntityTypeByCode(final EntityKind entityKind,
            final String entityTypeCode, final EntityTypePE entityType)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(entityTypeDAO).tryToFindEntityTypeByCode(entityTypeCode);
                    will(returnValue(entityType));
                }
            });
    }

    protected void prepareEntityTypes(final EntityTypePE... types)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(entityTypeDAO).listEntityTypes();
                    will(returnValue(Arrays.asList(types)));
                    for (EntityTypePE type : types)
                    {
                        allowing(entityTypeDAO).tryToFindEntityTypeByCode(type.getCode());
                        will(returnValue(type));
                        if (type instanceof ExperimentTypePE)
                        {
                            ExperimentTypePE experimentType = (ExperimentTypePE) type;
                            allowing(entityPropertyTypeDAO).listEntityPropertyTypes(type);
                            will(returnValue(new ArrayList<EntityTypePropertyTypePE>(experimentType
                                    .getExperimentTypePropertyTypes())));
                        }
                        if (type instanceof SampleTypePE)
                        {
                            SampleTypePE sampleType = (SampleTypePE) type;
                            allowing(entityPropertyTypeDAO).listEntityPropertyTypes(type);
                            will(returnValue(new ArrayList<EntityTypePropertyTypePE>(sampleType
                                    .getSampleTypePropertyTypes())));
                        }
                    }
                }
            });
    }

    protected void prepareFindSpace(final String spaceCode, final SpacePE space)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(spaceDAO).tryFindSpaceByCode(with(equal(spaceCode)));
                    will(returnValue(space));
                }
            });
    }

    protected void prepareFindProject(final String spaceCode, final String projectCode,
            final ProjectPE project)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(projectDAO).tryFindProject(spaceCode, projectCode);
                    will(returnValue(project));
                }
            });
    }

    protected void prepareFindProject(final String permId, final ProjectPE project)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(projectDAO).tryGetByPermID(permId);
                    will(returnValue(project));
                }
            });
    }

    protected void prepareFindPropertyTypeByCode(final String code,
            final PropertyTypePE propertyType)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(propertyTypeDAO).tryFindPropertyTypeByCode(code);
                    will(returnValue(propertyType));
                }
            });
    }

    protected void prepareListEntityPropertyTypesEmpty()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getEntityPropertyTypeDAO(with(any(EntityKind.class)));
                    will(returnValue(entityPropertyTypeDAO));
                    allowing(entityPropertyTypeDAO).listEntityPropertyTypes(with(any(EntityTypePE.class)));
                    will(returnValue(Collections.emptyList()));
                }
            });
    }

    protected void prepareHasNoDataSets()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataSetDAO).hasDataSet(with(any(SamplePE.class)));
                    will(returnValue(false));
                }
            });
    }

    protected void prepareFindTag(final String userId, final String tagCode, final MetaprojectPE tag)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(metaprojectDAO).tryFindByOwnerAndName(userId, tagCode);
                    will(returnValue(tag));
                }
            });
    }

    protected void prepareFindExperiment(final String experimentId, final ExperimentPE experiment)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(experimentDAO).listByPermID(Arrays.asList(experimentId));
                    if (experiment != null)
                    {
                        will(returnValue(Arrays.asList(experiment)));
                    } else
                    {
                        will(returnValue(Collections.emptyList()));
                    }
                }
            });
    }

    protected void prepareFindSample(final String sampleId, final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(sampleDAO).listByPermID(Arrays.asList(sampleId));
                    if (sample != null)
                    {
                        will(returnValue(Arrays.asList(sample)));
                    } else
                    {
                        will(returnValue(Collections.emptyList()));
                    }
                }
            });
    }

    protected void prepareFindSamples(final Collection<String> sampleIds, final SamplePE... samples)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(experimentDAO).listByPermID(sampleIds);
                    will(returnValue(samples));
                }
            });
    }

    protected void prepareCreateAttachment(RecordingMatcherRepository repository)
    {
        final RecordingMatcher<AttachmentPE> attachmentRecorder =
                repository.getRecordingMatcher(AttachmentPE.class);
        final RecordingMatcher<AttachmentHolderPE> attachmentHolderRecorder =
                repository.getRecordingMatcher(AttachmentHolderPE.class);
        context.checking(new Expectations()
            {
                {
                    allowing(attachmentDAO).createAttachment(with(attachmentRecorder),
                            with(attachmentHolderRecorder));
                }
            });
    }

    protected void prepareSetSampleParents(final Matcher<Long> sampleIdMatcher, final Matcher<List<Long>> parentIdsMatcher)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(sampleDAO).setSampleRelationshipParents(with(sampleIdMatcher), with(parentIdsMatcher), with(1L), with(person));
                }
            });
    }

    protected void prepareSetSampleChildren(final Matcher<Long> sampleIdMatcher, final Matcher<List<Long>> childIdsMatcher)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(sampleDAO).setSampleRelationshipChildren(with(sampleIdMatcher), with(childIdsMatcher), with(1L), with(person));
                }
            });
    }

    protected void prepareListSampleParents(final Matcher<List<TechId>> childTechIdsMatcher, final Set<TechId> parentTechIds)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(sampleDAO).listSampleIdsByChildrenIds(with(childTechIdsMatcher), with(new TechId(1L)));
                    will(returnValue(parentTechIds));
                }
            });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void prepareListSamplesByIDs(final Matcher<Collection> idsMatcher, final List<SamplePE> samples)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(sampleDAO).listByIDs(with(idsMatcher));
                    will(returnValue(samples));
                }
            });
    }

}