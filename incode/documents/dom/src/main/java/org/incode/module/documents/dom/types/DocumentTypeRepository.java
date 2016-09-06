/*
 *  Copyright 2016 Dan Haywood
 *
 *  Licensed under the Apache License, Version 2.0 (the
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
package org.incode.module.documents.dom.types;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.repository.RepositoryService;

@DomainService(
        nature = NatureOfService.DOMAIN,
        repositoryFor = DocumentType.class
)
public class DocumentTypeRepository {


    @Programmatic
    public DocumentType create(
            final String reference,
            final String name) {
        final DocumentType documentType = new DocumentType(reference, name);
        repositoryService.persist(documentType);
        return documentType;
    }


    @Programmatic
    public DocumentType findByReference(
            final String reference) {
        return repositoryService.firstMatch(
                new QueryDefault<>(DocumentType.class,
                        "findByReference",
                        "reference", reference));
    }


    @Programmatic
    public List<DocumentType> allTypes() {
        return repositoryService.allInstances(DocumentType.class);
    }


    //region > injected services
    @Inject
    RepositoryService repositoryService;
    //endregion

}
