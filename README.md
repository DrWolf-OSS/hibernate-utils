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

BaseEntityDAO is an abstract DAO that provides basic functionalities to manage entities, in order to use it your
entities must extend BaseEntity and your DAOs must extend BaseEntityDAO.

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
public class UserDAO extends BaseEntityDAO<User> {

}
```

BaseEntity also overrides default toString() in order to obtain a readable and standard output among all entities.
Output example: `User #42`. Obviously this method can be overridden to obtain a better result.

Example:

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

## PaginatedData, PageParameter and OrderParameter

PaginatedData, PageParameter and OrderParameter are classes useful to standardise the management of paginated and sorted
data. All DAO's methods that involve paginated and order data should take PageParameter and OrderParameter objects and
return an appropriate instance of PaginatedData.

Example:

```java
public class UserDAO extends BaseEntityDAO<User> {

	public PaginatedData<User> getAll(EntityManager em, OrderParameter order, PageParameter page) {
		// retrieve users and return instance of PaginatedData<User>
	}

}
```

## FilterParameter

FilterParameter is meant to be an abstraction of a filter in an entity search. It has a type, a path, an operator and a
value:

- **type**: the type of the field to be filtered
- **path**: the root's related path of the field to be filtered
- **operator**: the operator used to compare *the value of the attribute* with *the value in the filter* (EQ, LIKE, IN,
  etc.. see `FilterOperator` enum for a complete list)
- **value**: the value to be compared

Es: if we have an `Order`class linked to `Customer` and `Address` classes a filter might looks like this:

```java
FilterParameter<String> addressFilter=new SingleAttributeFilter<>("customer.address.city",FilterOperator.LIKE,"Flor");
```

A set of FilterParameter is meant to be used in `BaseEntityDAO.search(...)` method. All filters are chained with the
boolean operator AND.

## QueryManager

QueryManager is a utility class that provides functionalities to build a javax.persistence.criteria.CriteriaQuery.

It can create and retrieve a `javax.persistence.criteria.Join` using its **path**, generate a
`javax.persistence.criteria.Order` from a OrderParameter and create a `javax.persistence.criteria.Predicate` from a
FilterParameter.
