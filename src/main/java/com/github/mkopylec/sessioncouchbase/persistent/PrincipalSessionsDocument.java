package com.github.mkopylec.sessioncouchbase.persistent;

import com.couchbase.client.java.repository.annotation.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.List;

import static java.util.Collections.emptyList;

@Document
public class PrincipalSessionsDocument {

    @Id
    protected final String principal;
    @Field
    protected final List<String> sessionIds;

    public PrincipalSessionsDocument(String principal, List<String> sessionIds) {
        this.principal = principal;
        this.sessionIds = sessionIds;
    }

    public String getPrincipal() {
        return principal;
    }

    public List<String> getSessionIds() {
        return sessionIds == null ? emptyList() : sessionIds;
    }
}
