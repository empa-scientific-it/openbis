/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.yeastx.quant;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import ch.systemsx.cisd.yeastx.db.AbstractDatasetLoader;
import ch.systemsx.cisd.yeastx.db.DBUtils;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;
import ch.systemsx.cisd.yeastx.db.IGenericDAO;
import ch.systemsx.cisd.yeastx.quant.dto.ConcentrationCompounds;
import ch.systemsx.cisd.yeastx.quant.dto.MSConcentrationDTO;
import ch.systemsx.cisd.yeastx.quant.dto.MSQuantificationDTO;
import ch.systemsx.cisd.yeastx.quant.dto.MSQuantificationsDTO;
import ch.systemsx.cisd.yeastx.utils.JaxbXmlParser;

/**
 * Tool for uploading <code>quantML</code> files to the database.
 * 
 * @author Tomasz Pylak
 */
public class QuantML2Database extends AbstractDatasetLoader
{
    private final IQuantMSDAO dao;

    public QuantML2Database(DataSource datasource)
    {
        this.dao = DBUtils.getQuery(datasource, IQuantMSDAO.class);
    }

    /**
     * Method for uploading an <var>fiaMLFile</var> to the database.
     */
    public void upload(final File file, final DMDataSetDTO dataSet) throws SQLException
    {
        try
        {
            createDataSet(dataSet);
            MSQuantificationsDTO quantifications =
                    JaxbXmlParser.parse(MSQuantificationsDTO.class, file, false);
            uploadQuantifications(quantifications, dataSet);
        } catch (Throwable th)
        {
            rollbackAndRethrow(th);
        }
    }

    private void uploadQuantifications(MSQuantificationsDTO quantifications, DMDataSetDTO dataSet)
    {
        for (MSQuantificationDTO quantification : quantifications.getQuantifications())
        {
            long quantificationId =
                    dao.addQuantification(dataSet.getExperimentId(), dataSet.getId(),
                            quantification);
            uploadConcentrations(quantificationId, quantification.getConcentrations());
        }
    }

    private void uploadConcentrations(long quantificationId, List<MSConcentrationDTO> concentrations)
    {
        for (MSConcentrationDTO concentration : concentrations)
        {
            long concentrationId = dao.addConcentration(quantificationId, concentration);
            uploadCompoundIds(concentrationId, concentration.getCompounds());
        }
    }

    private void uploadCompoundIds(long concentrationId, ConcentrationCompounds compounds)
    {
        dao.addCompoundIds(concentrationId, compounds.getCompoundIds());
    }

    @Override
    protected IGenericDAO getDao()
    {
        return dao;
    }

}
