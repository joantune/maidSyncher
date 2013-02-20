package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.pstm.IllegalWriteException;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class GHObject extends GHObject_Base {

    private static final Logger LOGGER = LoggerFactory.getLogger(GHObject.class);

    public static enum ObjectFindStrategy {
        FIND_BY_ID {
            @Override
            public Optional<GHObject> find(Object object, Class<? extends GHObject> clazz, Iterable iterable) {
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
            public Optional<GHObject> find(Object object, Class<? extends GHObject> clazz, Iterable iterable) {

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

        public static class PredicateFindGHObjectByClassAndId implements Predicate<GHObject> {

            private final Class<? extends GHObject> clazz;
            private final long id;

            public PredicateFindGHObjectByClassAndId(Class<? extends GHObject> clazz, long id) {
                checkNotNull(clazz);
                checkArgument(id > 0, "The id must be > 0");
                this.clazz = clazz;
                this.id = id;
            }

            @Override
            public boolean apply(GHObject input) {
                if (input == null)
                    return false;
                if (this.clazz.isAssignableFrom(input.getClass()) && input.getId() == id)
                    return true;
                return false;
            }

        }

        public static class PredicateFindGHObjectByClassAndUrl implements Predicate<GHObject> {

            private final Class<? extends GHObject> clazz;
            private final URL url;

            public PredicateFindGHObjectByClassAndUrl(Class<? extends GHObject> clazz, String url) throws MalformedURLException {
                checkNotNull(clazz);
                this.url = new URL(url);
                this.clazz = clazz;
            }

            @Override
            public boolean apply(GHObject input) {
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

        public abstract Optional<GHObject> find(Object object, Class<? extends GHObject> clazz, Iterable iterable);

    }

    /**
     * 
     * @param object the object from the API containing all the data
     * @param clazz the class of the object to find
     * @param iterable the iterable where to find it
     * @return
     */
    @Service
    static GHObject findOrCreateAndProccess(Object object, Class<? extends GHObject> clazz, Iterable iterable,
            ObjectFindStrategy... optionalObjectFindStrategy) {
        GHObject toProccessAndReturn = null;
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

        Optional<GHObject> ghObject = optionalObjectFindStrategyToBeUsed.find(object, clazz, iterable);
        if (ghObject.isPresent()) {
            toProccessAndReturn = ghObject.get();
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

    public GHObject() {
        super();
    }

}
