package sh.ivan.jty.schema;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import sh.ivan.jty.schema.attribute.Attribute;

import java.util.Set;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class StringSchema extends Schema {
    public StringSchema(Set<Attribute> attributes) {
        super(attributes);
    }

    @Override
    public String yupType() {
        return "string()";
    }
}
