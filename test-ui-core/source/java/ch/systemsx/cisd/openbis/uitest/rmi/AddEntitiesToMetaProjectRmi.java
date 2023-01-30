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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.MaterialCodeAndTypeCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Console;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.Entity;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.Material;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class AddEntitiesToMetaProjectRmi implements Command<Void>
{

    @Inject
    private User user;

    @Inject
    private String session;

    @Inject
    private IGeneralInformationChangingService openbis;

    @Inject
    private Console console;

    private MetaProject metaProject;

    private Collection<Entity> entities;

    public AddEntitiesToMetaProjectRmi(MetaProject metaProject, Entity first, Entity... rest)
    {
        this.metaProject = metaProject;
        this.entities = new HashSet<Entity>();
        this.entities.add(first);
        this.entities.addAll(Arrays.asList(rest));
    }

    public AddEntitiesToMetaProjectRmi(MetaProject metaProject, Collection<Entity> entities)
    {
        this.metaProject = metaProject;
        this.entities = entities;
    }

    @Override
    public Void execute()
    {
        MetaprojectAssignmentsIds ids = new MetaprojectAssignmentsIds();

        for (Entity entity : entities)
        {
            if (entity instanceof Sample)
            {
                ids.addSample(new SampleIdentifierId(Identifiers.get((Sample) entity).toString()));
            } else if (entity instanceof Experiment)
            {
                ids.addExperiment(new ExperimentIdentifierId(Identifiers.get((Experiment) entity)
                        .toString()));
            } else if (entity instanceof DataSet)
            {
                ids.addDataSet(new DataSetCodeId(((DataSet) entity).getCode()));
            } else if (entity instanceof Material)
            {
                Material material = (Material) entity;
                ids.addMaterial(new MaterialCodeAndTypeCodeId(material.getCode(), material
                        .getType().getCode()));
            } else
            {
                throw new IllegalArgumentException(entity.getClass().getCanonicalName());
            }
        }

        console.startBuffering();
        openbis.addToMetaproject(session,
                new MetaprojectIdentifierId("/" + user.getName() + "/" + metaProject.getName()),
                ids);
        return null;
    }
}
