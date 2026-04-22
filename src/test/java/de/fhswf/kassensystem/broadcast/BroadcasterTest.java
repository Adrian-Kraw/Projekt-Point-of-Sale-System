package de.fhswf.kassensystem.broadcast;

import com.vaadin.flow.shared.Registration;
import de.fhswf.kassensystem.exception.KassensystemException;
import de.fhswf.kassensystem.exception.UngueltigeEingabeException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Testet den zentralen Event-Bus {@link Broadcaster}.
 *
 * <p>Die Tests sind in drei Gruppen gegliedert:
 * <ul>
 *   <li>{@code register()} – Registrierung von Listenern, inkl. Null-Übergabe und Mehrfachregistrierung</li>
 *   <li>{@code Registration.remove()} – Abmeldeverhalten, inkl. mehrfachem Aufruf und Selbstabmeldung</li>
 *   <li>{@code broadcast()} – Zustellung von Events, inkl. Grenzfällen wie null, leerem String,
 *       fehlerhaftem Listener, sequenziellen und nebenläufigen Broadcasts</li>
 * </ul>
 *
 * <p>Da der {@link Broadcaster} statischen Zustand hält, werden alle registrierten
 * {@link com.vaadin.flow.shared.Registration Registrations} in {@code tearDown()} wieder entfernt,
 * um gegenseitige Testbeeinflussung zu verhindern.
 *
 * <p>Asynchrone Zustellungen werden mit {@link java.util.concurrent.CountDownLatch} synchronisiert,
 * um deterministische Assertions ohne künstliche {@code Thread.sleep}-Wartezeiten zu ermöglichen.
 *
 * @author Adrian Krawietz
 */
@DisplayName("Broadcaster Tests")
class BroadcasterTest {

    /**
     * Alle Tests bereinigen nach sich selbst – da Broadcaster statischen Zustand hält,
     * wird jede registrierte Registration nach dem Test entfernt.
     */
    private final List<Registration> registrations = new ArrayList<>();

    @AfterEach
    void tearDown() {
        registrations.forEach(Registration::remove);
        registrations.clear();
    }

    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Null-Listener wirft IllegalArgumentException")
        void register_nullListener_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> Broadcaster.register(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Listener darf nicht null sein");
        }

        @Test
        @DisplayName("Gültiger Listener liefert non-null Registration zurück")
        void register_validListener_returnsRegistration() {
            Registration reg = Broadcaster.register(event -> {});
            registrations.add(reg);

            assertThat(reg).isNotNull();
        }

        @Test
        @DisplayName("Mehrere Listener können gleichzeitig registriert werden")
        void register_multipleListeners_allRegistered() throws InterruptedException {
            List<String> received = new CopyOnWriteArrayList<>();
            CountDownLatch latch = new CountDownLatch(3);

            registrations.add(Broadcaster.register(e -> { received.add("A:" + e); latch.countDown(); }));
            registrations.add(Broadcaster.register(e -> { received.add("B:" + e); latch.countDown(); }));
            registrations.add(Broadcaster.register(e -> { received.add("C:" + e); latch.countDown(); }));

            Broadcaster.broadcast("test");

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(received).hasSize(3)
                    .containsExactlyInAnyOrder("A:test", "B:test", "C:test");
        }

