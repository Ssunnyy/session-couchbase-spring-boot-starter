package com.github.mkopylec.sessioncouchbase.inmemory;

import com.github.mkopylec.sessioncouchbase.SessionCouchbaseProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;

@Configuration("sessionInMemoryConfiguration")
@EnableConfigurationProperties(SessionCouchbaseProperties.class)
@ConditionalOnProperty(name = "session-couchbase.in-memory.enabled")
public class InMemoryConfiguration {

    @Autowired
    private SessionCouchbaseProperties sessionCouchbase;

    @Bean
    @ConditionalOnMissingBean
    public SessionRepository mapSessionRepository() {
        MapSessionRepository repository = new MapSessionRepository();
        repository.setDefaultMaxInactiveInterval(sessionCouchbase.getTimeoutInSeconds());
        return repository;
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionRepositoryFilter sessionRepositoryFilter(SessionRepository<MapSession> repository) {
        return new SessionRepositoryFilter<>(repository);
    }
}