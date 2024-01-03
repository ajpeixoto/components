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
package org.talend.components.google.drive;

import static java.util.Arrays.asList;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.HashSet;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public abstract class GoogleDriveComponentProperties extends FixedConnectorsComponentProperties
implements GoogleDriveProvideConnectionProperties {

    public enum AccessMethod {
        Name,
        Id
    }

    public enum Corpora {
        USER("user"),
        DRIVE("drive"),
        DOMAIN("domain"),
        ALL_DRIVES("allDrives");
        private final String corporaName;

        private Corpora(String corporaName) {
            this.corporaName = corporaName;
        }

        public String getCorporaName() {
            return corporaName;
        }
    }

    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaMain");

    public SchemaProperties schemaMain = new SchemaProperties("schemaMain");

    public GoogleDriveConnectionProperties connection = new GoogleDriveConnectionProperties("connection");

    public Property<Boolean> includeSharedItems = newBoolean("includeSharedItems", false);

    public Property<Boolean> includeSharedDrives = newBoolean("includeSharedDrives", false);

    public Property<Corpora> corpora = newEnum("corpora", Corpora.class);

    public Property<String> driveId = newString("driveId");

    public GoogleDriveComponentProperties(String name) {
        super(name);
    }

    @Override
    public Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        Set<PropertyPathConnector> connectors = new HashSet<>();
        if (isOutputConnection) {
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        corpora.setValue(Corpora.USER);
        corpora.setPossibleValues(asList(Corpora.values()));
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(connection.getForm(Form.REFERENCE));

        Form advancedForm = Form.create(this, Form.ADVANCED);
        advancedForm.addRow(connection.getForm(Form.ADVANCED));
        advancedForm.addRow(includeSharedItems);
        advancedForm.addRow(includeSharedDrives);
        advancedForm.addRow(Widget.widget(corpora).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        advancedForm.addColumn(driveId);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        for (Form childForm : connection.getForms()) {
            connection.refreshLayout(childForm);
        }
        if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(corpora.getName()).setHidden(!includeSharedDrives.getValue());
            form
                    .getWidget(driveId.getName())
                    .setHidden(!includeSharedDrives.getValue()
                            || corpora.getValue() != Corpora.DRIVE);
        }
    }

    public void afterIncludeSharedDrives() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterCorpora() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    protected boolean checkIdAccessMethod(AccessMethod accessMethod) {
        return accessMethod == AccessMethod.Id;
    }

    @Override
    public GoogleDriveConnectionProperties getConnectionProperties() {
        return connection.getEffectiveConnectionProperties();
    }
}
