/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.*;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.collection.TableMap.UniqueKeyViolationStrategy;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IPerson;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyWithSampleDataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IManagedPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.SupportedDateTimePattern;

/**
 * The unique {@link IEntityPropertiesConverter} implementation.
 * <p>
 * This implementation caches as much as possible to avoid redundant database requests. This also
 * means that this class should not be reused. Creating a new instance each time this class is
 * needed should be preferred.
 * </p>
 *
 * @author Christian Ribeaud
 */
public final class EntityPropertiesConverter implements IEntityPropertiesConverter
{

    private static final IKeyExtractor<PropertyTypePE, ExtendedEntityTypePropertyType>
            EXTENDED_ETPT_KEY_EXTRACTOR =
            new IKeyExtractor<PropertyTypePE, ExtendedEntityTypePropertyType>()
            {
                @Override
                public PropertyTypePE getKey(ExtendedEntityTypePropertyType etpt)
                {
                    return etpt.getEntityTypePropertyTypePE().getPropertyType();
                }
            };

    private static final String SEPARATOR = ",";

    private static final List<DataTypeCode> ARRAY_TYPES = List.of(DataTypeCode.ARRAY_STRING,
            DataTypeCode.ARRAY_INTEGER, DataTypeCode.ARRAY_REAL, DataTypeCode.ARRAY_TIMESTAMP);

    private static final String NO_ENTITY_PROPERTY_VALUE_FOR_S =
            "Value of mandatory property '%s' not specified.";

    private final IDAOFactory daoFactory;

    private final EntityKind entityKind;

    private TableMap<String, EntityTypePE> entityTypesByCode;

    private Map<String, Set<String>> dynamicPropertiesByEntityTypeCode =
            new HashMap<String, Set<String>>();

    private Map<String, Set<String>> managedPropertiesByEntityTypeCode =
            new HashMap<String, Set<String>>();

    private final TableMap<String, PropertyTypePE> propertyTypesByCode =
            new TableMap<String, PropertyTypePE>(
                    KeyExtractorFactory.getPropertyTypeByCodeKeyExtractor());

    private Map<String /* Entity type code */, TableMap<PropertyTypePE, ExtendedEntityTypePropertyType>>
            entityTypePropertyTypesByEntityTypeAndPropertyType =
            new HashMap<String, TableMap<PropertyTypePE, ExtendedEntityTypePropertyType>>();

    private final ComplexPropertyValueHelper complexPropertyValueHelper;

    private final IPropertyValueValidator propertyValueValidator;

    private final IPropertyPlaceholderCreator placeholderCreator;

    private final IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    public EntityPropertiesConverter(final EntityKind entityKind, final IDAOFactory daoFactory,
            IEntityInformationProvider entityInfoProvider,
            final IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this(entityKind, daoFactory, new PropertyValidator(), new PlaceholderPropertyCreator(),
                entityInfoProvider, managedPropertyEvaluatorFactory);
    }

    @Private
    EntityPropertiesConverter(final EntityKind entityKind, final IDAOFactory daoFactory,
            final IPropertyValueValidator propertyValueValidator,
            IPropertyPlaceholderCreator placeholderCreator,
            IEntityInformationProvider entityInfoProvider,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        assert entityKind != null : "Unspecified entity kind.";
        assert daoFactory != null : "Unspecified DAO factory.";
        assert propertyValueValidator != null : "Unspecified property value validator.";

        this.daoFactory = daoFactory;
        this.entityKind = entityKind;
        this.propertyValueValidator = propertyValueValidator;
        this.placeholderCreator = placeholderCreator;
        this.complexPropertyValueHelper =
                new ComplexPropertyValueHelper(daoFactory, null, entityInfoProvider);
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    private final Set<String> getDynamicProperties(final EntityTypePE entityTypePE)
    {
        String code = entityTypePE.getCode();
        if (dynamicPropertiesByEntityTypeCode.containsKey(code) == false)
        {
            HashSet<String> set = new HashSet<String>();
            List<EntityTypePropertyTypePE> list =
                    daoFactory.getEntityPropertyTypeDAO(entityKind).listEntityPropertyTypes(
                            entityTypePE);
            for (EntityTypePropertyTypePE etpt : list)
            {
                if (etpt.isDynamic())
                {
                    set.add(etpt.getPropertyType().getCode());
                }
            }
            dynamicPropertiesByEntityTypeCode.put(code, set);
        }
        return dynamicPropertiesByEntityTypeCode.get(code);
    }

    private final Set<String> getManagedProperties(final EntityTypePE entityTypePE)
    {
        String code = entityTypePE.getCode();
        if (managedPropertiesByEntityTypeCode.containsKey(code) == false)
        {
            HashSet<String> set = new HashSet<String>();
            List<EntityTypePropertyTypePE> list =
                    daoFactory.getEntityPropertyTypeDAO(entityKind).listEntityPropertyTypes(
                            entityTypePE);
            for (EntityTypePropertyTypePE etpt : list)
            {
                if (etpt.isManaged())
                {
                    set.add(etpt.getPropertyType().getCode());
                }
            }
            managedPropertiesByEntityTypeCode.put(code, set);
        }
        return managedPropertiesByEntityTypeCode.get(code);
    }

    private final EntityTypePE getEntityType(final String entityTypeCode)
    {
        if (entityTypesByCode == null)
        {
            entityTypesByCode =
                    new TableMap<String, EntityTypePE>(daoFactory.getEntityTypeDAO(entityKind)
                            .listEntityTypes(),
                            KeyExtractorFactory.getEntityTypeByCodeKeyExtractor());
        }
        final EntityTypePE entityType = entityTypesByCode.tryGet(entityTypeCode.toUpperCase());
        if (entityType == null)
        {
            throw UserFailureException.fromTemplate("Entity type with code '%s' does not exist!",
                    entityTypeCode);
        }
        return entityType;
    }

    private final PropertyTypePE getPropertyType(final String propertyCode)
    {
        PropertyTypePE propertyType = propertyTypesByCode.tryGet(propertyCode.toUpperCase());
        if (propertyType == null)
        {
            propertyType = daoFactory.getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyCode);
            if (propertyType == null)
            {
                throw UserFailureException.fromTemplate(
                        "Property type with code '%s' does not exist!", propertyCode);
            }
            propertyTypesByCode.add(propertyType);
        }
        return propertyType;
    }

