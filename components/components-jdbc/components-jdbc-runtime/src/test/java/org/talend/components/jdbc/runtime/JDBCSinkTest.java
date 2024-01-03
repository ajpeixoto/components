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
package org.talend.components.jdbc.runtime;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties;
import org.talend.daikon.properties.ValidationResult;

public class JDBCSinkTest {

    private JDBCSink sink;
    private TJDBCOutputProperties properties;

    @Before
    public void setUp() {
        properties = new TJDBCOutputProperties("properties");
        properties.setupProperties();
        properties.connection.jdbcUrl.setValue("jdbc://someJDBCUrl");
        properties.connection.driverClass.setValue("java.lang.Object");

        sink = Mockito.spy(JDBCSink.class);
    }
    @Test
    public void validateForNotEmptyTableName() throws SQLException, ClassNotFoundException {
        Mockito.doReturn(Mockito.mock(Connection.class)).when(sink).connect(Mockito.any());
        properties.tableSelection.tablename.setValue("someTableName");

        sink.initialize(null, properties);
        ValidationResult result = sink.validate(null);

        Assert.assertEquals(ValidationResult.Result.OK, result.getStatus());
    }

    @Test
    public void validateForEmptyTableName() {
        properties.tableSelection.tablename.setValue("");

        sink.initialize(null, properties);
        ValidationResult result = sink.validate(null);

        Assert.assertEquals(ValidationResult.Result.ERROR, result.getStatus());
    }

    @Test
    public void validateForNullTableName() {
        properties.tableSelection.tablename.setValue(null);

        sink.initialize(null, properties);
        ValidationResult result = sink.validate(null);

        Assert.assertEquals(ValidationResult.Result.ERROR, result.getStatus());
    }
}