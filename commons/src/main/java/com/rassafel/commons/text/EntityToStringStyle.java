/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rassafel.commons.text;

import java.io.Serial;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Custom ToStringStyle for entities that includes escaping special characters and handling null values.
 * <p>
 * String pattern:
 * $simpleClassName[$property1=$value1;...;$propertyN=$valueN]
 */
public class EntityToStringStyle extends ToStringStyle {
    public static final ToStringStyle ENTITY_STYLE = new EntityToStringStyle();
    @Serial
    private static final long serialVersionUID = 1L;

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
