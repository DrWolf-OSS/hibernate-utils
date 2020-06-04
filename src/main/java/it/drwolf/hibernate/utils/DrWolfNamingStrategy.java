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
		String name = String.format("%s_%s", tableName,
				columnNames.stream().map(Identifier::getText).collect(Collectors.joining("_")));

		if (name.length() > 60) {
			name = name.replaceAll("[aeiouyAEIOUY]", "");
		}

		name = String.format("%s_%s", prefix, name);

		if (name.length() > 63) {
			name = name.substring(0, 63);
		}

		return source.getBuildingContext()
				.getMetadataCollector()
				.getDatabase()
				.getJdbcEnvironment()
				.getIdentifierHelper()
				.toIdentifier(name);
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
