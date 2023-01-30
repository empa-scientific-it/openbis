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

package ch.systemsx.cisd.openbis.util;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.MaterialCodeAndTypeCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.MaterialTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SamplePermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;

/**
 * @author pkupczyk
 */
public class GeneralInformationServiceUtil
{

    private IGeneralInformationService service;

    private IGeneralInformationChangingService changingService;

    public GeneralInformationServiceUtil(IGeneralInformationService service,
            IGeneralInformationChangingService changingService)
    {
        this.service = service;
        this.changingService = changingService;
    }

    public List<String> listMetaprojectNames(String sessionToken)
    {
        List<Metaproject> metaprojects = service.listMetaprojects(sessionToken);
        List<String> names = new ArrayList<String>();

        for (Metaproject metaproject : metaprojects)
        {
            names.add(metaproject.getName());
        }

        return names;
    }

    public Metaproject createMetaprojectWithAssignments(String sessionToken)
    {
        Metaproject metaproject =
                changingService.createMetaproject(sessionToken, "BRAND_NEW_METAPROJECT", null);

        MetaprojectAssignmentsIds assignmentsToAdd = new MetaprojectAssignmentsIds();

        assignmentsToAdd.addExperiment(new ExperimentIdentifierId("/CISD/NEMO/EXP1")); // id: 2
        assignmentsToAdd.addExperiment(new ExperimentPermIdId("201108050937246-1031")); // id: 22
        assignmentsToAdd.addExperiment(new ExperimentTechIdId(23L)); // id: 23

        assignmentsToAdd.addSample(new SampleIdentifierId("/A03")); // id: 647
        assignmentsToAdd.addSample(new SampleIdentifierId("/CISD/N19")); // id: 602
        assignmentsToAdd.addSample(new SamplePermIdId("200811050917877-346")); // id: 340
        assignmentsToAdd.addSample(new SampleTechIdId(342L)); // id: 342

        assignmentsToAdd.addDataSet(new DataSetCodeId("20081105092259000-8")); // id: 8
        assignmentsToAdd.addDataSet(new DataSetTechIdId(12L)); // id: 12

        assignmentsToAdd.addMaterial(new MaterialCodeAndTypeCodeId("GFP", "CONTROL")); // id: 18
        assignmentsToAdd.addMaterial(new MaterialTechIdId(8L)); // id: 8

        changingService.addToMetaproject(sessionToken,
                new MetaprojectIdentifierId(metaproject.getIdentifier()), assignmentsToAdd);

        return metaproject;
    }

    public List<Long> getObjectIds(List<? extends IIdHolder> objects)
    {
        List<Long> ids = new ArrayList<Long>();

        for (IIdHolder object : objects)
        {
            ids.add(object.getId());
        }

        return ids;
    }

}
