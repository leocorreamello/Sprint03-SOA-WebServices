package br.com.fiap.autointel.unit;

import br.com.fiap.autointel.security.LoginThrottle;
import br.com.fiap.autointel.security.MuitasTentativasException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginThrottleTest {

    @Test
    void bloqueiaSprayEntreContasDiferentesDaMesmaOrigem() {
        LoginThrottle throttle = new LoginThrottle();
        for (int i = 0; i < 30; i++) {
            String email = "conta" + i + "@teste.com";
            throttle.check("192.0.2.1", email);
            throttle.failed("192.0.2.1", email);
        }
        assertThrows(MuitasTentativasException.class,
                () -> throttle.check("192.0.2.1", "outra@teste.com"));
    }
}
