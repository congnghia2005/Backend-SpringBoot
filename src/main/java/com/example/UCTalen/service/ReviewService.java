package com.example.UCTalen.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import UCTalent.UCOrm.model.Review;

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

    public List<Review> getReviewsByPlaceId(String placeId) throws Exception {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> querySnapshot = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("placeId", placeId)
                .get();

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