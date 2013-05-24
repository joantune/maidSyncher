/*******************************************************************************
 * Copyright (c) 2013 Instituto Superior Técnico - João Antunes
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Luis Silva - ACGHSync
 *     João Antunes - initial API and implementation
 ******************************************************************************/
package pt.ist.maidSyncher.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ObjectUtils;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.joda.time.LocalTime;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.core.WriteOnReadError;
import pt.ist.maidSyncher.api.activeCollab.ACContext;
import pt.ist.maidSyncher.api.activeCollab.ACObject;
import pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor;
import pt.ist.maidSyncher.domain.SyncEvent.SyncUniverse;
import pt.ist.maidSyncher.domain.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.exceptions.SyncEventIllegalConflict;
import pt.ist.maidSyncher.domain.exceptions.SyncEventOriginObjectChanged;
import pt.ist.maidSyncher.domain.github.GHObject;
import pt.ist.maidSyncher.domain.sync.APIObjectWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.utils.MiscUtils;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class SynchableObject extends SynchableObject_Base {

    private static final Logger LOGGER = LoggerFactory.getLogger(SynchableObject.class);

    //The fields for operations with PropertyDescriptor s
    public static final String DSC_LAST_SYNC_TIME = "lastSynchTime"; //not used
    public static final String DSC_ID = "id";
    public static final String DSC_URL = "url";

    public SynchableObject() {
        super();
    }

    protected abstract DSIObject getDSIObject();

    public abstract DSIObject findOrCreateDSIObject();

    static void checkProccessPreconditions(Class clazz, Object object) {
        checkNotNull(object);
        checkArgument(clazz.isAssignableFrom(object.getClass()),
                "Object's class must be a class (or superclass) of " + clazz.getName());
    }

    public static enum ObjectFindStrategy {
        FIND_BY_ID {
            @Override
            public Optional<SynchableObject> find(Object object, Class<? extends SynchableObject> clazz, Iterable iterable) {
                long id;
                try {
                    id = Long.valueOf(object.getClass().getMethod("getId").invoke(object).toString());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                        | SecurityException e1) {
                    throw new UnsupportedOperationException("Class: " + object.getClass().getName()
                            + " has no usable getter for an id");
                }

                return Iterables.tryFind(iterable, new PredicateFindGHObjectByClassAndId(clazz, id));
            }
        },
        FIND_BY_URL {
            @Override
            public Optional<SynchableObject> find(Object object, Class<? extends SynchableObject> clazz, Iterable iterable) {

                String url;
                try {
                    url = (String) object.getClass().getMethod("getUrl").invoke(object);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                        | SecurityException e1) {
                    throw new UnsupportedOperationException("Class: " + object.getClass().getName()
                            + " has no usable getter for an url");
                }

                try {
                    return Iterables.tryFind(iterable, new PredicateFindGHObjectByClassAndUrl(clazz, url));
                } catch (MalformedURLException e) {
                    LOGGER.error("trying to find an object by url with a malformed url on the origin object"
                            + " origin object class: " + object.getClass().getName() + " url: " + url, e);
                    throw new IllegalArgumentException(e);
                }

            }
        };

        public static class PredicateFindGHObjectByClassAndId implements Predicate<SynchableObject> {

            private final Class<? extends SynchableObject> clazz;
            private final long id;

            public PredicateFindGHObjectByClassAndId(Class<? extends SynchableObject> clazz, long id) {
                checkNotNull(clazz);
                checkArgument(id > 0, "The id must be > 0");
                this.clazz = clazz;
                this.id = id;
            }

            @Override
            public boolean apply(SynchableObject input) {
                if (input == null)
                    return false;
                if (this.clazz.isAssignableFrom(input.getClass()) && input.getId() == id)
                    return true;
                return false;
            }

        }

        public static class PredicateFindGHObjectByClassAndUrl implements Predicate<SynchableObject> {

            private final Class<? extends SynchableObject> clazz;
            private final URL url;

            public PredicateFindGHObjectByClassAndUrl(Class<? extends SynchableObject> clazz, String url)
                    throws MalformedURLException {
                checkNotNull(clazz);
                this.url = new URL(url);
                this.clazz = clazz;
            }

            @Override
            public boolean apply(SynchableObject input) {
                if (input == null)
                    return false;
                URL urlOfInput = null;
                try {
                    urlOfInput = new URL(input.getUrl());
                } catch (MalformedURLException ex) {
                    LOGGER.warn("Found a persisted GHObject with a malformed URL. Object: " + input.getExternalId(), ex);
                    return false;
                }
                if (this.clazz.isAssignableFrom(input.getClass()) && this.url.equals(urlOfInput))
                    return true;
                return false;
            }

        }

        public abstract Optional<SynchableObject> find(Object object, Class<? extends SynchableObject> clazz, Iterable iterable);

    }

    protected static SynchableObject findOrCreateAndProccess(Object object, Class<? extends SynchableObject> clazz,
            Iterable iterable, boolean skipGenerateSyncEvent, ObjectFindStrategy... optionalObjectFindStrategy)
                    throws SyncEventIllegalConflict {
        SynchableObject toProccessAndReturn = null;
        checkNotNull(object);
        checkNotNull(iterable);
//        checkArgument(clazz.isAssignableFrom(object.getClass()),
//                "Object's class must be a class (or superclass) of " + clazz.getName());

        ObjectFindStrategy optionalObjectFindStrategyToBeUsed = null;
        if (optionalObjectFindStrategy == null || optionalObjectFindStrategy.length == 0) {
            optionalObjectFindStrategyToBeUsed = ObjectFindStrategy.FIND_BY_ID;
        } else if (optionalObjectFindStrategy.length >= 1) {
            optionalObjectFindStrategyToBeUsed = optionalObjectFindStrategy[0];
        }

        Optional<SynchableObject> synchableObject = optionalObjectFindStrategyToBeUsed.find(object, clazz, iterable);
        if (synchableObject.isPresent()) {
            toProccessAndReturn = synchableObject.get();
        } else {
            //let's create it
            try {
                toProccessAndReturn = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                if (e.getCause() instanceof WriteOnReadError)
                    throw (WriteOnReadError) e.getCause();
                throw new UnsupportedOperationException("Class: " + clazz.getName()
                        + " has no usable, simple, e.g. Constructor() constructor", e);
            }
        }

        //let's copy the values
        HashSet<PropertyDescriptor> changedDescriptors = new HashSet<>();
        try {
            changedDescriptors.addAll(toProccessAndReturn.copyPropertiesFrom(object));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            if (e.getCause() instanceof WriteOnReadError)
                throw ((WriteOnReadError) e.getCause());
            throw new IllegalArgumentException("There were problems copying properties from object of class "
                    + object.getClass().getName() + " to " + toProccessAndReturn.getClass().getName() + " oid: "
                    + toProccessAndReturn.getExternalId(), e);
        }

        if (!skipGenerateSyncEvent && changedDescriptors.isEmpty() == false) {
            //we changed something, let's create and add the syncEvent to the ChangesBuzz
            generateSyncEvent(toProccessAndReturn, changedDescriptors, object);
        }
        else if (skipGenerateSyncEvent == true && toProccessAndReturn.getDSIObject() == null) {
            //we should create the DSIObject on this file
            try {
                toProccessAndReturn.findOrCreateDSIObject();
            } catch (UnsupportedOperationException ex) {
                //continue silently
            }

        }

        return toProccessAndReturn;

    }

    /**
     * 
     * @param object the object from the API containing all the data
     * @param clazz the class of the object to find
     * @param iterable the iterable where to find it
     * @return
     * @throws SyncEventIllegalConflict
     * @throws TaskNotVisibleException
     */
    protected static SynchableObject findOrCreateAndProccess(Object object, Class<? extends SynchableObject> clazz,
            Iterable iterable, ObjectFindStrategy... optionalObjectFindStrategy) throws SyncEventIllegalConflict {
        return findOrCreateAndProccess(object, clazz, iterable, false, optionalObjectFindStrategy);

    }

    private static RequestProcessor acRequestProcessor = ACContext.getInstance();

    private static void generateSyncEvent(final SynchableObject toProccessAndReturn,
            final Collection<PropertyDescriptor> changedDescriptors, final Object apiObject) throws SyncEventIllegalConflict {
        final SynchableObject originObject = toProccessAndReturn;
        SyncEvent.TypeOfChangeEvent typeOfChange = null;
        DSIObject dsiObject = toProccessAndReturn.getDSIObject();
        if (dsiObject == null || dsiObject.getLastSynchedAt() == null) {
            typeOfChange = TypeOfChangeEvent.CREATE;
        }
        try {
            dsiObject = toProccessAndReturn.findOrCreateDSIObject();

        } catch (UnsupportedOperationException ex) {
            LOGGER.debug(toProccessAndReturn.getClass().getName() + " doesn't support Synch");
        }
        if (dsiObject != null && dsiObject.getLastSynchedAt() != null) {
            if (typeOfChange == null) {
                typeOfChange = TypeOfChangeEvent.UPDATE;
            }

            SyncUniverse syncUniverse = null;
            if (originObject instanceof pt.ist.maidSyncher.domain.activeCollab.ACObject) {
                syncUniverse = SyncUniverse.GITHUB;
            } else if (originObject instanceof GHObject) {
                syncUniverse = SyncUniverse.ACTIVE_COLLAB;
            }
            SyncEvent syncEvent =
                    new SyncEvent(toProccessAndReturn.getUpdatedAtDate(), typeOfChange, changedDescriptors, dsiObject,
                            new APIObjectWrapper() {

                        @Override
                        public void validateAPIObject() throws SyncEventOriginObjectChanged {
                            try {

                                Object currentAPIObject = null;

                                if (apiObject instanceof ACObject) {
                                    //let's get the object from the repository
                                    ACObject acObject = (ACObject) apiObject;
                                    Constructor<? extends Object> constructor = null;
                                    JSONObject jsonObject = null;

                                    jsonObject = (JSONObject) acRequestProcessor.processGet(acObject.getUrl());
                                    constructor = apiObject.getClass().getConstructor(JSONObject.class);
                                    currentAPIObject = constructor.newInstance(jsonObject);

                                } else {
                                    //if it's not an active collab one, let's assume it's a GH
                                    GitHubRequest gitHubRequest = new GitHubRequest();
                                    getPropertyDescriptorAndCheckItExists(apiObject, "url");
                                    String uri = null;
                                    uri = (String) PropertyUtils.getSimpleProperty(apiObject, "url");
                                    gitHubRequest.setUri(uri);
                                    currentAPIObject = MaidRoot.getGitHubClient().get(gitHubRequest).getBody();

                                }

                                Collection<PropertyDescriptor> changedProperties;
                                changedProperties = propertiesEqual(apiObject, currentAPIObject);
                                if (changedProperties.isEmpty() == false) {
                                    String changedDescriptors = "";
                                    for (PropertyDescriptor changedDescriptor : changedProperties)
                                        changedDescriptors += " " + changedDescriptor.getName();

                                    throw new SyncEventOriginObjectChanged("Origin object: "
                                            + originObject.getClass().getSimpleName() + " unequal descriptors:"
                                            + changedDescriptors);

                                }
                            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                    | InvocationTargetException | IOException | NoSuchMethodException | SecurityException e) {
                                throw new SyncEventOriginObjectChanged(
                                        "Could not assert if Origin object changed. Origin obj: "
                                                + apiObject.getClass().getSimpleName(), e);
                            }

                        }

                        @Override
                        public Object getAPIObject() {
                            return apiObject;
                        }
                    }, syncUniverse, originObject);
            addSyncEvent(syncEvent);

        }
    }

    protected static void addSyncEvent(SyncEvent syncEvent) {
        MaidRoot.getInstance().addSyncEvent(syncEvent);
        logSync(syncEvent);
    }

    private static void logSync(SyncEvent syncEvent) {
        SynchableObject originObject = syncEvent.getOriginObject();
        Set<PropertyDescriptor> changedPropertyDescriptors = syncEvent.getChangedPropertyDescriptors();
        String logContent =
                "Added Sync event with origin object: " + originObject.getExternalId() + " class: "
                        + originObject.getClass().getName() + " changed properties: ";
        for (PropertyDescriptor descriptor : changedPropertyDescriptors) {
            logContent += " " + descriptor.getName();
        }
        logContent += " type: " + syncEvent.getTypeOfChangeEvent();
        LOGGER.debug(logContent);

    }

    public abstract LocalTime getUpdatedAtDate();

    /**
     * 
     * @param syncEvent the syncEvent to process
     * @return the {@link SyncActionWrapper} with the PropertyDescriptors 'ticked' i.e. acted upon - used to trace which ones we
     *         weren't able to 'reach' due to
     *         coding errors - and a method that will do the event
     */
    public abstract SyncActionWrapper sync(SyncEvent syncEvent);

    /**
     * 
     * @param orig
     * @return a collection with the {@link PropertyDescriptor} s that changed
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws TaskNotVisibleException
     */
    public Collection<PropertyDescriptor> copyPropertiesFrom(Object orig) throws IllegalAccessException,
    InvocationTargetException, NoSuchMethodException, TaskNotVisibleException {
        Set<PropertyDescriptor> propertyDescriptorsThatChanged = new HashSet<PropertyDescriptor>();

        Object dest = this;

        if (orig == null) {
            throw new IllegalArgumentException("No origin bean specified");
        }
        PropertyDescriptor origDescriptors[] = PropertyUtils.getPropertyDescriptors(orig);
        for (PropertyDescriptor origDescriptor : origDescriptors) {
            String name = origDescriptor.getName();
            if (PropertyUtils.isReadable(orig, name)) {
                if (PropertyUtils.isWriteable(dest, name)) {
                    Object valueDest = PropertyUtils.getSimpleProperty(dest, name);
                    Object valueOrigin = PropertyUtils.getSimpleProperty(orig, name);
                    if (valueOrigin != null
                            && valueOrigin.getClass().getPackage().getName().equalsIgnoreCase("org.eclipse.egit.github.core"))
                        continue; //let's skip the properties with egit core objects (they shall be copied from a custom overriden version of this method)
                    if (valueOrigin instanceof Date)
                        valueOrigin = LocalTime.fromDateFields((Date) valueOrigin);
                    if (Objects.equal(valueDest, valueOrigin) == false)
                        propertyDescriptorsThatChanged.add(origDescriptor);
                    try {
                        //let's see if this is actually a Date, if so, let's convert it
                        PropertyUtils.setSimpleProperty(dest, name, valueOrigin);
                    } catch (IllegalArgumentException ex) {
                        throw new Error("setSimpleProperty returned an exception, dest: " + dest.getClass().getName() + " oid: "
                                + ((DomainObject) dest).getExternalId() + " name : " + name + " valueOrig: " + valueOrigin, ex);
                    }
                    LOGGER.trace("Copied property " + name + " from " + orig.getClass().getName() + " object to a "
                            + dest.getClass().getName() + " oid: " + getExternalId());
                }
            }
        }

        return propertyDescriptorsThatChanged;
    }

    public void copyPropertiesTo(Object dest) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,
    TaskNotVisibleException {
        Set<PropertyDescriptor> propertyDescriptorsThatChanged = new HashSet<PropertyDescriptor>();

        Object orig = this;
        checkNotNull(dest);

        PropertyDescriptor origDescriptors[] = PropertyUtils.getPropertyDescriptors(orig);
        for (PropertyDescriptor origDescriptor : origDescriptors) {
            String name = origDescriptor.getName();
            if (PropertyUtils.isReadable(orig, name)) {
                PropertyDescriptor destDescriptor = PropertyUtils.getPropertyDescriptor(dest, origDescriptor.getName());
                if (PropertyUtils.isWriteable(dest, name)
                        || (destDescriptor != null && MiscUtils.getWriteMethodIncludingFlowStyle(destDescriptor, dest.getClass()) != null)) {
                    Object valueDest = PropertyUtils.getSimpleProperty(dest, name);
                    Object valueOrigin = PropertyUtils.getSimpleProperty(orig, name);

                    LOGGER.debug("OrigDescriptor PropertyType: " + origDescriptor.getPropertyType().getName());
//                    System.out.println("OrigDescriptor PropertyType: " + origDescriptor.getPropertyType().getName());
                    //let's ignore the properties were the values are our domain packages
                    if (valueOrigin != null && (SynchableObject.class.isAssignableFrom(valueOrigin.getClass()))) {
//                        System.out.println("Skipping");
                        continue; //let's skip these properties
                    }
                    if (SynchableObject.class.isAssignableFrom(origDescriptor.getPropertyType())) {
//                        System.out.println("Skipping");
                        continue;
                    }
                    if (origDescriptor instanceof IndexedPropertyDescriptor) {
                        IndexedPropertyDescriptor indexedPropertyDescriptor = (IndexedPropertyDescriptor) origDescriptor;
//                        System.out.println("OrigDescriptor IndexedPropertyDescriptor: " + indexedPropertyDescriptor.getName());
                        if (SynchableObject.class.isAssignableFrom(indexedPropertyDescriptor.getIndexedPropertyType())) {
//                            System.out.println("Skipping");
                            continue;
                        }

                    }

                    //let's ignore all of the dates - as they should be filled by
                    //the system
                    if (valueOrigin instanceof LocalTime)
                        continue;
                    if (Objects.equal(valueDest, valueOrigin) == false)
                        propertyDescriptorsThatChanged.add(origDescriptor);
                    try {
                        if (PropertyUtils.isWriteable(dest, name) == false) {
                            //let's use the flow version
                            Class<?> origPropertyType = origDescriptor.getPropertyType();
                            Method writeMethodIncludingFlowStyle =
                                    MiscUtils.getWriteMethodIncludingFlowStyle(destDescriptor, dest.getClass());
                            if (Arrays.asList(writeMethodIncludingFlowStyle.getParameterTypes()).contains(origPropertyType)) {
                                writeMethodIncludingFlowStyle.invoke(dest, valueOrigin);
                            } else {
                                continue;
                            }

                        } else {
                            PropertyUtils.setSimpleProperty(dest, name, valueOrigin);

                        }
                    } catch (IllegalArgumentException ex) {
                        throw new Error("setSimpleProperty returned an exception, dest: " + dest.getClass().getName()
                                + " name : " + name + " valueOrig: " + valueOrigin, ex);
                    }
                    LOGGER.trace("Copied property " + name + " from " + orig.getClass().getName() + " object to a "
                            + dest.getClass().getName() + " oid: " + getExternalId());
                }
//                System.out.println("--");
            }
        }

    }

    /**
     * 
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws TaskNotVisibleException
     * @throws IllegalArgumentException if the property descriptors of the arguments differ
     * 
     * @return a collection of {@link PropertyDescriptor} that changed, or an empty collection
     */
    public static Collection<PropertyDescriptor> propertiesEqual(Object object1, Object object2) throws IllegalAccessException,
    InvocationTargetException, NoSuchMethodException, TaskNotVisibleException, IllegalArgumentException {

        List<PropertyDescriptor> changedDescriptors = new ArrayList<>();

        if (object1 == null || object2 == null) {
            throw new IllegalArgumentException("Both objects must be non null");
        }
        PropertyDescriptor object1Descriptors[] = PropertyUtils.getPropertyDescriptors(object1);
        PropertyDescriptor object2Descriptors[] = PropertyUtils.getPropertyDescriptors(object2);

        //let's make sure that they match
        checkArgument(ObjectUtils.equals(object1Descriptors, object2Descriptors), "Error, object1 : "
                + object1.getClass().getSimpleName() + " and object2 : " + object2.getClass().getSimpleName() + " don't match");

        for (PropertyDescriptor object1Descriptor : object1Descriptors) {
            String name = object1Descriptor.getName();
            if (PropertyUtils.isReadable(object1, name) && PropertyUtils.isReadable(object2, name)) {

                //if both are readable, let's check on the values
                Object valueObject1 = PropertyUtils.getSimpleProperty(object1, name);
                Object valueObject2 = PropertyUtils.getSimpleProperty(object2, name);
//                if (isGitHubObject(valueObject1) || isGitHubObject(valueObject2))
//                    continue; //let's skip the GitHub properties, we won't be able to compare that

                if (!ObjectUtils.equals(valueObject1, valueObject2))
                    changedDescriptors.add(object1Descriptor);
            }
        }
        return changedDescriptors;
    }

    private static boolean isGitHubObject(Object valueObject1) {
        return valueObject1 != null
                && valueObject1.getClass().getPackage().getName().equalsIgnoreCase("org.eclipse.egit.github.core");
    }

    protected static PropertyDescriptor getPropertyDescriptorAndCheckItExists(Object bean, String propertyName) {
        PropertyDescriptor propertyDescriptor;
        try {
            propertyDescriptor = PropertyUtils.getPropertyDescriptor(bean, propertyName);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new Error(e);
        }
        checkNotNull(propertyDescriptor);
        return propertyDescriptor;
    }
}
