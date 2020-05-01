package com.yannbriancon.interceptor;

import com.yannbriancon.exception.NPlusOneQueryException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Component
public class HibernateQueryInterceptor extends EmptyInterceptor {

    private transient ThreadLocal<Long> threadQueryCount = new ThreadLocal<>();

    private transient ThreadLocal<Set<String>> threadPreviouslyLoadedEntities = new ThreadLocal<>();

    public HibernateQueryInterceptor() {
        threadPreviouslyLoadedEntities.set(new HashSet<>());
    }

    /**
     * Start or reset the query count to 0 for the considered thread
     */
    public void startQueryCount() {
        threadQueryCount.set(0L);
    }

    /**
     * Get the query count for the considered thread
     */
    public Long getQueryCount() {
        return threadQueryCount.get();
    }

    /**
     * Increment the query count for the considered thread for each new statement if the count has been initialized
     *
     * @param sql Query to be executed
     * @return Query to be executed
     */
    @Override
    public String onPrepareStatement(String sql) {
        Long count = threadQueryCount.get();
        if (count != null) {
            threadQueryCount.set(count + 1);
        }
        return super.onPrepareStatement(sql);
    }

    /**
     * Reset previously loaded entities after the end of a transaction to avoid triggering
     * N+1 query exceptions because of loading same instance in two different transactions
     *
     * @param tx Transaction having been completed
     */
    @Override
    public void afterTransactionCompletion(Transaction tx) {
        threadPreviouslyLoadedEntities.set(new HashSet<>());
    }

    /**
     * Detect the N+1 queries by checking if two calls were made to getEntity for the same instance
     * <p>
     * The first call is made with the instance filled with a {@link HibernateProxy}
     * and the second is made after a query was executed to fetch the data in the Entity
     *
     * @param entityName Name of the entity to get
     * @param id         Id of the entity to get
     */
    @Override
    public Object getEntity(String entityName, Serializable id) {
        Set<String> previouslyLoadedEntities = threadPreviouslyLoadedEntities.get();

        if (previouslyLoadedEntities.contains(entityName + id)) {
            previouslyLoadedEntities.remove(entityName + id);
            threadPreviouslyLoadedEntities.set(previouslyLoadedEntities);
            throw new NPlusOneQueryException("N+1 query detected for entity: " + entityName);
        }

        previouslyLoadedEntities.add(entityName + id);
        threadPreviouslyLoadedEntities.set(previouslyLoadedEntities);

        return null;
    }
}
