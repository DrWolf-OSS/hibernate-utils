package it.drwolf.base.model.entities;

/**
 *
 * Your entities must extend this abstract class in order to be used with DAOs
 * that extend BaseEntityDAO.<br>
 *
 * It also overrides default toString() in order to obtain a readable and
 * standard output among all entities.<br>
 * Output es: <i>User #42</i>
 *
 * @author spaladini
 *
 */
public abstract class BaseEntity {

	public abstract Object getId();

	@Override
	public String toString() {
		return String.format("%s #%s", this.getClass().getSimpleName(), this.getId());
	}

}