    private final ExtendedEntityTypePropertyType getEntityTypePropertyType(
            final EntityTypePE entityTypePE, final PropertyTypePE propertyType)
    {
        String entityTypeCode = entityTypePE.getCode();
        TableMap<PropertyTypePE, ExtendedEntityTypePropertyType> map =
                entityTypePropertyTypesByEntityTypeAndPropertyType.get(entityTypeCode);
        if (map == null)
        {
            List<ExtendedEntityTypePropertyType> entityTypePropertyTypes =
                    getEntityTypePropertyTypes(entityTypePE);
            map =
                    new TableMap<PropertyTypePE, ExtendedEntityTypePropertyType>(
                            entityTypePropertyTypes, EXTENDED_ETPT_KEY_EXTRACTOR,
                            UniqueKeyViolationStrategy.KEEP_FIRST);
            entityTypePropertyTypesByEntityTypeAndPropertyType.put(entityTypeCode, map);
        }

        final ExtendedEntityTypePropertyType entityTypePropertyType = map.tryGet(propertyType);

        if (entityTypePropertyType == null)
        {
            throw UserFailureException.fromTemplate(
                    "No assigment between property type '%s' and entity type '%s' could be found.",
                    propertyType.getCode(), entityTypeCode);
        }
        return entityTypePropertyType;
    }

    private List<ExtendedEntityTypePropertyType> getEntityTypePropertyTypes(
            EntityTypePE entityTypePE)
    {
        IEntityPropertyTypeDAO entityPropertyTypeDAO =
                daoFactory.getEntityPropertyTypeDAO(entityKind);
        List<EntityTypePropertyTypePE> entityPropertyTypes =
                entityPropertyTypeDAO.listEntityPropertyTypes(entityTypePE);
        List<ExtendedEntityTypePropertyType> result =
                new ArrayList<ExtendedEntityTypePropertyType>();
        for (EntityTypePropertyTypePE entityTypePropertyTypePE : entityPropertyTypes)
        {
            result.add(new ExtendedEntityTypePropertyType(entityTypePropertyTypePE,
                    managedPropertyEvaluatorFactory));
        }
        return result;
    }

