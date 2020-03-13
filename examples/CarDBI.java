import uk.tsarcasm.tsorm.modulardbi.ModularDbi;
import uk.tsarcasm.tsorm.modulardbi.fields.IntField;
import uk.tsarcasm.tsorm.modulardbi.fields.StringField;

import javax.sql.DataSource;

public class CarDBI extends ModularDbi<Car> {
    public CarDBI(DataSource dataSource, boolean canDelete) {
        super(dataSource, canDelete);
        addPk();
        addField("year", new IntField());
        addField("model", new StringField());
        addField("company_pk", new IntField());

        setupQueryStrings();
    }

    @Override
    protected Car instantiateSelect() {
        return new Car(
                getValue("pk"),
                getValue("year"),
                getValue("model"),
                getValue("company_pk")
        );
    }

    @Override
    protected Car instantiateInsert(int pk) {
        return new Car(
                pk,
                getValue("year"),
                getValue("model"),
                getValue("company_pk")
        );
    }

    @Override
    protected void entityToFieldValues(Car entity) {
        setValue("pk", entity.pk);
        setValue("year", entity.year);
        setValue("model", entity.model);
        setValue("company_pk", entity.companyPk);
    }

    @Override
    public Car refreshRelations(Car obj) {
        return obj;
    }
}
