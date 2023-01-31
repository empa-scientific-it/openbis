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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.ITableModelTransformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Decorator of a {@link TableModel} instance by turning cell of some columns into {@link EntityTableCell}.
 * 
 * @author Franz-Josef Elmer
 */
public class EntityLinksDecorator implements ITableModelTransformation
{
    private interface IEntityTableCellTranslation
    {
        ISerializableComparable translation(ISerializableComparable value);

        EntityKind getEntityKind();
    }

    private enum EntityTableCellTranslationFactory
    {
        MATERIAL()
        {
            private static final String MATERIAL_TYPE_KEY = "material-type";

            @Override
            void assertValidProperties(Properties properties)
            {
                getMaterialType(properties);
            }

            @Override
            IEntityTableCellTranslation create(Properties properties)
            {
                final String materialType = getMaterialType(properties);
                return new IEntityTableCellTranslation()
                    {
                        @Override
                        public ISerializableComparable translation(ISerializableComparable value)
                        {
                            String materialCode = value.toString();
                            EntityTableCell entityTableCell =
                                    new EntityTableCell(EntityKind.MATERIAL,
                                            MaterialIdentifier.print(materialCode, materialType));
                            entityTableCell.setLinkText(materialCode);
                            return entityTableCell;
                        }

                        @Override
                        public EntityKind getEntityKind()
                        {
                            return EntityKind.MATERIAL;
                        }
                    };
            }

            String getMaterialType(Properties properties)
            {
                return PropertyUtils.getMandatoryProperty(properties, MATERIAL_TYPE_KEY).trim();
            }
        },
        SAMPLE()
        {
            private static final String DEFAULT_SPACE_KEY = "default-space";

            @Override
            IEntityTableCellTranslation create(Properties properties)
            {
                String property = properties.getProperty(DEFAULT_SPACE_KEY);
                final String defaultSpace;
                if (property == null)
                {
                    defaultSpace = null;
                } else
                {
                    defaultSpace = "/" + property.trim();
                }
                return new IEntityTableCellTranslation()
                    {

                        @Override
                        public ISerializableComparable translation(ISerializableComparable value)
                        {
                            String identifier =
                                    SampleIdentifierFactory.parse(value.toString(), defaultSpace)
                                            .toString();
                            EntityTableCell entityTableCell =
                                    new EntityTableCell(EntityKind.SAMPLE, "", identifier);
                            entityTableCell.setLinkText(value.toString());
                            return entityTableCell;
                        }

                        @Override
                        public EntityKind getEntityKind()
                        {
                            return EntityKind.SAMPLE;
                        }
                    };
            }
        };

        void assertValidProperties(Properties properties)
        {
        }

        abstract IEntityTableCellTranslation create(Properties properties);
    }

    private static final String LINK_COLUMNS_KEY = "link-columns";

    private static final String ENTITY_KIND_KEY = "entity-kind";

    private final Map<String, IEntityTableCellTranslation> translations =
            new HashMap<String, IEntityTableCellTranslation>();

    public EntityLinksDecorator(Properties properties)
    {
        SectionProperties[] sections =
                PropertyParametersUtil
                        .extractSectionProperties(properties, LINK_COLUMNS_KEY, false);
        for (SectionProperties section : sections)
        {
            Properties sectionProperties = section.getProperties();
            EntityTableCellTranslationFactory factory =
                    getEntityTableCellTranslationFactory(sectionProperties);
            translations.put(section.getKey(), factory.create(sectionProperties));
        }

    }

    private EntityTableCellTranslationFactory getEntityTableCellTranslationFactory(
            Properties sectionProperties)
    {
        String ek = PropertyUtils.getMandatoryProperty(sectionProperties, ENTITY_KIND_KEY);
        EntityTableCellTranslationFactory factory;
        try
        {
            factory = EntityTableCellTranslationFactory.valueOf(ek);
            factory.assertValidProperties(sectionProperties);
        } catch (IllegalArgumentException e)
        {
            throw new ConfigurationFailureException("Unknown entity kind: " + ek);
        }
        return factory;
    }

    @Override
    public TableModel transform(TableModel tableModel)
    {
        class TranslationAndIndex
        {
            int index;

            IEntityTableCellTranslation translation;

            TranslationAndIndex(int index, IEntityTableCellTranslation translation)
            {
                this.index = index;
                this.translation = translation;
            }
        }
        List<TranslationAndIndex> translationsAndIndices = new ArrayList<TranslationAndIndex>();
        List<TableModelColumnHeader> headers = tableModel.getHeader();
        for (int i = 0; i < headers.size(); i++)
        {
            TableModelColumnHeader header = headers.get(i);
            IEntityTableCellTranslation translation = translations.get(header.getId());
            if (translation != null)
            {
                translationsAndIndices.add(new TranslationAndIndex(i, translation));
                header.setEntityKind(translation.getEntityKind());
            }
        }
        List<TableModelRow> rows = tableModel.getRows();
        for (TableModelRow row : rows)
        {
            List<ISerializableComparable> values = row.getValues();
            for (TranslationAndIndex generatorAndIndex : translationsAndIndices)
            {
                int index = generatorAndIndex.index;
                ISerializableComparable value = values.get(index);
                values.set(index, generatorAndIndex.translation.translation(value));
            }
        }
        return tableModel;
    }

}
