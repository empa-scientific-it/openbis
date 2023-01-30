package ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity;

import ch.rinn.restrictions.Private;

/**
 * A class representing an experiment, project and space code. It contains also deletion id, code of experiment type and database instance id.
 */
@Private
public class ExperimentProjectSpaceCodeRecord
{
    public long id;

    public String e_code;

    public String e_permid;

    public String et_code;

    public String p_code;

    public String spc_code;

    public Long p_id;

    public String p_perm_id;

    public Long dbin_id;

    public Long del_id;

}
