package it.drwolf.hibernate.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitForeignKeyNameSource;
import org.hibernate.boot.model.naming.ImplicitNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.naming.ImplicitUniqueKeyNameSource;

public class DrWolfNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {
	private static final long serialVersionUID = 9028918904905810792L;

	private Identifier buildIdentifier(String prefix, String tableName, List<Identifier> columnNames,
			ImplicitNameSource source) {
		return source.getBuildingContext()
				.getMetadataCollector()
				.getDatabase()
				.getJdbcEnvironment()
				.getIdentifierHelper()
				.toIdentifier(String.format("%s_%s_%s", "fk", tableName,
						columnNames.stream().map(Identifier::getText).collect(Collectors.joining("_"))));
	}

	@Override
	public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
		return this.buildIdentifier("fk", source.getTableName().getText(), source.getColumnNames(), source);
	}

	@Override
	public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
		return this.buildIdentifier("uk", source.getTableName().getText(), source.getColumnNames(), source);
	}
}
