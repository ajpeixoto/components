//============================================================================
//
// Copyright (C) 2006-2024 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//============================================================================
package org.talend.components.snowflake.runtime;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.snowflake.runtime.utils.SchemaResolver;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class SnowflakeReader extends AbstractBoundedReader<IndexedRecord> {

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider().getI18nMessages(SnowflakeReader.class);

    private transient Connection connection;

    private transient SnowflakeResultSetIndexedRecordConverter factory;

    private final transient Pattern identifierPattern;

    protected TSnowflakeInputProperties properties;

    protected int dataCount;

    private RuntimeContainer container;

    protected ResultSet resultSet;

    private transient Schema querySchema;

    private Statement statement;

    private Result result;

    public SnowflakeReader(RuntimeContainer container, BoundedSource source, TSnowflakeInputProperties props) {
        super(source);
        this.container = container;
        this.properties = props;
        factory = new SnowflakeResultSetIndexedRecordConverter();
        identifierPattern = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9$]*");
    }

    protected Connection getConnection() throws IOException {
        if (null == connection) {
            connection = ((SnowflakeSource) getCurrentSource()).createConnection(container);
        }
        return connection;
    }

    protected Schema getSchema() throws IOException {
        if (querySchema == null) {
            querySchema = getRuntimeSchema();
        }
        return querySchema;
    }

    protected String getQueryString() throws IOException {
        String condition = null;
        if (properties.manualQuery.getValue()) {
            return properties.getQuery();
        } else {
            condition = properties.condition.getStringValue();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("select "); //$NON-NLS-1$
        int count = 0;
        for (Schema.Field se : getSchema().getFields()) {
            if (count++ > 0) {
                sb.append(", "); //$NON-NLS-1$
            }
            String columnName = se.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            sb.append(useUnquotedIndentifier(columnName) ? columnName : StringUtils.wrap(columnName, '"'));
        }
        sb.append(" from "); //$NON-NLS-1$
        String tableName = useUnquotedIndentifier(properties.getTableName()) ? properties.getTableName() : StringUtils.wrap(properties.getTableName(), '"');
        sb.append(tableName);
        if (condition != null && condition.trim().length() > 0) {
            sb.append(" where ");
            sb.append(condition);
        }
        return sb.toString();
    }

    /**
     * Checks whether <b>value</b> has to be unquoted or quoted.
     * <ul><li>If property <b>convertColumnsAndTableToUppercase</b> is false we don't need to check other conditions, return <b>false</b></li>
     * <li>If <b>value</b> starts and ends with quotes \"...\" we shouldn't quote it again, return <b>true</b></li>
     * <li>Return <b>true</b>, if <b>value</b> conforms Snowflake restriction of Unquoted object identifiers: <ol>
     * <li>Start with a letter (A-Z, a-z) or an underscore (“_”).</li>
     * <li>Contain only letters, underscores, decimal digits (0-9), and dollar signs (“$”).</li>
     * <li>Are case-insensitive.</li></ol>
     * </li></ul>
     *
     * @param value
     * @return
     */
    private boolean useUnquotedIndentifier(String value) {
            return properties.convertColumnsAndTableToUppercase.getValue() &&
                    ((value.startsWith("\"") && value.endsWith("\""))
                            || identifierPattern.matcher(value).matches());
    }

    @Override
    public boolean start() throws IOException {
        result = new Result();
        try {
            statement = getConnection().createStatement();
            resultSet = statement.executeQuery(getQueryString());
            return haveNext();
        } catch (Exception e) {
            throw new IOException(i18nMessages.getMessage("error.processQuery", getQueryString()), e);

        }
    }

    private boolean haveNext() throws SQLException {
        boolean haveNext = resultSet.next();

        if (haveNext) {
            result.totalCount++;
        }

        return haveNext;
    }

    @Override
    public boolean advance() throws IOException {
        try {
            return haveNext();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        try {
            if (null == factory.getSchema()) {
                factory.setSchema(getSchema());
            }
            return factory.convertToAvro(resultSet);
        } catch (Exception e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (resultSet != null) {
                resultSet.close();
                resultSet = null;
            }

            if (statement != null) {
                statement.close();
                statement = null;
            }

            if (connection != null) {
                ((SnowflakeSource) getCurrentSource()).closeConnection(container, connection);
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return result.toMap();
    }

    private Schema getRuntimeSchema() throws IOException {
        final SnowflakeSourceOrSink source = (SnowflakeSourceOrSink) getCurrentSource();
        return source.getRuntimeSchema(new SchemaResolver() {

            @Override
            public Schema getSchema() throws IOException {
                try {
                    final boolean isUpperCase = properties.convertColumnsAndTableToUppercase.getValue();
                    String tableName = properties.getTableName();
                    if(isUpperCase && !properties.manualQuery.getValue() && tableName != null) {
                        tableName = tableName.toUpperCase();
                    }
                    return properties.manualQuery.getValue()
                            ? factory.getRegistry().inferSchema(resultSet.getMetaData())
                                    : source.getSchema(container, connection, tableName);
                } catch (SQLException e) {
                    throw new IOException(e);
                }
            }
        });
    }

}