    private final <T extends EntityPropertyPE> List<T> tryConvertProperty(
            final PersonPE registrator,
            final EntityTypePE entityTypePE, final IEntityProperty property)
    {
        final String propertyCode = property.getPropertyType().getCode();
        final PropertyTypePE propertyType = getPropertyType(propertyCode);
        final Serializable valueOrNull = getPropertyValue(property);
        ExtendedEntityTypePropertyType extendedETPT =
                getEntityTypePropertyType(entityTypePE, propertyType);
        final EntityTypePropertyTypePE entityTypePropertyTypePE =
                extendedETPT.getEntityTypePropertyTypePE();

        if (entityTypePropertyTypePE.isMandatory() && isNullOrBlank(valueOrNull))
        {
            throw UserFailureException.fromTemplate(NO_ENTITY_PROPERTY_VALUE_FOR_S, propertyCode);
        }
        if (isNullOrBlank(valueOrNull) == false)
        {
            List<T> results = new ArrayList<>();
            Serializable parsedValue = translateProperty(propertyType, valueOrNull);
            if(propertyType.isMultiValue() && parsedValue.getClass().isArray()) {
                for (Serializable value : (Serializable[]) parsedValue)
                {
                    Serializable translatedValue =
                            extendedETPT.translate(registrator, value);
                    final Serializable validatedValue =
                            propertyValueValidator.validatePropertyValue(propertyType,
                                    translatedValue);
                    results.addAll(createEntityProperty(registrator, propertyType,
                            entityTypePropertyTypePE,
                            validatedValue));

                }
            } else {
                Serializable translatedValue = extendedETPT.translate(registrator, parsedValue);

                final Serializable validatedValue =
                        propertyValueValidator.validatePropertyValue(propertyType, translatedValue);
                results.addAll(createEntityProperty(registrator, propertyType, entityTypePropertyTypePE,
                        validatedValue));
            }
            return results;
        }
        return null;
    }

    private Serializable translateProperty(PropertyTypePE propertyType, Serializable value) {
        final String regex = "(?<!\\\\)" + Pattern.quote(SEPARATOR);
        if(value == null || !value.getClass().equals(String.class)) {
            if(propertyType.isMultiValue() && ARRAY_TYPES.contains(propertyType.getType().getCode())) {
                Serializable[] array = (Serializable[]) value;
                List<Serializable> values = new ArrayList<>();
                for (Serializable element : array)
                {
                    if (element.getClass().equals(String.class))
                    {
                        values.add(stripBracketsIfPresent((String) element).split(regex));
                    } else
                    {
                        // value is properly serialized already, nothing to do here
                        return value;
                    }
                }
                return values.toArray(Serializable[]::new);
            } else {
                //Nothing to translate
                return value;
            }
        }

        String propertyValue = value.toString();
        if(propertyValue.trim().isEmpty()) {
            return null;
        }

        if(propertyType.isMultiValue()) {
            propertyValue = propertyValue.trim();
            propertyValue = stripBracketsIfPresent(propertyValue);
            if(propertyValue.isEmpty()) {
                return null;
            }
            if(ARRAY_TYPES.contains(propertyType.getType().getCode())) {
                // Multi-value array properties
                String multiArrayRegex = "\\],\\s*\\[";
                propertyValue = stripBracketsIfPresent(propertyValue);
                return Arrays.stream(propertyValue.split(multiArrayRegex))
                        .map(String::trim)
                        .map(x -> Arrays.stream(x.split(regex))
                                .map(String::trim)
                                .toArray(String[]::new))
                        .toArray(String[][]::new);
            } else {
                return Arrays.stream(propertyValue.split(regex))
                        .map(String::trim)
                        .toArray(String[]::new);
            }
        } else {
            if(ARRAY_TYPES.contains(propertyType.getType().getCode())) {
                propertyValue = propertyValue.trim();
                propertyValue = stripBracketsIfPresent(propertyValue);
                return Arrays.stream(propertyValue.split(regex))
                                    .map(String::trim)
                                    .toArray(String[]::new);
            } else {
                return propertyValue;
            }
        }
    }

    private String stripBracketsIfPresent(String value) {
        if(value.startsWith("[")) {
            value = value.substring(1, value.length()-1).trim();
        }
        return value;
    }

    private Serializable getPropertyValue(final IEntityProperty property) {
        Serializable result = property.getValue();
        if(result != null) {
            return result;
        }
        result = property.getVocabularyTerm();
        if(result != null) {
            return result;
        }
        result = property.getSample();
        if(result != null) {
            return result;
        }
        result = property.getMaterial();
        if(result != null) {
            return result;
        }
        return property.tryGetAsString();
    }

    private final <T extends EntityPropertyPE> List<T> createEntityProperty(
            final PersonPE registrator,
            final PropertyTypePE propertyType,
            final EntityTypePropertyTypePE entityTypePropertyType, final Serializable value)
    {
        List<T> entityProperties = new ArrayList<>();
        final T entityProperty = getEntityPropertyBase(registrator, entityTypePropertyType);
        setPropertyValue(entityProperty, propertyType, value);
        entityProperties.add(entityProperty);
        return entityProperties;
    }

    private <T extends EntityPropertyPE> T getEntityPropertyBase(final PersonPE registrator,
            final EntityTypePropertyTypePE entityTypePropertyType)
    {
        final T entityPropertyBase = EntityPropertyPE.createEntityProperty(entityKind);
        entityPropertyBase.setRegistrator(registrator);
        entityPropertyBase.setAuthor(registrator);
        entityPropertyBase.setEntityTypePropertyType(entityTypePropertyType);
        entityPropertyBase.setUnique(entityTypePropertyType.isUnique());
        return entityPropertyBase;
    }

