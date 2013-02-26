package pt.ist.maidSyncher.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.beanutils.PropertyUtils;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.pstm.IllegalWriteException;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class SynchableObject extends SynchableObject_Base {

    private static final Logger LOGGER = LoggerFactory.getLogger(SynchableObject.class);

    public SynchableObject() {
        super();
    }

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
                    id = (long) object.getClass().getMethod("getId").invoke(object);
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
                            + " origin object class: " + object.getClass().getName(), e);
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

    /**
     * 
     * @param object the object from the API containing all the data
     * @param clazz the class of the object to find
     * @param iterable the iterable where to find it
     * @return
     * @throws TaskNotVisibleException
     */
    @Service
    protected static SynchableObject findOrCreateAndProccess(Object object, Class<? extends SynchableObject> clazz,
            Iterable iterable,
            ObjectFindStrategy... optionalObjectFindStrategy) {
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
                if (e.getCause() instanceof IllegalWriteException)
                    throw (IllegalWriteException) e.getCause();
                throw new UnsupportedOperationException("Class: " + clazz.getName()
                        + " has no usable, simple, e.g. Constructor() constructor", e);
            }
        }

        //let's copy the values
        boolean triggerSync = false;
        try {
            triggerSync = toProccessAndReturn.copyPropertiesFrom(object);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            if (e.getCause() instanceof IllegalWriteException)
                throw ((IllegalWriteException) e.getCause());
            throw new IllegalArgumentException("There were problems copying properties from object of class "
                    + object.getClass().getName() + " to " + toProccessAndReturn.getClass().getName() + " oid: "
                    + toProccessAndReturn.getExternalId(), e);
        }

        if (triggerSync) {
            LOGGER.debug("Synching " + toProccessAndReturn.getExternalId() + " class: "
                    + toProccessAndReturn.getClass().getName());
            toProccessAndReturn.sync(object);
        }

        return toProccessAndReturn;
    }

    public abstract void sync(Object objectThatTriggeredTheSync);

    public boolean copyPropertiesFrom(Object orig) throws IllegalAccessException, InvocationTargetException,
    NoSuchMethodException, TaskNotVisibleException {
        boolean changesWereMade = false;
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
                    if (valueOrigin instanceof Date)
                        valueOrigin = LocalTime.fromDateFields((Date) valueOrigin);
                    if (Objects.equal(valueDest, valueOrigin) == false)
                        changesWereMade = true;
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

        return changesWereMade;
    }
}
