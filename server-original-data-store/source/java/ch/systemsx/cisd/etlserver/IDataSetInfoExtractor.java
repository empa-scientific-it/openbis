/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A role to extract {@link DataSetInformation} from an incoming data set. Implementations of this interface are expected to have a constructor taking
 * a {@link java.util.Properties} object as their only argument. The properties can be used to get further arguments that the extractor implementation
 * requires to function.
 * <p>
 * The usage mode of implementations <var>extractorClassName</var>s is:
 * 
 * <pre>
 * Properties props = &lt;get some props from somewhere&gt;
 * Class clazz = Class.forName(extractorClassName);
 * IIDExtractor extractor = clazz.getConstructor(new Class[] { Properties.class } ).newInstance(new Object[] { props });
 * DataSetInformation info = extractor.getDataSetInformation(incomingDataSetPath);
 * String experimentCode = info.getExperimentCode();
 * String dataSetCode = info.getSampleCode();
 * </pre>
 * 
 * Implementations of this class are expected to be "re-usable". This is, calling the method
 * {@link #getDataSetInformation(File, IEncapsulatedOpenBISService)} multiple times for different data set on the same instance is expected to work.
 * 
 * @author Bernd Rinn
 */
public interface IDataSetInfoExtractor
{

    /** Properties key prefix for the extractor. */
    public static final String EXTRACTOR_KEY = "data-set-info-extractor";

    /**
     * Extracts data set information from the specified path of the incoming data set.
     * <p>
     * <i>Note that <code>incomingDataSetPath.getParent()</code> is arbitrary and the extracted id and code must not depend on it!</i>
     * </p>
     * 
     * @param incomingDataSetPath The path of the incoming data set. The path may be a file or directory. The caller needs to ensure that the path
     *            exists when this method is called.
     * @param openbisService the service which can communicate with openBIS server
     * @return The information extracted about this data set. The code extractor <i>can</i>, but <i>doesn't have to</i> provide an group. If no group
     *         has been provided by the extractor then the one specified for the thread in the <code>service.properties</code> file (if any) will be
     *         taken. Never returns <code>null</code>.
     * @throws UserFailureException If the incoming data set does not meet the expectations and thus the extractor can't extract either the experiment
     *             id or the data set code or both.
     */
    public DataSetInformation getDataSetInformation(final File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException;

}
