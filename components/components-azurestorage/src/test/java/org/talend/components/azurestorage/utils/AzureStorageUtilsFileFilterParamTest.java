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
package org.talend.components.azurestorage.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AzureStorageUtilsFileFilterParamTest {

    private AzureStorageUtils azureStorageUtils;

    private String remotedir = "remote-azure";

    private String folder;

    private final boolean isPlusCanBeProtected;

    public AzureStorageUtilsFileFilterParamTest(boolean isPlusCanBeProtected) {
        this.isPlusCanBeProtected = isPlusCanBeProtected;
    }

    @Before
    public void setUp() throws Exception {
        azureStorageUtils = new AzureStorageUtils();
        folder = getClass().getClassLoader().getResource("azurestorage-put").getPath();
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {true}, {false}
        });
    }

    /**
     *
     * @see org.talend.components.azurestorage.utils.AzureStorageUtils#genFileFilterList(List,String,String, boolean)
     */
    @Test
    public void genFileFilterList() {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> myMap = new HashMap<>();
        myMap.put("*.txt", "b");
        myMap.put("*", "d");
        myMap.put("c", "d");
        list.add(myMap);
        Map<String, String> result = azureStorageUtils.genFileFilterList(list, folder, remotedir, isPlusCanBeProtected);
        assertNotNull("result cannot be null", result);
    }

    @Test
    public void genFileFilterListPlusAsSymbol() {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> myMap = new HashMap<>();
        myMap.put("blob+.txt", "b");
        list.add(myMap);
        Map<String, String> result = azureStorageUtils.genFileFilterList(list, folder, remotedir, isPlusCanBeProtected);
        assertNotNull("result cannot be null", result);
        assertEquals(1, result.size());
        assertTrue(result.keySet().stream().anyMatch(e -> e.endsWith("blobbb.txt")));
    }

    @Test
    public void genFileFilterListInternalFolders() {
        String folder = getClass().getClassLoader().getResource("azurestorage-put2").getPath();

        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> myMap = new HashMap<>();
        myMap.put("sub1\\*.txt", "b");
        myMap.put("deepSub4\\sub4\\*.txt", "b");
        list.add(myMap);
        Map<String, String> result = azureStorageUtils.genFileFilterList(list, folder, remotedir, isPlusCanBeProtected);
        assertNotNull("result cannot be null", result);
        assertEquals(6, result.size());
    }

    @Test
    public void genFileFilterListFolder() {
        String folder = getClass().getClassLoader().getResource("azurestorage-put2").getPath();

        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> myMap = new HashMap<>();
        myMap.put("sub2\\*", "b");
        list.add(myMap);
        Map<String, String> result = azureStorageUtils.genFileFilterList(list, folder, remotedir, isPlusCanBeProtected);
        assertNotNull("result cannot be null", result);
        assertEquals(3, result.size());
    }
}
