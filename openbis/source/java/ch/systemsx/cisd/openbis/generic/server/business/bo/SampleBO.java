/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.ParameterChecker;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleToRegisterDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The unique {@link ISampleBO} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class SampleBO extends AbstractSampleIdentifierBusinessObject implements ISampleBO
{
    private final IEntityPropertiesConverter propertiesConverter;

    private SamplePE sample;

    private boolean dataChanged;

    public SampleBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
        propertiesConverter = new EntityPropertiesConverter(EntityKind.SAMPLE, daoFactory);
        this.dataChanged = false;
    }

    private final SampleTypePE getSampleType(final String code) throws UserFailureException
    {
        final SampleTypePE sampleType = getSampleTypeDAO().tryFindSampleTypeByCode(code);
        if (sampleType == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample type with code '%s' could be found in the database.", code);
        }
        return sampleType;
    }

    private final void defineSampleProperties(final SampleProperty[] sampleProperties)
    {
        final String sampleTypeCode = sample.getSampleType().getCode();
        final List<SamplePropertyPE> properties =
                propertiesConverter.convertProperties(sampleProperties, sampleTypeCode, sample
                        .getRegistrator());
        for (final SamplePropertyPE sampleProperty : properties)
        {
            sample.addProperty(sampleProperty);
        }
    }

    //
    // ISampleBO
    //

    public final SamplePE getSample()
    {
        if (sample == null)
        {
            throw new IllegalStateException("Unloaded sample.");
        }
        return sample;
    }

    public final void loadBySampleIdentifier(final SampleIdentifier identifier)
    {
        sample = getSampleByIdentifier(identifier);
        if (sample == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found with given identifier '%s'.", identifier);
        }
    }

    public final void define(final SampleToRegisterDTO newSample)
    {
        final SampleIdentifier sampleIdentifier = newSample.getSampleIdentifier();
        final String sampleTypeCode = newSample.getSampleTypeCode();
        ParameterChecker.checkIfNotNull(sampleTypeCode, "sample type");

        final SampleOwner sampleOwner = getSampleOwnerFinder().figureSampleOwner(sampleIdentifier);

        sample = new SamplePE();
        sample.setCode(sampleIdentifier.getSampleCode());
        sample.setRegistrator(findRegistrator());
        sample.setSampleType(getSampleType(sampleTypeCode));
        sample.setGroup(sampleOwner.tryGetGroup());
        sample.setDatabaseInstance(sampleOwner.tryGetDatabaseInstance());
        defineSampleProperties(newSample.getProperties());
        final SampleIdentifier generatorParentSampleIdentifier = newSample.getParent();
        if (generatorParentSampleIdentifier != null)
        {
            final SamplePE generatedFrom = getSampleByIdentifier(generatorParentSampleIdentifier);
            if (generatedFrom != null)
            {
                if (generatedFrom.getInvalidation() != null)
                {
                    throw UserFailureException.fromTemplate(
                            "Cannot register sample '%s': generator parent '%s' is invalid.",
                            sampleIdentifier, generatorParentSampleIdentifier);
                }
                sample.setGeneratedFrom(generatedFrom);
                sample.setTop(generatedFrom.getTop() == null ? generatedFrom : generatedFrom
                        .getTop());
            }
        }
        final SampleIdentifier containerParentSampleIdentifier = newSample.getContainer();
        if (containerParentSampleIdentifier != null)
        {
            final SamplePE contained = getSampleByIdentifier(containerParentSampleIdentifier);
            if (contained != null)
            {
                if (contained.getInvalidation() != null)
                {
                    throw UserFailureException.fromTemplate(
                            "Cannot register sample '%s': container parent '%s' is invalid.",
                            sampleIdentifier, containerParentSampleIdentifier);
                }
                sample.setContainer(contained);
                sample.setTop(contained.getTop() == null ? contained : contained.getTop());
            }
        }
        SampleGenericBusinessRules.assertValidParents(sample);
        dataChanged = true;
    }

    public final void save()
    {
        assert sample != null : "Sample not loaded.";
        assert dataChanged : "Data have not been changed.";

        try
        {
            getSampleDAO().createSample(sample);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Sample '%s'", sample.getSampleIdentifier()));
        }
        dataChanged = false;
    }

}
