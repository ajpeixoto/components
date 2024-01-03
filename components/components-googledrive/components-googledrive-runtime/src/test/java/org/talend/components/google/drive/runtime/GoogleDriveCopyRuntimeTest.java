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
package org.talend.components.google.drive.runtime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.talend.components.google.drive.runtime.GoogleDriveRuntime.getStudioName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.GoogleDriveMimeTypes;
import org.talend.components.google.drive.copy.GoogleDriveCopyDefinition;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties.CopyMode;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDriveCopyRuntimeTest extends GoogleDriveTestBaseRuntime {

    public static final String FILE_COPY_NAME = "fileName-copy-name";

    private GoogleDriveCopyProperties properties;

    private GoogleDriveCopyRuntime testRuntime;

    private String SOURCE_ID = "source-id";

    private String DESTINATION_ID = "destination-id";

    @Before
    public void setUp() throws Exception {
        super.setUp();

        testRuntime = spy(GoogleDriveCopyRuntime.class);
        doReturn(drive).when(testRuntime).getDriveService();

        properties = new GoogleDriveCopyProperties("test");
        properties.setupProperties();
        properties = (GoogleDriveCopyProperties) setupConnectionWithInstalledApplicationWithIdAndSecret(properties);
        //
        properties.copyMode.setValue(CopyMode.File);
        properties.source.setValue(FILE_COPY_NAME);
        properties.destinationFolder.setValue("/A");
        properties.newName.setValue("newName");
        // source fileName/folder
        File dest = new File();
        dest.setId(SOURCE_ID);
        FileList list = new FileList();
        List<File> files = new ArrayList<>();
        files.add(dest);
        list.setFiles(files);
        final String q1 = "name='A' and 'root' in parents and mimeType='application/vnd.google-apps.folder'";
        final String q2 = "name='fileName-copy-name' and mimeType!='application/vnd.google-apps.folder' and 'me' in owners";
        final String q3 = "name='A' and mimeType='application/vnd.google-apps.folder' and 'me' in owners";

        when(drive
                .files()
                .list()
                .setQ(eq(q1))
                .setSupportsAllDrives(false)
                .setIncludeItemsFromAllDrives(false)
                .execute()).thenReturn(list);
        when(drive
                .files()
                .list()
                .setQ(eq(q2))
                .setSupportsAllDrives(false)
                .setIncludeItemsFromAllDrives(false)
                .execute()).thenReturn(list);
        when(drive
                .files()
                .list()
                .setQ(eq(q3))
                .setSupportsAllDrives(false)
                .setIncludeItemsFromAllDrives(false)
                .execute()).thenReturn(list);

        // destination/copied
        File copiedFile = new File();
        copiedFile.setId(DESTINATION_ID);
        copiedFile.setParents(Collections.singletonList(SOURCE_ID));
        when(drive
                .files()
                .copy(anyString(), any(File.class))
                .setFields(anyString())
                .setSupportsAllDrives(false)
                .execute()).thenReturn(copiedFile);

        File destFolder = new File();
        destFolder.setId(DESTINATION_ID);
        destFolder.setParents(Collections.singletonList(SOURCE_ID));
        when(drive
                .files()
                .create(any(File.class))
                .setFields(anyString())
                .setSupportsAllDrives(false)
                .execute()).thenReturn(destFolder);
    }

    @Test
    public void testInitialize() throws Exception {
        ValidationResult vr = testRuntime.initialize(container, properties);
        assertEquals(Result.OK, vr.getStatus());
        assertEquals(Result.OK, testRuntime.validateCopyProperties(properties).getStatus());
    }

    @Test
    public void testExceptionThrown() throws Exception {
        when(drive
                .files()
                .copy(anyString(), any(File.class))
                .setFields(anyString())
                .setSupportsAllDrives(false)
                .execute())
                        .thenThrow(new IOException("error"));
        testRuntime.initialize(container, properties);
        try {
            testRuntime.runAtDriver(container);
            fail("Should not be here");
        } catch (Exception e) {
        }
    }

    @Test
    public void testRunAtDriverCopyFilePath() throws Exception {
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        assertEquals(SOURCE_ID,
                container.getComponentData(TEST_CONTAINER, getStudioName(GoogleDriveCopyDefinition.RETURN_SOURCE_ID)));
        assertEquals(DESTINATION_ID,
                container.getComponentData(TEST_CONTAINER, getStudioName(GoogleDriveCopyDefinition.RETURN_DESTINATION_ID)));
    }

    @Test
    public void testRunAtDriverCopyFileGlobalSearch() throws Exception {
        properties.destinationFolder.setValue("A");
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        assertEquals(SOURCE_ID,
                container.getComponentData(TEST_CONTAINER, getStudioName(GoogleDriveCopyDefinition.RETURN_SOURCE_ID)));
        assertEquals(DESTINATION_ID,
                container.getComponentData(TEST_CONTAINER, getStudioName(GoogleDriveCopyDefinition.RETURN_DESTINATION_ID)));
    }

    @Test
    public void testRunAtDriverCopyFolder() throws Exception {
        final String q1 = "name='folder' and 'root' in parents and mimeType='application/vnd.google-apps.folder'";
        final String q2 = "'source-id' in parents and trashed=false";
        final String q3 = "'folder-id2' in parents and trashed=false";
        //
        FileList fsource = new FileList();
        List<File> fsfiles = new ArrayList<>();
        File fsfolder = new File();
        fsfolder.setMimeType(GoogleDriveMimeTypes.MIME_TYPE_FOLDER);
        fsfolder.setName("folder");
        fsfolder.setId(SOURCE_ID);
        fsfiles.add(fsfolder);
        fsource.setFiles(fsfiles);
        when(drive
                .files()
                .list()
                .setQ(eq(q1))
                .setSupportsAllDrives(false)
                .setIncludeItemsFromAllDrives(false)
                .execute()).thenReturn(fsource);

        FileList flist = new FileList();
        List<File> ffiles = new ArrayList<>();
        File ffile = new File();
        ffile.setMimeType(GoogleDriveMimeTypes.MIME_TYPE_CSV);
        ffile.setName("fileName");
        ffile.setId("fileName-id");
        ffiles.add(ffile);
        File ffolder = new File();
        ffolder.setMimeType(GoogleDriveMimeTypes.MIME_TYPE_FOLDER);
        ffolder.setName("folder");
        ffolder.setId("folder-id2");
        ffiles.add(ffolder);
        flist.setFiles(ffiles);
        when(drive
                .files()
                .list()
                .setQ(eq(q2))
                .setSupportsAllDrives(false)
                .setIncludeItemsFromAllDrives(false)
                .execute()).thenReturn(flist);
        when(drive
                .files()
                .list()
                .setQ(eq(q3))
                .setSupportsAllDrives(false)
                .setIncludeItemsFromAllDrives(false)
                .execute()).thenReturn(emptyFileList);

        properties.copyMode.setValue(CopyMode.Folder);
        properties.source.setValue("/folder");
        properties.newName.setValue("");
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        assertEquals(SOURCE_ID,
                container.getComponentData(TEST_CONTAINER, getStudioName(GoogleDriveCopyDefinition.RETURN_SOURCE_ID)));
        assertEquals(DESTINATION_ID,
                container.getComponentData(TEST_CONTAINER, getStudioName(GoogleDriveCopyDefinition.RETURN_DESTINATION_ID)));
    }

}
