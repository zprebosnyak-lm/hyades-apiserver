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
package org.dependencytrack.persistence;

import org.dependencytrack.model.Epss;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.List;

final class EpssQueryManager extends QueryManager implements IQueryManager {

    /**
     * Constructs a new QueryManager.
     *
     * @param pm a PersistenceManager object
     */
    EpssQueryManager(final PersistenceManager pm) {
        super(pm);
    }

    /**
     * Synchronizes a Epss record. Method first checkes to see if the record already
     * exists and if so, updates it. If the record does not already exist,
     * this method will create a new Epss record.
     * @param epss the Epss record to synchronize
     * @return a Epss object
     */
    public Epss synchronizeEpss(Epss epss) {
        Epss result = updateEpss(epss);
        if (result == null) {
            final Epss epssNew = persist(epss);
            return epssNew;
        }
        return result;
    }

    private Epss updateEpss(Epss epss) {
        var epssExisting = getEpssByCveId(epss.getCve());
        if (epssExisting != null) {
            epssExisting.setScore(epss.getScore());
            epssExisting.setPercentile(epss.getPercentile());
            return epssExisting;
        }
        return null;
    }

    /**
     * Returns a Epss record by its CVE id.
     * @param cveId the CVE id of the record
     * @return the matching Epss object, or null if not found
     */
    public Epss getEpssByCveId(String cveId) {
        final Query<Epss> query = pm.newQuery(Epss.class, "cve == :cveId");
        query.setRange(0, 1);
        return singleResult(query.execute(cveId));
    }

    /**
     * Returns a list of Epss records matching list of CVE ids.
     * @param cveIds List of CVE ids to match
     * @return the matching list of Epss, or null if not found
     */
    public List<Epss> getEpssForCveIds(List<String> cveIds) {
        final Query<Epss> query = pm.newQuery(Epss.class);
        query.setFilter(":cveList.contains(cve)");
        return (List<Epss>) query.execute(cveIds);
    }
}
