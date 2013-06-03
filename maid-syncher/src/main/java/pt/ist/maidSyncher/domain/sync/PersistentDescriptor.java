package pt.ist.maidSyncher.domain.sync;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class PersistentDescriptor extends PersistentDescriptor_Base {

    public PersistentDescriptor() {
        super();
    }

    public PersistentDescriptor(String propertyDescriptorName, String className) {
        super();
        setPropertyName(propertyDescriptorName);
        setClassName(className);
    }

    public static Set<? extends PersistentDescriptor> convert(Collection<PropertyDescriptor> propertyDescriptors,
            Class<? extends Object> class1) {
        Set<PersistentDescriptor> persistentDescriptors = new HashSet<>();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            persistentDescriptors.add(new PersistentDescriptor(propertyDescriptor.getName(), class1.getName()));
        }
        return persistentDescriptors;
    }

    public static Iterable<? extends PropertyDescriptor> convert(Set<PersistentDescriptor> changedPropertyDescriptorsSet) {
        return Iterables.transform(changedPropertyDescriptorsSet, new Function<PersistentDescriptor, PropertyDescriptor>() {
            @Override
            public PropertyDescriptor apply(PersistentDescriptor persistedDescriptor) {
                checkNotNull(persistedDescriptor);
                try {
                    return new PropertyDescriptor(persistedDescriptor.getPropertyName(), PersistentDescriptor.class
                            .getClassLoader().loadClass(persistedDescriptor.getClassName()));
                } catch (ClassNotFoundException | IntrospectionException e) {
                    throw new Error(e);
                }

            }
        });
    }

    public void delete() {

        setSyncEvent(null);
        deleteDomainObject();
    }
}
