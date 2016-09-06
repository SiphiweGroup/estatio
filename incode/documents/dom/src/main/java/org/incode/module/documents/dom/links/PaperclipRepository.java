/*
 *  Copyright 2015 incode.org
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
package org.incode.module.documents.dom.links;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.repository.RepositoryService;

import org.incode.module.documents.dom.docs.Document;
import org.incode.module.documents.dom.docs.DocumentAbstract;

@DomainService(
        nature = NatureOfService.DOMAIN,
        repositoryFor = Paperclip.class
)
public class PaperclipRepository {

    //region > findByDocument (programmatic)
    @Programmatic
    public List<Paperclip> findByDocument(final DocumentAbstract document) {
        return repositoryService.allMatches(
                new QueryDefault<>(Paperclip.class,
                        "findByDocument",
                        "document", document));
    }
    //endregion

    //region > findByAttachedTo (programmatic)
    @Programmatic
    public List<Paperclip> findByAttachedTo(final Object attachedTo) {
        if(attachedTo == null) {
            return null;
        }
        final Bookmark bookmark = bookmarkService.bookmarkFor(attachedTo);
        if(bookmark == null) {
            return null;
        }
        final String attachedToStr = bookmark.toString();
        return repositoryService.allMatches(
                new QueryDefault<>(Paperclip.class,
                        "findByAttachedTo",
                        "attachedToStr", attachedToStr));
    }
    //endregion

    //region > findByAttachedToAndRoleName (programmatic)
    @Programmatic
    public List<Paperclip> findByAttachedToAndRoleName(
            final Object attachedTo,
            final String roleName) {
        if(attachedTo == null) {
            return null;
        }
        if(roleName == null) {
            return null;
        }
        final Bookmark bookmark = bookmarkService.bookmarkFor(attachedTo);
        if(bookmark == null) {
            return null;
        }
        final String attachedToStr = bookmark.toString();
        return repositoryService.allMatches(
                new QueryDefault<>(Paperclip.class,
                        "findByAttachedToAndRoleName",
                        "attachedToStr", attachedToStr,
                        "roleName", roleName));
    }
    //endregion

    //region > attach (programmatic)
    @Programmatic
    public boolean canAttach(
            final DocumentAbstract document,
            final String roleName,
            final Object candidateToAttachTo) {
        final Class<? extends Paperclip> subtype = subtypeClassForElseNull(candidateToAttachTo, roleName);
        return subtype != null;
    }
    //endregion

    //region > attach (programmatic)
    @Programmatic
    public Paperclip attach(
            final DocumentAbstract document,
            final String roleName,
            final Object attachTo) {

        final Class<? extends Paperclip> subtype = subtypeClassFor(attachTo, roleName);

        final Paperclip paperclip = repositoryService.instantiate(subtype);

        paperclip.setDocument(document);
        paperclip.setRoleName(roleName);
        if(document instanceof Document) {
            paperclip.setDocumentCreatedAt(((Document)document).getCreatedAt());
        }

        final Bookmark bookmark = bookmarkService.bookmarkFor(attachTo);
        paperclip.setAttachedTo(attachTo);
        paperclip.setAttachedToStr(bookmark.toString());

        repositoryService.persistAndFlush(paperclip);

        return paperclip;
    }

    private Class<? extends Paperclip> subtypeClassFor(
            final Object classified,
            String type) {
        Class<? extends Paperclip> subtype = subtypeClassForElseNull(classified, type);
        if (subtype != null) {
            return subtype;
        }
        throw new IllegalStateException(String.format(
                "No subtype of Paperclip was found for '%s' and type '%s'; implement the PaperclipRepository.SubtypeProvider SPI",
                classified.getClass().getName(), type));
    }

    private Class<? extends Paperclip> subtypeClassForElseNull(final Object classified, final String type) {
        Class<?> domainClass = classified.getClass();
        for (SubtypeProvider subtypeProvider : subtypeProviders) {
            Class<? extends Paperclip> subtype = subtypeProvider.subtypeFor(domainClass, type);
            if(subtype != null) {
                return subtype;
            }
        }
        return null;
    }

    //endregion


    //region > delete
    @Programmatic
    public void delete(final Paperclip paperclip) {
        repositoryService.remove(paperclip);
    }
    //endregion



    //region > SubtypeProvider SPI

    /**
     * SPI to be implemented (as a {@link DomainService}) for any domain object to which {@link Paperclip}s can be
     * attached.
     */
    public interface SubtypeProvider {
        /**
         * @return the subtype of {@link Paperclip} to use to hold the (type-safe) paperclip of the domain object with respect to the provided roleName.
         */
        @Programmatic
        Class<? extends Paperclip> subtypeFor(
                Class<?> domainObject,
                String roleName);
    }
    /**
     * Convenience adapter to help implement the {@link SubtypeProvider} SPI; ignores the roleName passed into
     * {@link #subtypeFor(Class, String)}, simply returns the class pair passed into constructor.
     */
    public abstract static class SubtypeProviderAbstract implements SubtypeProvider {
        private final Class<?> attachedToDomainType;
        private final Class<? extends Paperclip> attachedToSubtype;

        protected SubtypeProviderAbstract(final Class<?> attachedToDomainType, final Class<? extends Paperclip> attachedToSubtype) {
            this.attachedToDomainType = attachedToDomainType;
            this.attachedToSubtype = attachedToSubtype;
        }

        @Override
        public Class<? extends Paperclip> subtypeFor(final Class<?> domainType, String roleName) {
            return attachedToDomainType.isAssignableFrom(domainType) ? attachedToSubtype : null;
        }
    }

    //endregion

    //region > injected services
    @Inject
    RepositoryService repositoryService;

    @Inject
    BookmarkService bookmarkService;

    @Inject
    List<SubtypeProvider> subtypeProviders;
    //endregion


}
