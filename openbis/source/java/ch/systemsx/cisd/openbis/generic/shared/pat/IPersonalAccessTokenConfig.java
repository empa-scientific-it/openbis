package ch.systemsx.cisd.openbis.generic.shared.pat;

public interface IPersonalAccessTokenConfig
{

    public boolean arePersonalAccessTokensEnabled();

    public String getPersonalAccessTokensFilePath();

    public long getPersonalAccessTokensMaxValidityPeriod();

}
