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
package org.talend.components.salesforce.runtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.salesforce.SalesforceOutputProperties.OutputAction;
import org.talend.components.salesforce.integration.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.avro.SchemaConstants;
import com.sforce.ws.util.Base64;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties.FIELD_SALESFORCE_ID;

public class SalesforceWriterTestIT extends SalesforceTestBase {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceWriterTestIT.class);

    private static final String UNIQUE_NAME = "deleteme_" + System.getProperty("user.name");

    private static final String UNIQUE_ID = Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));

    SalesforceRuntimeTestUtil runtimeTestUtil = new SalesforceRuntimeTestUtil();

    /** Test schema for inserting accounts. */
    public static Schema SCHEMA_INSERT_ACCOUNT = SchemaBuilder.builder().record("Schema").fields() //
            .name("Name").type().stringType().noDefault() //
            .name("BillingStreet").type().stringType().noDefault() //
            .name("BillingCity").type().stringType().noDefault() //
            .name("BillingState").type().stringType().noDefault().endRecord();

    /** Test schema for updating accounts. */
    public static Schema SCHEMA_UPDATE_ACCOUNT = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").type().stringType().noDefault() //
            .name("Name").type().stringType().noDefault() //
            .name("BillingStreet").type().stringType().noDefault() //
            .name("BillingCity").type().stringType().noDefault() //
            .name("BillingState").type().stringType().noDefault().endRecord();

    /** Test schema for insert/update accounts case insensitive. */
    public static Schema SCHEMA_ACCOUNT_CASE_INSENSITIVE = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").type().stringType().noDefault() //
            .name("NamE").type().stringType().noDefault() //
            .name("test").type().stringType().noDefault().endRecord();

    public static Schema SCHEMA_INSERT_EVENT = SchemaBuilder.builder().record("Schema").fields() //
            .name("StartDateTime").type().stringType().noDefault() // Actual type:dateTime
            .name("EndDateTime").type().stringType().noDefault() // Actual type:dateTime
            .name("ActivityDate").type().stringType().noDefault() // Actual type:date
            .name("DurationInMinutes").type().stringType().noDefault() // Actual type:int
            .name("IsPrivate").type().stringType().noDefault() // Actual type:boolean
            .name("Subject").type().stringType().noDefault() // Actual type:boolean
            .endRecord();

    public static Schema SCHEMA_INPUT_AND_DELETE_EVENT = SchemaBuilder.builder().record("Schema").fields() //
            .name("Id").type().stringType().noDefault() //
            .name("StartDateTime").type().stringType().noDefault() // Actual type:dateTime
            .name("EndDateTime").type().stringType().noDefault() // Actual type:dateTime
            .name("ActivityDate").type().stringType().noDefault() // Actual type:date
            .name("DurationInMinutes").type().stringType().noDefault() // Actual type:int
            .name("IsPrivate").type().stringType().noDefault() // Actual type:boolean
            .name("Subject").type().stringType().noDefault() // Actual type:boolean
            .endRecord();

    /** Test schema for inserting Attachment. */
    public static Schema SCHEMA_ATTACHMENT = SchemaBuilder.builder().record("Schema").fields() //
            .name("Name").type().stringType().noDefault() //
            .name("Body").type().stringType().noDefault() //
            .name("ContentType").type().stringType().noDefault() //
            .name("ParentId").type().stringType().noDefault() //
            .name("Id").type().stringType().noDefault() //
            .endRecord();

    /** Test schema for inserting Attachment. */
    public static Schema SCHEMA_STATIC_RESOURCE = SchemaBuilder.builder().record("Schema").fields() //
            .name("Name").type().stringType().noDefault() //
            .name("ContentType").type().stringType().noDefault() //
            .name("Body").type().stringType().noDefault() //
            .name("Description").type().stringType().noDefault() //
            .name("Id").type().stringType().noDefault() //
            .endRecord();

    public static Schema SCHEMA_CONTACT = SchemaBuilder.builder().record("Schema").fields() //
            .name("Email").type().stringType().noDefault() //
            .name("FirstName").type().stringType().noDefault() //
            .name("LastName").type().stringType().noDefault() //
            .name("Id").type().stringType().noDefault() //
            .endRecord();

    public Writer<Result> createSalesforceOutputWriter(TSalesforceOutputProperties props) {
        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, props);
        SalesforceWriteOperation writeOperation = salesforceSink.createWriteOperation();
        Writer<Result> saleforceWriter = writeOperation.createWriter(adaptor);
        return saleforceWriter;
    }

    public static TSalesforceOutputProperties createSalesforceoutputProperties(String moduleName) throws Exception {
        TSalesforceOutputProperties props = (TSalesforceOutputProperties) new TSalesforceOutputProperties("foo").init();
        setupProps(props.connection);
        props.module.moduleName.setValue(moduleName);
        props.module.afterModuleName();// to setup schema.
        return props;
    }

    @AfterClass
    public static void cleanupAllRecords() throws NoSuchElementException, IOException {
        List<IndexedRecord> recordsToClean = new ArrayList<>();
        String prefixToDelete = UNIQUE_NAME + "_" + UNIQUE_ID;

        // Get the list of records that match the prefix to delete.
        {
            TSalesforceInputProperties sfProps = getSalesforceInputProperties();
            SalesforceTestBase.setupProps(sfProps.connection);
            sfProps.module.setValue("moduleName", "Account");
            sfProps.module.main.schema.setValue(SCHEMA_UPDATE_ACCOUNT);
            DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

            // Initialize the Source and Reader
            SalesforceSource sfSource = new SalesforceSource();
            sfSource.initialize(container, sfProps);
            sfSource.validate(container);

            int nameIndex = -1;
            @SuppressWarnings("unchecked")
            Reader<IndexedRecord> sfReader = sfSource.createReader(container);
            if (sfReader.start()) {
                do {
                    IndexedRecord r = sfReader.getCurrent();
                    if (nameIndex == -1) {
                        nameIndex = r.getSchema().getField("Name").pos();
                    }
                    if (String.valueOf(r.get(nameIndex)).startsWith(prefixToDelete)) {
                        recordsToClean.add(r);
                    }
                } while (sfReader.advance());
            }
        }

        // Delete those records.
        {
            ComponentDefinition sfDef = new TSalesforceOutputDefinition();

            TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
            SalesforceTestBase.setupProps(sfProps.connection);
            sfProps.outputAction.setValue(OutputAction.DELETE);
            sfProps.module.setValue("moduleName", "Account");
            sfProps.module.main.schema.setValue(SCHEMA_UPDATE_ACCOUNT);
            DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

            // Initialize the Sink, WriteOperation and Writer
            SalesforceSink sfSink = new SalesforceSink();
            sfSink.initialize(container, sfProps);
            sfSink.validate(container);

            SalesforceWriteOperation sfWriteOp = sfSink.createWriteOperation();
            sfWriteOp.initialize(container);

            Writer<Result> sfWriter = sfSink.createWriteOperation().createWriter(container);
            sfWriter.open("uid1");

            // Write one record.
            for (IndexedRecord r : recordsToClean) {
                sfWriter.write(r);
            }

            // Finish the Writer, WriteOperation and Sink.
            Result wr1 = sfWriter.close();
            sfWriteOp.finalize(Arrays.asList(wr1), container);
        }
    }

    @Test
    public void testOutputInsertAndDelete() throws Throwable {
        runOutputInsert(false);
    }

    @Test
    public void testOutputInsertAndDeleteDynamic() throws Throwable {
        runOutputInsert(true);
    }

    @Test
    public void testWriterOpenCloseWithEmptyData() throws Throwable {
        TSalesforceOutputProperties props = createSalesforceoutputProperties(EXISTING_MODULE_NAME);
        Map<String, Object> resultMap;

        // this is mainly to check that open and close do not throw any exceptions.
        // insert
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);
        props.afterOutputAction();
        Writer<Result> saleforceWriter = createSalesforceOutputWriter(props);
        Result writeResult = writeRows(saleforceWriter, Collections.EMPTY_LIST);
        resultMap = getConsolidatedResults(writeResult, saleforceWriter);
        assertEquals(0, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));

        // deleted
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.DELETE);
        props.afterOutputAction();
        saleforceWriter = createSalesforceOutputWriter(props);
        writeResult = writeRows(saleforceWriter, Collections.EMPTY_LIST);
        resultMap = getConsolidatedResults(writeResult, saleforceWriter);
        assertEquals(0, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));

        // update
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPDATE);
        props.afterOutputAction();
        saleforceWriter = createSalesforceOutputWriter(props);
        writeResult = writeRows(saleforceWriter, Collections.EMPTY_LIST);
        resultMap = getConsolidatedResults(writeResult, saleforceWriter);
        assertEquals(0, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));

        // upsert
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPSERT);
        props.afterOutputAction();
        saleforceWriter = createSalesforceOutputWriter(props);
        writeResult = writeRows(saleforceWriter, Collections.EMPTY_LIST);
        resultMap = getConsolidatedResults(writeResult, saleforceWriter);
        assertEquals(0, resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
    }

    @Ignore("Need to add some custom modules in salesforce account for this test")
    @Test
    public void testOutputUpsert() throws Throwable {

        Schema CUSTOM_LOOKUP_MODULE_SCHEMA = SchemaBuilder.builder().record("Schema").fields() //
                .name("ExternalID__c").type().stringType().noDefault() // External ID column
                .name("Name").type().stringType().noDefault() //
                .name("Id").type().stringType().noDefault() //
                .endRecord();

        Schema CUSTOM_TEST_MODULE_SCHEMA = SchemaBuilder.builder().record("Schema").fields() //
                .name("ExternalID__c").type().stringType().noDefault() // External ID column
                .name("LookupModuleExternalId").type().stringType().noDefault() // Not a module field. keep the value
                // of lookup module external id
                .name("Name").type().stringType().noDefault() //
                .name("Id").type().stringType().noDefault() //
                .endRecord();

        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        // Prepare the lookup module data
        TSalesforceOutputProperties sfLookupProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfLookupProps.connection);
        sfLookupProps.module.setValue("moduleName", "TestLookupModule__c");
        sfLookupProps.module.main.schema.setValue(CUSTOM_LOOKUP_MODULE_SCHEMA);
        sfLookupProps.ceaseForError.setValue(true);
        // Automatically generate the out schemas.
        sfLookupProps.module.schemaListener.afterSchema();

        List<IndexedRecord> records = new ArrayList<>();
        IndexedRecord r1 = new GenericData.Record(CUSTOM_LOOKUP_MODULE_SCHEMA);
        r1.put(0, "EXTERNAL_ID_" + UNIQUE_ID);
        r1.put(1, UNIQUE_NAME + "_" + UNIQUE_ID);
        records.add(r1);

        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, sfLookupProps);
        salesforceSink.validate(adaptor);
        Writer<Result> batchWriter = salesforceSink.createWriteOperation().createWriter(adaptor);
        writeRows(batchWriter, records);

        List<IndexedRecord> successRecords = ((SalesforceWriter) batchWriter).getSuccessfulWrites();
        assertEquals(1, successRecords.size());

        // 2. Upsert "TestModule__c" with upsert relation table
        TSalesforceOutputProperties sfTestLookupProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties().init();
        SalesforceTestBase.setupProps(sfTestLookupProps.connection);
        sfTestLookupProps.module.setValue("moduleName", "TestModule__c");
        sfTestLookupProps.module.main.schema.setValue(CUSTOM_TEST_MODULE_SCHEMA);
        // Automatically generate the out schemas.
        sfTestLookupProps.module.schemaListener.afterSchema();

        sfTestLookupProps.outputAction.setValue(OutputAction.UPSERT);
        sfTestLookupProps.afterOutputAction();
        assertEquals(4, sfTestLookupProps.upsertKeyColumn.getPossibleValues().size());

        sfTestLookupProps.upsertKeyColumn.setValue("ExternalID__c");
        sfTestLookupProps.ceaseForError.setValue(true);
        // setup relation table
        sfTestLookupProps.upsertRelationTable.columnName.setValue(Arrays.asList("LookupModuleExternalId"));
        sfTestLookupProps.upsertRelationTable.lookupFieldName.setValue(Arrays.asList("TestLookupModule__c"));
        sfTestLookupProps.upsertRelationTable.lookupRelationshipFieldName.setValue(Arrays.asList("TestLookupModule__r"));
        sfTestLookupProps.upsertRelationTable.lookupFieldModuleName.setValue(Arrays.asList("TestLookupModule__c"));
        sfTestLookupProps.upsertRelationTable.lookupFieldExternalIdName.setValue(Arrays.asList("ExternalID__c"));

        records = new ArrayList<>();
        r1 = new GenericData.Record(CUSTOM_TEST_MODULE_SCHEMA);
        r1.put(0, "EXTERNAL_ID_" + UNIQUE_ID);
        r1.put(1, "EXTERNAL_ID_" + UNIQUE_ID);
        r1.put(2, UNIQUE_NAME + "_" + UNIQUE_ID);
        records.add(r1);

        salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, sfTestLookupProps);
        salesforceSink.validate(adaptor);
        batchWriter = salesforceSink.createWriteOperation().createWriter(adaptor);
        writeRows(batchWriter, records);

        assertEquals(1, ((SalesforceWriter) batchWriter).getSuccessfulWrites().size());

        TSalesforceInputProperties sfInputProps = getSalesforceInputProperties();
        sfInputProps.copyValuesFrom(sfTestLookupProps);
        // "LookupModuleExternalId" is not the column of module. So "CUSTOM_LOOKUP_MODULE_SCHEMA" for query
        sfInputProps.module.main.schema.setValue(CUSTOM_LOOKUP_MODULE_SCHEMA);
        sfInputProps.condition.setValue("ExternalID__c = 'EXTERNAL_ID_" + UNIQUE_ID + "'");

        List<IndexedRecord> inpuRecords = readRows(sfInputProps);
        assertEquals(1, inpuRecords.size());
        LOGGER.debug("Upsert operation insert a record in module \"TestModule__c\" with ID: " + inpuRecords.get(0).get(2));
    }

    /**
     * @param isDynamic true if the actual rows should contain more columns than the schema specified in the component
     * properties.
     */
    protected void runOutputInsert(boolean isDynamic) throws Exception {
        TSalesforceOutputProperties props = createSalesforceoutputProperties(EXISTING_MODULE_NAME);
        setupProps(props.connection);

        props.module.moduleName.setValue(EXISTING_MODULE_NAME);
        props.module.main.schema.setValue(getMakeRowSchema(isDynamic));

        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);

        Writer<Result> saleforceWriter = createSalesforceOutputWriter(props);

        String random = createNewRandom();
        List<IndexedRecord> outputRows = makeRows(random, 10, isDynamic);
        List<IndexedRecord> inputRows = null;
        Exception firstException = null;
        try {
            Result writeResult = writeRows(saleforceWriter, outputRows);
            Map<String, Object> resultMap = getConsolidatedResults(writeResult, saleforceWriter);
            assertEquals(outputRows.size(), resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
            // create a new props for reading the data, the schema may be altered in the original output props
            TSalesforceOutputProperties readprops = createSalesforceoutputProperties(EXISTING_MODULE_NAME);
            setupProps(readprops.connection);
            readprops.module.beforeModuleName();
            readprops.module.moduleName.setValue(EXISTING_MODULE_NAME);
            readprops.module.afterModuleName();// to update the schema.
            inputRows = readRows(readprops);
            List<IndexedRecord> allReadTestRows = filterAllTestRows(random, inputRows);
            assertNotEquals(0, allReadTestRows.size());
            assertEquals(outputRows.size(), allReadTestRows.size());
        } catch (Exception e) {
            firstException = e;
        } finally {
            if (firstException == null) {
                if (inputRows == null) {
                    inputRows = readRows(props);
                }
                List<IndexedRecord> allReadTestRows = filterAllTestRows(random, inputRows);
                deleteRows(allReadTestRows, props);
                inputRows = readRows(props);
                assertEquals(0, filterAllTestRows(random, inputRows).size());
            } else {
                throw firstException;
            }
        }
    }

    /**
     * Basic test that shows how the {@link SalesforceSink} is meant to be used to write data.
     */
    @Test
    public void testSinkWorkflow_insert() throws Exception {
        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.module.setValue("moduleName", "Account");
        sfProps.module.main.schema.setValue(SCHEMA_INSERT_ACCOUNT);
        sfProps.ceaseForError.setValue(false);
        // Automatically generate the out schemas.
        sfProps.module.schemaListener.afterSchema();

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        // Initialize the Sink, WriteOperation and Writer
        SalesforceSink sfSink = new SalesforceSink();
        sfSink.initialize(container, sfProps);
        sfSink.validate(container);

        SalesforceWriteOperation sfWriteOp = sfSink.createWriteOperation();
        sfWriteOp.initialize(container);

        SalesforceWriter sfWriter = sfSink.createWriteOperation().createWriter(container);
        sfWriter.open("uid1");

        // Write one record.
        IndexedRecord r = new GenericData.Record(SCHEMA_INSERT_ACCOUNT);
        r.put(0, UNIQUE_NAME + "_" + UNIQUE_ID);
        r.put(1, "deleteme");
        r.put(2, "deleteme");
        r.put(3, "deleteme");
        sfWriter.write(r);

        sfWriter.close();

        assertThat(sfWriter.getRejectedWrites(), empty());
        assertThat(sfWriter.getSuccessfulWrites(), hasSize(1));
        assertThat(sfWriter.getSuccessfulWrites().get(0), is(r));

        sfWriter.cleanWrites();

        // Rejected and successful writes are reset on the next record.
        r = new GenericData.Record(SCHEMA_INSERT_ACCOUNT);
        r.put(0, UNIQUE_NAME + "_" + UNIQUE_ID);
        r.put(1, "deleteme2");
        r.put(2, "deleteme2");
        r.put(3, "deleteme2");
        sfWriter.write(r);

        sfWriter.close();

        assertThat(sfWriter.getRejectedWrites(), empty());
        assertThat(sfWriter.getSuccessfulWrites(), hasSize(1));
        assertThat(sfWriter.getSuccessfulWrites().get(0), is(r));

        // Finish the Writer, WriteOperation and Sink.
        Result wr1 = sfWriter.close();
        sfWriteOp.finalize(Arrays.asList(wr1), container);
    }

    /**
     * Test for a Sink that has an output flow containing the salesforce id.
     */
    @Test
    public void testSinkWorkflow_insertAndRetrieveId() throws Exception {
        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.module.setValue("moduleName", "Account");
        sfProps.extendInsert.setValue(false);
        sfProps.ceaseForError.setValue(false);
        sfProps.retrieveInsertId.setValue(true);
        sfProps.module.main.schema.setValue(SCHEMA_INSERT_ACCOUNT);
        // Automatically generate the out schemas.
        sfProps.module.schemaListener.afterSchema();

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        // Initialize the Sink, WriteOperation and Writer
        SalesforceSink sfSink = new SalesforceSink();
        sfSink.initialize(container, sfProps);
        sfSink.validate(container);

        SalesforceWriteOperation sfWriteOp = sfSink.createWriteOperation();
        sfWriteOp.initialize(container);

        SalesforceWriter sfWriter = sfSink.createWriteOperation().createWriter(container);
        sfWriter.open("uid1");

        // Write one record.
        IndexedRecord r = new GenericData.Record(SCHEMA_INSERT_ACCOUNT);
        r.put(0, UNIQUE_NAME + "_" + UNIQUE_ID);
        r.put(1, "deleteme");
        r.put(2, "deleteme");
        r.put(3, "deleteme");
        sfWriter.write(r);

        assertThat(sfWriter.getRejectedWrites(), empty());
        assertThat(sfWriter.getSuccessfulWrites(), hasSize(1));

        // Check the successful record (main output)
        IndexedRecord main = sfWriter.getSuccessfulWrites().get(0);
        assertThat(main.getSchema().getFields(), hasSize(5));

        // Check the values copied from the incoming record.
        for (int i = 0; i < r.getSchema().getFields().size(); i++) {
            assertThat(main.getSchema().getFields().get(i), is(r.getSchema().getFields().get(i)));
            assertThat(main.get(i), is(r.get(i)));
        }

        // The enriched fields.
        assertThat(main.getSchema().getFields().get(4).name(), is("salesforce_id"));
        assertThat(main.get(4), not(nullValue()));

        // Finish the Writer, WriteOperation and Sink.
        Result wr1 = sfWriter.close();
        sfWriteOp.finalize(Arrays.asList(wr1), container);
    }

    /**
     * Basic test that shows how the {@link SalesforceSink} is meant to be used to write data.
     */
    @Test
    public void testSinkWorkflow_insertRejected() throws Exception {

        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.module.setValue("moduleName", "Account");
        sfProps.module.main.schema.setValue(SCHEMA_INSERT_ACCOUNT);
        sfProps.extendInsert.setValue(false);
        sfProps.ceaseForError.setValue(false);
        // Automatically generate the out schemas.
        sfProps.module.schemaListener.afterSchema();

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        // Initialize the Sink, WriteOperation and Writer
        SalesforceSink sfSink = new SalesforceSink();
        sfSink.initialize(container, sfProps);
        sfSink.validate(container);

        SalesforceWriteOperation sfWriteOp = sfSink.createWriteOperation();
        sfWriteOp.initialize(container);

        SalesforceWriter sfWriter = sfSink.createWriteOperation().createWriter(container);
        sfWriter.open("uid1");

        // Write one record, which should fail for missing name.
        IndexedRecord r = new GenericData.Record(SCHEMA_INSERT_ACCOUNT);
        r.put(0, "");
        r.put(1, "deleteme");
        r.put(2, "deleteme");
        r.put(3, "deleteme");
        sfWriter.write(r);

        assertThat(sfWriter.getSuccessfulWrites(), empty());
        assertThat(sfWriter.getRejectedWrites(), hasSize(1));

        // Check the rejected record.
        IndexedRecord rejected = sfWriter.getRejectedWrites().get(0);
        assertThat(rejected.getSchema().getFields(), hasSize(7));

        // Check the values copied from the incoming record.
        for (int i = 0; i < r.getSchema().getFields().size(); i++) {
            assertThat(rejected.getSchema().getFields().get(i), is(r.getSchema().getFields().get(i)));
            assertThat(rejected.get(i), is(r.get(i)));
        }

        // The enriched fields.
        assertThat(rejected.getSchema().getFields().get(4).name(), is("errorCode"));
        assertThat(rejected.getSchema().getFields().get(5).name(), is("errorFields"));
        assertThat(rejected.getSchema().getFields().get(6).name(), is("errorMessage"));
        assertThat(rejected.get(4), is((Object) "REQUIRED_FIELD_MISSING"));
        assertThat(rejected.get(5), is((Object) "Name"));
        // removed the check on value cause it is i18n
        assertThat(rejected.get(6), instanceOf(String.class));

        // Finish the Writer, WriteOperation and Sink.
        Result wr1 = sfWriter.close();
        sfWriteOp.finalize(Arrays.asList(wr1), container);
    }

    /**
     * Basic test that shows how the {@link SalesforceSink} is meant to be used to write data.
     */
    @Test
    public void testSinkWorkflow_updateRejected() throws Exception {
        testUpdateError(false);
    }

    @Test(expected = IOException.class)
    public void testSinkWorkflow_updateCeaseForError() throws Exception {
        testUpdateError(true);
    }

    // This is for reject and caseForError not real test for update
    protected void testUpdateError(boolean ceaseForError) throws Exception {

        // Generate log file path
        String logFilePath = tempFolder.getRoot().getAbsolutePath() + "/salesforce_error_" + (ceaseForError ? 0 : 1) + ".log";
        File file = new File(logFilePath);
        assertFalse(file.exists());

        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.module.setValue("moduleName", "Account");
        sfProps.module.main.schema.setValue(SCHEMA_UPDATE_ACCOUNT);
        sfProps.outputAction.setValue(OutputAction.UPDATE);
        sfProps.extendInsert.setValue(false);
        sfProps.ceaseForError.setValue(ceaseForError);
        // Setup log file path
        LOGGER.debug("Error log path: " + logFilePath);
        sfProps.logFileName.setValue(logFilePath);
        // Automatically generate the out schemas.
        sfProps.module.schemaListener.afterSchema();

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        // Initialize the Sink, WriteOperation and Writer
        SalesforceSink sfSink = new SalesforceSink();
        sfSink.initialize(container, sfProps);
        sfSink.validate(container);

        SalesforceWriteOperation sfWriteOp = sfSink.createWriteOperation();
        sfWriteOp.initialize(container);
        try {

            SalesforceWriter sfWriter = sfSink.createWriteOperation().createWriter(container);
            sfWriter.open("uid1");

            // Write one record, which should fail for the bad ID
            IndexedRecord r = new GenericData.Record(SCHEMA_UPDATE_ACCOUNT);
            r.put(0, "bad id");
            r.put(1, UNIQUE_NAME + "_" + UNIQUE_ID);
            r.put(2, "deleteme");
            r.put(3, "deleteme");
            r.put(4, "deleteme");
            if (!ceaseForError) {
                sfWriter.write(r);

                assertThat(sfWriter.getSuccessfulWrites(), empty());
                assertThat(sfWriter.getRejectedWrites(), hasSize(1));

                // Check the rejected record.
                IndexedRecord rejected = sfWriter.getRejectedWrites().get(0);
                assertThat(rejected.getSchema().getFields(), hasSize(8));

                // Check the values copied from the incoming record.
                for (int i = 0; i < r.getSchema().getFields().size(); i++) {
                    assertThat(rejected.getSchema().getFields().get(i), is(r.getSchema().getFields().get(i)));
                    assertThat(rejected.get(0), is(r.get(0)));
                }

                // The enriched fields.
                assertThat(rejected.getSchema().getFields().get(5).name(), is("errorCode"));
                assertThat(rejected.getSchema().getFields().get(6).name(), is("errorFields"));
                assertThat(rejected.getSchema().getFields().get(7).name(), is("errorMessage"));
                assertThat(rejected.get(5), is((Object) "MALFORMED_ID"));
                assertThat(rejected.get(6), is((Object) "Id"));
                // removed the check on value cause it is i18n
                assertThat(rejected.get(7), instanceOf(String.class));

                // Finish the Writer, WriteOperation and Sink.
                Result wr1 = sfWriter.close();
                sfWriteOp.finalize(Arrays.asList(wr1), container);
            } else {
                try {
                    sfWriter.write(r);
                    sfWriter.close();
                    fail("It should get error when insert data!");
                } catch (IOException e) {
                    // removed the check on value cause it is i18n
                    // assertThat(e.getMessage(), is((Object) "Account ID: id value of incorrect type: bad id\n"));
                    throw e;
                }
            }
        } finally {
            assertTrue(file.exists());
            assertNotEquals(0, file.length());
        }
    }

    /**
     * This test about: 1) Insert record which "Id" is passed from input data 2) Upsert with Id as a upsert key column
     */
    @Test
    public void testSourceIncludedId() throws Throwable {

        // Generate log file path
        String logFilePath = tempFolder.getRoot().getAbsolutePath() + "/salesforce_error_" + UNIQUE_ID + ".log";
        File file = new File(logFilePath);
        assertFalse(file.exists());
        // Prepare the input properties for check record in server side
        TSalesforceInputProperties inputProperties = getSalesforceInputProperties();
        List<IndexedRecord> inputRecords = null;

        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.module.setValue("moduleName", "Account");
        sfProps.module.main.schema.setValue(SCHEMA_UPDATE_ACCOUNT);
        sfProps.extendInsert.setValue(false);
        sfProps.ceaseForError.setValue(false);
        // Setup log file path
        LOGGER.debug("Error log path: " + logFilePath);
        sfProps.logFileName.setValue(logFilePath);

        /////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////// 1. Insert the record and get the record Id //////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////
        // Automatically generate the out schemas.
        sfProps.retrieveInsertId.setValue(true);
        sfProps.module.schemaListener.afterSchema();
        Schema flowSchema = sfProps.schemaFlow.schema.getValue();
        Schema.Field field = flowSchema.getField(FIELD_SALESFORCE_ID);
        assertEquals(6, flowSchema.getFields().size());
        assertNotNull(field);
        assertEquals(5, field.pos());

        // Initialize the Writer
        LOGGER.debug("Try to insert the record");
        SalesforceWriter sfWriterInsert = (SalesforceWriter) createSalesforceOutputWriter(sfProps);
        sfWriterInsert.open("uid_insert");

        // Insert one record with Id column. The "Id" column should be ignore and insert successfully
        IndexedRecord insertRecord_1 = new GenericData.Record(SCHEMA_UPDATE_ACCOUNT);
        insertRecord_1.put(0, "bad id");
        insertRecord_1.put(1, UNIQUE_NAME + "_" + UNIQUE_ID + "_insert");
        insertRecord_1.put(2, "deleteme_insert");
        insertRecord_1.put(3, "deleteme_insert");
        insertRecord_1.put(4, "deleteme_insert");
        IndexedRecord insertRecord_2 = new GenericData.Record(SCHEMA_UPDATE_ACCOUNT);
        insertRecord_2.put(0, "bad id");
        insertRecord_2.put(2, "deleteme_insert");
        insertRecord_2.put(3, "deleteme_insert");
        insertRecord_2.put(4, "deleteme_insert");

        // Test wrong record
        sfWriterInsert.write(insertRecord_2);
        assertThat(sfWriterInsert.getSuccessfulWrites(), empty());
        assertThat(sfWriterInsert.getRejectedWrites(), hasSize(1));
        LOGGER.debug("1 record is reject by insert action.");
        sfWriterInsert.cleanWrites();

        sfWriterInsert.write(insertRecord_1);
        assertThat(sfWriterInsert.getSuccessfulWrites(), hasSize(1));
        assertThat(sfWriterInsert.getRejectedWrites(), empty());
        // Check the rejected record.
        IndexedRecord successRecord = sfWriterInsert.getSuccessfulWrites().get(0);
        sfWriterInsert.cleanWrites();
        assertThat(successRecord.getSchema().getFields(), hasSize(6));
        assertEquals(FIELD_SALESFORCE_ID, successRecord.getSchema().getFields().get(5).name());
        // The enriched fields.
        String recordID = String.valueOf(successRecord.get(5));
        LOGGER.debug("1 record insert successfully and get record Id: " + recordID);
        // Finish the Writer, WriteOperation and Sink for insert action
        Result wr1 = sfWriterInsert.close();

        inputProperties.copyValuesFrom(sfProps);
        inputProperties.condition.setValue("Name='" + UNIQUE_NAME + "_" + UNIQUE_ID + "_insert'");
        inputRecords = readRows(inputProperties);
        assertEquals(1, inputRecords.size());
        // Check record in server side
        successRecord = inputRecords.get(0);
        assertThat(successRecord.get(1), is((Object) (UNIQUE_NAME + "_" + UNIQUE_ID + "_insert")));
        assertThat(successRecord.get(2), is((Object) "deleteme_insert"));
        assertThat(successRecord.get(3), is((Object) "deleteme_insert"));
        assertThat(successRecord.get(4), is((Object) "deleteme_insert"));

        // Check error log
        assertTrue(file.exists());
        assertNotEquals(0, file.length());
        // removed this test caus the message is i18n
        // runtimeTestUtil.compareFileContent(sfProps.logFileName.getValue(),
        // new String[] { "\tStatus Code: REQUIRED_FIELD_MISSING", "", "\tRowKey/RowNo: 1", "\tFields: Name", "",
        // "\tMessage: Required fields are missing: [Name]",
        // "\t--------------------------------------------------------------------------------", "" });
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////// 2.Update the inserted record /////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Flow schema change back to same with main schema
        sfProps.extendInsert.setValue(true);
        sfProps.outputAction.setValue(OutputAction.UPDATE);
        sfProps.module.schemaListener.afterSchema();
        flowSchema = sfProps.schemaFlow.schema.getValue();
        assertEquals(5, flowSchema.getFields().size());

        // Initialize the Writer

        LOGGER.debug("Try to update the record which Id is: " + recordID);
        SalesforceWriter sfWriter_Update = (SalesforceWriter) createSalesforceOutputWriter(sfProps);
        sfWriter_Update.open("uid_update");
        IndexedRecord updateRecord_1 = new GenericData.Record(SCHEMA_UPDATE_ACCOUNT);
        updateRecord_1.put(0, "0019000001n3Kasss");
        updateRecord_1.put(1, UNIQUE_NAME + "_" + UNIQUE_ID + "_update");
        updateRecord_1.put(2, "deleteme_update");
        updateRecord_1.put(3, "deleteme_update");
        updateRecord_1.put(4, "deleteme_update");
        IndexedRecord updateRecord_2 = new GenericData.Record(SCHEMA_UPDATE_ACCOUNT);
        updateRecord_2.put(0, recordID);
        updateRecord_2.put(1, UNIQUE_NAME + "_" + UNIQUE_ID + "_update");
        updateRecord_2.put(2, "deleteme_update");
        updateRecord_2.put(3, "deleteme_update");
        updateRecord_2.put(4, "deleteme_update");
        IndexedRecord updateRecord_3 = new GenericData.Record(SCHEMA_UPDATE_ACCOUNT);
        updateRecord_3.put(0, "0019000001n3Kabbb");
        updateRecord_3.put(1, UNIQUE_NAME + "_" + UNIQUE_ID + "_update");
        updateRecord_3.put(2, "deleteme_update");
        updateRecord_3.put(3, "deleteme_update");
        updateRecord_3.put(4, "deleteme_update");
        sfWriter_Update.write(updateRecord_1);
        sfWriter_Update.write(updateRecord_2);
        sfWriter_Update.write(updateRecord_3);

        // Finish the Writer, WriteOperation and Sink for insert action
        Result wr2 = sfWriter_Update.close();

        assertEquals(1, wr2.getSuccessCount());
        assertEquals(2, wr2.getRejectCount());

        // Check record in server side
        inputProperties.copyValuesFrom(sfProps);
        inputProperties.condition.setValue("Name='" + UNIQUE_NAME + "_" + UNIQUE_ID + "_update'");
        inputRecords = readRows(inputProperties);
        assertEquals(1, inputRecords.size());

        successRecord = inputRecords.get(0);
        assertThat(successRecord.get(1), is((Object) (UNIQUE_NAME + "_" + UNIQUE_ID + "_update")));
        assertThat(successRecord.get(2), is((Object) "deleteme_update"));
        assertThat(successRecord.get(3), is((Object) "deleteme_update"));
        assertThat(successRecord.get(4), is((Object) "deleteme_update"));
        LOGGER.debug("1 record update successfully.");
        LOGGER.debug("2 record is reject by update action");

        // Check error log
        assertTrue(file.exists());
        assertNotEquals(0, file.length());
        // removed the check on value cause it is i18n
        // runtimeTestUtil.compareFileContent(sfProps.logFileName.getValue(),
        // new String[] { "\tStatus Code: MALFORMED_ID", "", "\tRowKey/RowNo: 0019000001n3Kasss", "\tFields: Id", "",
        // "\tMessage: Account ID: id value of incorrect type: 0019000001n3Kasss",
        // "\t--------------------------------------------------------------------------------", "",
        // "\tStatus Code: MALFORMED_ID", "", "\tRowKey/RowNo: 0019000001n3Kabbb", "\tFields: Id", "",
        // "\tMessage: Account ID: id value of incorrect type: 0019000001n3Kabbb",
        // "\t--------------------------------------------------------------------------------", "" });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////// 3.Upsert the record with Id as upsertKeyColumn ///////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        sfProps.outputAction.setValue(OutputAction.UPSERT);
        sfProps.module.schemaListener.afterSchema();
        // Test upsertkey column is "Id"
        sfProps.upsertKeyColumn.setValue("Id");

        // Initialize the Writer

        LOGGER.debug("Try to upsert the record which Id is: " + recordID);
        SalesforceWriter sfWriter_Upsert = (SalesforceWriter) createSalesforceOutputWriter(sfProps);
        sfWriter_Upsert.open("uid_upsert");
        IndexedRecord upsertRecord_1 = new GenericData.Record(SCHEMA_UPDATE_ACCOUNT);
        upsertRecord_1.put(0, "0019000001n3Kasss");
        upsertRecord_1.put(1, UNIQUE_NAME + "_" + UNIQUE_ID + "_upsert");
        upsertRecord_1.put(2, "deleteme_upsert");
        upsertRecord_1.put(3, "deleteme_upsert");
        upsertRecord_1.put(4, "deleteme_upsert");
        IndexedRecord upsertRecord_2 = new GenericData.Record(SCHEMA_UPDATE_ACCOUNT);
        upsertRecord_2.put(0, recordID);
        upsertRecord_2.put(1, UNIQUE_NAME + "_" + UNIQUE_ID + "_upsert");
        upsertRecord_2.put(2, "deleteme_upsert");
        upsertRecord_2.put(3, "deleteme_upsert");
        upsertRecord_2.put(4, "deleteme_upsert");
        sfWriter_Upsert.write(upsertRecord_1);
        sfWriter_Upsert.write(upsertRecord_2);
        // Finish the Writer, WriteOperation and Sink for insert action
        Result wr3 = sfWriter_Upsert.close();
        assertEquals(1, wr3.getSuccessCount());
        assertEquals(1, wr3.getRejectCount());

        // Check record in server side
        inputProperties.copyValuesFrom(sfProps);
        inputProperties.condition.setValue("Name='" + UNIQUE_NAME + "_" + UNIQUE_ID + "_upsert'");
        inputRecords = readRows(inputProperties);
        assertEquals(1, inputRecords.size());

        successRecord = inputRecords.get(0);
        assertThat(successRecord.get(1), is((Object) (UNIQUE_NAME + "_" + UNIQUE_ID + "_upsert")));
        assertThat(successRecord.get(2), is((Object) "deleteme_upsert"));
        assertThat(successRecord.get(3), is((Object) "deleteme_upsert"));
        assertThat(successRecord.get(4), is((Object) "deleteme_upsert"));
        LOGGER.debug("1 record upsert successfully.");
        LOGGER.debug("1 record is reject by upsert action.");

        // Check error log
        assertTrue(file.exists());
        assertNotEquals(0, file.length());
        runtimeTestUtil.compareFileContent(sfProps.logFileName.getValue(),
                new String[] { "\tStatus Code: MALFORMED_ID", "", "\tRowKey/RowNo: 0019000001n3Kasss", "\tFields: ", "",
                        "\tMessage: Id in upsert is not valid",
                        "\t--------------------------------------------------------------------------------", "", });

        // Test wrong module name value
        LOGGER.debug("Try to upsert the record which Id is: " + recordID);
        sfWriter_Upsert = (SalesforceWriter) createSalesforceOutputWriter(sfProps);
        sfWriter_Upsert.open("uid_upsert");
        upsertRecord_1 = new GenericData.Record(SCHEMA_UPDATE_ACCOUNT);
        upsertRecord_1.put(0, recordID);
        upsertRecord_1.put(1,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                        + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                        + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" + "\n");
        upsertRecord_1.put(2, "deleteme_upsert");
        upsertRecord_1.put(3, "deleteme_upsert");
        upsertRecord_2.put(4, "deleteme_upsert");
        sfWriter_Upsert.write(upsertRecord_1);
        // Finish the Writer, WriteOperation and Sink for insert action
        wr3 = sfWriter_Upsert.close();
        assertEquals(0, wr3.getSuccessCount());
        assertEquals(1, wr3.getRejectCount());

        // Check error log
        assertTrue(file.exists());
        assertNotEquals(0, file.length());
        // removed the check on value cause it is i18n
        // runtimeTestUtil.compareFileContent(sfProps.logFileName.getValue(),
        // new String[] { "\tStatus Code: STRING_TOO_LONG", "", "\tRowKey/RowNo: " + recordID, "\tFields: Name", "",
        // "\tMessage: Account Name: data value too large: aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        // + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        // + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        // + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa (max length=255)",
        // "\t--------------------------------------------------------------------------------", "", });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////// 4.Delete the record with Id /////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        sfProps.outputAction.setValue(OutputAction.DELETE);

        // Initialize the Writer
        LOGGER.debug("Try to delete the record which Id is: " + recordID);
        SalesforceWriter sfWriter_Delete = (SalesforceWriter) createSalesforceOutputWriter(sfProps);
        sfWriter_Delete.open("uid_delete");

        IndexedRecord deleteRecord_1 = new GenericData.Record(SCHEMA_UPDATE_ACCOUNT);
        deleteRecord_1.put(0, recordID);
        deleteRecord_1.put(1, UNIQUE_NAME + "_" + UNIQUE_ID + "_delete");
        IndexedRecord deleteRecord_2 = new GenericData.Record(SCHEMA_UPDATE_ACCOUNT);
        // Id not exist
        deleteRecord_2.put(0, "0019000001n3Kabbb");
        deleteRecord_2.put(1, UNIQUE_NAME + "_" + UNIQUE_ID + "_delete");
        sfWriter_Delete.write(deleteRecord_1);
        sfWriter_Delete.write(deleteRecord_2);

        // Finish the Writer, WriteOperation and Sink for insert action
        Result wr4 = sfWriter_Delete.close();
        assertEquals(1, wr4.getSuccessCount());
        assertEquals(1, wr4.getRejectCount());

        // Check record in server side
        inputProperties.copyValuesFrom(sfProps);
        inputProperties.condition.setValue("Name='" + UNIQUE_NAME + "_" + UNIQUE_ID + "_upsert'");
        inputRecords = readRows(inputProperties);
        assertEquals(0, inputRecords.size());
        LOGGER.debug("1 record delete successfully.");
        LOGGER.debug("1 record is reject by delete action.");

        // Check error log
        assertTrue(file.exists());
        assertNotEquals(0, file.length());
        // removed the check on value cause it is i18n
        // runtimeTestUtil.compareFileContent(sfProps.logFileName.getValue(),
        // new String[] { "\tStatus Code: MALFORMED_ID", "", "\tRowKey/RowNo: 0019000001n3Kabbb", "\tFields: ", "",
        // "\tMessage: bad id 0019000001n3Kabbb",
        // "\t--------------------------------------------------------------------------------", "", });
    }

    /*
     * With current API like date/datetime/int/.... string value can't be write to server side So we need convert the field
     * value type.
     */
    @Test
    public void testSinkAllWithStringValue() throws Exception {
        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.module.setValue("moduleName", "Event");
        sfProps.module.main.schema.setValue(SCHEMA_INSERT_EVENT);
        sfProps.ceaseForError.setValue(true);
        // Automatically generate the out schemas.
        sfProps.module.schemaListener.afterSchema();

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        List<IndexedRecord> records = new ArrayList<>();
        String random = createNewRandom();
        IndexedRecord r1 = new GenericData.Record(SCHEMA_INSERT_EVENT);
        r1.put(0, "2011-02-02T02:02:02");
        r1.put(1, "2011-02-02T22:02:02.000Z");
        r1.put(2, "2011-02-02");
        r1.put(3, "1200");
        r1.put(4, "true");
        r1.put(5, random);
        // Rejected and successful writes are reset on the next record.
        IndexedRecord r2 = new GenericData.Record(SCHEMA_INSERT_EVENT);
        r2.put(0, "2016-02-02T02:02:02.000Z");
        r2.put(1, "2016-02-02T12:02:02");
        r2.put(2, "2016-02-02");
        r2.put(3, "600");
        r2.put(4, "0");
        r2.put(5, random);

        records.add(r1);
        records.add(r2);

        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, sfProps);
        salesforceSink.validate(adaptor);
        Writer<Result> batchWriter = salesforceSink.createWriteOperation().createWriter(adaptor);
        writeRows(batchWriter, records);

        assertEquals(2, ((SalesforceWriter) batchWriter).getSuccessfulWrites().size());

        TSalesforceInputProperties sfInputProps = getSalesforceInputProperties();
        sfInputProps.copyValuesFrom(sfProps);
        sfInputProps.condition.setValue("Subject = '" + random + "' ORDER BY DurationInMinutes ASC");

        sfInputProps.module.main.schema.setValue(SCHEMA_INPUT_AND_DELETE_EVENT);
        List<IndexedRecord> inpuRecords = readRows(sfInputProps);
        try {
            assertEquals(2, inpuRecords.size());
            IndexedRecord inputRecords_1 = inpuRecords.get(0);
            IndexedRecord inputRecords_2 = inpuRecords.get(1);
            assertEquals(random, inputRecords_1.get(6));
            assertEquals(random, inputRecords_2.get(6));
            // we use containsInAnyOrder because we are not garanteed to have the same order every run.
            assertThat(Arrays.asList("2011-02-02T02:02:02.000Z", "2016-02-02T02:02:02.000Z"),
                    containsInAnyOrder(inputRecords_1.get(1), inputRecords_2.get(1)));
            assertThat(Arrays.asList("2011-02-02T22:02:02.000Z", "2016-02-02T12:02:02.000Z"),
                    containsInAnyOrder(inputRecords_1.get(2), inputRecords_2.get(2)));
            assertThat(Arrays.asList("2011-02-02", "2016-02-02"),
                    containsInAnyOrder(inputRecords_1.get(3), inputRecords_2.get(3)));
            assertThat(Arrays.asList("1200", "600"), containsInAnyOrder(inputRecords_1.get(4), inputRecords_2.get(4)));
            assertThat(Arrays.asList("true", "false"), containsInAnyOrder(inputRecords_1.get(5), inputRecords_2.get(5)));

        } finally {
            deleteRows(inpuRecords, sfInputProps);
        }
    }

    /**
     * Test tSalesforceOutput add additional information for upsert
     */
    @Test
    public void testUpsertAdditionalInfo() throws Throwable {
        // 1.Prepare output component configuration
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();
        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.module.setValue("moduleName", "Contact");
        sfProps.module.main.schema.setValue(SCHEMA_CONTACT);
        sfProps.outputAction.setValue(OutputAction.UPSERT);
        sfProps.ceaseForError.setValue(false);
        sfProps.extendInsert.setValue(false);
        sfProps.retrieveInsertId.setValue(true);
        sfProps.upsertKeyColumn.setValue("Email");
        sfProps.module.schemaListener.afterSchema();

        // 2.Prepare the data
        List records = new ArrayList<IndexedRecord>();
        String random = String.valueOf(createNewRandom());
        IndexedRecord r1 = new GenericData.Record(SCHEMA_CONTACT);
        r1.put(0, "aaa" + random + "@talend.com");
        r1.put(1, "F_" + random);
        r1.put(2, "L_" + random);
        IndexedRecord r2 = new GenericData.Record(SCHEMA_CONTACT);
        r2.put(0, "bbb" + random + "@talend.com");
        IndexedRecord r3 = new GenericData.Record(SCHEMA_CONTACT);
        r3.put(0, "ccc" + random + "@talend.com");
        r3.put(1, "F_" + random);
        r3.put(2, "L_" + random);
        IndexedRecord r4 = new GenericData.Record(SCHEMA_CONTACT);
        r4.put(0, "aaa" + random + "@talend.com");
        r4.put(1, "F_update_" + random);
        r4.put(2, "L_update_" + random);

        // 3. Write data
        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, sfProps);
        salesforceSink.validate(adaptor);
        SalesforceWriter writer = salesforceSink.createWriteOperation().createWriter(adaptor);

        List<IndexedRecord> successRecords = new ArrayList<>();
        List<IndexedRecord> rejectRecords = new ArrayList<>();
        writer.open("foo");
        try {
            // writing and collect the result
            // insert
            writer.write(r1);
            successRecords.addAll(writer.getSuccessfulWrites());
            rejectRecords.addAll(writer.getRejectedWrites());
            // reject
            writer.write(r2);
            successRecords.addAll(writer.getSuccessfulWrites());
            rejectRecords.addAll(writer.getRejectedWrites());
            // insert
            writer.write(r3);
            successRecords.addAll(writer.getSuccessfulWrites());
            rejectRecords.addAll(writer.getRejectedWrites());
            // update
            writer.write(r4);
            successRecords.addAll(writer.getSuccessfulWrites());
            rejectRecords.addAll(writer.getRejectedWrites());
        } finally {
            writer.close();
        }

        // 4.Check the write return IndexRecords whether include expect information
        assertEquals(3, successRecords.size());
        assertEquals(1, rejectRecords.size());
        IndexedRecord record_1 = successRecords.get(0);
        IndexedRecord record_2 = successRecords.get(1);
        IndexedRecord record_3 = successRecords.get(2);
        Schema recordSchema = record_1.getSchema();
        assertEquals(6, recordSchema.getFields().size());
        assertEquals(4, recordSchema.getField(TSalesforceOutputProperties.FIELD_SALESFORCE_ID).pos());
        assertEquals(5, recordSchema.getField(TSalesforceOutputProperties.FIELD_STATUS).pos());

        assertEquals("aaa" + random + "@talend.com", record_1.get(0));
        assertNotNull(record_1.get(4));
        assertEquals("created", record_1.get(5));

        assertEquals("ccc" + random + "@talend.com", record_2.get(0));
        assertNotNull(record_2.get(4));
        assertEquals("created", record_2.get(5));

        assertEquals("aaa" + random + "@talend.com", record_3.get(0));
        assertEquals(record_3.get(4), record_1.get(4));
        assertEquals("updated", record_3.get(5));

        // 5.Check the result in salesforce
        TSalesforceInputProperties sfInputProps = getSalesforceInputProperties();
        sfInputProps.copyValuesFrom(sfProps);
        sfInputProps.condition.setValue("FirstName like '%" + random + "'");
        List<IndexedRecord> inpuRecords = readRows(sfInputProps);
        assertEquals(2, inpuRecords.size());
        IndexedRecord inputRecords_1 = inpuRecords.get(0);
        IndexedRecord inputRecords_2 = inpuRecords.get(1);
        assertThat(Arrays.asList("aaa" + random + "@talend.com", "ccc" + random + "@talend.com"),
                containsInAnyOrder(inputRecords_1.get(0), inputRecords_2.get(0)));
        assertThat(Arrays.asList("F_" + random, "F_update_" + random),
                containsInAnyOrder(inputRecords_1.get(1), inputRecords_2.get(1)));
        assertThat(Arrays.asList("L_" + random, "L_update_" + random),
                containsInAnyOrder(inputRecords_1.get(2), inputRecords_2.get(2)));
        // 6.Delete test data
        deleteRows(inpuRecords, sfInputProps);
    }

    @Test
    public void testUploadAttachment() throws Throwable {

        ComponentDefinition sfDef = new TSalesforceOutputDefinition();
        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.module.setValue("moduleName", "Attachment");
        sfProps.module.main.schema.setValue(SCHEMA_ATTACHMENT);
        sfProps.ceaseForError.setValue(true);
        sfProps.module.schemaListener.afterSchema();

        List records = new ArrayList<IndexedRecord>();
        String random = String.valueOf(createNewRandom());
        LOGGER.debug("Getting the ParentId for attachment reocrds...");
        String parentId = getFirstCreatedAccountRecordId();
        LOGGER.debug("ParentId for attachments is:" + parentId);
        IndexedRecord r1 = new GenericData.Record(SCHEMA_ATTACHMENT);
        r1.put(0, "attachment_1_" + random + ".txt");
        r1.put(1, "VGhpcyBpcyBhIHRlc3QgZmlsZSAxICE=");
        r1.put(2, "text/plain");
        r1.put(3, parentId);

        IndexedRecord r2 = new GenericData.Record(SCHEMA_ATTACHMENT);
        r2.put(0, "attachment_2_" + random + ".txt");
        r2.put(1,
                "QmFzZSA2NC1lbmNvZGVkIGJpbmFyeSBkYXRhLiBGaWVsZHMgb2YgdGhpcyB0eXBlIGFyZSB1c2VkIGZvciBzdG9yaW5"
                        + "nIGJpbmFyeSBmaWxlcyBpbiBBdHRhY2htZW50IHJlY29yZHMsIERvY3VtZW50IHJlY29yZHMsIGFuZCBTY2"
                        + "9udHJvbCByZWNvcmRzLiBJbiB0aGVzZSBvYmplY3RzLCB0aGUgQm9keSBvciBCaW5hcnkgZmllbGQgY29udGFpbn"
                        + "MgdGhlIChiYXNlNjQgZW5jb2RlZCkgZGF0YSwgd2hpbGUgdGhlIEJvZHlMZW5ndGggZmllbGQgZGVmaW5lcyB0aGU"
                        + "gbGVuZ3RoIG9mIHRoZSBkYXRhIGluIHRoZSBCb2R5IG9yIEJpbmFyeSBmaWVsZC4gSW4gdGhlIERvY3VtZW50IG9"
                        + "iamVjdCwgeW91IGNhbiBzcGVjaWZ5IGEgVVJMIHRvIHRoZSBkb2N1bWVudCBpbnN0ZWFkIG9mIHN0b3JpbmcgdGh"
                        + "lIGRvY3VtZW50IGRpcmVjdGx5IGluIHRoZSByZWNvcmQu");
        r2.put(2, "text/plain");
        r2.put(3, parentId);

        records.add(r1);
        records.add(r2);

        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, sfProps);
        salesforceSink.validate(adaptor);
        Writer<Result> batchWriter = salesforceSink.createWriteOperation().createWriter(adaptor);

        LOGGER.debug("Uploading 2 attachments ...");
        writeRows(batchWriter, records);
        assertEquals(2, ((SalesforceWriter) batchWriter).getSuccessfulWrites().size());
        LOGGER.debug("2 attachments uploaded successfully!");

        TSalesforceInputProperties sfInputProps = getSalesforceInputProperties();
        sfInputProps.copyValuesFrom(sfProps);
        sfInputProps.condition.setValue("Name = 'attachment_1_" + random + ".txt' or Name = 'attachment_2_" + random + ".txt'");

        sfInputProps.module.main.schema.setValue(SCHEMA_ATTACHMENT);
        List<IndexedRecord> inpuRecords = readRows(sfInputProps);
        try {
            assertEquals(2, inpuRecords.size());
            IndexedRecord inputRecords_1 = null;
            IndexedRecord inputRecords_2 = null;
            if (("attachment_1_" + random + ".txt").equals(String.valueOf(inpuRecords.get(0).get(0)))) {
                inputRecords_1 = inpuRecords.get(0);
                inputRecords_2 = inpuRecords.get(1);
            } else {
                inputRecords_1 = inpuRecords.get(1);
                inputRecords_2 = inpuRecords.get(0);
            }
            assertEquals("attachment_1_" + random + ".txt", inputRecords_1.get(0));
            assertEquals("attachment_2_" + random + ".txt", inputRecords_2.get(0));
            assertEquals("VGhpcyBpcyBhIHRlc3QgZmlsZSAxICE=", inputRecords_1.get(1));
            assertEquals(
                    "Base 64-encoded binary data. Fields of this type are used for storing binary files in Attachment "
                            + "records, Document records, and Scontrol records. In these objects, the Body or Binary "
                            + "field contains the (base64 encoded) data, while the BodyLength field defines the length"
                            + " of the data in the Body or Binary field. In the Document object, you can specify a "
                            + "URL to the document instead of storing the document directly in the record.",
                    new String(Base64.decode(((String) inputRecords_2.get(1)).getBytes())));
            assertEquals("text/plain", inputRecords_1.get(2));
            assertEquals("text/plain", inputRecords_2.get(2));
            assertEquals(parentId, inputRecords_1.get(3));
            assertEquals(parentId, inputRecords_2.get(3));
            assertNotNull(inputRecords_1.get(4));
            assertNotNull(inputRecords_2.get(4));

        } finally {
            deleteRows(inpuRecords, sfInputProps);
        }
    }

    @Test
    public void testNillableBase64Field() throws Throwable {

        String moduleName = "StaticResource";

        ComponentDefinition sfDef = new TSalesforceOutputDefinition();
        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.module.setValue("moduleName", moduleName);
        sfProps.module.main.schema.setValue(SCHEMA_STATIC_RESOURCE);
        sfProps.ceaseForError.setValue(true);
        sfProps.module.schemaListener.afterSchema();
        List records = new ArrayList<IndexedRecord>();
        String randomName = getRandomString(10);
        IndexedRecord record = new GenericData.Record(SCHEMA_STATIC_RESOURCE);
        record.put(0, randomName);
        record.put(1, "text/plain");
        record.put(2, "dGhpcyBpcyBiYXNlNjQgIHRlc3QgZmlsZS4g");
        record.put(3, "this is base64 test file.");

        records.add(record);

        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, sfProps);
        salesforceSink.validate(adaptor);
        Writer<Result> batchWriter = salesforceSink.createWriteOperation().createWriter(adaptor);

        writeRows(batchWriter, records);

        TSalesforceInputProperties sfInputProps = getSalesforceInputProperties();
        sfInputProps.copyValuesFrom(sfProps);
        sfInputProps.condition.setValue("Name = '" + randomName + "'");

        sfProps.module.setValue("moduleName", moduleName);
        sfProps.module.main.schema.setValue(SCHEMA_STATIC_RESOURCE);
        List<IndexedRecord> inpuRecords = readRows(sfInputProps);
        try {
            assertEquals(1, inpuRecords.size());
            IndexedRecord r = inpuRecords.get(0);
            assertEquals(randomName, r.get(0));
            assertEquals("text/plain", r.get(1));
            assertEquals("dGhpcyBpcyBiYXNlNjQgIHRlc3QgZmlsZS4g", r.get(2));
            assertEquals("this is base64 test file.", r.get(3));
            assertNotNull(r.get(4));
        } finally {
            deleteRows(inpuRecords, sfInputProps);
        }
    }

    /**
     * Test write base64 nullable fields, with image file
     */
    @Test
    public void testBase64FieldWithException() throws Throwable {

        String moduleName = "StaticResource";

        ComponentDefinition sfDef = new TSalesforceOutputDefinition();
        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.module.setValue("moduleName", moduleName);
        sfProps.module.main.schema.setValue(SCHEMA_STATIC_RESOURCE);
        sfProps.ceaseForError.setValue(true);
        sfProps.module.schemaListener.afterSchema();
        List records = new ArrayList<IndexedRecord>();
        String randomName = getRandomString(10);
        String resources = getClass().getResource("/component.gif").toURI().getPath();
        byte[] base64Content = org.apache.commons.io.FileUtils.readFileToByteArray(new java.io.File(resources));
        IndexedRecord record = new GenericData.Record(SCHEMA_STATIC_RESOURCE);
        record.put(0, randomName);
        record.put(1, "image/gif");
        record.put(2, base64Content);
        record.put(3, "component.gif.");

        records.add(record);

        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, sfProps);
        salesforceSink.validate(adaptor);
        Writer<Result> batchWriter = salesforceSink.createWriteOperation().createWriter(adaptor);

        writeRows(batchWriter, records);

        TSalesforceInputProperties sfInputProps = getSalesforceInputProperties();
        sfInputProps.copyValuesFrom(sfProps);
        sfInputProps.condition.setValue("Name = '" + randomName + "'");

        sfProps.module.setValue("moduleName", moduleName);
        sfProps.module.main.schema.setValue(SCHEMA_STATIC_RESOURCE);
        List<IndexedRecord> inpuRecords = readRows(sfInputProps);
        try {
            assertEquals(1, inpuRecords.size());
            IndexedRecord r = inpuRecords.get(0);
            assertEquals(randomName, r.get(0));
            assertEquals("image/gif", r.get(1));
            String base64ContentString =
                    "R0lGODdhwgEsAYQAAP///////wAAAL+/v9/f319fXz8/P5+fn39/fx8fHwcHB3d3d6enpzc3Nzs7Ow8PD4uLiyMjIy8vL29vbxMTEw"
                            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACwAAAAAwgEsAQAF/iAgjmRpnmiqrmzrvnAsz3Rt33iu73zv/8CgcEgs"
                            + "Go/IpHLJbDqf0Kh0Sq1ar9isdsvter/gsHhMLpvP6LR6zW673/C4fE6v2+/4vH7P7/v/gIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZ"
                            + "aXmJmam5ydnp+goaKjpKWmp6ipqqusra6vsLGys7S1tre4ubq7vL2+v8DBwsPExcbHyMnKy8zNzs/Q0dLT1NXW19jZ2tvc3d7f"
                            + "4OHi4+Tl5ufo6err7O3u7/Dx8vP09fb3+Pn6+/z9/v8AAwocSLCgwYMIEypcyLChw4cQI0qcSLGixYsYM2rcyLGjx48gQ4ocSb"
                            + "KkyZMo/lOqXMmypcuXMGPKnEmzps2bOHPq3Mmzp8+fQIMKfTZgwFAkBIw+IlBAgFMDR4sMeOrogNOrAg5EHdLUKQJGVrE6VboV"
                            + "SIKrXxWFFXsgQNkgWNMiWhvX7dsfBOImmio2q927PvKiRcQXawECfwH3EOz1UGGnCQYkVrxYb6HHAhIQoCyEsQC5gjBrLtUUqj"
                            + "nPWgcROIt1M6m1oMWhVs36quRStQtIYlr7agIDqc3MDrSa7WRQj3VDotv36uEyw/8UN3wcVNfGjxA03/6ZTHQ/BsQmqP7Jc9ZHz"
                            + "LljNR3mMdk+4cW6XnLAgP379pW70d76SX389+knBWYGFGXgAAX0/uaUgF6490d8dSmBoHoCGDDfGr2Nx8SE6lkohXkGkFefWGLQ"
                            + "9Z4e110VIlIIKMjdaGswx2ARBLRIoW8XNgFhheSNwJ9TOXJBV5B3/IjjEUzd2Bd7aSgYHI0pKlkhFEbCuAJWJ3JhJJF1pCdAlkE"
                            + "YKeVgThDAZQyYCXCmD2KO2Z0TOz6pApZhpLhmHGl+1iMPBOzo5lV3ClEYmDD4uSIRff7ZV6BBeHYoC3SCsSOjbUxHnVSKNhfbEX"
                            + "QxKYN5bw6Rp6KbHmEkoSdE+kVvqMbhZ2Z77uBlXwnUamtzCeCFQAHPmZCmDVGqScSs4tlaK646qhgrCap6IVarJ5hJ6RVe/k5rQ"
                            + "5tYJYCAZImNam0LKeZaQp7QpgCqhiTUWAB+vc6ArW/bBtDtdt/uYJkLzXJhnpwqHJDbGKD6Nex22iKWwnblvjCAiyVYihW/LrQJ"
                            + "2gCv2jbDrAX3iPASj1mb7xbMldpwsF+K4eKjQeSZQFsskCyyDO8mtiOEL6/gIlnv+iaDyiyv4HIOTPFaAMQlGLnsCJ7Vq0LQvBJ"
                            + "9rVg1c9hcwlQEa2XKmh4NgJczzkCyU3+leABsF9Nq19fPwpAnAlpzjYPDwqoAIbot0KV1C3ArrUKUXQNAsXo9e+El1U+LZeHdeX"
                            + "rq9bO12UUX24z1bXNfaYkJHGKWOm1Cm4cr3Jzi/jHAXfKcKhbq290riE64CwrOOCJ3CBj8RZ4FoP7pq7XL8PkN4UrWuN+XRi7D4"
                            + "Ftne9sIZLOQqFi5x7C7DRW3+ljN4xq2Q/Q9LEmCvy+2ZXsULtL9w8KUfw/AyYVD5trvtaErfAyvjufZ1SIURr3fLrI9A/o17Fhb"
                            + "qyZ6wY5WJ7dsWWwHaRIX93BVgOOJgWQERFNzmieDV4HOBXSxUuNSRJb3eY5yAJgbkey3AtqZL4Tao0GKeHVAFFxHfCp4DAxVaJj"
                            + "rRDCGtLLR5wI3BmxR0AdemuELOEeD+V2oNo/7iwddEL0AnoCEKQjiCQFARBMQ4AADOJORQmTDAi7oBb3R/pwIrpjFFGwxAF1E4J"
                            + "gKNMUB0QtrU7NBei7IgvblyEWwSpdz1IarACCRPFB84sZqMMcSvM4pEMvgZtKIAiS6IFx7OuR5TKBIADAyB8TKFg+9g0f9jW87n"
                            + "qRBmui4t4/h8URLZEHFEPAjlFUPO74CZRtHOQLy9SWWkbqkCT4mSF7WD4+4PKAueUehlbWRChUTItDwqMwPrmd4DztO/iaTShxO"
                            + "rT8pCGS6mHlMWlqSO+9xWM+GWUusLMs8oSwB2sIZxrO1MAd4hMwmzfCuGzJxO2LEWwqLJjRUXsoECpphNVNQMQP86IfBLFXFJmk"
                            + "DUFlooWMhwY4oSE4RrKWZI+iN/ivHCNHRiWCiYXvnDUZlTDaMaqM6eBdKdWe4oinoQhBaqYLANNBeLqk2d9KmCFR6TBH0pQEBde"
                            + "I3lUWCihaPKivwH5HgtjJVpQhlRpVB+NKphnjaU3ncySe+WmrRTiIPmyfoDULHuMcWdHRBsSqMnAKGSB305QHMc8v/LJqtHBn1o"
                            + "EmVDwr8lLu5HlV96hRpEf2krZ5e4WsrxcHXMPoCh24NbabJXI9YhYKavpJCgYKQXQlmWLZepWdzdViWjHqdmvnJgSQQE2gtJloT"
                            + "RDVicXwDsa6aOgOWFQejDBZwdDZUHqmgfYC8rRfVM9bLomx+1svBqBD5l7nuiKq9RVVp/it72ursq7kWe+5xXruCBYrHsFbwLGN"
                            + "rsKWH6SA9CmLj+a4CPMGW4F8oKIzkvuqcQQ5XTuX97Hm588P/nfEERoUQxOCG2tT+UwT+XU91uBtfiJLSDBDVKg1yEwBfygBtXs"
                            + "EukCBU3HLqN75kMlcYPxeri06GwhaOwWJPjJa6utC9H/3w9vpSYBIA970tPhKAYRzDs96vh4bpDXiDWbIUwyCeY5FZjoHkM7BSM"
                            + "sQ2fQq5UudIEjjIyGAEJUDt61oe+3UEUYpMj6a35djuOKIKW6iRJAwGzCRnyIY0JwCw3ILlfmkyJOvwCMTSo+SdgDma4VysruNK"
                            + "u82Zx3x8I477/qJno37Zljo2o5MRPMEeMdhvCz2MZ2iLBbEK1QeEdgudWZCzsWLrTp3q0Y+I1iajBEuMnxZBqA+N5hvwtMzZ2pO"
                            + "jhbmkzBJ10bkuZa2jGE/1dqwNVULj6YJQm7SMenJcpu+BXyzczcl4xs+S63eXFiFgOxvRWdZUWGncZCY30jbedc7Rmj3utAk7bg"
                            + "27ImQdaGI2mMco8AVCpJ4dRdtepTrpCVRvavajLDGAxnYBoYh/XYJ9g7tu/vaojaEWK82OYAIFQMABNhNxtGiNlwqCbgksLgIIn"
                            + "FXKk2ll6A4wNCzEKQDf8QE2+b1XFf33ydOOMkPPLPH2+uZ4aVqTCE8w/imfPlyVNgd5sPNq7oCqR8wvUPqymR43CSipgQuudo9p"
                            + "TqMIDalRMz+6NREZaw8/XExCB1QJGuBiaaOVoLxM0QMSLvZsPgwCkzb6tVOAUwBMwE0iTwHb1b5L89ZR7Tl7ysZ7RDO8BYvTQOi"
                            + "KJ3803hocm9Y9r6CcC6AAASwguFN/N9iGe6E3F83dVnw5kQWgH66PXM4RcIoEjpPBo5nTzsyT3SOxogDaL/1K/ybp0HTP970/cZ"
                            + "pY0PhfVB4EO+k98zDwzA/HfK/f/r7dAkjMdRxwHP9VB9J6smL7Aud6pP1T+USvvrlaU5vOM5D4EDf866G8fkDVhgJZRAzqjJye/"
                            + "sPAeQh4xWxhN2yaR3h1hmUgAm3iI3WwhDRRInJDxyx1l34GaHd0Vhja0hpJMTaul4CrlzAYuCVFlHcGth41tgUB+Enq9nxX9RiJ"
                            + "ZW3/xgKYMV+0Ji509RTVoVcOyGjVcSqFR4CJhoMtg3r9oiIClhiuFyVEEiUJsxb28VkndHk8VyEnqCVatwMctGcTOH9AOFwvuFN"
                            + "QA3xTklHGBwCOsoE6RHFRNk/lh0JHR26kRmINt4U+l2GpQoSStiRTZGhTqGdW+EVmEXrll2qNlVzlVobmh1Q+J0SZZDgnaCnFNY"
                            + "gKxgLpkTAkEyRcNzMAB4eHSIJDFHoSlXNhkII9QBdP/qJ+4WZusEV/NZdiHjRdJ5B40QR3gHWHDXhknhhYeNiKsziHXTh2qqiLd"
                            + "PYq8wQDTkiBb0dPV5gDO4KEvBV92mU6R/dSY5cWSUNtiIRkIpOFKJAtt9NtYkhnAVUdVeYC4kQeZmZ91zcD8mUCIIUGpLgD0qdk"
                            + "iBhfGYI65sEofRFJZHIdHZYibpEUfyMeY1OQLAeO7ih/dXaP8aeD4fiLHCaNQvhnzaGPu7gz9PeO8LiMxCRSMvSJF1mEclZb6xh"
                            + "nbbVptJhHVoZkZgOMNhiHIdll+4hVkxiLuSiMmXEmJHNOhtNTa/EkGrmRgMgnNVlUhphNLlKMopd9Msg8K7Bq/lTEcLh2QbJYi6"
                            + "JHg/jTF0qJa1IJYhYmfeASk+vVk03ZhiXIUEEplGPIA3ESLWyRTRWzlQdTkjhXj7JmMeWIfZJTIx3CKPsCl80hl1Y2QQ2pkrwIf"
                            + "Wdph17plJRIlxdGeE/1f08ggljomCHTMGloPN9DIIUJb9h4RaDYMKjYMAfASqbJSlVIkepXI8yUmjAYhjDZViIJGdIVVyqALT+2"
                            + "ZjoQHxpyc2tAZkDjJzWjW/aBZO0SA5bTAslGdZkzmzsnBMRpAMb5LXEZlhV4mJ+RIwO5gsOlkCngfPBEFTIkmVEwQLgVUHdycus"
                            + "BfxI5lKL3YG6YFaGJk5AnQG7SOc6T/o4p+YVmGD7GooeQop+8yJ9bxXpjmZNw4GbpE2nmop7+dwMK14n3s0rQFoxDsDwU8qAzoG"
                            + "gVKmG4R5YBuigtkJcNhRY+GAdRwmbpYkF3A1mL9zZaaZ2y2Z3X+YOjhwQuyp4w0Ec0aaFLw5LcGaKOqUc1Kkqf1YtwADdYaUjIF"
                            + "32leZrcIisiinRFyoUeB4xMmQRXdJrxYljPU0Ij+QKSJB6u6YvPVJY3igNcQ55VkEAJkyRkyqa2NpNUmqYpCRklNqRkQJiNiaei"
                            + "NAAGiUW2w6fdNZ800CaVlwZBlB8aV0b1QzIeggVRQqBzsyx+kk9P5QbiFpvwOQSb+pSlowOP/ocHfJkpCGkFfHVP7nmn/DlRmsq"
                            + "Jt8mRRiCgNrmq0AOieIBhFIKfWeAkrMOKPEc/3TiaZ1CRyimrRWCssbmkv+qdd8Ab91mmUkCNI2qXSBMekaFPzooGLbmsT9CtoG"
                            + "qrNSAeerMGW7ou8YR1q8JnzYqYKKA1CtoG+1So4ooE8zqbzNqjvygdB6J/YaBRWtN3bOKQa8BoL9AU2eoEBrt7CYtboqgMA5eK5"
                            + "BklctoEMQoDFVug1moCQ1Z2y0BZzZqo8KOne0qwzmKySEB5GesKvTEtrNGp+3Om8squYUCnSwCL0HBjqpqv+YmsJduVXYCrSgAh"
                            + "9ZkL+YY3tVK0okms/sXKtFoAm0xwHSvLsht7BJihol8gllmgtReqmNBAclHAHEpLBUI2BmVbJtJ6DKtRUlKAdm/AYVPrA3DbE0r"
                            + "4BoNCBnfbE34St0ogkHwbGEXxtxbhfZxBtaFauKzAmIi7ClC7uKjwl46bCvcWuamAGeVKuZOAGYKLuX7AHJvLuXxQe6BbCp0yuq"
                            + "R7lKYLCkbyY6lrCbrZuqFworD7CbI7u51AmbZ7u2Gau5ugsryru5Dxu8C7lsKbCfFYvK5Lh8iLCMVBoMvrCDr6vNI7vdRbvdZ7v"
                            + "dibvdq7vdzbvd77veAbvuI7vuRbvuZ7vuibvuq7vuzbvu77vvAbv/I7v/Rbdr/2e7/4m7/6u7/827/++78AHMACPMAEXMAGfMAI"
                            + "nMAKvMAM3MAO/MAQHMESPMEUXMEWfMEYnMEavMEc3MEe/MEgHMIiPMIkXMImfMIonMIqvMIs3MIu/MIwHMMyPMM0XMM2fMM4nMM"
                            + "6vMM83MM+/MNAHMQgHAIAOw==";
            assertEquals(base64ContentString, r.get(2));
            assertEquals("component.gif.", r.get(3));
        } finally {
            deleteRows(inpuRecords, sfInputProps);
        }
    }

    /**
     * Test write dynamic with feedback
     */
    @Test
    public void testWriteDynamicWithFeedback() throws Exception {

        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.module.setValue("moduleName", "Account");
        Schema dynamicSchema=SchemaBuilder.builder().record("Schema") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true") //
                .prop("di.dynamic.column.position","1").fields() //
                .name("Name").type().stringType().noDefault() //
                .endRecord();

        Schema dynamicRuntimeSchema=SchemaBuilder.builder().record("Schema") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true") //
                .prop("di.dynamic.column.position","1").fields() //
                .name("Name").type().stringType().noDefault() //
                .name("BillingStreet").type().stringType().noDefault() //
                .name("BillingCity").type().stringType().noDefault() //
                .name("BillingState").type().stringType().noDefault()//
                .endRecord();
        sfProps.module.main.schema.setValue(dynamicSchema);

        sfProps.extendInsert.setValue(false);
        sfProps.ceaseForError.setValue(false);
        // Automatically generate the out schemas.
        sfProps.module.schemaListener.afterSchema();

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        // Initialize the Sink, WriteOperation and Writer
        SalesforceSink sfSink = new SalesforceSink();
        sfSink.initialize(container, sfProps);
        sfSink.validate(container);

        SalesforceWriteOperation sfWriteOp = sfSink.createWriteOperation();
        sfWriteOp.initialize(container);

        SalesforceWriter sfWriter = sfSink.createWriteOperation().createWriter(container);
        sfWriter.open("uid1");

        // Write one record, which should fail for missing name.
        IndexedRecord r1 = new GenericData.Record(dynamicRuntimeSchema);
        r1.put(0, "test");
        r1.put(1, "deleteme");
        r1.put(2, "deleteme");
        r1.put(3, "deleteme");
        sfWriter.write(r1);

        // Check success
        assertThat(sfWriter.getSuccessfulWrites(), hasSize(1));
        IndexedRecord success = sfWriter.getSuccessfulWrites().get(0);
        assertThat(success.getSchema().getFields(), hasSize(4));
        // Check the values copied from the incoming record.
        for (int i = 0; i < r1.getSchema().getFields().size(); i++) {
            assertThat(success.getSchema().getFields().get(i), is(r1.getSchema().getFields().get(i)));
            assertThat(success.get(i), is(r1.get(i)));
        }
        // The success fields.
        assertThat(success.getSchema().getFields().get(0).name(), is("Name"));
        assertThat(success.getSchema().getFields().get(1).name(), is("BillingStreet"));
        assertThat(success.getSchema().getFields().get(2).name(), is("BillingCity"));
        assertThat(success.getSchema().getFields().get(3).name(), is("BillingState"));
        assertThat(success.get(0), is((Object) "test"));
        assertThat(success.get(1), is((Object) "deleteme"));
        assertThat(success.get(2), is((Object) "deleteme"));
        assertThat(success.get(3), is((Object) "deleteme"));

        // Check reject
        IndexedRecord r2 = new GenericData.Record(dynamicRuntimeSchema);
        r2.put(1, "deleteme");
        r2.put(2, "deleteme");
        r2.put(3, "deleteme");
        sfWriter.write(r2);

        assertThat(sfWriter.getRejectedWrites(), hasSize(1));
        IndexedRecord rejected = sfWriter.getRejectedWrites().get(0);
        assertThat(rejected.getSchema().getFields(), hasSize(7));
        // The rejected fields.
        assertThat(rejected.getSchema().getFields().get(0).name(), is("Name"));
        assertThat(rejected.getSchema().getFields().get(1).name(), is("BillingStreet"));
        assertThat(rejected.getSchema().getFields().get(2).name(), is("BillingCity"));
        assertThat(rejected.getSchema().getFields().get(3).name(), is("BillingState"));
        assertThat(rejected.getSchema().getFields().get(4).name(), is(TSalesforceOutputProperties.FIELD_ERROR_CODE));
        assertThat(rejected.getSchema().getFields().get(5).name(), is(TSalesforceOutputProperties.FIELD_ERROR_FIELDS));
        assertThat(rejected.getSchema().getFields().get(6).name(), is(TSalesforceOutputProperties.FIELD_ERROR_MESSAGE));
        assertNull(rejected.get(0));
        assertThat(rejected.get(1), is((Object) "deleteme"));
        assertThat(rejected.get(2), is((Object) "deleteme"));
        assertThat(rejected.get(3), is((Object) "deleteme"));
        assertNotNull(rejected.get(4));
        assertNotNull(rejected.get(5));
        assertNotNull(rejected.get(6));

        // Finish the Writer, WriteOperation and Sink.
        Result wr1 = sfWriter.close();
        sfWriteOp.finalize(Arrays.asList(wr1), container);

    }

    /**
     * Test salesforce output upsert reject records when active extend insert
     */
    @Test
    public void testUpsertExtendInsertReject() throws Exception {
        // Component framework objects.
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();

        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.outputAction.setValue(OutputAction.UPSERT);
        sfProps.upsertKeyColumn.setValue("Id");
        sfProps.module.setValue("moduleName", "Account");
        sfProps.module.main.schema.setValue(SCHEMA_INSERT_ACCOUNT);
        sfProps.ceaseForError.setValue(false);
        sfProps.extendInsert.setValue(true);
        // Automatically generate the out schemas.
        sfProps.module.schemaListener.afterSchema();

        DefaultComponentRuntimeContainerImpl container = new DefaultComponentRuntimeContainerImpl();

        // Initialize the Sink, WriteOperation and Writer
        SalesforceSink sfSink = new SalesforceSink();
        sfSink.initialize(container, sfProps);
        sfSink.validate(container);

        SalesforceWriteOperation sfWriteOp = sfSink.createWriteOperation();
        sfWriteOp.initialize(container);

        SalesforceWriter sfWriter = sfSink.createWriteOperation().createWriter(container);
        sfWriter.open("uid1");

        // Write one record.
        IndexedRecord r1 = new GenericData.Record(SCHEMA_INSERT_ACCOUNT);
        r1.put(1, "deleteme1");
        r1.put(2, "deleteme1");
        r1.put(3, "deleteme1");
        sfWriter.write(r1);
        IndexedRecord r2 = new GenericData.Record(SCHEMA_INSERT_ACCOUNT);
        r2.put(1, "deleteme2");
        r2.put(2, "deleteme2");
        r2.put(3, "deleteme2");
        sfWriter.write(r2);

        sfWriter.close();

        List<IndexedRecord> rejectRecords = sfWriter.getRejectedWrites();

        assertThat(rejectRecords, hasSize(2));
        assertThat(sfWriter.getSuccessfulWrites(), empty());

        IndexedRecord reject2 = rejectRecords.get(1);

        assertNull(reject2.get(0));
        assertThat(reject2.get(1), is((Object) "deleteme2"));
        assertThat(reject2.get(2), is((Object) "deleteme2"));
        assertThat(reject2.get(3), is((Object) "deleteme2"));
        assertNotNull(reject2.get(4));
        assertThat(reject2.get(5), is((Object) "Name"));
        assertNotNull(reject2.get(6));

        sfWriter.cleanWrites();
        // Finish the Writer, WriteOperation and Sink.
        Result wr1 = sfWriter.close();
        sfWriteOp.finalize(Arrays.asList(wr1), container);
    }

    @Test
    public void testUpdateColumnInsensitive() throws Throwable {

        String random = createNewRandom();
        ComponentDefinition sfDef = new TSalesforceOutputDefinition();
        TSalesforceOutputProperties sfProps = (TSalesforceOutputProperties) sfDef.createRuntimeProperties();
        SalesforceTestBase.setupProps(sfProps.connection);
        sfProps.module.setValue("moduleName", "Account");
        sfProps.outputAction.setValue(OutputAction.INSERT);
        sfProps.module.main.schema.setValue(SCHEMA_ACCOUNT_CASE_INSENSITIVE);
        sfProps.retrieveInsertId.setValue(true);
        sfProps.extendInsert.setValue(false);
        sfProps.ceaseForError.setValue(false);
        sfProps.module.schemaListener.afterSchema();
        List records = new ArrayList<IndexedRecord>();
        try {
            // insert
            IndexedRecord r1 = new GenericData.Record(SCHEMA_ACCOUNT_CASE_INSENSITIVE);
            r1.put(1, "name_1_" + random);
            r1.put(2, "test_1_" + random);

            records.add(r1);

            SalesforceSink salesforceSink = new SalesforceSink();
            salesforceSink.initialize(adaptor, sfProps);
            salesforceSink.validate(adaptor);
            Writer<Result> batchWriter = salesforceSink.createWriteOperation().createWriter(adaptor);

            writeRows(batchWriter, records);
            assertEquals(1, ((SalesforceWriter) batchWriter).getSuccessfulWrites().size());

            IndexedRecord insertResult = ((SalesforceWriter) batchWriter).getSuccessfulWrites().get(0);

            String recordId = String.valueOf(insertResult.get(3));

            // update
            sfProps.outputAction.setValue(OutputAction.UPDATE);
            records.clear();
            r1 = new GenericData.Record(SCHEMA_ACCOUNT_CASE_INSENSITIVE);
            r1.put(0, recordId);
            r1.put(1, "name_1_update_" + random);
            r1.put(2, "test_1_update_" + random);
            records.add(r1);

            SalesforceSink salesforceSink2 = new SalesforceSink();
            salesforceSink2.initialize(adaptor, sfProps);
            salesforceSink2.validate(adaptor);
            Writer<Result> batchWriter2 = salesforceSink2.createWriteOperation().createWriter(adaptor);

            writeRows(batchWriter2, records);

            System.out.println(((SalesforceWriter) batchWriter2).getRejectedWrites());
            assertEquals(1, ((SalesforceWriter) batchWriter2).getSuccessfulWrites().size());

            IndexedRecord updateResult = ((SalesforceWriter) batchWriter2).getSuccessfulWrites().get(0);

            assertEquals("name_1_update_" + random, updateResult.get(1));
        } finally {
            TSalesforceInputProperties sfInputProps = getSalesforceInputProperties();
            sfInputProps.copyValuesFrom(sfProps);
            sfInputProps.condition.setValue("Name = '%" + random + "%");
            sfInputProps.module.main.schema.setValue(SCHEMA_ACCOUNT_CASE_INSENSITIVE);
            deleteRows(records, sfInputProps);
        }

    }

    public String getFirstCreatedAccountRecordId() throws Exception {
        TSalesforceInputProperties sfInputProps = getSalesforceInputProperties();
        SalesforceTestBase.setupProps(sfInputProps.connection);
        sfInputProps.module.setValue("moduleName", "Account");
        sfInputProps.module.main.schema.setValue(SCHEMA_UPDATE_ACCOUNT);
        sfInputProps.condition.setValue("Id != null ORDER BY CreatedDate");

        List<IndexedRecord> inpuRecords = readRows(sfInputProps);
        String firstId = null;
        if (inpuRecords != null && inpuRecords.size() > 0) {
            LOGGER.debug("Retrieve records size from Account is:" + inpuRecords.size());
            assertNotNull(inpuRecords.get(0).get(0));
            firstId = String.valueOf(inpuRecords.get(0).get(0));
            LOGGER.debug("The first record Id:" + firstId);
        } else {
            LOGGER.error("Module Account have no records!");
        }
        return firstId;
    }

    protected static TSalesforceInputProperties getSalesforceInputProperties() {
        ComponentDefinition sfInputDef = new TSalesforceInputDefinition();
        return (TSalesforceInputProperties) sfInputDef.createRuntimeProperties();
    }

    /**
     * Return a randomly generated String (Only include upper case)
     */
    public static String getRandomString(int length) {
        Random random = new Random();
        int cnt = 0;
        StringBuffer buffer = new StringBuffer();
        char ch;
        int end = 'Z' + 1;
        int start = 'A';
        while (cnt < length) {
            ch = (char) (random.nextInt(end - start) + start);
            if (Character.isLetterOrDigit(ch)) {
                buffer.append(ch);
                cnt++;
            }
        }
        return buffer.toString();
    }

}
