package com.yannbriancon.interceptor;

import com.yannbriancon.exception.NPlusOneQueriesException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Component
@EnableConfigurationProperties(HibernateQueryInterceptorProperties.class)
public class HibernateQueryInterceptor extends EmptyInterceptor {

    private transient ThreadLocal<Long> threadQueryCount = new ThreadLocal<>();

    private transient ThreadLocal<Set<String>> threadPreviouslyLoadedEntities =
            ThreadLocal.withInitial(new EmptySetSupplier());

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateQueryInterceptor.class);

    private final HibernateQueryInterceptorProperties hibernateQueryInterceptorProperties;

    public HibernateQueryInterceptor(HibernateQueryInterceptorProperties hibernateQueryInterceptorProperties) {
        this.hibernateQueryInterceptorProperties = hibernateQueryInterceptorProperties;
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
     * N+1 queries exceptions because of loading same instance in two different transactions
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
        detectNPlusOneQueriesOfMissingQueryEagerFetching(entityName, id);

        Set<String> previouslyLoadedEntities = threadPreviouslyLoadedEntities.get();

        if (previouslyLoadedEntities.contains(entityName + id)) {
            previouslyLoadedEntities.remove(entityName + id);
            threadPreviouslyLoadedEntities.set(previouslyLoadedEntities);
        } else {
            previouslyLoadedEntities.add(entityName + id);
            threadPreviouslyLoadedEntities.set(previouslyLoadedEntities);
        }

        return null;
    }

    /**
     * Detect the N+1 queries caused by a missing eager fetching configuration on a query with a lazy loaded field
     * <p>
     * <p>
     * Detection checks:
     * - The getEntity was called twice for the couple (entity, id)
     * <p>
     * - There is an occurrence of hibernate proxy followed by entity class in the stackTraceElements
     * Avoid detecting calls to queries like findById and queries with eager fetching on some entity fields
     *
     * @param entityName Name of the entity
     * @param id         Id of the entity objecy
     * @return Boolean telling whether N+1 queries were detected or not
     */
    private boolean detectNPlusOneQueriesOfMissingQueryEagerFetching(String entityName, Serializable id) {
        Set<String> previouslyLoadedEntities = threadPreviouslyLoadedEntities.get();

        if (!previouslyLoadedEntities.contains(entityName + id)) {
            return false;
        }

        // Detect N+1 queries by searching for newest occurrence of Hibernate proxy followed by entity class in stack
        // elements
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement originStackTraceElement = null;

        for (int i = 0; i < stackTraceElements.length - 3; i++) {
            if (
                    stackTraceElements[i].getClassName().indexOf(HIBERNATE_PROXY_PREFIX) == 0
                            && stackTraceElements[i + 1].getClassName().indexOf(entityName) == 0
            ) {
                originStackTraceElement = stackTraceElements[i + 2];
                break;
            }
        }

        if (originStackTraceElement == null) {
            return false;
        }

        String errorMessage = "N+1 queries detected on a getter of the entity " + entityName +
                "\n    at " + originStackTraceElement.toString() +
                "\n    Hint: Missing Eager fetching configuration on the query that fetched the object of " +
                "type " + entityName + "\n";
        logDetectedNPlusOneQueries(errorMessage);

        return true;
    }


    /**
     * Get the Proxy method name that was called first to know which query triggered the interceptor
     *
     * @return Optional of method name if found
     */
    private Optional<String> getProxyMethodName() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        for (int i = stackTraceElements.length - 1; i >= 0; i--) {
            StackTraceElement stackTraceElement = stackTraceElements[i];

            if (stackTraceElement.getClassName().indexOf("com.sun.proxy") == 0) {
                return Optional.of(stackTraceElement.getClassName() + stackTraceElement.getMethodName());
            }
        }

        return Optional.empty();
    }

    /**
     * Log the detected N+1 queries error message or throw an exception depending on the configured error level
     *
     * @param errorMessage Error message for the N+1 queries detected
     */
    private void logDetectedNPlusOneQueries(String errorMessage) {
        switch (hibernateQueryInterceptorProperties.getErrorLevel()) {
            case INFO:
                LOGGER.info(errorMessage);
                break;
            case WARN:
                LOGGER.warn(errorMessage);
                break;
            case ERROR:
                LOGGER.error(errorMessage);
                break;
            default:
                throw new NPlusOneQueriesException(errorMessage, new Exception(new Throwable()));
        }
    }
}

class EmptySetSupplier implements Supplier<Set<String>> {
    public Set<String> get() {
        return new HashSet<>();
    }
}
