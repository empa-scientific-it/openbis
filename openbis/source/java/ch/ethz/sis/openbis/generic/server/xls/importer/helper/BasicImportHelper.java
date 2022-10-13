package ch.ethz.sis.openbis.generic.server.xls.importer.helper;

import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importer.XLSImport;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public abstract class BasicImportHelper extends AbstractImportHelper
{
    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, XLSImport.class);

    protected final ImportModes mode;

    protected final ImportOptions options;

    public BasicImportHelper(ImportModes mode, ImportOptions options)
    {
        this.mode = mode;
        this.options = options;
    }

    protected abstract ImportTypes getTypeName();

    protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        return true;
    }

    protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        // do nothing
    }

    protected abstract boolean isObjectExist(Map<String, Integer> header, List<String> values);

    protected abstract void createObject(Map<String, Integer> header, List<String> values, int page, int line);

    protected abstract void updateObject(Map<String, Integer> header, List<String> values, int page, int line);

    public void importBlock(List<List<String>> page, int pageIndex, int start, int end)
    {
        int lineIndex = start;

        try
        {
            Map<String, Integer> header = parseHeader(page.get(lineIndex), true);
            lineIndex++;

            while (lineIndex < end)
            {
                validateLine(header, page.get(lineIndex));
                if (isNewVersion(header, page.get(lineIndex)))
                {
                    if (!isObjectExist(header, page.get(lineIndex)))
                    {
                        if (options.getDisallowEntityCreations() && getTypeName().isMetadata())
                        {
                            throw new UserFailureException("Entity creations disallowed but found with block type: " + getTypeName());
                        }
                        createObject(header, page.get(lineIndex), pageIndex, lineIndex);
                        updateVersion(header, page.get(lineIndex));
                    } else
                    {
                        switch (mode)
                        {
                            case FAIL_IF_EXISTS:
                                throw new UserFailureException("Mode FAIL_IF_EXISTS - Found existing " + getTypeName());
                            case UPDATE_IF_EXISTS:
                                updateObject(header, page.get(lineIndex), pageIndex, lineIndex);
                                updateVersion(header, page.get(lineIndex));
                                break;
                            case IGNORE_EXISTING:
                                // do nothing
                                break;
                            default:
                                throw new UserFailureException("Unknown mode");
                        }
                    }
                }

                lineIndex++;
            }

        } catch (Exception e)
        {
            UserFailureException userFailureException = new UserFailureException(
                    "sheet: " + (pageIndex + 1) + " line: " + (lineIndex + 1) + " message: " + e.getMessage());
            userFailureException.setStackTrace(e.getStackTrace());
            throw userFailureException;
        }
    }
}
