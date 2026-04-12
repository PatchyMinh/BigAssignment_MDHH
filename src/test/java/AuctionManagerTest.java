import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

public class AuctionManagerTest {

    private AuctionManager manager;

    @BeforeEach
    public void setUp() {
        // Get the single instance of AuctionManager
        manager = AuctionManager.getInstance();
    }

    @Test
    public void testSingletonInstance() {
        AuctionManager anotherManager = AuctionManager.getInstance();
        
        // Assert that both references point to the exact same object in memory
        assertSame(manager, anotherManager, "Both instances should be exactly the same object (Singleton)");
    }

    @Test
    public void testAddAndFindSession() {
        // Assume AuctionSession constructor is: (sessionID, startingPrice, incrementStep, openDays)
        AuctionSession session = new AuctionSession("SS_001", 50000, 10000, 2);
        manager.addSession(session);

        AuctionSession foundSession = manager.findSessionByID("SS_001");
        
        assertNotNull(foundSession, "Should find the session that was just added");
        assertEquals("SS_001", foundSession.getSessionID(), "The IDs should match exactly");
    }

    @Test
    public void testFindSessionNotFound() {
        AuctionSession notFound = manager.findSessionByID("INVALID_ID_999");
        
        assertNull(notFound, "Should return null for non-existent session IDs");
    }

    @Test
    public void testGetSessionsByStatus() {
        // 1. Create a PENDING session
        AuctionSession pendingSession = new AuctionSession("SS_PENDING_01", 10000, 1000, 1);
        
        // 2. Create an OPEN session
        AuctionSession openSession = new AuctionSession("SS_OPEN_01", 20000, 2000, 1);
        openSession.startSession(1); // This changes status to OPEN

        manager.addSession(pendingSession);
        manager.addSession(openSession);

        // 3. Fetch filtered lists
        List<AuctionSession> pendingList = manager.getSessionsByStatus(AuctionSession.Status.PENDING);
        List<AuctionSession> openList = manager.getSessionsByStatus(AuctionSession.Status.OPEN);

        // 4. Assertions
        assertTrue(pendingList.contains(pendingSession), "Pending list should contain the pending session");
        assertFalse(pendingList.contains(openSession), "Pending list should NOT contain the open session");

        assertTrue(openList.contains(openSession), "Open list should contain the open session");
        assertFalse(openList.contains(pendingSession), "Open list should NOT contain the pending session");
    }
}