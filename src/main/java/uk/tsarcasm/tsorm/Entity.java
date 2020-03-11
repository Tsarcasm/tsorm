package uk.tsarcasm.tsorm;

public abstract class Entity {
    final int pk;

    public Entity(int pk) {
        this.pk = pk;
    }
}
