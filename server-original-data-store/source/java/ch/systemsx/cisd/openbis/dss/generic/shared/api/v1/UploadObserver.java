package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

public interface UploadObserver {
   	 abstract void updateTotalBytesRead(long totalBytesRead);
}
