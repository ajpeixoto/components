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
package org.talend.components.api.component;

/**
 * Use to define product type which is supported by component.
 */
public interface SupportedProduct {

    public final static String ALL = "ALL";

    public final static String DI = "DI";

    public final static String MAP_REDUCE = "MAP_REDUCE";

    public final static String STORM = "STORM";

    public final static String SPARK = "SPARK";

    public final static String SPARKSTREAMING = "SPARKSTREAMING";

    public final static String DATAPREP = "DATAPREP";

    public final static String DATASTREAMS = "DATASTREAMS";

}
