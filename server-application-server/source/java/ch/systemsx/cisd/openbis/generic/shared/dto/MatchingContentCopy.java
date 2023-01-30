package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.regex.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;

public class MatchingContentCopy implements IRelatedEntity
{

    private String externalCode;

    private String path;

    @SuppressWarnings("unused")
    private String gitCommitHash;

    @SuppressWarnings("unused")
    private String gitRepositoryId;

    @SuppressWarnings("unused")
    private String externalDmsCode;

    @SuppressWarnings("unused")
    private String externalDmsLabel;

    @SuppressWarnings("unused")
    private String externalDmsAddress;

    @SuppressWarnings("unused")
    private ExternalDataManagementSystemPE externalDms;

    public MatchingContentCopy(String externalCode, String path, String gitCommitHash, String gitRepositoryId,
            String externalDmsCode, String externalDmsLabel, String externalDmsAddress, ExternalDataManagementSystemPE externalDms)
    {
        this.externalCode = externalCode;
        this.path = path;
        this.gitCommitHash = gitCommitHash;
        this.gitRepositoryId = gitRepositoryId;
        this.externalDmsCode = externalDmsCode;
        this.externalDmsLabel = externalDmsLabel;
        this.externalDmsAddress = externalDmsAddress;
        this.externalDms = externalDms;
    }

    @Override
    public String toString()
    {
        if (externalDms.getAddressType().equals(ExternalDataManagementSystemType.FILE_SYSTEM))
        {
            return externalDmsAddress + path;
        } else
        {
            return externalDmsAddress.replaceAll(Pattern.quote("${") + ".*" + Pattern.quote("}"), externalCode);
        }
    }

}
