import uk.tsarcasm.tsorm.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Company extends Entity {
    public final String name;
    public final int numEmployees;
    public final double netWorth;

    public final Collection<Car> cars;


    public Company(int pk, String name, int numEmployees, double netWorth, Collection<Car> cars) {
        super(pk);
        this.name = name;
        this.numEmployees = numEmployees;
        this.netWorth = netWorth;
        this.cars = cars;
    }

    public Collection<Car> getCarsSinceYear(int year) {
        List<Car> carsSince = new ArrayList<>();
        for (Car car : cars) {
            if (car.year >= year) {
                carsSince.add(car);
            }
        }
        return carsSince;
    }

}
