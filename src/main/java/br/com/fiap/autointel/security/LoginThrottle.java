package br.com.fiap.autointel.security;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/** Limites locais por conta/origem e por origem total durante 15 minutos. */
@Component
public class LoginThrottle {
    private static final Logger log = LoggerFactory.getLogger(LoginThrottle.class);
    private static final int MAX_ACCOUNT_FAILURES = 5;
    private static final int MAX_ADDRESS_FAILURES = 30;
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

    private String accountKey(String address, String email) {
        return "account|" + address + "|" + email.trim().toLowerCase(Locale.ROOT);
    }

    private String addressKey(String address) {
        return "address|" + address;
    }

    public void check(String address, String email) {
        checkBucket(accountKey(address, email), MAX_ACCOUNT_FAILURES);
        checkBucket(addressKey(address), MAX_ADDRESS_FAILURES);
    }

    private void checkBucket(String key, int limit) {
        Attempt attempt = attempts.get(key);
        if (attempt != null && clock.instant().isBefore(attempt.expiresAt()) && attempt.failures() >= limit) {
            log.warn("security_event=LOGIN_RATE_LIMIT");
            throw new MuitasTentativasException();
        }
    }

    public void failed(String address, String email) {
        Instant now = clock.instant();
        increment(accountKey(address, email), now);
        increment(addressKey(address), now);
        if (attempts.size() > 10_000) {
            attempts.entrySet().removeIf(entry -> !now.isBefore(entry.getValue().expiresAt()));
        }
    }

    private void increment(String key, Instant now) {
        attempts.compute(key, (ignored, previous) ->
                previous == null || !now.isBefore(previous.expiresAt())
                        ? new Attempt(1, now.plus(WINDOW))
                        : new Attempt(previous.failures() + 1, previous.expiresAt()));
    }

    public void succeeded(String address, String email) {
        attempts.remove(accountKey(address, email));
    }
}
