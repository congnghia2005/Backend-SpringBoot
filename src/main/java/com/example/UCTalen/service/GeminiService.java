package com.example.UCTalen.service;


import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import java.util.*;

@Service
public class GeminiService {

    @Autowired
    private GoogleGenAiChatModel chatModel;

    private static final String SYSTEM_PROMPT = """
    Bạn là CRM chuyên nghiệp. Phân tích review và trả về JSON thuần (không markdown):
    {"standard":"...","friendly":"...","escalation":"..."}
    Dùng tiếng Việt lịch sự. Ngắn gọn, đúng trọng tâm.
    """;

    public boolean saveReplyToFirebase(String reviewId, String selectedReply) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            db.collection("reviews").document(reviewId)
                    .update(
                            "replyContent", selectedReply,
                            "status", "Resolved"
                    ).get();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // GeminiService.java - thêm method này
    public String generateReply(String reviewContent) {
        try {
            String finalPrompt = SYSTEM_PROMPT + "\nNội dung review: " + reviewContent;
            String response = chatModel.call(finalPrompt);

            if (response == null) return "{\"error\":\"Không có dữ liệu\"}";

            response = response.trim()
                    .replaceAll("(?i)^```json", "")
                    .replaceAll("(?i)^```", "")
                    .replaceAll("```$", "")
                    .trim();
            return response;
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    public boolean fetchAndSaveMockReviews(String placeId) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            List<Map<String, Object>> mockPool = new ArrayList<>();
            String cleanPlaceId = placeId.trim().toLowerCase();

            // 1. Kho tác giả ngẫu nhiên
            String[] authors = {
                    "Lê Minh Hoàng", "Phạm Thanh Thảo", "Nguyễn Hoàng Nam", "Trần Thu Hà", "Nguyễn Đình Tú",
                    "Vũ Thị Ngọc Anh", "Phan Văn Đức", "Đỗ Hải Yến", "Bùi Quang Huy", "Lý Thanh Hằng",
                    "Nguyễn Tiến Minh", "Đặng Minh Triết", "Ngô Quốc Bảo", "Hoàng Tuấn Kiệt", "Trần Mai Linh",
                    "Phạm Quốc Anh", "Nguyễn Phương Thảo", "Đặng Hải Nam", "Bùi Quang Đạt", "Lê Thị Thu Hương"
            };

            // 2. Phân loại nội dung review theo từng nhóm ngành nghề thực tế
            String[][] hotelTemplates = {
                    {"5", "Dịch vụ ở đây rất tốt, nhân viên thân thiện và hỗ trợ nhiệt tình. Sẽ quay lại lần sau!"},
                    {"1", "Phòng ốc hơi bí, wifi tầng 3 sóng rất yếu không thể làm việc được, đề nghị nâng cấp kiểm tra lại."},
                    {"5", "Vị trí đắc địa ngay trung tâm Đà Nẵng, phòng view sông Hàn ngắm cảnh siêu đỉnh, rất đáng tiền."},
                    {"2", "Hệ thống cách âm quá kém! Đêm khuya phòng bên cạnh nói chuyện ồn ào mà bên mình nghe rõ mồn một, mất ngủ cả đêm."},
                    {"4", "Khách sạn sạch sẽ, gần biển đi bộ vài bước là tới. Đồ ăn sáng ngon nhưng nệm hơi cứng chút."},
                    {"3", "Phòng ốc giống hình, tuy nhiên nước trong nhà vệ sinh thoát hơi chậm. Lễ tân nhiệt tình bù lại."},
                    {"1", "Trải nghiệm tệ, đặt phòng giường đôi nhưng khi nhận lại là 2 giường đơn ghép lại. Phòng có mùi ẩm mốc."}
            };

            String[][] cafeTemplates = {
                    {"2", "Giá nước hơi cao so với chất lượng. Mình gọi một ly Matcha Latte nhưng vị hơi nhạt và đá tan nhanh quá."},
                    {"5", "Quán decor tone trắng rất xinh, hợp gu chụp ảnh sống ảo. Thức uống Oolong Gạo Rang Sữa vị béo ngậy, rất thơm ngon!"},
                    {"4", "Không gian quán rộng rãi, thích hợp để học tập và làm việc chạy deadline. Menu Jasmine milk tea rất thơm."},
                    {"5", "Nhân viên siêu dễ thương, mình dặn đá riêng mang về tiệm đóng gói rất cẩn thận, chu đáo."},
                    {"5", "Không gian ở đây cực kỳ yên tĩnh, nhạc mở nhẹ nhàng rất hợp để ngồi đọc sách hoặc làm việc."},
                    {"3", "Chất lượng đồ uống ở mức bình thường, không có gì quá nổi bật. Điểm cộng là view cửa sổ nhìn đường phố chill."},
                    {"4", "Quán nước rộng, điều hòa mát mẻ. Điểm trừ duy nhất là vào giờ cao điểm quán hơi ồn ào chút."},
                    {"1", "Thái độ nhân viên phục vụ rất kém, gọi nước mà đợi hơn 20 phút không thấy đâu, lúc nhắc thì tỏ thái độ."}
            };

            String[][] foodTemplates = {
                    {"4", "Cơm thố xá xíu ở đây ngon đậm đà, thịt mềm thơm. Tuy nhiên shipper giao hàng giờ cao điểm hơi lâu nên bị nguội chút."},
                    {"5", "Món bún thái hải sản ở đây nước dùng chua cay đậm đà, tôm mực tươi rói ngọt thịt. Sẽ giới thiệu cho bạn bè ghé quán."},
                    {"1", "Đặt súp hải sản giao tận nơi dặn đi dặn lại là KHÔNG BỎ MUỐI vì mình ăn kiêng, thế mà lúc nhận vẫn mặn chát. Cẩu thả!"},
                    {"5", "Bún bò Huế ở đây chuẩn vị, nước dùng thơm mùi mắm ruốc, thịt nạm mềm và chả cua rất to, ăn siêu dính."},
                    {"2", "Hẹn lịch trước nhưng khi đến vẫn phải đợi hơn 30 phút mới có bàn. Cách sắp xếp của quản lý quán ăn quá kém."},
                    {"4", "Giá cả hợp lý so với mặt bằng trung tâm. Đồ lên nhanh, nhân viên phục vụ chu đáo, đồ ăn đậm đà vị miền Trung."},
                    {"1", "Làm ăn cẩu thả! Đồ ăn dính cọng tóc bên trong, gọi quản lý ra thì chỉ xin lỗi qua loa chứ không đổi món mới."},
                    {"5", "Thực sự ấn tượng với quán này. Khi mình làm đổ nước ngọt, nhân viên lập tức đến lau dọn và tặng lại ly mới miễn phí."}
            };

            String[][] entertainmentTemplates = {
                    {"4", "Rạp chiếu phim ghế ngồi êm ái, âm thanh sống động thích hơp xem bom tấn Chainsaw Man. Nhân viên phục vụ nhanh nhẹn."},
                    {"3", "Sân cầu lông thoáng mát, trần cao không bị chói mắt. Tuy nhiên bãi đỗ xe ô tô hơi chật, di chuyển giờ cao điểm vất vả."},
                    {"1", "Thái độ của nhân viên giữ xe rất thô lỗ với khách. Mình vừa dắt xe vào đã bị quát mắng, trải nghiệm cực kỳ tệ hại."},
                    {"5", "Sân futsal cỏ nhân tạo đá rất êm chân, hệ thống đèn chiếu ban đêm sáng rõ, giá thuê khung giờ vàng hợp lý."},
                    {"4", "Rạp phim sạch sẽ, phòng chiếu màn hình lớn xem rất đã mắt. Bắp nước ngon, nhân viên quầy phục vụ lịch sự."},
                    {"2", "Sân thể thao trần hơi thấp nên đánh cầu thỉnh thoảng bị vướng, lưới căng chưa được chuẩn lắm."},
                    {"5", "Rạp này âm thanh thuộc dạng đỉnh nhất Đà Nẵng, ghế ngồi đôi rộng rãi thoải mái, đi xem phim với người yêu rất hợp."},
                    {"1", "Sân bãi xuống cấp, mặt cỏ futsal bị bong tróc nhiều chỗ dễ gây chấn thương khi chạy, không bao giờ quay lại."}
            };

            // 3. 🔑 NHẬN DIỆN THÔNG MINH: Dựa vào từ khóa trong placeId để chọn đúng kho mẫu tương ứng
            String[][] selectedTemplates;
            if (cleanPlaceId.contains("hotel") || cleanPlaceId.contains("khachsan") || cleanPlaceId.contains("resort")) {
                selectedTemplates = hotelTemplates;
            } else if (cleanPlaceId.contains("cafe") || cleanPlaceId.contains("coffee") || cleanPlaceId.contains("tra")) {
                selectedTemplates = cafeTemplates;
            } else if (cleanPlaceId.contains("food") || cleanPlaceId.contains("com") || cleanPlaceId.contains("bun") || cleanPlaceId.contains("quan")) {
                selectedTemplates = foodTemplates;
            } else if (cleanPlaceId.contains("cinema") || cleanPlaceId.contains("rạp") || cleanPlaceId.contains("san") || cleanPlaceId.contains("sport")) {
                selectedTemplates = entertainmentTemplates;
            } else {
                // Nếu gõ ID bất kỳ không chứa từ khóa đặc biệt -> Trộn chung tất cả các nhóm lại làm mẫu
                List<String[]> allTemplates = new ArrayList<>();
                Collections.addAll(allTemplates, hotelTemplates);
                Collections.addAll(allTemplates, cafeTemplates);
                Collections.addAll(allTemplates, foodTemplates);
                Collections.addAll(allTemplates, entertainmentTemplates);
                selectedTemplates = allTemplates.toArray(new String[0][]);
            }

            // 4. Tự động sinh tổ hợp ma trận dựa trên kho mẫu đã chọn lọc
            int authorIndex = 0;
            int templateIndex = 0;
            for (int i = 0; i < 100; i++) {
                String author = authors[authorIndex];
                String[] template = selectedTemplates[templateIndex];
                int rating = Integer.parseInt(template[0]);
                String text = template[1];

                Map<String, Object> reviewMap = new HashMap<>();
                reviewMap.put("authorName", author);
                reviewMap.put("rating", rating);
                reviewMap.put("text", text);
                reviewMap.put("status", "pending");

                mockPool.add(reviewMap);

                authorIndex = (authorIndex + 1) % authors.length;
                templateIndex = (templateIndex + 1) % selectedTemplates.length;
            }

            // Xáo trộn ngẫu nhiên kho dữ liệu ngành nghề đã chọn
            Collections.shuffle(mockPool);

            // Tìm và xóa sạch tất cả review cũ của đúng placeId này trên Firebase trước khi nạp mới
            com.google.api.core.ApiFuture<com.google.cloud.firestore.QuerySnapshot> oldReviewsQuery = db.collection("reviews")
                    .whereEqualTo("placeId", cleanPlaceId)
                    .get();

            List<com.google.cloud.firestore.QueryDocumentSnapshot> oldDocuments = oldReviewsQuery.get().getDocuments();
            for (com.google.cloud.firestore.QueryDocumentSnapshot oldDoc : oldDocuments) {
                db.collection("reviews").document(oldDoc.getId()).delete().get();
            }

            // Bốc ngẫu nhiên ra đúng 5 bài review tương ứng để lưu vào DB
            int reviewsToFetch = 5;
            for (int i = 0; i < reviewsToFetch; i++) {
                Map<String, Object> sample = mockPool.get(i);

                String authorName = (String) sample.get("authorName");
                String cleanAuthorName = authorName.replaceAll("\\s+", "").toLowerCase();
                String reviewId = "rev_" + cleanPlaceId + "_" + cleanAuthorName;

                Map<String, Object> docData = new HashMap<>();
                docData.put("id", reviewId);
                docData.put("placeId", cleanPlaceId);
                docData.put("authorName", authorName);
                docData.put("rating", sample.get("rating"));
                docData.put("text", sample.get("text"));
                docData.put("status", sample.get("status"));

                db.collection("reviews").document(reviewId).set(docData).get();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
