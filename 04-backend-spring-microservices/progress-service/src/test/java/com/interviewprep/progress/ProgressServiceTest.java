package com.interviewprep.progress;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProgressServiceTest {
    private final CatalogClient catalog = mock(CatalogClient.class);
    private final ProgressStore store = mock(ProgressStore.class);
    private final ProgressService service = new ProgressService(catalog, store);

    @Test
    void remoteValidationPrecedesTransaction() {
        var request = new ProgressRequest(true, "Practice");
        service.replace("react-effects", request);
        var order = inOrder(catalog, store);
        order.verify(catalog).requireTopic("react-effects");
        order.verify(store).replace("react-effects", request);
    }

    @Test
    void catalogFailurePreventsAnyWrite() {
        doThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE))
                .when(catalog).requireTopic("react-effects");
        assertThrows(ResponseStatusException.class, () ->
                service.replace("react-effects", new ProgressRequest(true, "")));
        verifyNoInteractions(store);
    }

    @Test
    void readsDoNotDependOnCatalog() {
        service.list();
        verify(store).list();
        verifyNoInteractions(catalog);
    }
}
