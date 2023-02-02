/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem;

/**
 * Common constants related to files.
 * 
 * @author Bernd Rinn
 */
public class FileConstants
{
    /** String that indicates a marker file */
    public static final String MARKER_PREFIX = ".MARKER_";

    /** The prefix of marker files that indicate that the processing of some path is finished. */
    public static final String IS_FINISHED_PREFIX = MARKER_PREFIX + "is_finished_";

    /** The prefix of marker files that indicate that the processing of some path is finished. */
    public static final String DELETION_IN_PROGRESS_PREFIX = MARKER_PREFIX
            + "deletion_in_progress_";

    /** The prefix of marker files that indicate that a directory is currently being processed. */
    public static final String PROCESSING_PREFIX = MARKER_PREFIX + "processing_";

    /** The file name of the file that contains file names which are known to be bad. */
    public static final String FAULTY_PATH_FILENAME = ".faulty_paths";

    private FileConstants()
    {
        // This class can not be instantiated.
    }

}
