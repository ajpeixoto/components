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
package org.talend.components.jdbc;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class JdbcComponentErrorsCodeTest {

    JdbcComponentErrorsCode errosCode = new JdbcComponentErrorsCode("SQL_ERROR", 1, "info1", "info2");

    @Test
    public void testGetProduct() {
        Assert.assertEquals("TCOMP", errosCode.getProduct());
    }

    @Test
    public void testGetGroup() {
        Assert.assertEquals("JDBC", errosCode.getGroup());
    }

    @Test
    public void testGetHttpStatus() {
        Assert.assertEquals(1, errosCode.getHttpStatus());
    }

    @Test
    public void testGetExpectedContextEntries() {
        Assert.assertEquals(Arrays.asList("info1", "info2"), errosCode.getExpectedContextEntries());
    }

    @Test
    public void testGetCode() {
        Assert.assertEquals("SQL_ERROR", errosCode.getCode());
    }

}
