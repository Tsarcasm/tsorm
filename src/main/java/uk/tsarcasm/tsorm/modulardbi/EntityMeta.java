package uk.tsarcasm.tsorm.modulardbi;

import uk.tsarcasm.tsorm.Entity;
import uk.tsarcasm.tsorm.modulardbi.fields.IntField;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class EntityMeta<T extends Entity> {
    private final List<AbstractMap.SimpleEntry<String, Field<?>>> fields;
    private HashMap<String, FieldValue<?>> values;
    private final HashMap<String, FieldValue<?>> internalValues;

    protected EntityMeta() {
        this.fields = new ArrayList<>();
        this.internalValues = new HashMap<>();
    }

    protected void addPk() {
        fields.add(new AbstractMap.SimpleEntry<>("pk", new IntField()));
    }

    protected void addField(String name, Field<?> field) {
        fields.add(new AbstractMap.SimpleEntry<>(name, field));
    }

    public List<AbstractMap.SimpleEntry<String, Field<?>>> getFields() {
        return fields;
    }

    protected abstract void getValuesImpl(T obj);

    protected abstract T instantiateImpl();

    public final HashMap<String, FieldValue<?>> getValues(T obj) {
        this.values = internalValues;
        getValuesImpl(obj);
        return values;
    }

    public final T instantiate(HashMap<String, FieldValue<?>> values) {
        this.values = values;
        return instantiateImpl();
    }

    public abstract T refreshRelations(T obj);

    protected final <V> void setValue(String name, V value) {
        values.put(name, new FieldValue<>(value));
    }

    protected final <V> V getValue(String name) {
        @SuppressWarnings("unchecked") V value = (V) values.get(name).getValue();
        return value;
    }




}
