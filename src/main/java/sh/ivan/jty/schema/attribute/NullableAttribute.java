package sh.ivan.jty.schema.attribute;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class NullableAttribute implements Attribute {
    @Override
    public String yupMethod() {
        return "nullable()";
    }
}
