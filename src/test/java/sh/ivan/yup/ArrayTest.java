package sh.ivan.yup;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.junit.jupiter.api.Test;
import sh.ivan.yup.schema.ObjectSchema;

class ArrayTest {
    Jsr380ToYupConverter converter = new Jsr380ToYupConverter();

    @Test
    void shouldHandleListProperty() {
        var schema = converter.buildSchema(Author.class);
        assertThat(schema).isInstanceOf(ObjectSchema.class);
        var objectSchema = (ObjectSchema) schema;
        assertThat(objectSchema.getFields()).hasSize(1);
        var booksSchema = objectSchema.getFields().get("books");
        assertThat(booksSchema.asYupSchema()).isEqualTo("array().of(string().required())");
    }

    static class Author {
        @NotNull
        public List<@NotEmpty String> books;
    }

    @Test
    void shouldHandleListPropertyAnnotatedOnlyOnGetter() {
        var schema = converter.buildSchema(GetterAuthor.class);
        assertThat(schema).isInstanceOf(ObjectSchema.class);
        var objectSchema = (ObjectSchema) schema;
        assertThat(objectSchema.getFields()).hasSize(1);
        var booksSchema = objectSchema.getFields().get("books");
        assertThat(booksSchema.asYupSchema()).isEqualTo("array().of(string().required())");
    }

    static class GetterAuthor {
        private List<String> books;

        @NotNull
        public List<@NotEmpty String> getBooks() {
            return books;
        }
    }

    @Test
    void shouldHandleArrayProperty() {
        var schema = converter.buildSchema(NotePad.class);
        assertThat(schema).isInstanceOf(ObjectSchema.class);
        var objectSchema = (ObjectSchema) schema;
        assertThat(objectSchema.getFields()).hasSize(1);
        var booksSchema = objectSchema.getFields().get("notes");
        assertThat(booksSchema.asYupSchema()).isEqualTo("array().of(string().nullable())");
    }

    static class NotePad {
        @NotNull
        public String[] notes;
    }

    @Test
    void shouldHandleNotEmptyList() {
        var schema = converter.buildSchema(PublishedAuthor.class);
        assertThat(schema).isInstanceOf(ObjectSchema.class);
        var objectSchema = (ObjectSchema) schema;
        assertThat(objectSchema.getFields()).hasSize(1);
        var booksSchema = objectSchema.getFields().get("books");
        assertThat(booksSchema.asYupSchema()).isEqualTo("array().of(string().nullable()).min(1)");
    }

    static class PublishedAuthor {
        @NotNull
        @NotEmpty
        public List<String> books;
    }
}
