/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
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
package org.estatio.app.menus.link;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.title.TitleService;

import org.isisaddons.module.security.dom.tenancy.WithApplicationTenancy;
import org.isisaddons.module.stringinterpolator.dom.StringInterpolatorService;

import org.incode.module.documents.dom.DocumentsModule;
import org.incode.module.documents.dom.docs.DocumentAbstract;
import org.incode.module.documents.dom.docs.DocumentTemplate;
import org.incode.module.documents.dom.links.PaperclipRepository;

public abstract class T_reports<T extends WithApplicationTenancy>  extends T_documentTemplates<T> {

    public T_reports(final T domainObject, final String... docTypes) {
        super(domainObject, docTypes);
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT, restrictTo = RestrictTo.PROTOTYPING)
    @ActionLayout(contributed = Contributed.AS_ACTION)
    public DocumentAbstract $$(
            final DocumentTemplate template,
            @Parameter(maxLength = DocumentsModule.JdoColumnLength.NAME, optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Document name")
            final String documentName,
            @Parameter(maxLength = DocumentsModule.JdoColumnLength.NAME, optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Role name")
            final String roleName) throws IOException {
        final StringInterpolatorService.Root root = newRoot();
        final String documentNameToUse = documentNameOf(domainObject, template, documentName);
        final DocumentAbstract doc = template.render(root, documentNameToUse);
        paperclipRepository.attach(doc, roleName, domainObject);
        return doc;
    }

    public boolean hide$$() {
        return choices0$$().isEmpty();
    }

    public List<DocumentTemplate> choices0$$() {
        return getDocumentTemplates();
    }

    protected String documentNameOf(
            final T domainObject,
            final DocumentTemplate template,
            final String documentName) {
        return documentName != null ? documentName : titleService.titleOf(domainObject);
    }


    @Inject
    PaperclipRepository paperclipRepository;

    @Inject
    TitleService titleService;

}
