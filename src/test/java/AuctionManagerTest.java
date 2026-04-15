import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import model.*;
public class AuctionManagerTest {

    private AuctionManager manager;
    private User dummySeller;

    @BeforeEach
    public void setUp() {
        manager = AuctionManager.getInstance();
        dummySeller = new User("Dummy Seller", "dummy", "dummy@mail.com", "pass", "123");
    }

    @Test
    public void testSingletonInstance() {
        AuctionManager anotherManager = AuctionManager.getInstance();
        assertSame(manager, anotherManager, "Both instances should be exactly the same object (Singleton)");
    }

    @Test
    public void testAddAndFindSession() {
        AuctionSession session = new AuctionSession(dummySeller, "SS_MGR_001", 50000, 10000, 2);
        manager.addSession(session);

        AuctionSession foundSession = manager.findSessionByID("SS_MGR_001");
        
        assertNotNull(foundSession, "Should find the session that was just added");
        assertEquals("SS_MGR_001", foundSession.getSessionID(), "The IDs should match exactly");
    }

    @Test
    public void testFindSessionNotFound() {
        AuctionSession notFound = manager.findSessionByID("INVALID_ID_999");
        assertNull(notFound, "Should return null for non-existent session IDs");
    }

    @Test
    public void testGetSessionsByStatus() {
        // 1. Create a PENDING session
        AuctionSession pendingSession = new AuctionSession(dummySeller, "SS_PENDING_01", 10000, 1000, 1);
        
        // 2. Create an OPEN session
        AuctionSession openSession = new AuctionSession(dummySeller, "SS_OPEN_01", 20000, 2000, 1);
        openSession.startSession(1); // Changes status to OPEN

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