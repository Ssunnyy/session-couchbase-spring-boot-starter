package com.github.mkopylec.sessioncouchbase.persistent;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import org.springframework.data.couchbase.core.CouchbaseQueryExecutionException;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import java.util.List;
import java.util.Map;

import static com.couchbase.client.java.document.json.JsonArray.from;
import static com.couchbase.client.java.query.N1qlQuery.parameterized;
import static org.springframework.util.Assert.isTrue;

public class CouchbaseDao {

    protected final CouchbaseTemplate couchbase;

    public CouchbaseDao(CouchbaseTemplate couchbase) {
        this.couchbase = couchbase;
    }

    public void updateSession(JsonObject attributes, String namespace, String id) {
        String statement = "UPDATE default USE KEYS $1 SET data.`" + namespace + "` = $2";
        N1qlQueryResult result = couchbase.queryN1QL(parameterized(statement, from(id, attributes)));
        failOnError(statement, result);
    }

    public void updatePutPrincipalSession(String principal, String sessionId) {
        String statement = "UPDATE default USE KEYS $1 SET sessionIds = ARRAY_PUT(sessionIds, $2)";
        N1qlQueryResult result = couchbase.queryN1QL(parameterized(statement, from(principal, sessionId)));
        failOnError(statement, result);
    }

    public void updateRemovePrincipalSession(String principal, String sessionId) {
        String statement = "UPDATE default USE KEYS $1 SET sessionIds = ARRAY_REMOVE(sessionIds, $2)";
        N1qlQueryResult result = couchbase.queryN1QL(parameterized(statement, from(principal, sessionId)));
        failOnError(statement, result);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> findSessionAttributes(String id, String namespace) {
        String statement = "SELECT * FROM default.data.`" + namespace + "` USE KEYS $1";
        N1qlQueryResult result = couchbase.queryN1QL(parameterized(statement, from(id)));
        failOnError(statement, result);
        List<N1qlQueryRow> attributes = result.allRows();
        isTrue(attributes.size() < 2, "Invalid HTTP session state. Multiple namespaces '" + namespace + "' for session ID '" + id + "'");
        if (attributes.isEmpty()) {
            return null;
        }
        return (Map<String, Object>) attributes.get(0).value().toMap().get(namespace);
    }

    public SessionDocument findById(String id) {
        return couchbase.findById(id, SessionDocument.class);
    }

    public PrincipalSessionsDocument findByPrincipal(String principal) {
        return couchbase.findById(principal, PrincipalSessionsDocument.class);
    }

    public void updateExpirationTime(String id, int expiry) {
        couchbase.getCouchbaseBucket().touch(id, expiry);
    }

    public void save(SessionDocument document) {
        couchbase.save(document);
    }

    public void save(PrincipalSessionsDocument document) {
        couchbase.save(document);
    }

    public boolean exists(String documentId) {
        return couchbase.exists(documentId);
    }

    public void delete(String id) {
        try {
            couchbase.remove(id);
        } catch (DocumentDoesNotExistException ex) {
            //Do nothing
        }
    }

    private void failOnError(String statement, N1qlQueryResult result) {
        if (!result.finalSuccess()) {
            throw new CouchbaseQueryExecutionException("Error executing N1QL statement '" + statement + "'. " + result.errors());
        }
    }
}
