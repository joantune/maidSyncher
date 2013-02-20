package pt.ist.maidSyncher.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.apache.commons.beanutils.PropertyUtils;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;

import com.google.common.base.Objects;

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

    public abstract void sync(Object objectThatTriggeredTheSync);

    public boolean copyPropertiesFrom(Object orig) throws IllegalAccessException, InvocationTargetException,
    NoSuchMethodException {
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
                                + ((DomainObject) dest).getExternalId() + " name : " + name + " valueOrig: " + valueOrigin);
                    }
                    LOGGER.trace("Copied property " + name + " from " + orig.getClass().getName() + " object to a "
                            + dest.getClass().getName() + " oid: " + getExternalId());
                }
            }
        }

        return changesWereMade;
    }
}
