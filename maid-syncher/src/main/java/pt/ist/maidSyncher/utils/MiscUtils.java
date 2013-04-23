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
/**
 * 
 */
package pt.ist.maidSyncher.utils;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Locale.ENGLISH;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SynchableObject;
import pt.ist.maidSyncher.domain.SynchableObject.ObjectFindStrategy.PredicateFindGHObjectByClassAndId;

import com.google.common.collect.Iterables;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 22 de Fev de 2013
 * 
 * 
 */
public class MiscUtils {
    private static final Logger LOGGER = Logger.getLogger(MiscUtils.class);

    public static SynchableObject findACObjectsById(long id, Class<? extends SynchableObject> clazz) {
        checkNotNull(clazz);
        if (id <= 0)
            return null;
        MaidRoot maidRoot = MaidRoot.getInstance();
        PredicateFindGHObjectByClassAndId predicateFindGHObjectByClassAndId =
                new SynchableObject.ObjectFindStrategy.PredicateFindGHObjectByClassAndId(clazz, id);
        return Iterables.tryFind(maidRoot.getAcObjects(), predicateFindGHObjectByClassAndId).get();

    }

    static public synchronized Method getWriteMethodIncludingFlowStyle(PropertyDescriptor descriptor, Class<?> cls)
    {
        Method writeMethod = descriptor.getWriteMethod();
        if (writeMethod == null) {
            //let's try to get the flow version
            LOGGER.debug("Trying to get write flow-like method of propertyDescriptor: " + descriptor.getShortDescription());

            String writeMethodName = "set" + capitalize(descriptor.getName());
            Class<?> type = descriptor.getPropertyType();
            Class[] args = (type == null) ? null : new Class[] { type };
            try {
                writeMethod = cls.getMethod(writeMethodName, type);
            } catch (SecurityException e) {
                throw new Error(e);
            } catch (NoSuchMethodException e) {
                return null;
            }
            if (writeMethod != null) {
                if (!writeMethod.getReturnType().equals(cls)) {
                    writeMethod = null;
                }
            }

        }
        return writeMethod;
    }

    /**
     * Returns a String which capitalizes the first letter of the string.
     */
    private static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }

}
