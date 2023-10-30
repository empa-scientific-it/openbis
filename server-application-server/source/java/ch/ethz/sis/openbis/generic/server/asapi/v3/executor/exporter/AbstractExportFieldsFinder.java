/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.Attribute;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.SelectedFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.xls.export.FieldType;

public abstract class AbstractExportFieldsFinder<ENTITY_TYPE extends IEntityType & IPermIdHolder> implements IExportFieldsFinder
{

    @Override
    public Map<String, List<Map<String, String>>> findExportFields(final Set<IPropertyTypeId> properties,
            final IApplicationServerInternalApi applicationServerApi, final String sessionToken, final SelectedFields selectedFields)
    {
        final SearchResult<ENTITY_TYPE> entityTypeSearchResult = findEntityTypes(properties, applicationServerApi, sessionToken);

        final List<ENTITY_TYPE> entityTypes = entityTypeSearchResult.getObjects();
        final Collector<ENTITY_TYPE, ?, Map<String, List<Map<String, String>>>> entityTypeToMapCollector =
                getEntityTypeMapCollector(selectedFields, entityTypes);
        return entityTypes.stream().collect(entityTypeToMapCollector);
    }

    private Collector<ENTITY_TYPE, ?, Map<String, List<Map<String, String>>>> getEntityTypeMapCollector(final SelectedFields selectedFields,
            final List<ENTITY_TYPE> entityTypes)
    {
        final Map<String, Map<PropertyTypePermId, String>> propertyTypePermIdsByEntityType =
                entityTypes.stream().collect(Collectors.toMap(this::getPermId,
                        entityType -> entityType.getPropertyAssignments().stream()
                                .map(PropertyAssignment::getPropertyType)
                                .collect(Collectors.toMap(PropertyType::getPermId, PropertyType::getCode))));

        return Collectors.toMap(this::getPermId,
                entityType ->
                {
                    final Map<PropertyTypePermId, String> propertyTypePermIds =
                            propertyTypePermIdsByEntityType.get(getPermId(entityType));
                    final List<String> selectedPropertyTypeCodes =
                            selectedFields.getProperties().stream().flatMap(
                                            propertyTypePermId ->
                                            {
                                                final String propertyTypeCode = propertyTypePermIds.get(propertyTypePermId);
                                                return propertyTypeCode != null ? Stream.of(propertyTypeCode) : Stream.empty();
                                            })
                                    .collect(Collectors.toList());
                    return mergePropertiesAndAttributes(selectedPropertyTypeCodes, selectedFields.getAttributes());
                });
    }

    private static List<Map<String, String>> mergePropertiesAndAttributes(final List<String> selectedPropertyTypeCodes,
            final Collection<Attribute> attributes)
    {
        final Stream<Map<String, String>> attributesStream = attributes.stream()
                .map(attribute -> Map.of(TYPE, FieldType.ATTRIBUTE.name(), ID, attribute.name()));

        final Stream<Map<String, String>> propertiesStream = selectedPropertyTypeCodes.stream()
                .map(propertyTypeCode -> Map.of(TYPE, FieldType.PROPERTY.name(), ID, propertyTypeCode));

        return Stream.concat(attributesStream, propertiesStream).collect(Collectors.toList());
    }

    public abstract SearchResult<ENTITY_TYPE> findEntityTypes(Set<IPropertyTypeId> properties,
            IApplicationServerInternalApi applicationServerApi, String sessionToken);

    public abstract String getPermId(ENTITY_TYPE entityType);

}