    //
    // IEntityPropertiesConverter
    //

    @Override
    public final <T extends EntityPropertyPE> List<T> convertProperties(
            final IEntityProperty[] properties, final String entityTypeCode,
            final PersonPE registrator)
    {
        return convertProperties(properties, entityTypeCode, registrator, true, true);
    }

    private final <T extends EntityPropertyPE> List<T> convertProperties(
            final IEntityProperty[] properties, final String entityTypeCode,
            final PersonPE registrator, final boolean createManagedPropertiesPlaceholders,
            boolean createDynamicPropertiesPlaceholders)
    {
        assert entityTypeCode != null : "Unspecified entity type code.";
        assert registrator != null : "Unspecified registrator";
        assert properties != null : "Unspecified entity properties";
        final EntityTypePE entityTypePE = getEntityType(entityTypeCode);
        Set<String> dynamicProperties = getDynamicProperties(entityTypePE);
        Set<String> managedProperties = getManagedProperties(entityTypePE);
        Set<IEntityProperty> definedProperties =
                new LinkedHashSet<IEntityProperty>(Arrays.asList(properties));
        if (createDynamicPropertiesPlaceholders)
        {
            placeholderCreator.addDynamicPropertiesPlaceholders(definedProperties,
                    dynamicProperties);
        }
        if (createManagedPropertiesPlaceholders)
        {
            placeholderCreator.addManagedPropertiesPlaceholders(definedProperties,
                    managedProperties);
        }
        final List<T> list = new ArrayList<T>();
        for (final IEntityProperty property : definedProperties)
        {
            final List<T> convertedPropertyOrNull =
                    tryConvertProperty(registrator, entityTypePE, property);
            if (convertedPropertyOrNull != null && !convertedPropertyOrNull.isEmpty())
            {
                list.addAll(convertedPropertyOrNull);
            }
        }
        return list;
    }

    @Override
    public <T extends EntityPropertyPE> void checkMandatoryProperties(Collection<T> properties,
            EntityTypePE entityTypePE)
    {
        assert properties != null;
        checkMandatoryProperties(
                properties,
                entityTypePE,
                daoFactory.getEntityPropertyTypeDAO(entityKind).listEntityPropertyTypes(
                        entityTypePE));
    }

    @Override
    public <T extends EntityPropertyPE> void checkMandatoryProperties(Collection<T> properties,
            EntityTypePE entityTypePE, Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache)
    {
        assert properties != null;
        checkMandatoryProperties(properties, entityTypePE,
                getAssignedPropertiesForEntityType(cache, entityTypePE));

    }

    private List<EntityTypePropertyTypePE> getAssignedPropertiesForEntityType(
            Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache, EntityTypePE entityTypePE)
    {
        List<EntityTypePropertyTypePE> assignedProperties = cache.get(entityTypePE);
        if (assignedProperties == null)
        {
            assignedProperties =
                    daoFactory.getEntityPropertyTypeDAO(entityKind).listEntityPropertyTypes(
                            entityTypePE);
            cache.put(entityTypePE, assignedProperties);
        }
        return assignedProperties;
    }

    private <T extends EntityPropertyPE> void checkMandatoryProperties(Collection<T> propertyValues,
            EntityTypePE entityTypePE, List<EntityTypePropertyTypePE> assignedProperties)
    {
        assert propertyValues != null;
        if (assignedProperties == null || assignedProperties.size() == 0)
        {
            return;
        }

        List<EntityTypePropertyTypePE> mandatoryAssignedProperties = new ArrayList<>(0);
        for (EntityTypePropertyTypePE etpt : assignedProperties)
        {
            if (etpt.isMandatory())
            {
                mandatoryAssignedProperties.add(etpt);
            }
        }
        if (mandatoryAssignedProperties.isEmpty())
        {
            return;
        }

        Set<EntityTypePropertyTypePE> definedProperties = new HashSet<EntityTypePropertyTypePE>();
        for (T p : propertyValues)
        {
            definedProperties.add(p.getEntityTypePropertyType());
        }

        for (EntityTypePropertyTypePE etpt : mandatoryAssignedProperties)
        {
            if (definedProperties.contains(etpt) == false)
            {
                throw UserFailureException.fromTemplate(NO_ENTITY_PROPERTY_VALUE_FOR_S, etpt
                        .getPropertyType().getCode());
            }
        }
    }

