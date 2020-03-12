package uk.tsarcasm.tsorm.modulardbi;

public class FieldValue<T> {
    private final T value;

    public FieldValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
