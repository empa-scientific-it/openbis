package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleChildrenInfo;

public class EntityDeletionConfirmationUtils
{
    
    //return information about selected samples child samples and data sets
    public static String getMessageForSingleSample(SampleChildrenInfo sampleChildrenInfo) {
        final int MAX_DISPLAY_SIZE = 10;

        StringBuffer additionalMessage = new StringBuffer();
        StringBuffer sampleSb = new StringBuffer();
        StringBuffer dataSetSb = new StringBuffer();
        
        for (String child : sampleChildrenInfo.getDerivedSamples())
        {
            sampleSb.append("<br>" + child);
        }
        for (String ds : sampleChildrenInfo.getDataSets())
        {
            dataSetSb.append("<br>" + ds);
        }

        if(sampleSb.length() > 0) {
            additionalMessage.append("<br>The sample has " +  sampleChildrenInfo.getChildCount() + " children samples, these relationships will be broken but the children will remain:");
            additionalMessage.append("<br>");
            additionalMessage.append(sampleSb);
            if(sampleChildrenInfo.getChildCount() > MAX_DISPLAY_SIZE ) {
                additionalMessage.append("<br> and " + (sampleChildrenInfo.getChildCount()-MAX_DISPLAY_SIZE) + " more");
            }
            additionalMessage.append("<br>");
        }
        if(dataSetSb.length() > 0) {
            additionalMessage.append("<br>The sample has " + sampleChildrenInfo.getDataSetCount() + " datasets, these will be deleted with the sample:");
            additionalMessage.append("<br>");
            additionalMessage.append(dataSetSb);
            if(sampleChildrenInfo.getDataSetCount() > MAX_DISPLAY_SIZE) {
                additionalMessage.append("<br>and " + (sampleChildrenInfo.getDataSetCount()-MAX_DISPLAY_SIZE) + " more");
            }
            additionalMessage.append("<br>");
        }        
        return additionalMessage.toString();
    }
    
    //return information about the number of samples with child samples and data sets
    //and a summary of those
    public static String getMessageForMultipleSamples(List<SampleChildrenInfo> sampleChildrenInfo, Map<String, String> techIdsToSampleIds) {
        StringBuffer additionalMessage = new StringBuffer();
        int samplesWithChildren = 0; 
        int samplesWithDataSets = 0; 
        final int MAX_DISPLAY_SIZE = 10;
        
        Set<String> samplesWithChildrenToDisplay = new HashSet<String>();
        Set<String> samplesWithDataSetsToDisplay = new HashSet<String>();
        
        for(SampleChildrenInfo info: sampleChildrenInfo) {
            if(info.getChildCount() > 0) {
                samplesWithChildren++;
                if(samplesWithChildren <= MAX_DISPLAY_SIZE) {
                    samplesWithChildrenToDisplay.add(techIdsToSampleIds.get(info.getSampleIdentifier()));
                }
            }
            if(info.getDataSetCount() > 0) {
                samplesWithDataSets++;
                if(samplesWithDataSets <= MAX_DISPLAY_SIZE) {
                    samplesWithDataSetsToDisplay.add(techIdsToSampleIds.get(info.getSampleIdentifier()));
                }
            }
        }
       if(samplesWithChildren > 0) {
          additionalMessage.append("<br>There are " + samplesWithChildren + " sample(s) with children samples, these relationships will be broken but the children will remain:<br>");
          for(String sample : samplesWithChildrenToDisplay) {
              additionalMessage.append("<br>" + sample);
          }
          if(samplesWithChildren > MAX_DISPLAY_SIZE) {
              additionalMessage.append("<br>and " + (samplesWithChildren-MAX_DISPLAY_SIZE) + " more");
          }
          additionalMessage.append("<br>");
        }
       if(samplesWithDataSets > 0) {
           additionalMessage.append("<br>There are " + samplesWithDataSets + " sample(s) with data sets, these will be deleted with the sample:<br>");
           for(String sample : samplesWithDataSetsToDisplay) {
               additionalMessage.append("<br>" + sample);
           }
           if(samplesWithDataSets > MAX_DISPLAY_SIZE) {
               additionalMessage.append("<br>and " + (samplesWithDataSets-MAX_DISPLAY_SIZE) + " more");
           }
           additionalMessage.append("<br>");
         }
       return additionalMessage.toString();
     }

}
