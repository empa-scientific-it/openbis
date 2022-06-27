package ch.ethz.sis.openbis.generic.server.xls.importxls.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.ethz.sis.openbis.generic.server.xls.importxls.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.ImportUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.List;
import java.util.Map;

public class SpaceImportHelper extends BasicImportHelper
{
    private final ImportOptions options;

    private final DelayedExecutionDecorator delayedExecutor;

    public SpaceImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, ImportOptions options)
    {
        super(mode);
        this.options = options;
        this.delayedExecutor = delayedExecutor;
    }

    @Override protected String getTypeName()
    {
        return "space";
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, "code");
        final ISpaceId spaceId = new SpacePermId(ImportUtils.valueNormalizer("code", code, false));
        return delayedExecutor.getSpace(spaceId, new SpaceFetchOptions()) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = ImportUtils.valueNormalizer("code", getValueByColumnName(header, values, "code"), false);
        String description = getValueByColumnName(header, values, "description");

        if (options.getDisallowEntityCreations())
        {
            throw new UserFailureException("Entity creations disallowed but found at line: " + line + " [" + getTypeName() + "]");
        }

        SpaceCreation creation = new SpaceCreation();
        creation.setCode(code);
        creation.setDescription(description);

        delayedExecutor.createSpace(creation);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, "code");
        final ISpaceId spaceId = new SpacePermId(ImportUtils.valueNormalizer("code", code, false));
        String description = getValueByColumnName(header, values, "description");

        SpaceUpdate update = new SpaceUpdate();
        update.setSpaceId(spaceId);
        if (description != null)
        {
            update.setDescription(description);
        }

        delayedExecutor.updateSpace(update);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        checkKeyExistence(header, "code");
    }
}