    @Override
    public final String tryCreateValidatedPropertyValue(PropertyTypePE propertyType,
            EntityTypePropertyTypePE entityTypPropertyType, String value)
    {
        if (entityTypPropertyType.isMandatory() && isNullOrBlank(value))
        {
            throw UserFailureException.fromTemplate(NO_ENTITY_PROPERTY_VALUE_FOR_S,
                    propertyType.getCode());
        }
        if (isNullOrBlank(value) == false)
        {
            final Serializable validated =
                    propertyValueValidator.validatePropertyValue(propertyType, value);
            return validated.toString();
        }
        return null;
    }

    @Override
    public final <T extends EntityPropertyPE> List<T> createValidatedProperty(
            PropertyTypePE propertyType, EntityTypePropertyTypePE entityTypPropertyType,
            final PersonPE registrator, String validatedValue)
    {
        assert validatedValue != null;
        return createEntityProperty(registrator, propertyType, entityTypPropertyType,
                validatedValue);
    }

    @Override
    public final <T extends EntityPropertyPE> void setPropertyValue(final T entityProperty,
            final PropertyTypePE propertyType, final Serializable validatedValue)
    {
        assert validatedValue != null;

        if(validatedValue.getClass().equals(String.class)) {
            String value = (String) validatedValue;
            if (value.startsWith(BasicConstant.ERROR_PROPERTY_PREFIX))
            {
                // save errors as strings
                entityProperty.setUntypedValue(value, null, null, null, null, null, null, null,
                        null);
            }
        }


        {
            final VocabularyTermPE vocabularyTerm =
                    complexPropertyValueHelper.tryGetVocabularyTerm(validatedValue, propertyType);
            final MaterialPE material =
                    complexPropertyValueHelper.tryGetMaterial(validatedValue, propertyType);
            SamplePE sample = complexPropertyValueHelper.tryGetSample(validatedValue, propertyType);
            final Long[] integerArray =
                    complexPropertyValueHelper.tryGetIntegerArray(validatedValue, propertyType);
            final Double[] realArray =
                    complexPropertyValueHelper.tryGetRealArray(validatedValue, propertyType);
            final String[] stringArray =
                    complexPropertyValueHelper.tryGetStringArray(validatedValue, propertyType);
            final Date[] timestampArray =
                    complexPropertyValueHelper.tryGetTimestampArray(validatedValue, propertyType);
            final String jsonValue =
                    complexPropertyValueHelper.tryGetJsonValue(validatedValue, propertyType);
            entityProperty.setUntypedValue(validatedValue.toString(), vocabularyTerm, material, sample,
                    integerArray, realArray, stringArray, timestampArray, jsonValue);
        }
    }

    @Override
    public <T extends EntityPropertyPE> Set<T> updateProperties(Collection<T> oldProperties,
            EntityTypePE entityType, List<IEntityProperty> newProperties, PersonPE author)
    {
        // the existing properties will change their values, so we need to cache them for a moment
        Map<T, String> existingPropertyValues = new HashMap<T, String>();
        for (T existingProperty : oldProperties)
        {
            existingPropertyValues.put(existingProperty, existingProperty.tryGetUntypedValue());
        }

        Set<T> deletedProperties = findDeletedProperties(oldProperties, newProperties);
        for (T t : deletedProperties)
        {
            // TODO: create null property history entry
        }
        Set<T> oldPropertiesTemp = new HashSet<>(oldProperties);
        final List<T> convertedProperties =
                convertPropertiesForUpdate(newProperties, entityType.getCode(), author);
        final Set<T> set = new LinkedHashSet<>();

        for (int i=0; i< convertedProperties.size(); i++) {
            T newProperty = convertedProperties.get(i);
            PropertyTypePE propertyType = newProperty.getEntityTypePropertyType().getPropertyType();
            T existingProperty = null;
            if(!propertyType.isMultiValue())
            {
                existingProperty = tryFind(oldPropertiesTemp, propertyType);
            } else {
                List<T> oldMulti = oldPropertiesTemp.stream()
                        .filter(oldProp -> oldProp.getEntityTypePropertyType().getPropertyType().equals(propertyType))
                        .sorted(Comparator.comparing(IIdHolder::getId))
                        .collect(Collectors.toList());
                if(!oldMulti.isEmpty()) {
                    existingProperty = oldMulti.get(0);
                    oldPropertiesTemp.remove(existingProperty);
                }
            }

            if (existingProperty != null)
            {
                SamplePE sample = newProperty instanceof EntityPropertyWithSampleDataTypePE
                        ? ((EntityPropertyWithSampleDataTypePE) newProperty).getSampleValue()
                        : null;
                existingProperty.setUntypedValue(newProperty.getValue(),
                        newProperty.getVocabularyTerm(), newProperty.getMaterialValue(), sample,
                        newProperty.getIntegerArrayValue(), newProperty.getRealArrayValue(),
                        newProperty.getStringArrayValue(),
                        newProperty.getTimestampArrayValue(), newProperty.getJsonValue());
                if (existingPropertyValues.containsKey(
                        existingProperty) && !existingPropertyValues.get(existingProperty)
                        .equals(newProperty.tryGetUntypedValue()))
                {
                    existingProperty.setAuthor(author);
                    // TODO: create modified property history entry
                }
                set.add(existingProperty);
            } else
            {
                if (propertyType.isMultiValue()) {
                    newProperty.setIndex(i);
                }
                // TODO: create new property history entry
                set.add(newProperty);
            }

        }
        return set;
    }

