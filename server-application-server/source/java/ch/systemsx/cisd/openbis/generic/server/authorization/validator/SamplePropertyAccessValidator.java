package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

public class SamplePropertyAccessValidator implements IValidator<IIdentifierHolder>
{
    private PersonPE user;

    private SampleByIdentiferValidator validator;

    public SamplePropertyAccessValidator(Session session, IAuthorizationDAOFactory daoFactory)
    {
        user = session.tryGetPerson();
        validator = new SampleByIdentiferValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));
    }

    @Override
    public boolean isValid(IIdentifierHolder identifierHolder)
    {
        return validator.isValid(user, identifierHolder);
    }

}
