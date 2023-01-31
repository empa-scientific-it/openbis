/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.archiveverifier.verifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.IArchiveFileVerifier;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationError;

/**
 * Combines multiple verifiers to one.
 * 
 * @author anttil
 */
public class CompositeVerifier implements IArchiveFileVerifier
{

    private final List<IArchiveFileVerifier> verifiers;

    public CompositeVerifier(List<IArchiveFileVerifier> verifiers)
    {
        this.verifiers = verifiers;
    }

    @Override
    public List<VerificationError> verify(File file)
    {
        List<VerificationError> errors = new ArrayList<VerificationError>();
        for (IArchiveFileVerifier verifier : verifiers)
        {
            errors.addAll(verifier.verify(file));
        }
        return errors;
    }

}
