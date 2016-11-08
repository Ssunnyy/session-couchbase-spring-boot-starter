package com.github.mkopylec.sessioncouchbase.data;

import com.couchbase.client.java.repository.annotation.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

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
        return sessionIds == null ? new ArrayList<>() : sessionIds;
    }
}
