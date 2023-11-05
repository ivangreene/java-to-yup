package sh.ivan.yup;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import sh.ivan.yup.schema.attribute.Attribute;
import sh.ivan.yup.schema.attribute.NullableAttribute;
import sh.ivan.yup.schema.attribute.RequiredAttribute;
import sh.ivan.yup.schema.attribute.SizeAttribute;

public class AttributeProcessor {
    private static final Set<Class<? extends Annotation>> JSR_380_ANNOTATIONS = Set.of(
            AssertFalse.class,
            AssertTrue.class,
            DecimalMax.class,
            DecimalMin.class,
            Digits.class,
            Email.class,
            Future.class,
            FutureOrPresent.class,
            Max.class,
            Min.class,
            Negative.class,
            NegativeOrZero.class,
            NotBlank.class,
            NotEmpty.class,
            NotNull.class,
            Null.class,
            Past.class,
            PastOrPresent.class,
            Pattern.class,
            Positive.class,
            PositiveOrZero.class,
            Size.class);

    private static final Set<Class<? extends Annotation>> NOT_NULL_ANNOTATIONS =
            Set.of(NotBlank.class, NotEmpty.class, NotNull.class);

    public Set<Attribute> getAttributes(Class<?> container, Type type, Member member, String propertyName) {
        var attributes = new HashSet<Attribute>();
        Field field = null;
        try {
            field = container.getDeclaredField(propertyName);
        } catch (NoSuchFieldException ignored) {
        }
        if (isNullable(field) && isNullable(member)) {
            attributes.add(new NullableAttribute());
        }
        var annotations = new HashSet<Annotation>();
        if (field != null) {
            annotations.addAll(Set.of(field.getAnnotations()));
        }
        if (member instanceof AnnotatedElement) {
            annotations.addAll(Set.of(((AnnotatedElement) member).getAnnotations()));
        }
        attributes.addAll(processAnnotations(type, annotations));
        return Set.copyOf(attributes);
    }

    private boolean isNullable(Member member) {
        if (member instanceof AnnotatedElement) {
            return Stream.of(((AnnotatedElement) member).getAnnotations())
                    .map(Annotation::annotationType)
                    .noneMatch(NOT_NULL_ANNOTATIONS::contains);
        }
        return true;
    }

    private Set<Attribute> processAnnotations(Type type, Set<Annotation> annotations) {
        return annotations.stream()
                .filter(annotation -> JSR_380_ANNOTATIONS.contains(annotation.annotationType()))
                .map(annotation -> processAnnotation(type, annotation))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Attribute processAnnotation(Type type, Annotation annotation) {
        if (annotation.annotationType() == Size.class) {
            return new SizeAttribute(((Size) annotation).min(), ((Size) annotation).max());
        }
        if (annotation.annotationType() == NotEmpty.class) {
            if (type == String.class) {
                return new RequiredAttribute();
            }
        }
        return null;
    }
}
