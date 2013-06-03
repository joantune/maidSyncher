/**
 * 
 */
package pt.ist.maidSyncher.domain.test.utils;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SynchableObject;
import pt.ist.maidSyncher.domain.exceptions.SyncEventOriginObjectChanged;
import pt.ist.maidSyncher.domain.sync.APIObjectWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;
import pt.ist.maidSyncher.domain.sync.SyncEvent.SyncUniverse;
import pt.ist.maidSyncher.domain.sync.SyncEvent.TypeOfChangeEvent;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 29 de Abr de 2013
 * 
 *         A class with misc utility methods for the tests
 */
public class TestUtils {

    //Empty phony object
    public static class PhonyObject {
    }


    public static SyncEvent syncEventGenerator(final TypeOfChangeEvent typeOfChangeEvent, final SynchableObject originObject) {
        return syncEventGenerator(typeOfChangeEvent, originObject, Collections.<PropertyDescriptor> emptySet());
    }

    public static SyncEvent syncEventGenerator(final TypeOfChangeEvent typeOfChangeEvent, final SynchableObject originObject,
            Collection<PropertyDescriptor> changedDescriptors) {
        //let's make sure the changedDescriptors don't have certain, illegal descriptors
        Set<PropertyDescriptor> propertyDescriptorsToUse = new HashSet<>();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptorsToUse) {
            if (propertyDescriptor.getName().equalsIgnoreCase("class")) {
                //do nothing
            } else {
                propertyDescriptorsToUse.add(propertyDescriptor);
            }

        }
        return new SyncEvent(new LocalTime(), typeOfChangeEvent, propertyDescriptorsToUse, null, new APIObjectWrapper() {

            @Override
            public void validateAPIObject() throws SyncEventOriginObjectChanged {
                return;
            }

            @Override
            public Object getAPIObject() {
                //let's return a phony APIObject
                return new PhonyObject();

            }
        }, SyncUniverse.getTargetSyncUniverse(originObject), originObject);
    }

    @Atomic
    public static void clearInstancesWithRoot() {
        //let's clear out the objects
        MaidRoot instance = MaidRoot.getInstance();

        instance.getAcObjectsSet().clear();
        instance.getDsiObjectsSet().clear();
        instance.getGhCommentsSet().clear();
        instance.getGhIssuesSet().clear();
        instance.getGhLabelsSet().clear();
        instance.getGhMilestonesSet().clear();
//        instance.getGhOrganization().
        instance.getGhRepositoriesSet().clear();
        instance.getGhUsersSet().clear();

    }
}