    private static <T extends EntityPropertyPE> T tryFindMulti(Collection<T> oldProperties,
            PropertyTypePE propertyType, T newProperty)
    {
        String propertyValue = newProperty.tryGetUntypedValue();
        for (T oldProperty : oldProperties)
        {
            if (oldProperty.getEntityTypePropertyType().getPropertyType().equals(propertyType))
            {
                String oldValue = oldProperty.tryGetUntypedValue();
                if (oldValue != null && oldValue.equals(propertyValue))
                {
                    oldProperties.remove(oldProperty);
                    return oldProperty;
                }
            }
        }
        return null;
    }

    private static <T extends EntityPropertyPE> Set<T> findDeletedProperties(
            Collection<T> oldProperties,
            List<IEntityProperty> newProperties)
    {
        Set<String> newPropertiesCodes = new HashSet<String>();
        Set<T> deletedProperties = new HashSet<T>();

        for (IEntityProperty newP : newProperties)
        {
            if (newP.getValue() != null)
            {
                newPropertiesCodes.add(newP.getPropertyType().getCode());
            }
        }

        for (T old : oldProperties)
        {
            if (newPropertiesCodes.contains(
                    old.getEntityTypePropertyType().getPropertyType().getCode()) == false)
            {
                deletedProperties.add(old);
            }
        }
        return deletedProperties;
    }

    /**
     * Update the value of a managed property, assuming the managedProperty already has the updated
     * value.
     */
    @Override
    public <T extends EntityPropertyPE> Set<T> updateManagedProperty(Collection<T> oldProperties,
            EntityTypePE entityType, IManagedProperty managedProperty, PersonPE author)
    {

        final Set<T> set = new HashSet<T>();

        // Add all existing properties
        set.addAll(oldProperties);

        // Update the managed property we want to update
        T existingProperty = tryFind(oldProperties, managedProperty.getPropertyTypeCode());
        if (existingProperty != null)
        {
            existingProperty.setUntypedValue((String) managedProperty.getValue(), null, null, null,
                    null,
                    null, null, null, null);
            existingProperty.setAuthor(author);
        }
        return set;
    }

    private final <T extends EntityPropertyPE> List<T> convertPropertiesForUpdate(
            final List<? extends IEntityProperty> properties, final String entityTypeCode,
            final PersonPE registrator)
    {
        IEntityProperty[] propsArray = properties.toArray(new IEntityProperty[0]);
        return convertProperties(propsArray, entityTypeCode, registrator, false, false);
    }

    @Override
    public <T extends EntityPropertyPE> Set<T> updateProperties(Collection<T> oldProperties,
            EntityTypePE entityType, List<IEntityProperty> newProperties, PersonPE author,
            Set<String> propertiesToUpdate)
    {
        // all new properties should be among propertiesToUpdate (no need to check it)
        final Set<T> set = updateProperties(oldProperties, entityType, newProperties, author);
        // add old properties that are not among propertiesToUpdate (preserve those properties)
        for (T oldProperty : oldProperties)
        {
            final String oldPropertyCode =
                    oldProperty.getEntityTypePropertyType().getPropertyType().getCode();
            // TODO 2011-02-16, Piotr Buczek: improve case sensitivity checks
            if (propertiesToUpdate.contains(oldPropertyCode.toLowerCase()) == false
                    && propertiesToUpdate.contains(oldPropertyCode) == false)
            {
                set.add(oldProperty);
            }
        }
        return set;
    }

    private static <T extends EntityPropertyPE> T tryFind(Collection<T> oldProperties,
            PropertyTypePE propertyType)
    {
        for (T oldProperty : oldProperties)
        {
            if (oldProperty.getEntityTypePropertyType().getPropertyType().equals(propertyType))
            {
                oldProperties.remove(oldProperty);
                return oldProperty;
            }
        }
        return null;
    }

    private static <T extends EntityPropertyPE> T tryFind(Collection<T> oldProperties,
            String propertyTypeCode)
    {
        for (T oldProperty : oldProperties)
        {
            if (oldProperty.getEntityTypePropertyType().getPropertyType().getCode()
                    .equals(propertyTypeCode))
            {
                return oldProperty;
            }
        }
        return null;
    }

