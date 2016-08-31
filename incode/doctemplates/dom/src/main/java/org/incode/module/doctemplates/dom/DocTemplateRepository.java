/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.incode.module.doctemplates.dom;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.applib.services.repository.RepositoryService;

@DomainService(
        nature = NatureOfService.DOMAIN,
        repositoryFor = DocTemplate.class
)
public class DocTemplateRepository {


    @Programmatic
    public DocTemplate create(
            final String reference,
            final String atPath,
            final String templateText) {
        final DocTemplate docTemplate = new DocTemplate(reference, atPath, templateText);
        repositoryService.persist(docTemplate);
        return docTemplate;
    }


    @Programmatic
    public DocTemplate findByReferenceAndAtPath(
            final String reference,
            final String atPath) {
        return repositoryService.firstMatch(
                new QueryDefault<>(DocTemplate.class,
                        "findByReferenceAndAtPath",
                        "reference", reference,
                        "atPath", atPath));
    }


    @Programmatic
    public List<DocTemplate> allTemplates() {
        return repositoryService.allInstances(DocTemplate.class);
    }

    //region > injected services

    @Inject
    RepositoryService repositoryService;

    @Inject ServiceRegistry2 serviceRegistry;

    //endregion

}
