package org.hzero.generator.liquibase.metadata;

import org.hzero.generator.liquibase.metadata.dto.MetadataTable;

import java.sql.SQLException;
import java.util.Map;

public interface IMetadataDriver {
    Map<String, MetadataTable> selectTables() throws SQLException;
}
