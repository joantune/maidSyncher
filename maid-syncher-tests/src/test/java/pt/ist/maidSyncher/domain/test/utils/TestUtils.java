/**
 * 
 */
package pt.ist.maidSyncher.domain.test.utils;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.maidSyncher.api.activeCollab.ACContext;
import pt.ist.maidSyncher.api.activeCollab.ACInstance;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SynchableObject;
import pt.ist.maidSyncher.domain.exceptions.SyncEventOriginObjectChanged;
import pt.ist.maidSyncher.domain.sync.APIObjectWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;
import pt.ist.maidSyncher.domain.sync.SyncEvent.SyncUniverse;
import pt.ist.maidSyncher.domain.sync.SyncEvent.TypeOfChangeEvent;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 29 de Abr de 2013
 * 
 *         A class with misc utility methods for the tests
 */
public class TestUtils {

    //Empty phony object
    public static class PhonyObject {
    }

    public static Collection<PropertyDescriptor> propertyDescriptorsToUseMinus(final Class classToGetDescriptorsFrom,
            final String... pDnamesToExclude) {
        checkNotNull(classToGetDescriptorsFrom);
        final List<String> namesToExclude = Arrays.asList(pDnamesToExclude);
        Collection<PropertyDescriptor> propertyDescriptorsToUse =
                Collections2.filter(Arrays.asList(PropertyUtils.getPropertyDescriptors(classToGetDescriptorsFrom)),
                        new Predicate<PropertyDescriptor>() {
                    @Override
                    public boolean apply(PropertyDescriptor input) {
                        if (input == null)
                            return false;
                        if (namesToExclude.contains(input.getName()))
                            return false;
                        return true;
                    }
                });
        return propertyDescriptorsToUse;
    }

    public static SyncEvent syncEventGenerator(final TypeOfChangeEvent typeOfChangeEvent, final SynchableObject originObject,
            final Object apiObject) {
        return syncEventGenerator(typeOfChangeEvent, originObject, Collections.<PropertyDescriptor> emptySet());
    }

    public static SyncEvent syncEventGenerator(final TypeOfChangeEvent typeOfChangeEvent, final SynchableObject originObject,
            Collection<PropertyDescriptor> changedDescriptors) {
        //let's make sure the changedDescriptors don't have certain, illegal descriptors
        Set<String> propertyDescriptorsToUse = new HashSet<>();
        for (PropertyDescriptor propertyDescriptor : changedDescriptors) {
            if (propertyDescriptor.getName().equalsIgnoreCase("class")) {
                //do nothing
            } else {
                propertyDescriptorsToUse.add(propertyDescriptor.getName());
            }

        }
        return new SyncEvent(new DateTime(), typeOfChangeEvent, propertyDescriptorsToUse, null, new APIObjectWrapper() {

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

    /**
     * Makes sure that the test server properties are defined, and that
     * access to the live test server is working
     * 
     * @throws IOException
     */
    public static void checkLiveTestServerStatus() {

        try {
            Properties configurationProperties = new Properties();
            InputStream configurationInputStream = TestUtils.class.getResourceAsStream("/configuration.properties");
            configurationProperties.load(configurationInputStream);

            ACContext acContext = ACContext.getInstance();
            acContext.setServerBaseUrl(configurationProperties.getProperty("ac.test.server.baseUrl"));
            acContext.setToken(configurationProperties.getProperty("ac.test.server.token"));
            String companyName = configurationProperties.getProperty("ac.server.test.companyName");

            ACInstance instanceForDSI;
            instanceForDSI = ACInstance.getInstanceForCompanyName(companyName);
//            assumeNotNull(instanceForDSI);
            assertNotNull(instanceForDSI);
        } catch (IOException e) {
            fail();
//            assumeNoException(e);
        }

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
        instance.getSyncEventsToProcessSet().clear();
        instance.getSyncLogsSet().clear();

    }
}
