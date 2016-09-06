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
package org.estatio.app.menus.documents;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.applib.services.classdiscovery.ClassDiscoveryService2;

import org.incode.module.documents.dom.docs.DocumentNature;
import org.incode.module.documents.dom.rendering.Renderer;
import org.incode.module.documents.dom.services.ClassService;

import lombok.Getter;
import lombok.Setter;

@DomainService(nature = NatureOfService.DOMAIN)
public class RendererClassNameService  {

    @ViewModel
    public static class RendererClassViewModel {
        public RendererClassViewModel() {
        }
        public RendererClassViewModel(final Class<? extends Renderer> rendererClass) {
            this.simpleClassName = rendererClass.getSimpleName();
            this.fullyQualifiedClassName = rendererClass.getName();
        }
        @Title
        @Getter @Setter
        private String simpleClassName;
        @Getter @Setter
        private String fullyQualifiedClassName;
    }

    private static final String PACKAGE_PREFIX = "org.estatio";


    @Programmatic
    public Class<? extends Renderer> asRendererClass(final String rendererClassName) {
        return (Class<? extends Renderer>) classService.load(rendererClassName);
    }


    // cached
    private List<Class<? extends Renderer>> cachedRendererClasses;

    @Programmatic
    public List<RendererClassViewModel> renderClassNamesFor(
            final DocumentNature inputNature,
            final DocumentNature outputNature) {
        if(inputNature == null || outputNature == null){
            return Lists.newArrayList();
        }
        if(cachedRendererClasses == null) {
            final Set<Class<? extends Renderer>> rendererClasses = classDiscoveryService2
                    .findSubTypesOfClasses(Renderer.class, PACKAGE_PREFIX);

            cachedRendererClasses = rendererClasses.stream()
                    .filter(x -> !Modifier.isAbstract(x.getModifiers()))
                    .collect(Collectors.toList());

        }
        return Lists.newArrayList(
                cachedRendererClasses.stream()
                        .filter(x -> inputNature.canActAsInputTo(x))
                        .filter(x -> outputNature.canActAsOutputTo(x))
                        .map(x -> new RendererClassViewModel(x) )
                        .collect(Collectors.toList()));
    }


    @Inject
    private ClassDiscoveryService2 classDiscoveryService2;

    @Inject
    private ClassService classService;


}
