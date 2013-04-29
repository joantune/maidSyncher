/**
 * 
 */
package pt.ist.maidSyncher.domain.test.utils;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Collections;

import org.joda.time.LocalTime;

import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.SyncEvent.SyncUniverse;
import pt.ist.maidSyncher.domain.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.SynchableObject;
import pt.ist.maidSyncher.domain.exceptions.SyncEventOriginObjectChanged;
import pt.ist.maidSyncher.domain.sync.APIObjectWrapper;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 29 de Abr de 2013
 * 
 *         A class with misc utility methods for the tests
 */
public class TestUtils {


    public static SyncEvent syncEventGenerator(final TypeOfChangeEvent typeOfChangeEvent, final SynchableObject originObject) {
        return syncEventGenerator(typeOfChangeEvent, originObject, Collections.<PropertyDescriptor> emptySet());
    }

    public static SyncEvent syncEventGenerator(final TypeOfChangeEvent typeOfChangeEvent, final SynchableObject originObject,
            Collection<PropertyDescriptor> changedDescriptors) {
        return new SyncEvent(new LocalTime(), typeOfChangeEvent, changedDescriptors, null, new APIObjectWrapper() {

            @Override
            public void validateAPIObject() throws SyncEventOriginObjectChanged {
                return;
            }

            @Override
            public Object getAPIObject() {
                return null;
            }
        }, SyncUniverse.getTargetSyncUniverse(originObject), originObject);
    }

}
