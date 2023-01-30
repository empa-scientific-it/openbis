package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

public interface IContentCopy extends Serializable
{

    String getExternalDMSCode();

    String getExternalDMSLabel();

    String getExternalDMSAddress();

    String getPath();

    String getCommitHash();

    String getRespitoryId();

    String getExternalCode();

    String getLocation();
}
