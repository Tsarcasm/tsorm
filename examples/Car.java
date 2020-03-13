import uk.tsarcasm.tsorm.Entity;

public class Car extends Entity {
    public final int year;
    public final String model;

    // Reference to the company that made the car
    public final int companyPk;



    public Car(int pk, int year, String model, int companyPk) {
        super(pk);
        this.year = year;
        this.model = model;
        this.companyPk = companyPk;
    }
}
