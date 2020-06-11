package it.drwolf.base.model.entities;

public abstract class BaseEntity {

	public abstract Object getId();

	@Override
	public String toString() {
		return String.format("%s #%s", this.getClass().getSimpleName(), this.getId());
	}

}
