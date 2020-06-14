# DrWolf Hibernate Utils

Contains useful classes to work with hibernate

# Install 

Install via jitpack.io

https://jitpack.io/#drwolf-oss/hibernate-utils

## DrWolf Naming Strategy

Creates meaningful names for foreign keys and unique constraints

### Setup

Add to `persistence.xml`

```xml
<property name="hibernate.implicit_naming_strategy" value="it.drwolf.hibernate.utils.DrWolfNamingStrategy"/>
```
 
### Warning

It looks like there is a bug in hibernate for unique names, but there are some workarounds:

https://hibernate.atlassian.net/browse/HHH-12160?focusedCommentId=101110


## BaseEntity and BaseEntityDAO

BasicEntityDAO is an abstract DAO that provides basic funcionalities to manage entities, in order to use it your entities must extend BaseEntity and your DAOs must extend BasicEntityDAO.

Example:

```java
@Entity
public class User extends BaseEntity { 

	@Id
	private Long id;

	@Override
	public Long getId() {
		return this.id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
}
```

```java
public class UserDAO extends BasicEntityDAO<User> {

}
```

BaseEntity also overrides default toString() in order to obtain a readable and standard output among all entities.
Output example: `User #42`. 
Obviously this method can be overridden to obtain a better result.

Example

```java
@Entity
public class User extends BaseEntity { 

	@Id
	private Long id;
	
	private String username;

	@Override
	public Long getId() {
		return this.id;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public String toString() {
		return super.toString().concat(String.format(" (%s)", this.getUsername()));
	}
}
```
Output: `User #42 (admin)`.

## PaginatedData

PaginatedData is an utility class useful to standardise the management of paginated data. All DAO's methods that involve paginated data should return an appropriate instance of this class.

Example

```java
public class UserDAO extends BasicEntityDAO<User> {

	public PaginatedData<User> getAllPaginated(EntityManager em, String orderCol, OrderType orderType, int page, int size) {
		// retrieve users and return instance of PaginatedData<User>
	}

}
```

## Loggable interface

Loggable is an interface whit a default method that provides an instance of org.slf4j.Logger

Example:

```java
public class Importer implements Loggable {

	protected final Logger logger = this.getLogger();
	
	public void process() {
		this.logger.info("Start import process...");
	}
	
}

```

