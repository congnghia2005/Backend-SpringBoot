package UCTalent.UCOrm.model;

import java.util.Map;

import lombok.Data;

@Data
public class Review {
    private String id;
    private String placeId;
    private String authorName;
    private int rating;
    private String text;
    private String status;
    private Map<String, String> aiResponses;
    private String selectedResponse;
    private String replyContent;
}