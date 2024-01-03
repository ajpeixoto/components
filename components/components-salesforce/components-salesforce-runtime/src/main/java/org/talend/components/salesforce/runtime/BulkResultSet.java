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

import com.talend.csv.CSVReader;

import java.io.IOException;
import java.util.List;

public class BulkResultSet {

    CSVReader reader;

    List<String> header;

    public BulkResultSet(CSVReader reader, List<String> header) {
        this.reader = reader;
        this.header = header;
    }

    public BulkResult next() throws IOException {

        boolean hasNext = false;
        try {
            hasNext = reader.readNext();
        } catch (IOException e) {
            if (this.reader != null) {
                this.reader.close();
            }
            throw e;
        }

        BulkResult result = null;
        String[] row;

        if (hasNext) {
            if ((row = reader.getValues()) != null) {
                result = new BulkResult();
                for (int i = 0; i < this.header.size(); i++) {
                    //We replace the . with _ to add support of relationShip Queries
                    //The relationShip Queries Use . in Salesforce and we use _ in Talend (Studio)
                    //So Account.Name in SF will be Account_Name in Talend
                    result.setValue(header.get(i).replace('.', '_'), row[i]);
                }
                return result;
            } else {
                return next();
            }
        } else {
            if (this.reader != null) {
                this.reader.close();
            }
        }
        return null;

    }

}