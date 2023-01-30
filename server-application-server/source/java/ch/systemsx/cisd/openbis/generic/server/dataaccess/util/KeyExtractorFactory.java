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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.util;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * A <code>IdKeyExtractor</code> factory.
 * 
 * @author Christian Ribeaud
 */
public final class KeyExtractorFactory
{
    private final static IKeyExtractor<Long, ExperimentPE> BASE_EXPERIMENT_BY_ID_KEY_EXTRACTOR =
            createIdKeyExtractor();

    private final static IKeyExtractor<String, VocabularyTermPE> VOCABULARY_TERM_BY_CODE_KEY_EXTRACTOR =
            createCodeKeyExtractor();

    private final static IKeyExtractor<String, PropertyTypePE> PROPERTY_TYPE_BY_CODE_KEY_EXTRACTOR =
            createCodeKeyExtractor();

    private final static IKeyExtractor<Long, SamplePE> SAMPLE_BY_ID_KEY_EXTRACTOR =
            createIdKeyExtractor();

    private static final IKeyExtractor<String, PersonPE> PERSON_BY_USER_ID_KEY_EXTRACTOR =
            new PersonByUserIdKeyExtractor();

    private static final IKeyExtractor<String, AuthorizationGroupPE> AUTHORIZATION_GROUP_BY_CODE_KEY_EXTRACTOR =
            new AuthorizationGroupByCodeKeyExtractor();

    private static final IKeyExtractor<String, MaterialPE> MATERIAL_BY_CODE_KEY_EXTRACTOR =
            createCodeKeyExtractor();

    private static final IKeyExtractor<String, EntityTypePE> ENTITY_TYPE_BY_CODE_KEY_EXTRACTOR =
            createCodeKeyExtractor();

    private KeyExtractorFactory()
    {
        // Can not be instantiated.
    }

    /** Creates an <code>IKeyExtractor</code> implementation based on {@link IIdHolder}. */
    public final static <T extends IIdHolder> IKeyExtractor<Long, T> createIdKeyExtractor()
    {
        return new IdKeyExtractor<T>();
    }

    /**
     * Creates an <code>IKeyExtractor</code> implementation based on {@link ICodeHolder} extension.
     */
    public final static <T extends ICodeHolder> IKeyExtractor<String, T> createCodeKeyExtractor()
    {
        return new CodeKeyExtractor<T>();
    }

    /**
     * Returns an <code>IKeyExtractor</code> for <i>BaseExperimentDTO</i> based on <code>Id</code>.
     */
    public final static IKeyExtractor<Long, ExperimentPE> getBaseExperimentByIdKeyExtractor()
    {
        return BASE_EXPERIMENT_BY_ID_KEY_EXTRACTOR;
    }

    /**
     * Returns an <code>IKeyExtractor</code> for <i>VocabularyTermDTO</i> based on <code>code</code> .
     */
    public final static IKeyExtractor<String, VocabularyTermPE> getVocabularyTermByCodeKeyExtractor()
    {
        return VOCABULARY_TERM_BY_CODE_KEY_EXTRACTOR;
    }

    /**
     * Returns an <code>IKeyExtractor</code> for <i>PropertyTypePE</i> based on {@link PropertyTypePE#getCode()}.
     */
    public final static IKeyExtractor<String, PropertyTypePE> getPropertyTypeByCodeKeyExtractor()
    {
        return PROPERTY_TYPE_BY_CODE_KEY_EXTRACTOR;
    }

    /** Returns an <code>IKeyExtractor</code> for <i>SampleDTO</i> based on <code>Id</code>. */
    public final static IKeyExtractor<Long, SamplePE> getSampleByIdKeyExtractor()
    {
        return SAMPLE_BY_ID_KEY_EXTRACTOR;
    }

    /**
     * Returns an <code>IKeyExtractor</code> for <i>PersonPE</i> based on {@link PersonPE#getUserId()}.
     */
    public final static IKeyExtractor<String, PersonPE> getPersonByUserIdKeyExtractor()
    {
        return PERSON_BY_USER_ID_KEY_EXTRACTOR;
    }

    /**
     * Returns an <code>IKeyExtractor</code> for authorization group based on {@link AuthorizationGroupPE#getCode()}.
     */
    public final static IKeyExtractor<String, AuthorizationGroupPE> getAuthorizationGroupByCodeKeyExtractor()
    {
        return AUTHORIZATION_GROUP_BY_CODE_KEY_EXTRACTOR;
    }

    /**
     * Returns an <code>IKeyExtractor</code> for <i>EntityTypePE</i> based on <code>code</code>.
     */
    public final static IKeyExtractor<String, EntityTypePE> getEntityTypeByCodeKeyExtractor()
    {
        return ENTITY_TYPE_BY_CODE_KEY_EXTRACTOR;
    }

    /**
     * Returns an <code>IKeyExtractor</code> for <i>MaterialPE</i> based on <code>code</code>.
     */
    public final static IKeyExtractor<String, MaterialPE> getMaterialByCodeKeyExtractor()
    {
        return MATERIAL_BY_CODE_KEY_EXTRACTOR;
    }

    //
    // Helper classes
    //

    private final static class IdKeyExtractor<T extends IIdHolder> implements
            IKeyExtractor<Long, T>
    {

        //
        // IKeyExtractor
        //

        @Override
        public final Long getKey(final IIdHolder id)
        {
            return id.getId();
        }
    }

    private final static class CodeKeyExtractor<T extends ICodeHolder> implements
            IKeyExtractor<String, T>
    {
        //
        // IKeyExtractor
        //

        @Override
        public final String getKey(final ICodeHolder id)
        {
            return id.getCode();
        }
    }

    private final static class PersonByUserIdKeyExtractor implements
            IKeyExtractor<String, PersonPE>
    {

        //
        // IKeyExtractor
        //

        @Override
        public final String getKey(final PersonPE e)
        {
            return e.getUserId();
        }
    }

    private final static class AuthorizationGroupByCodeKeyExtractor implements
            IKeyExtractor<String, AuthorizationGroupPE>
    {

        //
        // IKeyExtractor
        //

        @Override
        public final String getKey(final AuthorizationGroupPE e)
        {
            return e.getCode();
        }
    }
}
