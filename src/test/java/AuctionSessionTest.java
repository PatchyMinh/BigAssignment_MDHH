import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuctionSessionTest {

    private User seller;
    private User bidder1;
    private User bidder2;
    private Transaction transaction;
    private AuctionSession session;

    @BeforeEach
    public void setUp() {
        // 1. Create users for testing
        seller = new User("Alice Seller", "alice_seller", "alice@gmail.com", "password", "0111");
        bidder1 = new User("Bob Bidder", "bob_bidder", "bob@gmail.com", "password", "0222");
        bidder2 = new User("Charlie Bidder", "charlie_bidder", "charlie@gmail.com", "password", "0333");

        // 2. Setup initial balances via Transaction
        transaction = new Transaction();
        transaction.deposit(bidder1, 100000); // Bob has 100k
        transaction.deposit(bidder2, 500000); // Charlie has 500k

        // 3. Initialize an auction session (startingPrice: 50k, incrementStep: 10k, openDays: 2)
        session = new AuctionSession("S01", 50000, 10000, 2); 
    }

    @Test
    public void testPlaceBidBeforeStart() {
        // Attempt to place a bid when session status is still PENDING
        boolean result = session.placeBid(bidder1, 60000);
        
        assertFalse(result, "Should NOT be able to place a bid before the session starts");
    }

    @Test
    public void testPlaceBidSuccess() {
        session.startSession(2);
        
        // Bob bids 60k (50k starting price + 10k increment step) -> Valid
        boolean result = session.placeBid(bidder1, 60000);
        
        assertTrue(result, "Placing a valid bid of 60k should return true");
    }

    @Test
    public void testPlaceBidInvalidIncrement() {
        session.startSession(2);
        
        // Bob bids 55k (Does not meet the 10k increment requirement) -> Invalid
        boolean result = session.placeBid(bidder1, 55000);
        
        assertFalse(result, "Bid should fail due to invalid increment step");
    }

    @Test
    public void testPlaceBidInsufficientBalance() {
        session.startSession(2);
        
        // Bob has 100k, but attempts to bid 200k -> Invalid
        boolean result = session.placeBid(bidder1, 200000);
        
        assertFalse(result, "Bid should fail because the user does not have enough balance");
    }

    @Test
    public void testMultipleBidsCompetition() {
        session.startSession(2);
        
        // 1. Bob bids 60k (Valid)
        session.placeBid(bidder1, 60000);
        
        // 2. Charlie bids 65k (Invalid - current price is 60k, minimum next bid must be 70k)
        boolean resultCharlieFail = session.placeBid(bidder2, 65000);
        assertFalse(resultCharlieFail, "Charlie's bid of 65k should fail due to increment rule");
        
        // 3. Charlie bids 80k (Valid)
        boolean resultCharlieSuccess = session.placeBid(bidder2, 80000);
        assertTrue(resultCharlieSuccess, "Charlie's bid of 80k should be successful");
    }
}