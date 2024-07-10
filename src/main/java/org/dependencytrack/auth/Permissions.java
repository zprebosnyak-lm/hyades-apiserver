/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.auth;

/**
 * Defines permissions specific to Dependency-Track.
 *
 * @author Steve Springett
 * @since 3.0.0
 */
public enum Permissions {

    BOM_UPLOAD("Allows the ability to upload CycloneDX Software Bill of Materials (SBOM)"),
    VIEW_PORTFOLIO("Provides the ability to view the portfolio of projects, components, and licenses"),
    PORTFOLIO_MANAGEMENT("Allows the creation, modification, and deletion of data in the portfolio"),
    PORTFOLIO_MANAGEMENT_CREATE("Allows the creation of data in the portfolio"),
    PORTFOLIO_MANAGEMENT_READ("Allows the reading of data in the portfolio"),
    PORTFOLIO_MANAGEMENT_UPDATE("Allows the updating of data in the portfolio"),
    PORTFOLIO_MANAGEMENT_DELETE("Allows the deletion of data in the portfolio"),
    VIEW_VULNERABILITY("Provides the ability to view the vulnerabilities projects are affected by"),
    VULNERABILITY_ANALYSIS("Provides all abilities to make analysis decisions on vulnerabilities"),
    VULNERABILITY_ANALYSIS_CREATE("Provides the ability to upload supported VEX documents to a project"),
    VULNERABILITY_ANALYSIS_READ("Provides the ability read the VEX document for a project"),
    VULNERABILITY_ANALYSIS_UPDATE("Provides the ability to make analysis decisions on vulnerabilities and upload supported VEX documents for a project"),
    VIEW_POLICY_VIOLATION("Provides the ability to view policy violations"),
    VULNERABILITY_MANAGEMENT("Allows all management permissions of internally-defined vulnerabilities"),
    VULNERABILITY_MANAGEMENT_CREATE("Allows creation of internally-defined vulnerabilities"),
    VULNERABILITY_MANAGEMENT_READ("Allows reading internally-defined vulnerabilities"),
    VULNERABILITY_MANAGEMENT_UPDATE("Allows updating internally-defined vulnerabilities and vulnerability tags"),
    VULNERABILITY_MANAGEMENT_DELETE("Allows management of internally-defined vulnerabilities"),
    POLICY_VIOLATION_ANALYSIS("Provides the ability to make analysis decisions on policy violations"),
    ACCESS_MANAGEMENT("Allows the management of users, teams, and API keys"),
    ACCESS_MANAGEMENT_CREATE("Allows create permissions of users, teams, and API keys"),
    ACCESS_MANAGEMENT_READ("Allows read permissions of users, teams, and API keys"),
    ACCESS_MANAGEMENT_UPDATE("Allows update permissions of users, teams, and API keys"),
    ACCESS_MANAGEMENT_DELETE("Allows delete permissions of users, teams, and API keys"),
    SYSTEM_CONFIGURATION("Allows all access to configuration of the system including notifications, repositories, and email settings"),
    SYSTEM_CONFIGURATION_CREATE("Allows creating configuration of the system including notifications, repositories, and email settings"),
    SYSTEM_CONFIGURATION_READ("Allows reading the configuration of the system including notifications, repositories, and email settings"),
    SYSTEM_CONFIGURATION_UPDATE("Allows updating the configuration of the system including notifications, repositories, and email settings"),
    SYSTEM_CONFIGURATION_DELETE("Allows deleting the configuration of the system including notifications, repositories, and email settings"),
    PROJECT_CREATION_UPLOAD("Provides the ability to optionally create project (if non-existent) on BOM or scan upload"),
    POLICY_MANAGEMENT("Allows the creation, modification, and deletion of policy"),
    POLICY_MANAGEMENT_CREATE("Allows the creation of a policy"),
    POLICY_MANAGEMENT_READ("Allows reading of policies"),
    POLICY_MANAGEMENT_UPDATE("Allows the modification of a policy"),
    POLICY_MANAGEMENT_DELETE("Allows the deletion of a policy");

    private final String description;

    Permissions(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static class Constants {
        public static final String BOM_UPLOAD = "BOM_UPLOAD";
        public static final String VIEW_PORTFOLIO = "VIEW_PORTFOLIO";
        public static final String PORTFOLIO_MANAGEMENT = "PORTFOLIO_MANAGEMENT";
        public static final String PORTFOLIO_MANAGEMENT_CREATE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String PORTFOLIO_MANAGEMENT_READ = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String PORTFOLIO_MANAGEMENT_UPDATE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String PORTFOLIO_MANAGEMENT_DELETE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String VIEW_VULNERABILITY = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String VULNERABILITY_ANALYSIS = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String VULNERABILITY_ANALYSIS_CREATE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String VULNERABILITY_ANALYSIS_READ = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String VULNERABILITY_ANALYSIS_UPDATE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String VIEW_POLICY_VIOLATION = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String VULNERABILITY_MANAGEMENT = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String VULNERABILITY_MANAGEMENT_CREATE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String VULNERABILITY_MANAGEMENT_READ = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String VULNERABILITY_MANAGEMENT_UPDATE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String VULNERABILITY_MANAGEMENT_DELETE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String POLICY_VIOLATION_ANALYSIS = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String ACCESS_MANAGEMENT = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String ACCESS_MANAGEMENT_CREATE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String ACCESS_MANAGEMENT_READ = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String ACCESS_MANAGEMENT_UPDATE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String ACCESS_MANAGEMENT_DELETE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String SYSTEM_CONFIGURATION = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String SYSTEM_CONFIGURATION_CREATE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String SYSTEM_CONFIGURATION_READ = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String SYSTEM_CONFIGURATION_UPDATE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String SYSTEM_CONFIGURATION_DELETE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String PROJECT_CREATION_UPLOAD = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String POLICY_MANAGEMENT = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String POLICY_MANAGEMENT_CREATE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String POLICY_MANAGEMENT_READ = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String POLICY_MANAGEMENT_UPDATE = "PORTFOLIO_MANAGEMENT_CREATE";
        public static final String POLICY_MANAGEMENT_DELETE = "PORTFOLIO_MANAGEMENT_CREATE";
    }

}
