package br.com.fiap.autointel.security;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/** Limite local de falhas de login por endereço e conta durante uma janela de 15 minutos. */
@Component
public class LoginThrottle {
    private static final Logger log = LoggerFactory.getLogger(LoginThrottle.class);
    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginThrottle() {
        this(Clock.systemUTC());
    }

    LoginThrottle(Clock clock) {
        this.clock = clock;
    }

    private record Attempt(int failures, Instant expiresAt) {
    }

    private String key(String address, String email) {
        return address + "|" + email.trim().toLowerCase(Locale.ROOT);
    }

    public void check(String address, String email) {
        Attempt attempt = attempts.get(key(address, email));
        if (attempt != null && clock.instant().isBefore(attempt.expiresAt()) && attempt.failures() >= MAX_FAILURES) {
            log.warn("security_event=LOGIN_RATE_LIMIT");
            throw new MuitasTentativasException();
        }
    }

    public void failed(String address, String email) {
        Instant now = clock.instant();
        attempts.compute(key(address, email), (key, previous) ->
                previous == null || !now.isBefore(previous.expiresAt())
                        ? new Attempt(1, now.plus(WINDOW))
                        : new Attempt(previous.failures() + 1, previous.expiresAt()));
        if (attempts.size() > 10_000) {
            attempts.entrySet().removeIf(entry -> !now.isBefore(entry.getValue().expiresAt()));
        }
    }

    public void succeeded(String address, String email) {
        attempts.remove(key(address, email));
    }
}
