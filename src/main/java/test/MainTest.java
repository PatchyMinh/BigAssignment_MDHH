package test;

import dao.*;
import factory.*;
import model.*;
import java.time.LocalDate;

public class MainTest {
    public static void main(String[] args) {
        System.out.println("====== BẮT ĐẦU TEST HỆ THỐNG ĐẤU GIÁ ======\n");

        // 1. Khởi tạo các DAO
        UserDAO userDAO = new UserDAOImpl();
        ItemDAO itemDAO = new ItemDAOImpl();
        AuctionSessionDAO sessionDAO = new AuctionSessionDAOImpl();
        BidDAO bidDAO = new BidDAOImpl();

        // 2. TEST ĐĂNG NHẬP (NGƯỜI BÁN)
        System.out.println("--- BƯỚC 1: ĐĂNG NHẬP NGƯỜI BÁN ---");
        User seller = userDAO.login("admin", "123");
        if (seller == null) {
            System.out.println("❌ Lỗi: Không tìm thấy tài khoản admin.");
            return; // Dừng chương trình nếu không có user
        }
        System.out.println("✅ Đăng nhập thành công! Người bán: " + seller.getUsername());

        // 3. TEST TẠO HÀNG HÓA BẰNG FACTORY PATTERN
        System.out.println("\n--- BƯỚC 2: TẠO HÀNG HÓA & LƯU VÀO DB ---");

        // 3.1 Tạo bức tranh (Arts)
        ItemsAttributes artAttr = new ItemsAttributes();
        artAttr.setOwner(seller.getUsername());
        artAttr.setStartingPrice(5000.0);
        artAttr.setDescription("Bức tranh Đêm Đầy Sao (Bản sao siêu cấp)");
        artAttr.setArtistName("Vincent van Gogh");
        artAttr.setReleaseDate(LocalDate.of(1889, 6, 1));
        Items artItem = new TypeArts().createItems(artAttr);
        itemDAO.addItem(artItem);
        System.out.println("✅ Đã lưu Tranh (Arts) vào DB - ID: " + artItem.getItemID());

        // 3.2 Tạo đồ điện tử (Electronics)
        ItemsAttributes elecAttr = new ItemsAttributes();
        elecAttr.setOwner(seller.getUsername());
        elecAttr.setStartingPrice(12000.0);
        elecAttr.setDescription("Điện thoại iPhone 15 Pro Max");
        elecAttr.setBrand("Apple");
        elecAttr.setWarranty(12);
        Items elecItem = new TypeElectronics().createItems(elecAttr);
        itemDAO.addItem(elecItem);
        System.out.println("✅ Đã lưu Điện thoại (Electronics) vào DB - ID: " + elecItem.getItemID());

        // 3.3 Tạo xe cộ (Vehicles)
        ItemsAttributes vehicleAttr = new ItemsAttributes();
        vehicleAttr.setOwner(seller.getUsername());
        vehicleAttr.setStartingPrice(500000.0);
        vehicleAttr.setDescription("Xe máy SH 150i");
        vehicleAttr.setBrand("Honda");
        vehicleAttr.setMileage(1500);
        vehicleAttr.setVehicleID("29A1-123.45"); // Biển số
        Items vehicleItem = new TypeVehicles().createItems(vehicleAttr);
        itemDAO.addItem(vehicleItem);
        System.out.println("✅ Đã lưu Xe máy (Vehicles) vào DB - ID: " + vehicleItem.getItemID());


        // 4. TEST TẠO PHIÊN ĐẤU GIÁ CHO BỨC TRANH
        System.out.println("\n--- BƯỚC 3: TẠO PHIÊN ĐẤU GIÁ ---");
        String sessionId = "SS_ART_" + System.currentTimeMillis();
        AuctionSession session = new AuctionSession(
                seller,
                sessionId,
                artItem.getStartingPrice(), // Giá khởi điểm 5000
                500.0,                      // Bước giá
                3                           // Thời gian 3 ngày
        );
        boolean isSessionCreated = sessionDAO.createSession(session, artItem.getItemID());
        if (isSessionCreated) {
            System.out.println("✅ Tạo Phiên đấu giá thành công! ID Phiên: " + sessionId);
        } else {
            System.out.println("❌ Lỗi tạo phiên đấu giá.");
            return;
        }

        // 5. TEST NGƯỜI MUA ĐĂNG NHẬP VÀ ĐẶT GIÁ
        System.out.println("\n--- BƯỚC 4: NGƯỜI MUA ĐẶT GIÁ (BID) ---");
        User buyer = userDAO.login("nguoimua", "123");
        if (buyer != null) {
            System.out.println("✅ Đăng nhập thành công! Người mua: " + buyer.getUsername());

            // Người mua đặt giá 6000 (Lớn hơn giá khởi điểm 5000)
            Bid newBid = new Bid(buyer, 6000.0);
            boolean isBidSuccess = bidDAO.addBid(sessionId, newBid);

            if (isBidSuccess) {
                System.out.println("✅ Người mua '" + buyer.getUsername() + "' đã đặt giá THÀNH CÔNG: " + newBid.getAmount() + "$");
            } else {
                System.out.println("❌ Đặt giá thất bại.");
            }
        }

        System.out.println("\n====== KẾT THÚC TEST ======");
    }
}