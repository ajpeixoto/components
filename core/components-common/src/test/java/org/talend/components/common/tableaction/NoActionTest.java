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
package org.talend.components.common.tableaction;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NoActionTest {

    @Test
    public void getQueriesTest(){
        TableAction ta = new NoAction();

        try {
            List<String> queries = ta.getQueries();
            assertEquals(0, queries.size());
        }
        catch (Exception e){
            assertTrue("No exception can be thrown by NoAction TableAction.", false);
        }

    }

}