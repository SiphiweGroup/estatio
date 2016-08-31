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
package org.isisaddons.module.freemarker.dom.spi;

/**
 * Must be implemented by consuming application.
 *
 * <p>
 * Typically implemented by a "glue" domain service that retrieve a template from an appropriate repository.
 * For example, the incode-module-doctemplates module provides the DocTemplate and DocTemplateRepository
 * </p>
 */
public interface FreeMarkerTemplateLoader {

    TemplateSource load(final String reference, final String atPath);

    /**
     * For testing purposes.
     */
    class Simple implements FreeMarkerTemplateLoader {

        private final String templateText;
        private final long version;

        public Simple(final String templateText, final long version) {
            this.templateText = templateText;
            this.version = version;
        }

        @Override
        public TemplateSource load(final String reference, final String atPath) {
            return new TemplateSource(templateText, version);
        }
    }


}



