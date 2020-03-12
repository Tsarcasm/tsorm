package uk.tsarcasm.tsorm;

public abstract class Entity {
    public final int pk;

    public Entity(int pk) {
        this.pk = pk;
    }
}
