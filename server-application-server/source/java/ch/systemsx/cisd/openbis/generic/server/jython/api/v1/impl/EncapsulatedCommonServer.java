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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IDataSetTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExperimentTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IFileFormatTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMaterialTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyAssignmentImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.ISampleTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IScriptImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyImmutable;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Kaloyan Enimanev
 */
public class EncapsulatedCommonServer
{
    private static final String SERVICE_PATH = "/rmi-common";

    private final ICommonServer commonServer;

    private final String sessionToken;

    public static EncapsulatedCommonServer create(String openBisUrl, String userID, String password)
    {
        SslCertificateHelper.trustAnyCertificate(openBisUrl);
        ICommonServer commonService =
                HttpInvokerUtils.createServiceStub(ICommonServer.class, openBisUrl + SERVICE_PATH,
                        5 * DateUtils.MILLIS_PER_MINUTE);

        SessionContextDTO session = commonService.tryAuthenticate(userID, password);
        if (session == null)
        {
            throw UserFailureException.fromTemplate("Invalid username/password combination");
        }
        return new EncapsulatedCommonServer(commonService, session.getSessionToken());
    }

    public static EncapsulatedCommonServer create(ICommonServer commonServer, String sessionToken)
    {
        if (StringUtils.isBlank(sessionToken))
        {
            throw UserFailureException.fromTemplate("The specified session token cannot be empty.");
        }
        return new EncapsulatedCommonServer(commonServer, sessionToken);
    }

    private EncapsulatedCommonServer(ICommonServer commonServer, String sessionToken)
    {
        this.commonServer = commonServer;
        this.sessionToken = sessionToken;
    }

