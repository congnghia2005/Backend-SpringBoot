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
        You are an expert Customer Relations Manager for an e-commerce platform.
        Your task is to analyze the customer's review and generate exactly 3 types of replies in Vietnamese.
        
        CRITICAL: You must return the output strictly as a raw JSON object, without any markdown formatting, wrappers, or ```json blocks. The JSON must exactly match this structure:
        {
          "standard": "A professional, polite, and well-structured response.",
          "friendly": "A warm, enthusiastic, and friendly response using empathetic language.",
          "escalation": "An appropriate response focused on apology, conflict resolution, or compensation if the review is negative (1-3 stars), or an extra appreciative closing if positive."
        }
        
        Guidelines for replies:
        - Always use polite and natural Vietnamese ("Cảm ơn quý khách", "Dạ", "Rất tiếc về trải nghiệm này").
        - Keep responses concise, targeted, and appropriate to the review sentiment.
        - Do not invent external details not implied in the review.
        """;

    public String generateReply(String reviewContent) {
        try {
            String finalPrompt = SYSTEM_PROMPT + "\nNội dung review của khách hàng: " + reviewContent;
            String response = chatModel.call(finalPrompt);

            if (response == null) {
                return "{\"error\":\"Gemini không trả dữ liệu\"}";
            }

            response = response.trim();
            if (response.startsWith("```")) {
                response = response.replaceAll("(?i)^```json", "").replaceAll("(?i)^```", "").replaceAll("```$", "").trim();
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Lỗi khi gọi Gemini AI: " + e.getMessage() + "\"}";
        }
    }

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

    public boolean fetchAndSaveMockReviews(String placeId) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            List<Map<String, Object>> mockPool = new ArrayList<>();

            // === 3 MẪU CŨ CỦA BẠN ===
            mockPool.add(Map.of(
                    "authorName", "Lê Minh Hoàng", "rating", 5,
                    "text", "Dịch vụ ở đây rất tốt, nhân viên thân thiện và hỗ trợ nhiệt tình. Sẽ quay lại lần sau!",
                    "status", "pending"
            ));
            mockPool.add(Map.of(
                    "authorName", "Phạm Thanh Thảo", "rating", 1,
                    "text", "Phòng ốc hơi bí, wifi tầng 3 sóng rất yếu không thể làm việc được, đề nghị khách sạn nâng cấp kiểm tra lại.",
                    "status", "pending"
            ));
            mockPool.add(Map.of(
                    "authorName", "Nguyễn Hoàng Nam", "rating", 3,
                    "text", "Đồ ăn sáng tạm ổn nhưng thực đơn chưa đa dạng lắm. Không gian quán rộng rãi sạch sẽ.",
                    "status", "pending"
            ));

            // === THÊM 10 MẪU ĐA DẠNG NGỮ CẢNH (KHEN / CHÊ / TRUNG LẬP) ===
            mockPool.add(Map.of(
                    "authorName", "Trần Thu Hà", "rating", 5,
                    "text", "Giao diện trải nghiệm rất tuyệt vời! Vị trí đắc địa ngay trung tâm Đà Nẵng, phòng view sông Hàn ngắm pháo hoa siêu đỉnh.",
                    "status", "pending"
            ));
            mockPool.add(Map.of(
                    "authorName", "Nguyễn Đình Tú", "rating", 2,
                    "text", "Giá nước hơi cao so với chất lượng. Mình gọi một ly Matcha Latte nhưng vị hơi nhạt và đá tan nhanh quá.",
                    "status", "pending"
            ));
            mockPool.add(Map.of(
                    "authorName", "Vũ Thị Ngọc Anh", "rating", 5,
                    "text", "Quán cafe decor tone trắng rất xinh, hợp gu chụp ảnh sống ảo. Thức uống Oolong Gạo Rang Sữa vị béo ngậy, rất thơm ngon!",
                    "status", "pending"
            ));
            mockPool.add(Map.of(
                    "authorName", "Phan Văn Đức", "rating", 1,
                    "text", "Thái độ của nhân viên giữ xe rất thô lỗ với khách. Mình vừa dắt xe vào đã bị quát mắng, trải nghiệm cực kỳ tệ hại, không bao giờ quay lại.",
                    "status", "pending"
            ));
            mockPool.add(Map.of(
                    "authorName", "Đỗ Hải Yến", "rating", 4,
                    "text", "Cơm thố xá xíu ở đây ngon đậm đà, thịt mềm thơm. Tuy nhiên điểm trừ là shipper giao hàng giờ cao điểm hơi lâu nên cơm bị nguội mất một chút.",
                    "status", "pending"
            ));
            mockPool.add(Map.of(
                    "authorName", "Bùi Quang Huy", "rating", 2,
                    "text", "Hệ thống cách âm của phòng quá kém! Đêm khuya phòng bên cạnh nói chuyện ồn ào mà bên mình nghe rõ mồn một, mất ngủ cả đêm.",
                    "status", "pending"
            ));
            mockPool.add(Map.of(
                    "authorName", "Lý Thanh Hằng", "rating", 5,
                    "text", "Món bún thái hải sản ở đây nước dùng chua cay đậm đà, tôm mực tươi rói ngọt thịt. Sẽ giới thiệu cho bạn bè cùng ghé quán trải nghiệm.",
                    "status", "pending"
            ));
            mockPool.add(Map.of(
                    "authorName", "Nguyễn Tiến Minh", "rating", 3,
                    "text", "Sân cầu lông thoáng mát, trần cao không bị chói mắt. Tuy nhiên bãi đỗ xe ô tô hơi chật, di chuyển ra vào khung giờ cao điểm khá vất vả.",
                    "status", "pending"
            ));
            mockPool.add(Map.of(
                    "authorName", "Đặng Minh Triết", "rating", 1,
                    "text", "Đặt súp hải sản giao tận nơi dặn đi dặn lại là KHÔNG BỎ MUỐI vì mình ăn kiêng, thế mà lúc nhận hàng vẫn mặn chát không nuốt nổi. Làm ăn quá cẩu thả!",
                    "status", "pending"
            ));
            mockPool.add(Map.of(
                    "authorName", "Ngô Quốc Bảo", "rating", 4,
                    "text", "Rạp chiếu phim ghế ngồi êm ái, âm thanh sống động thích hơp xem bom tấn Chainsaw Man. Nhân viên quầy bắp nước phục vụ nhanh nhẹn.",
                    "status", "pending"
            ));

            // Xáo trộn ngẫu nhiên kho dữ liệu
            Collections.shuffle(mockPool);

            // Mỗi lần bấm Fetch sẽ bốc ra 3 review ngẫu nhiên (bạn có thể đổi thành 2, 3 hoặc 5 tùy ý)
            int reviewsToFetch = 5;
            for (int i = 0; i < reviewsToFetch; i++) {
                Map<String, Object> sample = mockPool.get(i);
                // Sử dụng nanoTime kết hợp vòng lặp để sinh ID duy nhất tuyệt đối, tránh trùng khóa DB
                String reviewId = "rev_" + System.nanoTime() + "_" + i;

                Map<String, Object> docData = new HashMap<>();
                docData.put("id", reviewId);
                docData.put("placeId", placeId);
                docData.put("authorName", sample.get("authorName"));
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