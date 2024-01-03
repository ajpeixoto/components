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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class AzureStorageUtilsTest {

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider().getI18nMessages(AzureStorageUtils.class);

    private AzureStorageUtils azureStorageUtils;

    String remotedir = "remote-azure";

    String localdir = "azure";

    String keyparent = "parent";

    String folder;

    String TEST_FOLDER_PUT = "azurestorage-put";

    @Before
    public void setUp() throws Exception {
        azureStorageUtils = new AzureStorageUtils();
        folder = getClass().getClassLoader().getResource("azurestorage-put").getPath();
    }

    /**
     *
     * @see org.talend.components.azurestorage.utils.AzureStorageUtils#genAzureObjectList(File,String)
     */
    @Test
    public void testGenAzureObjectList() {
        File file = new File(folder);
        Map<String, String> result = azureStorageUtils.genAzureObjectList(file, keyparent);
        assertNotNull("result cannot be null", result);

    }

    @Test
    public void testIlleagueArguementException() {
        try {
            File file = new File(folder + "/blob1.txt");
            azureStorageUtils.genAzureObjectList(file, keyparent);
        } catch (IllegalArgumentException ilae) {
            assertEquals(i18nMessages.getMessage("error.invalidDirectory"), ilae.getMessage());
        }
    }

    @Test
    public void genFileFilterListEscapedPlusCan() {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> myMap = new HashMap<>();
        myMap.put("blob\\+.txt", "b");
        list.add(myMap);
        Map<String, String> result = azureStorageUtils.genFileFilterList(list, folder, remotedir, true);
        assertNotNull("result cannot be null", result);
        assertEquals(1, result.size());
        assertTrue(result.keySet().stream().anyMatch(e -> e.endsWith("blob+.txt")));
    }

    @Test
    public void genFileFilterListEscapedPlusCanNot() {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> myMap = new HashMap<>();
        myMap.put("blob\\+.txt", "b");
        list.add(myMap);
        Map<String, String> result = azureStorageUtils.genFileFilterList(list, folder, remotedir, false);
        assertNotNull("result cannot be null", result);
        assertEquals(0, result.size());
    }

}
