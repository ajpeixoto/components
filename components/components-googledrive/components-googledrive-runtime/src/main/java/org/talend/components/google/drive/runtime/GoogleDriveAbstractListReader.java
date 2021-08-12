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

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

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
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public abstract class GoogleDriveAbstractListReader extends AbstractBoundedReader<IndexedRecord> {

    public static final String FIELDS_SELECTION = "files(id,name,mimeType,modifiedTime,kind,size,parents,trashed,webViewLink),nextPageToken";

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

    protected boolean includeSharedWithMe;

    protected boolean includeSharedDrives;

    protected boolean useCustomQuery;

    protected String customQuery;

    protected String listModeStr;

    protected String folderName;

    protected Files.List request;

    protected String query;

    protected String folderId;

    protected List<String> subFolders;

    protected int maxPageSize = 1000;

    protected static final Logger LOG = LoggerFactory.getLogger(GoogleDriveListReader.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(GoogleDriveListReader.class);

    protected GoogleDriveAbstractListReader(RuntimeContainer container, BoundedSource source) {
        super(source);
        this.container = container;
        result = new Result();
        subFolders = new ArrayList<>();
        searchResults = new ArrayList<>();
        folderAccessMethod = AccessMethod.Id;
    }

    protected String getQuery() {
        String query;
        if(useCustomQuery) {
            query = customQuery;
        } else {
            String qTrash = includeTrashedFiles ? "" : Q_AND + Q_NOT_TRASHED;
            query = Q_IN_PARENTS +
                    ("DIRECTORIES".equals(listModeStr) ? QUERY_MIME_FOLDER : "") + qTrash;
        }
        return query;
    }

    @Override
    public boolean start() throws IOException {
        /* build query string */
        query = getQuery();
        request = drive.files().list()
                .setSupportsAllDrives(includeSharedDrives)
                .setIncludeItemsFromAllDrives(includeSharedDrives);
        request.setFields(FIELDS_SELECTION);
        request.setPageSize(maxPageSize);
        LOG.debug("[start] request: {}.", request);
        //
        if(useCustomQuery) {
            return processCustomQuery(query);
        }
        if (folderAccessMethod.equals(AccessMethod.Id)) {
            subFolders.add(folderName);
        } else {
            subFolders = utils.getFolderIds(folderName, includeTrashedFiles);
        }
        LOG.debug("[start] subFolders = {}.", subFolders);
        if (subFolders.size() == 0) {
            LOG.warn(messages.getMessage("error.folder.inexistant", folderName));
            return false;
        }
        if (subFolders.size() > 1) {
            // TODO: Remove this log as we might have folders with same name in sharedWithMe?
            LOG.warn(messages.getMessage("error.folder.more.than.one", folderName));
        }
        return processFolder();
    }

    private boolean processCustomQuery(final String query) throws IOException {
        searchResults.clear();
        request.setQ(query);
        LOG.debug("query = {} {}.", query, folderId);
        FileList files = request.execute();
        for (File file : files.getFiles()) {
            if (canAddFile(file.getMimeType())) {
                searchResults.add(file);
                result.totalCount++;
            }
        }
        request.setPageToken(files.getNextPageToken());
        searchCount = searchResults.size();

        return searchCount > 0;
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
        } else if(!useCustomQuery) {
            while (!next && (!subFolders.isEmpty() || (folderId != null))) {
                next = processFolder();
            }
            if (next) {
                searchIdx = 0;
            }
        } else if(request.getPageToken() != null) {
            next = processCustomQuery(query);
        }

        return next;
    }

    private boolean canAddSubFolder(String mimeType) {
        return MIME_TYPE_FOLDER.equals(mimeType) && includeSubDirectories;
    }

    private boolean canAddFile(String mimeType) {
        return "BOTH".equals(listModeStr) || ("FILES".equals(listModeStr) && !MIME_TYPE_FOLDER.equals(mimeType))
                || ("DIRECTORIES".equals(listModeStr) && MIME_TYPE_FOLDER.equals(mimeType));
    }

    private boolean processFolder() throws IOException {
        if (folderId == null && !subFolders.isEmpty()) {
            folderId = subFolders.get(0);
            subFolders.remove(0);
            request.setQ(format(query, folderId));
            LOG.debug("query = {} {}.", query, folderId);
        }
        searchResults.clear();
        FileList files = request.execute();
        for (File file : files.getFiles()) {
            if (canAddSubFolder(file.getMimeType())) {
                subFolders.add(file.getId());
            }
            if (canAddFile(file.getMimeType())) {
                searchResults.add(file);
                result.totalCount++;
            }
        }
        request.setPageToken(files.getNextPageToken());
        searchCount = searchResults.size();
        // finished for folderId
        if (StringUtils.isEmpty(request.getPageToken()) || searchCount == 0) {
            folderId = null;
        }

        return searchCount > 0;
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
        if(file.getParents() != null) { //"Shared with me" files might not keep info about parent.
            main.put(7, file.getParents().toString()); // TODO This should be a List<String>
        }
        main.put(8, file.getWebViewLink());

        return main;
    }

}
