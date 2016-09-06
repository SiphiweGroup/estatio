/*
 *  Copyright 2016 Eurocommercial Properties NV
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
package org.estatio.fixture.documents;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.wicket.util.io.IOUtils;
import org.joda.time.LocalDate;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

import org.incode.module.documents.dom.rendering.RenderingStrategy;
import org.incode.module.documents.dom.docs.DocumentTemplate;
import org.incode.module.documents.dom.docs.DocumentTemplateRepository;
import org.incode.module.documents.dom.types.DocumentType;
import org.incode.module.documents.dom.types.DocumentTypeRepository;

import org.estatio.fixture.EstatioFixtureScript;

public abstract class DocumentTemplateAbstract extends EstatioFixtureScript {

    @Override
    protected abstract void execute(ExecutionContext executionContext);

    protected Clob readSvgResourceAsClob(String fileName) {
        try {
            InputStream is = getClass().getResourceAsStream("/svg/" + fileName);
            Clob blob = new Clob(fileName, "image/svg+xml", IOUtils.toCharArray(is));
            is.close();
            return blob;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * convenience, as templates and types often created together
     * @param reference
     * @param name
     * @param executionContext
     * @return
     */
    protected DocumentType createType(
            String reference,
            String name,
            ExecutionContext executionContext) {

        final DocumentType documentType = documentTypeRepository.create(reference, name);
        return executionContext.addResult(this, documentType);
    }


    protected DocumentTemplate createDocumentTextTemplate(
            final DocumentType documentType,
            final LocalDate date,
            final String name,
            final String mimeType,
            final String atPath,
            final String text, final String dataModelClassName,
            final RenderingStrategy renderingStrategy,
            ExecutionContext executionContext) {

        final DocumentTemplate documentTemplate = documentTemplateRepository
                .createText(documentType, date, atPath, name, mimeType, text, dataModelClassName, renderingStrategy);
        return executionContext.addResult(this, documentTemplate);
    }

    protected DocumentTemplate createDocumentClobTemplate(
            final DocumentType documentType,
            final LocalDate date,
            final String atPath,
            final Clob clob,
            final String dataModelClassName,
            final RenderingStrategy renderingStrategy,
            ExecutionContext executionContext) {

        final DocumentTemplate documentTemplate = documentTemplateRepository
                .createClob(documentType, date, atPath, clob, dataModelClassName, renderingStrategy);
        return executionContext.addResult(this, documentTemplate);
    }

    protected DocumentTemplate createDocumentBlobTemplate(
            final DocumentType documentType,
            final LocalDate date,
            final String atPath,
            final Blob blob,
            final String dataModelClassName,
            final RenderingStrategy renderingStrategy,
            ExecutionContext executionContext) {

        final DocumentTemplate documentTemplate = documentTemplateRepository
                .createBlob(documentType, date, atPath, blob, dataModelClassName, renderingStrategy);
        return executionContext.addResult(this, documentTemplate);
    }

    @Inject
    protected DocumentTemplateRepository documentTemplateRepository;

    @Inject
    protected DocumentTypeRepository documentTypeRepository;

}
