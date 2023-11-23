package sh.ivan.yup.schema;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import sh.ivan.yup.schema.attribute.Attribute;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ObjectSchema extends Schema {
    private final Map<String, Schema> fields;

    public ObjectSchema(Map<String, Schema> fields, Set<Attribute> attributes) {
        super(attributes);
        this.fields = Collections.unmodifiableMap(fields);
    }

    @Override
    protected String yupType(String prefix) {
        var stringBuilder = new StringBuilder(prefix).append("object({ ");
        fields.forEach((name, schema) -> stringBuilder
                .append(name)
                .append(": ")
                .append(schema.asYupSchema(prefix))
                .append(", "));
        return stringBuilder.append("})").toString();
    }

    @Override
    protected String yupType() {
        throw new UnsupportedOperationException("Implements yupType(String prefix) directly");
    }
}