    public List<IExperimentTypeImmutable> listExperimentTypes()
    {
        List<IExperimentTypeImmutable> result = new ArrayList<IExperimentTypeImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType type : commonServer
                .listExperimentTypes(sessionToken))
        {
            result.add(new ExperimentTypeImmutable(type));
        }
        return result;
    }

    public List<ISampleTypeImmutable> listSampleTypes()
    {
        List<ISampleTypeImmutable> result = new ArrayList<ISampleTypeImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType type : commonServer
                .listSampleTypes(sessionToken))
        {
            result.add(new SampleTypeImmutable(type));
        }
        return result;
    }

    public List<IDataSetTypeImmutable> listDataSetTypes()
    {
        List<IDataSetTypeImmutable> result = new ArrayList<IDataSetTypeImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType type : commonServer
                .listDataSetTypes(sessionToken))
        {
            result.add(new DataSetTypeImmutable(type));
        }
        return result;
    }

    public List<IMaterialTypeImmutable> listMaterialTypes()
    {
        List<IMaterialTypeImmutable> result = new ArrayList<IMaterialTypeImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType type : commonServer
                .listMaterialTypes(sessionToken))
        {
            result.add(new MaterialTypeImmutable(type));
        }
        return result;
    }

    public List<IPropertyTypeImmutable> listPropertyTypes()
    {
        List<IPropertyTypeImmutable> result = new ArrayList<IPropertyTypeImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType type : commonServer
                .listPropertyTypes(sessionToken, false))
        {
            result.add(new PropertyTypeImmutable(type));
        }
        return result;
    }

    public List<IFileFormatTypeImmutable> listFileFormatTypes()
    {
        List<IFileFormatTypeImmutable> result = new ArrayList<IFileFormatTypeImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType type : commonServer
                .listFileFormatTypes(sessionToken))
        {
            result.add(new FileFormatTypeImmutable(type));
        }
        return result;
    }

    public List<IScriptImmutable> listScripts()
    {
        List<IScriptImmutable> result = new ArrayList<IScriptImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script script : commonServer
                .listScripts(sessionToken, null, null))
        {
            result.add(new ScriptImmutable(script));
        }
        return result;
    }

    public void registerExperimentType(ExperimentTypeImmutable experimentType)
    {
        commonServer.registerExperimentType(sessionToken, experimentType.getExperimentType());
    }

    public void registerSampleType(SampleType sampleType)
    {
        commonServer.registerSampleType(sessionToken, sampleType.getSampleType());
    }

    public void registerDataSetType(DataSetType dataSetType)
    {
        commonServer.registerDataSetType(sessionToken, dataSetType.getDataSetType());
    }

    public void registerScript(Script script)
    {
        commonServer.registerScript(sessionToken, script.script);
    }

    public void registerMaterialType(MaterialTypeImmutable materialType)
    {
        commonServer.registerMaterialType(sessionToken, materialType.getMaterialType());
    }

    public void registerPropertyType(PropertyTypeImmutable propertyType)
    {
        commonServer.registerPropertyType(sessionToken, propertyType.getPropertyType());
    }

    public void registerPropertyAssignment(PropertyAssignment assignment)
    {
        commonServer.assignPropertyType(sessionToken, assignment.getAssignment());
    }

    public void registerFileFormatType(FileFormatTypeImmutable fileFormatType)
    {
        commonServer.registerFileFormatType(sessionToken, fileFormatType.getFileFormatType());
    }

    public void logout()
    {
        commonServer.logout(sessionToken);
    }

    public void registerVocabulary(VocabularyImmutable vocabulary)
    {
        NewVocabulary newVocabulary = asNewVocabulary(vocabulary.getVocabulary());
        commonServer.registerVocabulary(sessionToken, newVocabulary);
    }

    public void addVocabularyTerms(Long vocabularyId, List<VocabularyTerm> terms,
            Long previousTermOrdinal)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> list =
                new ArrayList<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm>();
        for (VocabularyTerm vocabularyTerm : terms)
        {
            list.add(vocabularyTerm.getVocabularyTerm());
        }
        commonServer.addVocabularyTerms(sessionToken, new TechId(vocabularyId), list,
                previousTermOrdinal);
    }

    public void update(VocabularyTermImmutable term)
    {
        commonServer.updateVocabularyTerm(sessionToken, term.getVocabularyTerm());
    }

    public List<IPropertyAssignmentImmutable> listPropertyAssignments()
    {
        ArrayList<IPropertyAssignmentImmutable> assignments =
                new ArrayList<IPropertyAssignmentImmutable>();
        for (EntityTypePropertyType<?> etpt : commonServer
                .listEntityTypePropertyTypes(sessionToken))
        {
            PropertyAssignmentImmutable assignment = new PropertyAssignmentImmutable(etpt);
            assignments.add(assignment);
        }
        return assignments;
    }

    public List<IVocabularyImmutable> listVocabularies()
    {
        ArrayList<IVocabularyImmutable> vocabularies = new ArrayList<IVocabularyImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary vocabulary : commonServer
                .listVocabularies(sessionToken, true, false))
        {
            vocabularies.add(new VocabularyImmutable(vocabulary));
        }
        return vocabularies;
    }

    private NewVocabulary asNewVocabulary(Vocabulary vocabulary)
    {
        NewVocabulary result = new NewVocabulary();
        result.setId(vocabulary.getId());
        result.setCode(vocabulary.getCode());
        result.setDescription(vocabulary.getDescription());
        result.setChosenFromList(vocabulary.isChosenFromList());
        result.setManagedInternally(vocabulary.isManagedInternally());
        result.setURLTemplate(vocabulary.getURLTemplate());
        result.setRegistrationDate(vocabulary.getRegistrationDate());
        result.setRegistrator(vocabulary.getRegistrator());
        result.setTerms(vocabulary.getTerms());
        return result;
    }

    public List<IExternalDataManagementSystemImmutable> listExternalDataManagementSystems()
    {
        List<IExternalDataManagementSystemImmutable> result =
                new ArrayList<IExternalDataManagementSystemImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem edms : commonServer
                .listExternalDataManagementSystems(sessionToken))
        {
            result.add(new ExternalDataManagementSystemImmutable(edms));
        }
        return result;
    }

    public void createOrUpdateExternalDataManagementSystem(ExternalDataManagementSystem edms)
    {
        commonServer.createOrUpdateExternalDataManagementSystem(sessionToken,
                edms.getExternalDataManagementSystem());
    }

    public IExternalDataManagementSystemImmutable getExternalDataManagementSystem(String code)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem edms =
                commonServer.getExternalDataManagementSystem(sessionToken, code);
        if (edms != null)
        {
            return new ExternalDataManagementSystemImmutable(edms);
        }

        return null;
    }

}
