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

