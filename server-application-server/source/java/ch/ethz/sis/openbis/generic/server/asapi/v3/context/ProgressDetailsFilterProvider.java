package ch.ethz.sis.openbis.generic.server.asapi.v3.context;

import java.lang.reflect.Method;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;

/**
 * @author pkupczyk
 */
public class ProgressDetailsFilterProvider extends FilterProvider
{

    public static class ProgressDetailsFilter extends SimpleBeanPropertyFilter
    {
        @Override
        public void serializeAsField(Object bean, JsonGenerator jgen,
                SerializerProvider provider,
                BeanPropertyWriter writer) throws Exception
        {
            AnnotatedMember member = writer.getMember();

            if (member instanceof AnnotatedMethod)
            {
                Method method = ((AnnotatedMethod) member).getMember();

                if (FieldUpdateValue.class.isAssignableFrom(method.getReturnType()))
                {
                    serializeFieldUpdateValue(bean, jgen, provider, writer, method.invoke(bean));
                    return;
                } else if (ListUpdateValue.class.isAssignableFrom(method.getReturnType()))
                {
                    serializeListUpdateValue(bean, jgen, provider, writer, method.invoke(bean));
                    return;
                }
            } else if (member instanceof AnnotatedField)
            {
                if (FieldUpdateValue.class.isAssignableFrom(member.getRawType()))
                {
                    serializeFieldUpdateValue(bean, jgen, provider, writer, member.getValue(bean));
                    return;
                } else if (ListUpdateValue.class.isAssignableFrom(member.getRawType()))
                {
                    serializeListUpdateValue(bean, jgen, provider, writer, member.getValue(bean));
                    return;
                }
            }

            writer.serializeAsField(bean, jgen, provider);
        }

        private void serializeFieldUpdateValue(Object bean, JsonGenerator jgen, SerializerProvider provider, BeanPropertyWriter writer, Object value)
                throws Exception
        {
            if (value != null)
            {
                FieldUpdateValue<?> fieldUpdateValue = (FieldUpdateValue<?>) value;

                if (fieldUpdateValue.isModified())
                {
                    writer.serializeAsField(bean, jgen, provider);
                }
            }
        }

        private void serializeListUpdateValue(Object bean, JsonGenerator jgen, SerializerProvider provider, BeanPropertyWriter writer, Object value)
                throws Exception
        {
            if (value != null)
            {
                ListUpdateValue<?, ?, ?, ?> listUpdateValue = (ListUpdateValue<?, ?, ?, ?>) value;

                if (listUpdateValue.hasActions())
                {
                    writer.serializeAsField(bean, jgen, provider);
                }
            }
        }

    }

    @Override
    public BeanPropertyFilter findFilter(Object paramObject)
    {
        return new ProgressDetailsFilter();
    }

}
