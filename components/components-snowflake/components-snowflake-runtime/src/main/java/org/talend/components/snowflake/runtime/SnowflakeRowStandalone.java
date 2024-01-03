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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.runtime.utils.SnowflakePreparedStatementUtils;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

/**
 * This runtime class is responsible for executing query without propagating results.
 * It is created for performing action on {@link ConnectorTopology#NONE} topology without any links.
 */
public class SnowflakeRowStandalone extends SnowflakeRuntime implements ComponentDriverInitialization<ComponentProperties> {

    private static final long serialVersionUID = 2488046352863638670L;

    private transient static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeRowStandalone.class);

    private static final I18nMessages I18N_MESSAGES = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(SnowflakeRowStandalone.class);

    static final String NB_LINE = "NB_LINE";

    static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    private TSnowflakeRowProperties rowProperties;

    private RuntimeContainer container;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.rowProperties = (TSnowflakeRowProperties) properties;
        this.container = container;
        return ValidationResult.OK;
    }

    @Override
    public void runAtDriver(RuntimeContainer container) {

        Connection connection = null;
        try {
            connection = createConnection(container);
            ResultSet rs = null;
            if (rowProperties.usePreparedStatement()) {
                try (PreparedStatement pstmt = connection.prepareStatement(rowProperties.getQuery())) {
                    SnowflakePreparedStatementUtils.fillPreparedStatement(pstmt, rowProperties.preparedStatementTable);
                    rs = pstmt.executeQuery();
                    storeReturnedRows(rs);
                }
            } else {
                try (Statement statement = connection.createStatement()) {
                    boolean isResultSet = statement.execute(rowProperties.getQuery());
                    int updateCount;
                    if (isResultSet) {
                        storeReturnedRows(statement.getResultSet());
                    } else if ((updateCount = statement.getUpdateCount()) != -1) {
                        addNBLine(updateCount);
                    } else {
                        // it might be multi-statement
                        while (statement.getMoreResults() || (updateCount = statement.getUpdateCount()) != -1) {
                            if (updateCount == -1) {
                                storeReturnedRows(statement.getResultSet());
                            } else {
                                addNBLine(updateCount);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throwComponentException(e, "error.queryExecution");
        } catch (IOException ioe) {
            throwComponentException(ioe, "error.acquiringConnection");
        } finally {
            if (connection != null) {
                try {
                    closeConnection(container, connection);
                } catch (SQLException e) {
                    throw new ComponentException(e);
                }
            }
        }
    }

    private void storeReturnedRows(ResultSet rs) throws SQLException {
        int count = 0;
        if (rs != null) {
            while (rs.next()) {
                count++;
            }
        }
        addNBLine(count);
    }

    private void addNBLine(int count) {
        final int prev = Optional.ofNullable(container.getComponentData(container.getCurrentComponentId(), NB_LINE))
                .map(it -> (int) it)
                .orElse(0);
        container.setComponentData(container.getCurrentComponentId(), NB_LINE, count + prev);
    }

    private void throwComponentException(Exception ex, String messageProperty) {
        if (rowProperties.dieOnError.getValue()) {
            throw new ComponentException(ex);
        }
        LOGGER.error(I18N_MESSAGES.getMessage(messageProperty), ex);
        container.setComponentData(container.getCurrentComponentId(), ERROR_MESSAGE, ex.getMessage());
    }

    @Override
    public SnowflakeConnectionProperties getConnectionProperties() {
        return rowProperties.getConnectionProperties();
    }

}
