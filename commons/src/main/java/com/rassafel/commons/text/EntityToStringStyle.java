package com.rassafel.commons.text;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;

/**
 * Custom ToStringStyle for entities that includes escaping special characters and handling null values.
 * <p>
 * String pattern:
 * $simpleClassName[$property1=$value1;...;$propertyN=$valueN]
 */
public class EntityToStringStyle extends ToStringStyle {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final ToStringStyle ENTITY_STYLE = new EntityToStringStyle();

    private EntityToStringStyle() {
        this.setUseIdentityHashCode(false);
        this.setFieldSeparator(";");
        this.setNullText("null");
        this.setUseShortClassName(true);
    }

    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, char value) {
        appendValueAsString(buffer, String.valueOf(value));
    }

    private void appendValueAsString(final StringBuffer buffer, final String value) {
        buffer.append('"').append(StringEscapeUtils.escapeJson(value)).append('"');
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final Object value) {
        if (value == null) {
            appendNullText(buffer, fieldName);
            return;
        }

        if (value instanceof CharSequence || value instanceof Character) {
            appendValueAsString(buffer, value.toString());
            return;
        }

        if (value instanceof Number || value instanceof Boolean) {
            buffer.append(value);
            return;
        }

        var valueAsString = value.toString();
        appendDetail(buffer, fieldName, valueAsString);
    }
}
