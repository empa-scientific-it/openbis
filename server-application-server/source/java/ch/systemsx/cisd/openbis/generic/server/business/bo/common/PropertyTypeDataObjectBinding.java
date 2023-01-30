package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.spi.util.NonUpdateCapableDataObjectBinding;

import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * A binding for the {@link IPropertyListingQuery#getPropertyTypes()} query.
 */
public class PropertyTypeDataObjectBinding extends NonUpdateCapableDataObjectBinding<PropertyType>
{
    @Override
    public void unmarshall(ResultSet row, PropertyType into) throws SQLException, EoDException
    {
        into.setId(row.getLong("pt_id"));
        into.setManagedInternally(row.getBoolean("is_managed_internally"));
        into.setSimpleCode(row.getString("pt_code"));
        into.setCode(CodeConverter.tryToBusinessLayer(into.getSimpleCode(),
                into.isManagedInternally()));
        into.setLabel(row.getString("pt_label"));
        into.setTransformation(row.getString("transformation"));
        final DataType dataType = new DataType();
        dataType.setCode(DataTypeCode.valueOf(row.getString("dt_code")));
        into.setDataType(dataType);
    }
}
