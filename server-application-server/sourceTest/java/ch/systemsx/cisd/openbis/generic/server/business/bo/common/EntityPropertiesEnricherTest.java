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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;

/**
 * @author Franz-Josef Elmer
 */
public class EntityPropertiesEnricherTest extends AssertJUnit
{
    private static final long VOCABULARY_TERM_ID = 42L;

    private static final long VOCABULARY_ID = 1L;

    private static final long ENTITY_ID = 42L;

    private static final long PROPERTY_TYPE_ID = 4711L;

    private Mockery context;

    private IPropertyListingQuery query;

    private IEntityPropertySetListingQuery setQuery;

    private IEntityPropertiesHolderResolver resolver;

    private EntityPropertiesEnricher enricher;

    private IEntityPropertiesHolder holder;

    private List<IEntityProperty> properties;

    @BeforeMethod
    public void setUpMocks()
    {
        context = new Mockery();
        query = context.mock(IPropertyListingQuery.class);
        setQuery = context.mock(IEntityPropertySetListingQuery.class);
        resolver = context.mock(IEntityPropertiesHolderResolver.class);
        holder = context.mock(IEntityPropertiesHolder.class);
        enricher = new EntityPropertiesEnricher(query, setQuery);
        properties = new ArrayList<IEntityProperty>();
        context.checking(new Expectations()
            {
                {
                    allowing(query).getPropertyTypes();
                    PropertyType propertyType = new PropertyType();
                    propertyType.setId(PROPERTY_TYPE_ID);
                    propertyType.setDataType(new DataType(DataTypeCode.CONTROLLEDVOCABULARY));
                    will(returnValue(new PropertyType[] { propertyType }));

                    allowing(resolver).get(ENTITY_ID);
                    will(returnValue(holder));

                    allowing(holder).getProperties();
                    will(returnValue(properties));
                }
            });
    }

    @AfterMethod
    public void checkMocks()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testURLTemplateForVocabularyTerms()
    {
        final LongSet entityIDs = LongSets.singleton(ENTITY_ID);
        context.checking(new Expectations()
            {
                {
                    one(setQuery).getEntityPropertyGenericValues(entityIDs);
                    GenericEntityPropertyRecord record = new GenericEntityPropertyRecord();
                    record.entity_id = ENTITY_ID;
                    record.cvte_id = VOCABULARY_TERM_ID;
                    record.prty_id = PROPERTY_TYPE_ID;
                    record.entity_id = ENTITY_ID;
                    will(returnValue(Arrays.asList(record)));

                    one(query).getVocabularyURLTemplates();
                    CodeRecord codeRecord = new CodeRecord();
                    codeRecord.id = VOCABULARY_ID;
                    codeRecord.code = "http://my.url.org/?q=${term}";
                    will(returnValue(new CodeRecord[]
                    { codeRecord }));

                    one(query).getVocabularyTerms(new LongOpenHashSet(Arrays.asList(record.cvte_id)));
                    VocabularyTermRecord termRecord = new VocabularyTermRecord();
                    termRecord.id = VOCABULARY_TERM_ID;
                    termRecord.covo_id = VOCABULARY_ID;
                    termRecord.code = "HELLO";
                    termRecord.label = "Hello";
                    termRecord.description = "greeting";
                    termRecord.ordinal = 1234L;
                    will(returnValue(new WrappingDataIterator<>(Arrays.asList(termRecord))));
                }
            });

        enricher.enrich(entityIDs, resolver);

        VocabularyTerm vocabularyTerm = properties.get(0).getVocabularyTerm();
        assertEquals("HELLO", vocabularyTerm.getCode());
        assertEquals("Hello", vocabularyTerm.getLabel());
        assertEquals("greeting", vocabularyTerm.getDescription());
        assertEquals(Long.valueOf(1234L), vocabularyTerm.getOrdinal());
        assertEquals("http://my.url.org/?q=HELLO", vocabularyTerm.getUrl());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeprecatedURLTemplateForVocabularyTerms()
    {
        final LongSet entityIDs = LongSets.singleton(ENTITY_ID);
        context.checking(new Expectations()
            {
                {
                    one(setQuery).getEntityPropertyGenericValues(entityIDs);
                    GenericEntityPropertyRecord record = new GenericEntityPropertyRecord();
                    record.entity_id = ENTITY_ID;
                    record.cvte_id = VOCABULARY_TERM_ID;
                    record.prty_id = PROPERTY_TYPE_ID;
                    record.entity_id = ENTITY_ID;
                    will(returnValue(Arrays.asList(record)));

                    one(query).getVocabularyURLTemplates();
                    CodeRecord codeRecord = new CodeRecord();
                    codeRecord.id = VOCABULARY_ID;
                    codeRecord.code = "http://my.url.org/?q=$term$";
                    will(returnValue(new CodeRecord[]
                    { codeRecord }));

                    one(query).getVocabularyTerms(new LongOpenHashSet(Arrays.asList(record.cvte_id)));
                    VocabularyTermRecord termRecord = new VocabularyTermRecord();
                    termRecord.id = VOCABULARY_TERM_ID;
                    termRecord.covo_id = VOCABULARY_ID;
                    termRecord.code = "HELLO";
                    termRecord.label = "Hello";
                    will(returnValue(new WrappingDataIterator<>(Arrays.asList(termRecord))));
                }
            });

        enricher.enrich(entityIDs, resolver);

        assertEquals("http://my.url.org/?q=HELLO", properties.get(0).getVocabularyTerm().getUrl());
        context.assertIsSatisfied();
    }
}
