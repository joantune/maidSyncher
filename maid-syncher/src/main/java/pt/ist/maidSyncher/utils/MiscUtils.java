/**
 * 
 */
package pt.ist.maidSyncher.utils;

import static com.google.common.base.Preconditions.checkNotNull;
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

    public static SynchableObject findACObjectsById(long id, Class<? extends SynchableObject> clazz) {
        checkNotNull(clazz);
        if (id <= 0)
            return null;
        MaidRoot maidRoot = MaidRoot.getInstance();
        PredicateFindGHObjectByClassAndId predicateFindGHObjectByClassAndId =
                new SynchableObject.ObjectFindStrategy.PredicateFindGHObjectByClassAndId(clazz, id);
        return Iterables.tryFind(maidRoot.getAcObjects(), predicateFindGHObjectByClassAndId).get();

    }

}
