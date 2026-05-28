package com.example.UCTalen.service;

import UCTalent.UCOrm.model.Review;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private static final String COLLECTION_NAME = "reviews";

    public List<Review> getAllReviews() throws Exception {
        Firestore dbFirestore = FirestoreClient.getFirestore();
                
        CollectionReference reviewsCollection = dbFirestore.collection(COLLECTION_NAME);
        
        ApiFuture<QuerySnapshot> querySnapshot = reviewsCollection.get();
        
        List<Review> reviewList = new ArrayList<>();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        
        for (QueryDocumentSnapshot document : documents) {
            Review review = document.toObject(Review.class);
            review.setId(document.getId()); 
            reviewList.add(review);
        }
        
        return reviewList;
    }
}