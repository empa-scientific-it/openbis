package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

public class InternalPropertyTypeAuthorization
{

    public void canCreatePropertyType(Session session, PropertyTypePE propertyType)
    {
        checkPropertyType(session, propertyType);
    }

    public void canUpdatePropertyType(Session session, PropertyTypePE propertyType)
    {
        checkPropertyType(session, propertyType);
    }

    public void canDeletePropertyType(Session session, PropertyTypePE propertyType)
    {
        checkPropertyType(session, propertyType);
    }

    public void canCreatePropertyAssignment(Session session, PropertyTypePE propertyType, EntityTypePropertyTypePE propertyAssignment)
    {
        // do not check anything - allow new assignments to be created even for internally managed property types
    }

    public void canUpdatePropertyAssignment(Session session, PropertyTypePE propertyType, EntityTypePropertyTypePE propertyAssignment)
    {
        checkPropertyAssignment(session, propertyType, propertyAssignment);
    }

    public void canDeletePropertyAssignment(Session session, PropertyTypePE propertyType, EntityTypePropertyTypePE propertyAssignment)
    {
        checkPropertyAssignment(session, propertyType, propertyAssignment);
    }

    private void checkPropertyType(Session session, PropertyTypePE propertyType)
    {
        if (propertyType.isManagedInternally() && isSystemUser(session) == false)
        {
            throw new AuthorizationFailureException("Internal property types can be managed only by the system user.");
        }
    }

    private void checkPropertyAssignment(Session session, PropertyTypePE propertyType, EntityTypePropertyTypePE propertyAssignment)
    {
        if (propertyType.isManagedInternally() && isSystemPropertyAssignment(propertyAssignment) && isSystemUser(session) == false)
        {
            throw new AuthorizationFailureException(
                    "Property assignments created by the system user for internal property types can be managed only by the system user.");
        }
    }

    private boolean isSystemPropertyAssignment(EntityTypePropertyTypePE propertyAssignment)
    {
        PersonPE registrator = propertyAssignment.getRegistrator();

        if (registrator == null)
        {
            throw new AuthorizationFailureException("Could not check access because the property assignment does not have any registrator assigned.");
        } else
        {
            return registrator.isSystemUser();
        }
    }

    private boolean isSystemUser(Session session)
    {
        PersonPE user = session.tryGetPerson();

        if (user == null)
        {
            throw new AuthorizationFailureException("Could not check access because the current session does not have any user assigned.");
        } else
        {
            return user.isSystemUser();
        }
    }

}
