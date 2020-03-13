package uk.tsarcasm.examples;

import uk.tsarcasm.tsorm.Store;
import uk.tsarcasm.tsorm.modulardbi.ModularDbi;
import uk.tsarcasm.tsorm.modulardbi.fields.DoubleField;
import uk.tsarcasm.tsorm.modulardbi.fields.IntField;
import uk.tsarcasm.tsorm.modulardbi.fields.StringField;

import javax.sql.DataSource;
import java.util.ArrayList;

public class CompanyDBI extends ModularDbi<Company> {
    final Store<Car> carStore;
    public CompanyDBI(DataSource dataSource, boolean canDelete, Store<Car> carStore) {
        super(dataSource, canDelete);
        this.carStore = carStore;
        addPk();
        addField("name", new StringField());
        addField("num_employees", new IntField());
        addField("net_worth", new DoubleField());

        setupQueryStrings();
    }

    @Override
    protected Company instantiateSelect() {
        int pk = getValue("pk");
        return new Company(
                pk,
                getValue("name"),
                getValue("num_employees"),
                getValue("net_worth"),
                carStore.getAllWhere(c->c.companyPk == pk)
        );
    }

    @Override
    protected Company instantiateInsert(int pk) {
        return new Company(
                pk,
                getValue("name"),
                getValue("num_employees"),
                getValue("net_worth"),
                new ArrayList<>()
        );
    }

    @Override
    protected void entityToFieldValues(Company entity) {
        setValue("pk", entity.pk);
        setValue("name", entity.name);
        setValue("num_employees", entity.numEmployees);
        setValue("net_worth", entity.netWorth);
    }

    @Override
    public Company refreshRelations(Company obj) {
        return new Company(
                obj.pk,
                obj.name,
                obj.numEmployees,
                obj.netWorth,
                carStore.getAllWhere(c->c.companyPk == obj.pk)
        );
    }
}
