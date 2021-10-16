package co.com.demo.rest.contracts;

public interface ElasticDocs {

    @FunctionalInterface
    interface Create {
        String sendFor(Object objectRequest, String id);
    }

    @FunctionalInterface
    interface Get {
        String get(String id, String... queryParams);
    }

    @FunctionalInterface
    interface Checks {
        boolean exists(String docId);
    }

    @FunctionalInterface
    interface Update {
        String setDoc(String docId, Object doc);
    }

    @FunctionalInterface
    interface Delete {
        boolean isDeleted(String id);
    }
}
