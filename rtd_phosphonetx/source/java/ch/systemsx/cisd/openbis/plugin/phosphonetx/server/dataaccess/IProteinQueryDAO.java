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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.Select;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProbabilityFDRMapping;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReference;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IProteinQueryDAO extends BaseQuery
{
    @Select("select * from probability_fdr_mappings where dase_id = ?{1}")
    public DataSet<ProbabilityFDRMapping> getProbabilityFDRMapping(long dataSetID);
    
    @Select("select distinct pr.id, pr.uniprot_id, pr.description "
            + "from identified_proteins as ip left join proteins as p on ip.prot_id = p.id "
            + "left join data_sets as d on p.dase_id = d.id "
            + "left join experiments as e on d.expe_id = e.id, " 
            + "sequences as s left join protein_references as pr on s.prre_id = pr.id "
            + "where e.perm_id = ?{1} and ip.sequ_id = s.id")
    public DataSet<ProteinReference> listProteinsByExperiment(String experimentPermID);
}
