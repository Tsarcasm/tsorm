package uk.tsarcasm.tsorm.examples;

import uk.tsarcasm.tsorm.modulardbi.EntityMeta;
import uk.tsarcasm.tsorm.modulardbi.fields.IntField;
import uk.tsarcasm.tsorm.modulardbi.fields.StringField;

public class CarMeta extends EntityMeta<Car> {
    public CarMeta() {
        addPk();
        addField("year", new IntField());
        addField("model", new StringField());
        addField("company_pk", new IntField());
    }

    @Override
    protected void getValuesImpl(Car entity) {
        setValue("pk", entity.pk);
        setValue("year", entity.year);
        setValue("model", entity.model);
        setValue("company_pk", entity.companyPk);
    }

    @Override
    protected Car instantiateImpl() {
        return new Car(
                getValue("pk"),
                getValue("year"),
                getValue("model"),
                getValue("company_pk")
        );
    }

    @Override
    public Car refreshRelations(Car obj) {
        return obj;
    }
}
