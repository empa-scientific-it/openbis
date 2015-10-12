package ch.systemsx.cisd.openbis.generic.shared.dto.hibernate;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

public class SortableNumberBridge implements TwoWayFieldBridge {
    @Override
    public void set(
            String name, 
            Object/* EntityPropertyPE */value,
            Document/* Lucene document */document, 
            LuceneOptions luceneOptions)
    {
        String fieldPrefix = name;
        String fieldValue = getNumberForLucene((Number) value);
        Field field = new Field(fieldPrefix, fieldValue, luceneOptions.getStore(), luceneOptions.getIndex());
        field.setBoost(luceneOptions.getBoost());
        document.add(field);
    }
    
    @Override
    public String objectToString(Object object) {
        return getNumberForLucene((Number) object);
    }
    
    @Override
    public Object get(String name, Document document)
    {
        return getObjectFromString(document.getField(name).stringValue());
    }
    
    //
    // Util Methods
    //
    private static int LUCENE_INTEGER_PADDING = 20; //On the UI a integer field can't have more than 18 characters
    
    public static Object getObjectFromString(String number) {
        try {
            return Long.parseLong(number);
        } catch(Exception ex) {
            try {
                return Double.parseDouble(number);
            } catch(Exception ex2) {
                return number;
            }
        }
    }
    
    public static String getNumberForLucene(String number) {
        try {
            return getNumberForLucene(Long.parseLong(number));
        } catch(Exception ex) {
            try {
                return getNumberForLucene(Double.parseDouble(number));
            } catch(Exception ex2) {
                return number.toString();
            }
        }
    }
    
    public static String getNumberForLucene(Number number) {
        if(number instanceof Integer || number instanceof Long) {
            return getIntegerAsStringForLucene(number);
        } if(number instanceof Float || number instanceof Double) {
            return getRealAsStringForLucene(number);
        } else {
            return number.toString();
        }
    }
    
    private static String getIntegerAsStringForLucene(Number number) {
        String rawInteger = number.toString();
        if (rawInteger.length() > LUCENE_INTEGER_PADDING) {
            throw new IllegalArgumentException( "Try to pad on a number too big, this shoudn't happen, on the UI a integer field can't have more than 18 characters." );
        }
        StringBuilder paddedInteger = new StringBuilder( );
        for ( int padIndex = rawInteger.length() ; padIndex < LUCENE_INTEGER_PADDING ; padIndex++ ) {
            paddedInteger.append('0');
        }
        return paddedInteger.append( rawInteger ).toString();
    }
    
    private static String getRealAsStringForLucene(Number number) {
        String rawReal = number.toString();
        int indexOfDot = rawReal.indexOf('.');
        return getNumberForLucene(rawReal.substring(0, indexOfDot)) + rawReal.substring(indexOfDot, rawReal.length());
    }
}