        @Test
        @DisplayName("Gleicher Listener kann mehrfach registriert werden und empfängt Events mehrfach")
        void register_sameListenerTwice_receivesEventTwice() throws InterruptedException {
            AtomicInteger callCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(2);

            registrations.add(Broadcaster.register(e -> { callCount.incrementAndGet(); latch.countDown(); }));
            registrations.add(Broadcaster.register(e -> { callCount.incrementAndGet(); latch.countDown(); }));

            Broadcaster.broadcast("doppelt");

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(callCount.get()).isEqualTo(2);
        }
    }

    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Registration.remove()")
    class RegistrationRemoveTests {

        @Test
        @DisplayName("Nach remove() empfängt der Listener keine weiteren Events")
        void remove_listenerNoLongerReceivesEvents() throws InterruptedException {
            List<String> received = new CopyOnWriteArrayList<>();
            CountDownLatch firstLatch = new CountDownLatch(1);

            Registration reg = Broadcaster.register(e -> {
                received.add(e);
                firstLatch.countDown();
            });

            // Erstes Event empfangen
            Broadcaster.broadcast("vor-remove");
            assertThat(firstLatch.await(2, TimeUnit.SECONDS)).isTrue();

            // Listener entfernen
            reg.remove();

            // Zweites Event – sollte nicht mehr ankommen
            Broadcaster.broadcast("nach-remove");
            Thread.sleep(300); // kurz warten, da async

            assertThat(received).containsExactly("vor-remove");
        }

        @Test
        @DisplayName("remove() darf mehrfach aufgerufen werden ohne Exception")
        void remove_calledMultipleTimes_noException() {
            Registration reg = Broadcaster.register(e -> {});

            assertThatCode(() -> {
                reg.remove();
                reg.remove();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("remove() eines nicht mehr vorhandenen Listeners wirft keine Exception")
        void remove_alreadyAbsentListener_noException() {
            Registration reg = Broadcaster.register(e -> {});
            reg.remove(); // bereits entfernt

            assertThatCode(reg::remove).doesNotThrowAnyException();
        }
    }

    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("broadcast()")
    class BroadcastTests {

        @Test
        @DisplayName("Null-Event wirft UngueltigeEingabeException")
        void broadcast_nullEvent_throwsUngueltigeEingabeException() {
            assertThatThrownBy(() -> Broadcaster.broadcast(null))
                    .isInstanceOf(UngueltigeEingabeException.class);
        }

        @Test
        @DisplayName("Leerer String ist ein gültiges Event und wird zugestellt")
        void broadcast_emptyString_isDelivered() throws InterruptedException {
            List<String> received = new CopyOnWriteArrayList<>();
            CountDownLatch latch = new CountDownLatch(1);

            registrations.add(Broadcaster.register(e -> { received.add(e); latch.countDown(); }));

            Broadcaster.broadcast("");

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(received).containsExactly("");
        }

        @Test
        @DisplayName("Event wird korrekt an alle Listener weitergegeben")
        void broadcast_validEvent_deliveredToAllListeners() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(2);
            List<String> received = new CopyOnWriteArrayList<>();

            registrations.add(Broadcaster.register(e -> { received.add("1:" + e); latch.countDown(); }));
            registrations.add(Broadcaster.register(e -> { received.add("2:" + e); latch.countDown(); }));

            Broadcaster.broadcast("bestand-geaendert");

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(received).containsExactlyInAnyOrder("1:bestand-geaendert", "2:bestand-geaendert");
        }

        @Test
        @DisplayName("Ohne registrierte Listener läuft broadcast() fehlerfrei durch")
        void broadcast_noListeners_noException() {
            // Alle eigenen Registrations bereits leer in diesem Test
            assertThatCode(() -> Broadcaster.broadcast("lager-geaendert"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Fehlerhafter Listener unterbricht nicht die Zustellung an andere Listener")
        void broadcast_faultyListener_otherListenersStillNotified() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            List<String> received = new CopyOnWriteArrayList<>();

            // Listener der eine Exception wirft
            registrations.add(Broadcaster.register(e -> {
                throw new RuntimeException("Simulierter Fehler");
            }));

            // Listener der korrekt arbeitet
            registrations.add(Broadcaster.register(e -> {
                received.add(e);
                latch.countDown();
            }));

            Broadcaster.broadcast("lager-geaendert");

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(received).containsExactly("lager-geaendert");
        }

        @Test
        @DisplayName("Mehrere Events hintereinander werden alle korrekt zugestellt")
        void broadcast_multipleEventsSequentially_allDelivered() throws InterruptedException {
            List<String> received = new CopyOnWriteArrayList<>();
            CountDownLatch latch = new CountDownLatch(3);

            registrations.add(Broadcaster.register(e -> { received.add(e); latch.countDown(); }));

            Broadcaster.broadcast("event-1");
            Broadcaster.broadcast("event-2");
            Broadcaster.broadcast("event-3");

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(received).containsExactlyInAnyOrder("event-1", "event-2", "event-3");
        }

        @Test
        @DisplayName("Sehr langer Event-String wird korrekt durchgereicht")
        void broadcast_veryLongEventString_delivered() throws InterruptedException {
            String longEvent = "x".repeat(10_000);
            List<String> received = new CopyOnWriteArrayList<>();
            CountDownLatch latch = new CountDownLatch(1);

            registrations.add(Broadcaster.register(e -> { received.add(e); latch.countDown(); }));

            Broadcaster.broadcast(longEvent);

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(received.get(0)).hasSize(10_000);
        }

        @Test
        @DisplayName("Concurrent broadcasts von mehreren Threads verursachen keine Race Condition")
        void broadcast_concurrentBroadcasts_allDelivered() throws InterruptedException {
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<String> received = Collections.synchronizedList(new ArrayList<>());

            registrations.add(Broadcaster.register(e -> {
                received.add(e);
                latch.countDown();
            }));

            for (int i = 0; i < threadCount; i++) {
                final String event = "concurrent-" + i;
                Thread t = new Thread(() -> Broadcaster.broadcast(event));
                t.start();
            }

            assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(received).hasSize(threadCount);
        }

        @Test
        @DisplayName("Listener der sich selbst während des Events abmeldet verursacht keinen Fehler")
        void broadcast_listenerDeregistersItself_noException() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            Registration[] selfRef = new Registration[1];

            selfRef[0] = Broadcaster.register(e -> {
                selfRef[0].remove();
                latch.countDown();
            });

            assertThatCode(() -> Broadcaster.broadcast("self-remove")).doesNotThrowAnyException();
            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        }
    }
}