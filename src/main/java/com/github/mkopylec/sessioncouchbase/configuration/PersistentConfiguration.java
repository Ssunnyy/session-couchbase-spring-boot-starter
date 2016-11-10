package com.github.mkopylec.sessioncouchbase.configuration;

import com.github.mkopylec.sessioncouchbase.data.PersistentDao;
import com.github.mkopylec.sessioncouchbase.data.RetryLoggingListener;
import com.github.mkopylec.sessioncouchbase.data.SessionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCouchbaseRepositories
@EnableConfigurationProperties({SessionCouchbaseProperties.class, CouchbaseProperties.class})
@ConditionalOnProperty(name = "session-couchbase.in-memory.enabled", havingValue = "false", matchIfMissing = true)
public class PersistentConfiguration {

    @Autowired
    protected CouchbaseProperties couchbase;
    @Autowired
    protected SessionCouchbaseProperties sessionCouchbase;

    @Bean
    @ConditionalOnMissingBean
    public RetryLoggingListener retryLoggingListener() {
        return new RetryLoggingListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTemplate sessionCouchbaseRetryTemplate(RetryLoggingListener listener) {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>(1);
        retryableExceptions.put(Exception.class, true);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(sessionCouchbase.getPersistent().getRetry().getMaxAttempts(), retryableExceptions);
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.registerListener(listener);
        return retryTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionDao sessionDao(CouchbaseTemplate couchbaseTemplate, @Qualifier("sessionCouchbaseRetryTemplate") RetryTemplate retryTemplate) {
        return new PersistentDao(couchbase, sessionCouchbase, couchbaseTemplate, retryTemplate);
    }
}
