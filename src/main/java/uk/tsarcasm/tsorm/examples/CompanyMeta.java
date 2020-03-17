package uk.tsarcasm.tsorm.examples;

import uk.tsarcasm.tsorm.Store;
import uk.tsarcasm.tsorm.modulardbi.EntityMeta;
import uk.tsarcasm.tsorm.modulardbi.fields.DoubleField;
import uk.tsarcasm.tsorm.modulardbi.fields.IntField;
import uk.tsarcasm.tsorm.modulardbi.fields.StringField;

public class CompanyMeta extends EntityMeta<Company> {
    final Store<Car> carStore;

    public CompanyMeta(Store<Car> carStore) {
        this.carStore = carStore;
        addPk();
        addField("name", new StringField());
        addField("num_employees", new IntField());
        addField("net_worth", new DoubleField());
    }


    @Override
    protected void getValuesImpl(Company obj) {
        setValue("pk", obj.pk);
        setValue("name", obj.name);
        setValue("num_employees", obj.numEmployees);
        setValue("net_worth", obj.netWorth);
    }

    @Override
    protected Company instantiateImpl() {
        int pk = getValue("pk");
        return new Company(
                pk,
                getValue("name"),
                getValue("num_employees"),
                getValue("net_worth"),
                carStore.getAllWhere(c -> c.companyPk == pk)
        );
    }

    @Override
    public Company refreshRelations(Company obj) {
        return new Company(
                obj.pk,
                obj.name,
                obj.numEmployees,
                obj.netWorth,
                carStore.getAllWhere(c -> c.companyPk == obj.pk)
        );
    }
}
