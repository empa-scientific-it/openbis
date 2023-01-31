package ch.systemsx.cisd.openbis.dss.generic.shared;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Returns <code>true</code> if speed of specified share and speed hint of specified data set are allowed.
 *
 * @author Franz-Josef Elmer
 */
public interface ISpeedChecker
{
    public boolean check(SimpleDataSetInformationDTO dataSet, Share share);
}