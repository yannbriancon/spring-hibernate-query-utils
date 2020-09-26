package com.yannbriancon.interceptor;

import com.yannbriancon.exception.NPlusOneQueriesException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Component
@EnableConfigurationProperties(HibernateQueryInterceptorProperties.class)
public class HibernateQueryInterceptor extends EmptyInterceptor {

    private transient ThreadLocal<Long> threadQueryCount = new ThreadLocal<>();

    private transient ThreadLocal<Set<String>> threadPreviouslyLoadedEntities =
            ThreadLocal.withInitial(new EmptySetSupplier());

    private transient ThreadLocal<Set<String>> threadPrevioulyQueriedProxyMethods =
            ThreadLocal.withInitial(new EmptySetSupplier());

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateQueryInterceptor.class);

    private final HibernateQueryInterceptorProperties hibernateQueryInterceptorProperties;

    private static final String HIBERNATE_PROXY_PREFIX = "org.hibernate.proxy";
    private static final String PROXY_METHOD_PREFIX = "com.sun.proxy";

    public HibernateQueryInterceptor(
            HibernateQueryInterceptorProperties hibernateQueryInterceptorProperties
    ) {
        this.hibernateQueryInterceptorProperties = hibernateQueryInterceptorProperties;
    }

    /**
     * Reset the N+1 query detection state
     */
    private void resetNPlusOneQueryDetectionState() {
        threadPreviouslyLoadedEntities.set(new HashSet<>());
        threadPrevioulyQueriedProxyMethods.set(new HashSet<>());
    }

    /**
     * Clear the Hibernate Session and reset the N+1 query detection state
     * <p>
     * Clearing the Hibernate Session is necessary to detect N+1 queries in tests as they would be in production.
     * Otherwise, every objects created in the setup of the tests would already be loaded in the Session and would
     * hide potential N+1 queries.
     */
    public void clearNPlusOneQuerySession(EntityManager entityManager) {
        entityManager.clear();
        this.resetNPlusOneQueryDetectionState();
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
     * Detect the N+1 queries by keeping the history of sql queries generated per proxy method.
     * Increment the query count for the considered thread for each new statement if the count has been initialized.
     *
     * @param sql Query to be executed
     * @return Query to be executed
     */
    @Override
    public String onPrepareStatement(String sql) {
        if (hibernateQueryInterceptorProperties.isnPlusOneDetectionEnabled()) {
            detectNPlusOneQueriesOfMissingEntityFieldLazyFetching();
        }
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
        this.resetNPlusOneQueryDetectionState();
    }

    /**
     * Detect the N+1 queries by keeping the history of the entities previously gotten.
     *
     * @param entityName Name of the entity to get
     * @param id         Id of the entity to get
     */
    @Override
    public Object getEntity(String entityName, Serializable id) {
        if (hibernateQueryInterceptorProperties.isnPlusOneDetectionEnabled()) {
            detectNPlusOneQueriesOfMissingQueryEagerFetching(entityName, id);
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
     */
    private void detectNPlusOneQueriesOfMissingQueryEagerFetching(String entityName, Serializable id) {
        Set<String> previouslyLoadedEntities = threadPreviouslyLoadedEntities.get();

        if (!previouslyLoadedEntities.contains(entityName + id)) {
            previouslyLoadedEntities.add(entityName + id);
            threadPreviouslyLoadedEntities.set(previouslyLoadedEntities);
            return;
        }
        previouslyLoadedEntities.remove(entityName + id);
        threadPreviouslyLoadedEntities.set(previouslyLoadedEntities);

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
            return;
        }

        String errorMessage = "N+1 queries detected on a getter of the entity " + entityName +
                "\n    at " + originStackTraceElement.toString() +
                "\n    Hint: Missing Eager fetching configuration on the query that fetched the object of " +
                "type " + entityName + "\n";
        logDetectedNPlusOneQueries(errorMessage);
    }

    /**
     * Detect the N+1 queries caused by a missing lazy fetching configuration on an entity field
     * <p>
     * Detection checks that several queries were generated from the same proxy method
     */
    private void detectNPlusOneQueriesOfMissingEntityFieldLazyFetching() {
        Optional<String> optionalProxyMethodName = getProxyMethodName();
        if (!optionalProxyMethodName.isPresent()) {
            return;
        }
        String proxyMethodName = optionalProxyMethodName.get();

        Set<String> previouslyQueriedProxyMethods = threadPrevioulyQueriedProxyMethods.get();

        if (!previouslyQueriedProxyMethods.contains(proxyMethodName)) {
            previouslyQueriedProxyMethods.add(proxyMethodName);
            return;
        }

        String errorMessage = "N+1 queries detected with eager fetching on the query";

        // Find origin of the N+1 queries in client package
        // by getting oldest occurrence of proxy method in stack elements
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        for (int i = stackTraceElements.length - 1; i >= 1; i--) {
            if (stackTraceElements[i - 1].getClassName().indexOf(PROXY_METHOD_PREFIX) == 0) {
                errorMessage += "\n    at " + stackTraceElements[i].toString();
                break;
            }
        }

        errorMessage += "\n    Hint: Missing Lazy fetching configuration on a field of one of the entities " +
                "fetched in the query\n";

        logDetectedNPlusOneQueries(errorMessage);
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

            if (stackTraceElement.getClassName().indexOf(PROXY_METHOD_PREFIX) == 0) {
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
                throw new NPlusOneQueriesException(errorMessage);
        }
    }
}

class EmptySetSupplier implements Supplier<Set<String>> {
    public Set<String> get() {
        return new HashSet<>();
    }
}

class EmptyMapSupplier implements Supplier<Map<String, String>> {
    public Map<String, String> get() {
        return new HashMap<>();
    }
}
