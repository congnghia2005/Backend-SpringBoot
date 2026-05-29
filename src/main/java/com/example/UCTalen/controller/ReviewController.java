package com.example.UCTalen.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.UCTalen.service.GeminiService;
import com.example.UCTalen.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import UCTalent.UCOrm.model.Review;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*") // Ngăn lỗi chặn CORS khi Angular gọi sang
public class ReviewController {

    // 🔑 ĐÃ SỬA: Chuyển cả 2 thành biến final an toàn
    private final ReviewService reviewService;
    private final GeminiService geminiService;

    // 🔑 ĐÃ THÊM: Tạo Constructor để Spring tự động tiêm cả 2 dịch vụ vào khi chạy
    public ReviewController(ReviewService reviewService, GeminiService geminiService) {
        this.reviewService = reviewService;
        this.geminiService = geminiService;
    }

//    @GetMapping
//    public List<Review> getReviews() {
//        try {
//            return reviewService.getAllReviews();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) String placeId) {
        try {
            // 🔑 YÊU CẦU 3: Khi chưa làm gì hoặc ô tìm kiếm trống -> Trả về mảng rỗng, giao diện trống trơn
            if (placeId == null || placeId.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return reviewService.getReviewsByPlaceId(placeId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> fetchGoogleReviews(@RequestBody Map<String, String> requestBody) {
        String placeId = requestBody.get("placeId");

        if (placeId == null || placeId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Place ID không được để trống!"));
        }

        // Gọi hàm sinh dữ liệu nâng cao miễn phí trong GeminiService mượt mà
        boolean isFetched = geminiService.fetchAndSaveMockReviews(placeId);

        if (isFetched) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã fetch thành công dữ liệu từ Place ID: " + placeId));
        } else {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi hệ thống lưu DB!"));
        }
    }
}