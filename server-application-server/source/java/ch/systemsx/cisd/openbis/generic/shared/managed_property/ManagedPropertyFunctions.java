package ch.systemsx.cisd.openbis.generic.shared.managed_property;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedUiActionDescriptionFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescriptionFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ValidationException;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElementFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IStructuredPropertyConverter;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.structured.ElementFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.structured.JsonStructuredPropertyConverter;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.structured.XmlOrJsonStructuredPropertyConverter;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.structured.XmlStructuredPropertyConverter;

/**
 * This is a utility class with functions to be used by Jython scripts for managed properties.
 * <p>
 * All public methods of this class are part of the Managed Properties API.
 */
@Component(value = ResourceNames.MANAGED_PROPERTY_SCRIPT_UTILITY_FACTORY)
public class ManagedPropertyFunctions
{
    private static final IManagedInputWidgetDescriptionFactory INPUT_WIDGET_FACTORY_INSTANCE =
            new ManagedUiActionDescriptionFactory();

    private static final String ORIGINAL_COLUMN_NAME_BINDING_KEY_PREFIX = "$ORIGINAL-COLUMN-NAME$";

    private static final IElementFactory ELEMENT_FACTORY_INSTANCE = new ElementFactory();

    private static final XmlStructuredPropertyConverter XML_ONLY_STRUCTURED_PROPERTY_CONVERTER_INSTANCE =
            new XmlStructuredPropertyConverter(ELEMENT_FACTORY_INSTANCE);

    private static final JsonStructuredPropertyConverter JSON_ONLY_STRUCTURED_PROPERTY_CONVERTER_INSTANCE =
            new JsonStructuredPropertyConverter(ELEMENT_FACTORY_INSTANCE);

    private static final IStructuredPropertyConverter XML_STRUCTURED_PROPERTY_CONVERTER_INSTANCE =
            new XmlOrJsonStructuredPropertyConverter(
                    XML_ONLY_STRUCTURED_PROPERTY_CONVERTER_INSTANCE,
                    JSON_ONLY_STRUCTURED_PROPERTY_CONVERTER_INSTANCE, true);

    private static final IStructuredPropertyConverter JSON_STRUCTURED_PROPERTY_CONVERTER_INSTANCE =
            new XmlOrJsonStructuredPropertyConverter(
                    XML_ONLY_STRUCTURED_PROPERTY_CONVERTER_INSTANCE,
                    JSON_ONLY_STRUCTURED_PROPERTY_CONVERTER_INSTANCE, false);

    // initialized by spring
    private static IEntityInformationProvider entityInformationProvider;

    public IEntityInformationProvider getEntityInformationProvider()
    {
        return entityInformationProvider;
    }

    // @Autowired
    @Resource(name = ResourceNames.ENTITY_INFORMATION_PROVIDER)
    public void setEntityInformationProvider(IEntityInformationProvider entityInformationProvider)
    {
        ManagedPropertyFunctions.entityInformationProvider = entityInformationProvider;
    }

    private ManagedPropertyFunctions()
    {

    }

    /**
     * Creates a table builder.
     */
    public static ISimpleTableModelBuilderAdaptor createTableBuilder()
    {
        return SimpleTableModelBuilderAdaptor.create(entityInformationProvider);
    }

    /**
     * Creates a {@link ValidationException} with specified message.
     */
    // NOTE: Violates Java naming conventions for method because it should look like a constructor
    // for invocations in jython.
    public static ValidationException ValidationException(String message)
    {
        return new ValidationException(message);
    }

    /**
     * @return a factory object that can be used to create {@link IManagedInputWidgetDescription}-s.
     */
    public static IManagedInputWidgetDescriptionFactory inputWidgetFactory()
    {
        return INPUT_WIDGET_FACTORY_INSTANCE;
    }

    /**
     * @return a factory object that can be used to create {@link IElement}-s.
     */
    public static IElementFactory elementFactory()
    {
        return ELEMENT_FACTORY_INSTANCE;
    }

    /**
     * @return a converter that can translate {@link IElement} to/from Strings using XML.
     */
    public static IStructuredPropertyConverter propertyConverter()
    {
        return XML_STRUCTURED_PROPERTY_CONVERTER_INSTANCE;
    }

    /**
     * @return a converter that can translate {@link IElement} to/from Strings using XML.
     */
    public static IStructuredPropertyConverter xmlPropertyConverter()
    {
        return XML_STRUCTURED_PROPERTY_CONVERTER_INSTANCE;
    }

    /**
     * @return a converter that can translate {@link IElement} to/from Strings using JSON.
     */
    public static IStructuredPropertyConverter jsonPropertyConverter()
    {
        return JSON_STRUCTURED_PROPERTY_CONVERTER_INSTANCE;
    }

    /**
     * @return a provider of information about entities.
     */
    public static IEntityInformationProvider entityInformationProvider()
    {
        return entityInformationProvider;
    }

    /**
     * @return name of the given original column name (from the batch file) as stored in binding map
     */
    public static String originalColumnNameBindingKey(String originalColumnName)
    {
        return ORIGINAL_COLUMN_NAME_BINDING_KEY_PREFIX + originalColumnName;
    }

    /**
     * @return true if the binding key is of a managed property
     */
    public static boolean isOriginalColumnNameBindingKey(String bindingKey)
    {
        return bindingKey.startsWith(ORIGINAL_COLUMN_NAME_BINDING_KEY_PREFIX);
    }

}