    private static boolean isNullOrBlank(Serializable value)
    {
        if (value == null)
        {
            return true;
        }
        if (value.getClass().isArray())
        {
            return ((Serializable[]) value).length == 0;
        }
        return value.toString().trim().length() == 0;
    }

    //
    // Helper classes
    //

    private static final class ExtendedEntityTypePropertyType
    {
        private final EntityTypePropertyTypePE entityTypePropertyTypePE;

        private final List<IManagedInputWidgetDescription> inputWidgetDescriptions;

        private IManagedPropertyEvaluator evaluator;

        ExtendedEntityTypePropertyType(EntityTypePropertyTypePE entityTypePropertyTypePE,
                IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
        {
            this.entityTypePropertyTypePE = entityTypePropertyTypePE;
            if (entityTypePropertyTypePE.isManaged())
            {
                evaluator =
                        managedPropertyEvaluatorFactory
                                .createManagedPropertyEvaluator(entityTypePropertyTypePE);
                inputWidgetDescriptions = evaluator.getInputWidgetDescriptions();
            } else
            {
                inputWidgetDescriptions = Collections.emptyList();
            }
        }

        public EntityTypePropertyTypePE getEntityTypePropertyTypePE()
        {
            return entityTypePropertyTypePE;
        }

        @SuppressWarnings("unchecked")
        Serializable translate(PersonPE personPE, Serializable value)
        {
            if(value == null || !value.getClass().equals(String.class)) {
                return value;
            }
            else
            {
                String propertyValue = (String) value;
                if (inputWidgetDescriptions.isEmpty()
                        || propertyValue.startsWith(BasicConstant.ERROR_PROPERTY_PREFIX)
                        || propertyValue.startsWith(BasicConstant.MANAGED_PROPERTY_JSON_PREFIX) == false)
                {
                    return propertyValue;
                }
                try
                {
                    List<?> readValue =
                            new ObjectMapper().readValue(propertyValue
                                            .substring(BasicConstant.MANAGED_PROPERTY_JSON_PREFIX.length()),
                                    List.class);
                    ManagedProperty managedProperty = new ManagedProperty();
                    IPerson person = PersonTranslator.translateToIPerson(personPE);

                    List<Map<String, String>> bindingsList = new ArrayList<Map<String, String>>();

                    for (Object row : readValue)
                    {
                        if (row instanceof Map == false)
                        {
                            continue;
                        }

                        bindingsList.add((Map<String, String>) row);
                    }

                    evaluator.updateFromRegistrationForm(managedProperty, person, bindingsList);
                    return managedProperty.getValue();
                } catch (Exception ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }

    }

    public interface IHibernateSessionProvider
    {
        public Session getSession();
    }

    public final static class ComplexPropertyValueHelper
    {

        private final IDAOFactory daoFactory;

        // WORKAROUND This information needs to be exposed to force MaterialDAO to use given session
        // for requests coming from DynamicPropertyEvaluator. Otherwise each call to tryGetMaterial
        // creates a new DB connection that are not closed.
        private final IHibernateSessionProvider customSessionProviderOrNull;

        private IEntityInformationProvider entityInfoProvider;

        /**
         * @param customSessionProviderOrNull Provider of custom session that should be used for
         *                                    accessing DB instead of default one. If null the
         *                                    standard way of getting the session should be used.
         * @param entityInfoProvider
         */
        public ComplexPropertyValueHelper(IDAOFactory daoFactory,
                IHibernateSessionProvider customSessionProviderOrNull,
                IEntityInformationProvider entityInfoProvider)
        {
            this.daoFactory = daoFactory;
            this.customSessionProviderOrNull = customSessionProviderOrNull;
            this.entityInfoProvider = entityInfoProvider;
        }

        public SamplePE tryGetSample(Serializable val, PropertyTypePE propertyType)
        {
            if (propertyType.getType().getCode() != DataTypeCode.SAMPLE)
            {
                return null; // this is not a property of SAMPLE type
            }
            String value;
            if(val.getClass().equals(Sample.class)) {
                value = ((Sample)val).getPermId();
            } else {
                value = val.toString();
            }

            ISampleDAO sampleDAO = daoFactory.getSampleDAO();
            String samplePermId = value;
            if (samplePermId.startsWith("["))
            {
                samplePermId = samplePermId.substring(1, samplePermId.length() - 1);
            }
            if (value.startsWith("/"))
            {
                samplePermId = entityInfoProvider.getSamplePermId(value);
                if (samplePermId == null)
                {
                    throw UserFailureException.fromTemplate(
                            "No sample could be found for identifier %s.", value);
                }
            }
            List<SamplePE> samples = sampleDAO.listByPermID(Collections.singleton(samplePermId));
            if (samples.isEmpty())
            {
                throw UserFailureException.fromTemplate("No sample could be found for perm id %s.",
                        value);
            }
            return samples.get(0);
        }

        public MaterialPE tryGetMaterial(Serializable value, PropertyTypePE propertyType)
        {
            if (propertyType.getType().getCode() != DataTypeCode.MATERIAL)
            {
                return null; // this is not a property of MATERIAL type
            }
            MaterialIdentifier materialIdentifier =
                    MaterialIdentifier.tryCreate((String)value, propertyType.getMaterialType());
            if (materialIdentifier == null)
            {
                return null;
            }

            final MaterialPE material;
            if (customSessionProviderOrNull != null)
            {
                material =
                        daoFactory.getMaterialDAO().tryFindMaterial(
                                customSessionProviderOrNull.getSession(), materialIdentifier);
            } else
            {
                material = daoFactory.getMaterialDAO().tryFindMaterial(materialIdentifier);
            }

            if (material == null)
            {
                throw UserFailureException.fromTemplate(
                        "No material could be found for identifier '%s'.", materialIdentifier);
            }
            return material;
        }

        public VocabularyTermPE tryGetVocabularyTerm(final Serializable value,
                final PropertyTypePE propertyType)
        {
            if (propertyType.getType().getCode() != DataTypeCode.CONTROLLEDVOCABULARY)
            {
                return null; // this is not a property of CONTROLLED VOCABULARY type
            }

            final VocabularyPE vocabulary = propertyType.getVocabulary();
            if (vocabulary == null)
            {
                return null;
            }
            final VocabularyTermPE term = vocabulary.tryGetVocabularyTerm((String)value);
            if (term != null)
            {
                return term;
            }
            throw UserFailureException.fromTemplate(
                    "Incorrect value '%s' for a controlled vocabulary set '%s'.", value,
                    vocabulary.getCode());
        }



        public Long[] tryGetIntegerArray(final Serializable value, final PropertyTypePE propertyType)
        {
            DataTypeCode code = propertyType.getType().getCode();
            if (code != DataTypeCode.ARRAY_INTEGER)
            {
                return null;
            }
            if (value == null || !value.getClass().isArray() || ((Serializable[])value).length == 0)
            {
                return null;
            }
            return Arrays.stream((Serializable[])value)
                    .map(x -> Long.parseLong(x.toString().trim()))
                    .toArray(Long[]::new);
        }

        public Double[] tryGetRealArray(final Serializable value, final PropertyTypePE propertyType)
        {
            DataTypeCode code = propertyType.getType().getCode();
            if (code != DataTypeCode.ARRAY_REAL)
            {
                return null;
            }
            if (value == null || !value.getClass().isArray() || ((Serializable[])value).length == 0)
            {
                return null;
            }
            return Arrays.stream((Serializable[])value)
                    .map(x -> Double.parseDouble(x.toString().trim()))
                    .toArray(Double[]::new);
        }

        public String[] tryGetStringArray(final Serializable value, final PropertyTypePE propertyType)
        {
            DataTypeCode code = propertyType.getType().getCode();
            if (code != DataTypeCode.ARRAY_STRING)
            {
                return null;
            }
            if (value == null || !value.getClass().isArray() || ((Serializable[])value).length == 0)
            {
                return null;
            }
            return Arrays.stream((Serializable[])value)
                    .map(Serializable::toString)
                    .toArray(String[]::new);
        }

        public Date[] tryGetTimestampArray(final Serializable value, final PropertyTypePE propertyType)
        {
            DataTypeCode code = propertyType.getType().getCode();
            if (code != DataTypeCode.ARRAY_TIMESTAMP)
            {
                return null;
            }
            if (value == null || !value.getClass().isArray() || ((Serializable[])value).length == 0)
            {
                return null;
            }
            return Arrays.stream((Serializable[])value)
                    .map(x -> parseDateFromString((String)x))
                    .toArray(Date[]::new);
        }

        private Date parseDateFromString(String dateTime)
        {
            for(SupportedDateTimePattern format : SupportedDateTimePattern.values()){
                try {
                    SimpleDateFormat simpleDateFormat =
                            new SimpleDateFormat(format.getPattern());
                    return simpleDateFormat.parse(dateTime);
                } catch(Exception e)
                {
                    //ignore
                }
            }
            throw new IllegalArgumentException("Wrong date format:" + dateTime);
        }

        public String tryGetJsonValue(final Serializable value, final PropertyTypePE propertyType)
        {
            DataTypeCode code = propertyType.getType().getCode();
            if (code != DataTypeCode.JSON || value == null)
            {
                return null;
            }
            return value.toString();
        }

    }

}
