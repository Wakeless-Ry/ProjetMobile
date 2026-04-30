package com.example.traveling.travelshare.data;

import com.example.traveling.travelshare.model.Author;
import com.example.traveling.travelshare.model.Group;
import com.example.traveling.travelshare.model.Photo;
import com.example.traveling.travelshare.model.PhotoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {

    private static final String COLLECTION_PHOTOS = "ts_photos";
    private static final String COLLECTION_GROUPS = "ts_groups";
    private static final String COLLECTION_USERS  = "ts_users";

    private final FirebaseFirestore db;

    public FirestoreManager() {
        this.db = FirebaseFirestore.getInstance();
    }

    // --- PHOTOS ---

    public void getPhotos(String groupId, String locationType, OnDataLoadedListener<List<Photo>> listener) {
        Query query = db.collection(COLLECTION_PHOTOS);

        if (groupId != null && !groupId.equals("all")) {
            query = query.whereEqualTo("groupId", groupId);
        } else {
            query = query.whereEqualTo("isPublic", true);
        }

        if (locationType != null && !locationType.equals("all")) {
            query = query.whereEqualTo("locationType", locationType);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Photo> photos = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    photos.add(mapDocToPhoto(doc));
                }
                
                // Tri manuel par date décroissante pour éviter les erreurs d'index composite
                photos.sort((p1, p2) -> {
                    String c1 = p1.getCreatedAt();
                    String c2 = p2.getCreatedAt();
                    if (c1 == null) return 1;
                    if (c2 == null) return -1;
                    return c2.compareTo(c1);
                });
                
                listener.onSuccess(photos);
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public void getPhotosByAuthor(String authorId, OnDataLoadedListener<List<Photo>> listener) {
        db.collection(COLLECTION_PHOTOS)
                .whereEqualTo("author.id", authorId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Photo> photos = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            photos.add(mapDocToPhoto(doc));
                        }
                        
                        // Tri manuel
                        photos.sort((p1, p2) -> {
                            String c1 = p1.getCreatedAt();
                            String c2 = p2.getCreatedAt();
                            if (c1 == null) return 1;
                            if (c2 == null) return -1;
                            return c2.compareTo(c1);
                        });

                        listener.onSuccess(photos);
                    } else {
                        listener.onError(task.getException());
                    }
                });
    }

    public void getUser(String userId, OnDataLoadedListener<Author> listener) {
        db.collection(COLLECTION_USERS).document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DocumentSnapshot doc = task.getResult();
                Author author = new Author(
                        doc.getId(),
                        doc.getString("name"),
                        doc.getString("avatar")
                );
                listener.onSuccess(author);
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public void createUser(Author author, OnDataLoadedListener<Void> listener) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", author.getName());
        map.put("avatar", author.getAvatar());
        
        db.collection(COLLECTION_USERS).document(author.getId())
                .set(map)
                .addOnSuccessListener(v -> listener.onSuccess(null))
                .addOnFailureListener(listener::onError);
    }

    public void getPhoto(String photoId, OnDataLoadedListener<Photo> listener) {
        db.collection(COLLECTION_PHOTOS).document(photoId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                listener.onSuccess(mapDocToPhoto(task.getResult()));
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public void likePhoto(String photoId, boolean like, OnDataLoadedListener<Void> listener) {
        db.collection(COLLECTION_PHOTOS).document(photoId)
                .update("likes", com.google.firebase.firestore.FieldValue.increment(like ? 1 : -1))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) listener.onSuccess(null);
                    else listener.onError(task.getException());
                });
    }

    public void addComment(String photoId, String comment, OnDataLoadedListener<Void> listener) {
        db.collection(COLLECTION_PHOTOS).document(photoId)
                .update("comments", com.google.firebase.firestore.FieldValue.arrayUnion(comment))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) listener.onSuccess(null);
                    else listener.onError(task.getException());
                });
    }

    public void addPhoto(Photo photo, OnDataLoadedListener<Void> listener) {
        Map<String, Object> data = mapPhotoToMap(photo);
        db.collection(COLLECTION_PHOTOS).document(photo.getId()).set(data)
                .addOnSuccessListener(aVoid -> listener.onSuccess(null))
                .addOnFailureListener(listener::onError);
    }

    // --- GROUPS ---

    public void getAllGroups(OnDataLoadedListener<List<Group>> listener) {
        db.collection(COLLECTION_GROUPS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Group> groups = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    groups.add(mapDocToGroup(doc));
                }
                listener.onSuccess(groups);
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public void joinGroup(String groupId, OnDataLoadedListener<Void> listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection(COLLECTION_GROUPS).document(groupId)
                .update("memberIds", FieldValue.arrayUnion(user.getUid()))
                .addOnSuccessListener(v -> listener.onSuccess(null))
                .addOnFailureListener(listener::onError);
    }

    public void leaveGroup(String groupId, OnDataLoadedListener<Void> listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection(COLLECTION_GROUPS).document(groupId)
                .update("memberIds", FieldValue.arrayRemove(user.getUid()))
                .addOnSuccessListener(v -> listener.onSuccess(null))
                .addOnFailureListener(listener::onError);
    }

    public void addGroup(Group group, OnDataLoadedListener<Void> listener) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", group.getName());
        map.put("description", group.getDescription());
        map.put("imageUrl", group.getImageUrl());
        map.put("memberIds", group.getMemberIds());

        db.collection(COLLECTION_GROUPS).document(group.getId())
                .set(map)
                .addOnSuccessListener(v -> listener.onSuccess(null))
                .addOnFailureListener(listener::onError);
    }

    // --- MAPPING ---

    private Photo mapDocToPhoto(DocumentSnapshot doc) {
        Map<String, Object> locMap = (Map<String, Object>) doc.get("location");
        PhotoLocation loc = new PhotoLocation(
                (String) locMap.get("name"),
                (Double) locMap.get("lat"),
                (Double) locMap.get("lng"),
                (Boolean) locMap.get("approximate")
        );

        Map<String, Object> authorMap = (Map<String, Object>) doc.get("author");
        Author author = new Author(
                (String) authorMap.get("id"),
                (String) authorMap.get("name"),
                (String) authorMap.get("avatar")
        );

        return new Photo(
                doc.getId(),
                doc.getString("imageUrl"),
                doc.getString("title"),
                doc.getString("description"),
                loc,
                doc.getString("date"),
                doc.getString("period"),
                (List<String>) doc.get("comments"),
                doc.getString("directions"),
                author,
                (List<String>) doc.get("tags"),
                doc.getString("locationType"),
                doc.getLong("likes").intValue(),
                false, // isLiked (devrait être géré par l'user)
                doc.getBoolean("isPublic"),
                doc.getString("createdAt"),
                doc.getString("groupId")
        );
    }

    private Map<String, Object> mapPhotoToMap(Photo p) {
        Map<String, Object> map = new HashMap<>();
        map.put("imageUrl", p.getImageUrl());
        map.put("title", p.getTitle());
        map.put("description", p.getDescription());
        map.put("date", p.getDate());
        map.put("period", p.getPeriod());
        map.put("comments", p.getComments());
        map.put("directions", p.getDirections());
        map.put("locationType", p.getLocationType());
        map.put("likes", p.getLikes());
        map.put("isPublic", p.isPublic());
        map.put("createdAt", p.getCreatedAt());
        map.put("groupId", p.getGroupId());

        Map<String, Object> loc = new HashMap<>();
        loc.put("name", p.getLocation().getName());
        loc.put("lat", p.getLocation().getLat());
        loc.put("lng", p.getLocation().getLng());
        loc.put("approximate", p.getLocation().isApproximate());
        map.put("location", loc);

        Map<String, Object> author = new HashMap<>();
        author.put("id", p.getAuthor().getId());
        author.put("name", p.getAuthor().getName());
        author.put("avatar", p.getAuthor().getAvatar());
        map.put("author", author);

        return map;
    }

    private Group mapDocToGroup(DocumentSnapshot doc) {
        Group g = new Group(
                doc.getId(),
                doc.getString("name"),
                doc.getString("description"),
                doc.getString("imageUrl")
        );
        List<String> members = (List<String>) doc.get("memberIds");
        if (members != null) {
            for (String m : members) g.getMemberIds().add(m);
        }
        return g;
    }

    public interface OnDataLoadedListener<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }
}
