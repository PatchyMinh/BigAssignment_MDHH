import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.*;
public class AuctionSessionTest {

    private User seller;
    private User bidder1;
    private User bidder2;
    private AuctionSession session;

    @BeforeEach
    public void setUp() {
        // 1. Create users
        seller = new User("Alice Seller", "alice_seller", "alice@gmail.com", "password", "0111");
        bidder1 = new User("Bob Bidder", "bob_bidder", "bob@gmail.com", "password", "0222");
        bidder2 = new User("Charlie Bidder", "charlie_bidder", "charlie@gmail.com", "password", "0333");

        // 2. Setup balances directly using User's deposit method
        bidder1.deposit(100000); // Bob has 100k
        bidder2.deposit(500000); // Charlie has 500k

        // 3. Initialize an auction session
        session = new AuctionSession(seller, "SS_TEST_01", 50000, 10000, 2); 
    }

    @Test
    public void testSessionInitializationAndSellerHistory() {
        // Check if status is correctly set to PENDING
        assertEquals(AuctionSession.Status.PENDING, session.status, "New session should be PENDING");
        
        // Check if the session was automatically added to the seller's created list
        assertTrue(seller.getCreatedAuctionSessions().contains(session), "Seller's history should contain this newly created session");
    }

    @Test
    public void testPlaceBidBeforeStart() {
        boolean result = session.placeBid(bidder1, 60000);
        assertFalse(result, "Should NOT be able to place a bid before the session starts");
        
        // Bidder's history should be empty since the bid failed
        assertFalse(bidder1.getJoinedAuctionSessions().contains(session), "Failed bid should not add session to joined history");
    }

    @Test
    public void testPlaceBidSuccessAndBidderHistory() {
        session.startSession(2);
        
        boolean result = session.placeBid(bidder1, 60000);
        assertTrue(result, "Placing a valid bid of 60k should return true");
        
        // Check if the session was added to the bidder's joined list
        assertTrue(bidder1.getJoinedAuctionSessions().contains(session), "Bidder's joined history should contain this session after a successful bid");
    }

    @Test
    public void testPlaceBidInvalidIncrement() {
        session.startSession(2);
        boolean result = session.placeBid(bidder1, 55000);
        assertFalse(result, "Bid should fail due to invalid increment step");
    }

    @Test
    public void testPlaceBidInsufficientBalance() {
        session.startSession(2);
        boolean result = session.placeBid(bidder1, 200000); // Bob only has 100k
        assertFalse(result, "Bid should fail because the user does not have enough balance");
    }

    @Test
    public void testMultipleBidsCompetitionAndHistoryDuplication() {
        session.startSession(2);
        
        // 1. Bob bids 60k (Valid)
        session.placeBid(bidder1, 60000);
        
        // 2. Charlie bids 80k (Valid)
        session.placeBid(bidder2, 80000);
        
        // 3. Bob bids again 100k (Valid)
        session.placeBid(bidder1, 100000);
        
        // Check if both bidders have the session in their history
        assertTrue(bidder1.getJoinedAuctionSessions().contains(session));
        assertTrue(bidder2.getJoinedAuctionSessions().contains(session));
        
        // Check that Bob's joined history does NOT have duplicates (size should be 1)
        assertEquals(1, bidder1.getJoinedAuctionSessions().size(), "Bidder history should not duplicate the same session");
    }
}