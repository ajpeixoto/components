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

import static java.lang.String.format;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MIME_TYPE_FOLDER;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.QUERY_MIME_FOLDER;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.Q_AND;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.Q_IN_PARENTS;
import static org.talend.components.google.drive.runtime.GoogleDriveConstants.Q_NOT_TRASHED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.GoogleDriveComponentProperties.Corpora;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public abstract class GoogleDriveAbstractListReader extends AbstractBoundedReader<IndexedRecord> {

    public static final String FIELDS_SELECTION =
            "files(id,name,mimeType,modifiedTime,kind,size,parents,trashed,webViewLink),nextPageToken";

    protected final RuntimeContainer container;

    protected Drive drive;

    protected GoogleDriveUtils utils;

    protected Result result;

    protected Schema schema;

    protected IndexedRecord record;

    protected List<File> searchResults;

    protected int searchIdx;

    protected int searchCount;

    protected AccessMethod folderAccessMethod;

    protected boolean includeSubDirectories;

    protected boolean includeTrashedFiles;

    protected boolean includeSharedItems;

    protected boolean includeSharedDrives;

    protected boolean useQuery;

    protected String customQuery;

    protected Corpora corpora = Corpora.USER;

    protected String driveId;

    protected String listModeStr;

    protected String folderName;

    protected Files.List request;

    protected String query;

    protected String folderId;

    protected List<String> subFolders;

    protected int maxPageSize = 1000;

    protected static final Logger LOG = LoggerFactory.getLogger(GoogleDriveListReader.class);

    private static final I18nMessages messages = GlobalI18N
            .getI18nMessageProvider()
            .getI18nMessages(GoogleDriveListReader.class);

    protected GoogleDriveAbstractListReader(RuntimeContainer container, BoundedSource source) {
        super(source);
        this.container = container;
        result = new Result();
        subFolders = new ArrayList<>();
        searchResults = new ArrayList<>();
        folderAccessMethod = AccessMethod.Id;
    }

    @Override
    public boolean start() throws IOException {
        request = drive
                .files()
                .list()
                .setSupportsAllDrives(includeSharedDrives)
                .setIncludeItemsFromAllDrives(includeSharedDrives);
        // Corpora and drive ID are only used in queries for shared drives.
        if (includeSharedDrives) {
            request.setCorpora(corpora.getCorporaName());
            if (corpora == Corpora.DRIVE) {
                request.setDriveId(driveId);
            }
        }
        request.setFields(FIELDS_SELECTION);
        request.setPageSize(maxPageSize);
        LOG.debug("[start] request: {}.", request);

        if (useQuery) {
            LOG.debug("[start] (query = [{}], includeSharedDrives = [{}]).", customQuery, includeSharedDrives);
            request.setQ(customQuery);
            return processQuery();
        }
        /* build query string */
        query = new StringBuilder(Q_IN_PARENTS)
                .append("DIRECTORIES".equals(listModeStr) ? QUERY_MIME_FOLDER : "")
                .append(includeTrashedFiles ? "" : Q_AND + Q_NOT_TRASHED)
                .toString();
        if (folderAccessMethod.equals(AccessMethod.Id)) {
            subFolders.add(folderName);
        } else {
            subFolders = utils.getFolderIds(folderName, includeTrashedFiles, includeSharedItems);
        }
        LOG.debug("[start] subFolders = {}.", subFolders);
        if (subFolders.size() == 0) {
            LOG.warn(messages.getMessage("error.folder.inexistant", folderName));
            return false;
        }
        if (subFolders.size() > 1 && !includeSharedDrives) {
            // Keep previous behavior, avoid printing this message only if not sharedWithMe.
            LOG.warn(messages.getMessage("error.folder.more.than.one", folderName));
        }
        return processFolder();
    }

    @Override
    public boolean advance() throws IOException {
        return hasNext();
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        record = convertSearchResultToIndexedRecord(searchResults.get(searchIdx));
        result.successCount++;

        return record;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return result.toMap();
    }

    private boolean hasNext() throws IOException {
        boolean next = (searchIdx + 1) < searchCount;
        if (next) {
            searchIdx++;
        } else {
            searchIdx = 0;
            searchResults.clear(); // moved from processFolder.
            if (useQuery) {
                return !hasNoMorePages() && processQuery();
            }
            while (!next && (hasMoreSubFolders() || (folderId != null))) {
                next = processFolder();
            }
        }

        return next;
    }

    private boolean canAddSubFolder(String mimeType) {
        return MIME_TYPE_FOLDER.equals(mimeType);
    }

    private boolean canAddFile(String mimeType) {
        return "BOTH".equals(listModeStr) || ("FILES".equals(listModeStr) && !MIME_TYPE_FOLDER.equals(mimeType))
                || ("DIRECTORIES".equals(listModeStr) && MIME_TYPE_FOLDER.equals(mimeType));
    }

    private boolean processFolder() throws IOException {
        if (folderId == null && hasMoreSubFolders()) {
            folderId = subFolders.remove(0);
            request.setQ(format(query, folderId));
            LOG.debug("[processFolder] query = {}.", format(query, folderId));
        }
        executeAndProcessQueryResults(includeSubDirectories);
        // finished for folderId
        if (hasNoMorePages() || searchCount == 0) {
            folderId = null;
            // If requested folder is empty on start() method call, but there are more sub folders left.
            if (hasMoreSubFolders()) {
                return processFolder();
            }
        }

        return searchCount > 0;
    }

    private boolean processQuery() throws IOException {
        executeAndProcessQueryResults(false);
        return searchCount > 0;
    }

    private void executeAndProcessQueryResults(boolean includeSubfolders) throws IOException {
        FileList files = request.execute();
        for (File file : files.getFiles()) {
            if (includeSubfolders && canAddSubFolder(file.getMimeType())) {
                subFolders.add(file.getId());
            }
            if (canAddFile(file.getMimeType())) {
                searchResults.add(file);
                result.totalCount++;
            }
        }
        request.setPageToken(files.getNextPageToken());
        searchCount = searchResults.size();
    }

    private boolean hasMoreSubFolders() {
        return !subFolders.isEmpty();
    }

    private boolean hasNoMorePages() {
        return StringUtils.isEmpty(request.getPageToken());
    }

    private IndexedRecord convertSearchResultToIndexedRecord(File file) {
        // Main record
        IndexedRecord main = new GenericData.Record(schema);
        main.put(0, file.getId());
        main.put(1, file.getName());
        main.put(2, file.getMimeType());
        main.put(3, file.getModifiedTime().getValue());
        main.put(4, file.getSize());
        main.put(5, file.getKind());
        main.put(6, file.getTrashed());
        if (file.getParents() != null) {
            main.put(7, file.getParents().toString()); // TODO This should be a List<String>
        }
        main.put(8, file.getWebViewLink());

        return main;
    }

}
