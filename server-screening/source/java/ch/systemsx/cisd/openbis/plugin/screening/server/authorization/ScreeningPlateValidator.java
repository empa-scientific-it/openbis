package ch.systemsx.cisd.openbis.plugin.screening.server.authorization;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.AbstractValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;

/**
 * Filters only samples from the spaces to which the user has rights. This code works only in the case of one database instance.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Tomasz Pylak
 */
public final class ScreeningPlateValidator extends AbstractValidator<Plate>
{

    private PlateValidator plateValidator = new PlateValidator();

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        plateValidator.init(provider);
    }

    @Override
    public final boolean doValidation(final PersonPE person, final Plate value)
    {
        return plateValidator.doValidation(person, value);
    }
}