/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("DatasetOverlayImagesReference")
public class DatasetOverlayImagesReference extends DatasetImagesReference
{

    private static final long serialVersionUID = 1L;

    private String analysisProcedureOrNull;

    public String getAnalysisProcedureOrNull()
    {
        return analysisProcedureOrNull;
    }

    public void setAnalysisProcedureOrNull(String analysisProcedureOrNull)
    {
        this.analysisProcedureOrNull = analysisProcedureOrNull;
    }

}
