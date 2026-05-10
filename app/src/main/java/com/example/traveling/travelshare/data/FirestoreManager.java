package com.example.traveling.travelshare.data;

import android.util.Log;

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

    private static final String TAG = "FirestoreManager";
    private static final String COLLECTION_PHOTOS = "ts_photos";
    private static final String COLLECTION_GROUPS = "ts_groups";
    private static final String COLLECTION_USERS  = "ts_users";

    private final FirebaseFirestore db;

    public FirestoreManager() {
        this.db = FirebaseFirestore.getInstance();
    }


    public void getPhotos(String groupId, String locationType, OnDataLoadedListener<List<Photo>> listener) {
        Log.d(TAG, "getPhotos called with groupId: " + groupId + ", locationType: " + locationType);
        Query query = db.collection(COLLECTION_PHOTOS);

        try {
            if (groupId != null && !groupId.equals("all")) {
                query = query.whereEqualTo("groupId", groupId);
            } else {
                query = query.whereEqualTo("isPublic", true);
            }

            if (locationType != null && !locationType.equals("all")) {
                query = query.whereEqualTo("locationType", locationType);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error building query: " + e.getMessage());
            listener.onError(e);
            return;
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Photo> photos = new ArrayList<>();
                if (task.getResult() != null) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        Photo p = mapDocToPhoto(doc);
                        if (p != null) photos.add(p);
                    }
                }
                
                try {
                    photos.sort((p1, p2) -> {
                        String c1 = p1.getCreatedAt();
                        String c2 = p2.getCreatedAt();
                        if (c1 == null && c2 == null) return 0;
                        if (c1 == null) return 1;
                        if (c2 == null) return -1;
                        return c2.compareTo(c1);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error sorting photos: " + e.getMessage());
                }
                
                Log.d(TAG, "Successfully loaded " + photos.size() + " photos");
                listener.onSuccess(photos);
            } else {
                Log.e(TAG, "Error getting photos: ", task.getException());
                listener.onError(task.getException());
            }
        });
    }

    public void getPhotosByAuthor(String authorId, OnDataLoadedListener<List<Photo>> listener) {
        Log.d(TAG, "getPhotosByAuthor called for: " + authorId);
        db.collection(COLLECTION_PHOTOS)
                .whereEqualTo("author.id", authorId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Photo> photos = new ArrayList<>();
                        if (task.getResult() != null) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                Photo p = mapDocToPhoto(doc);
                                if (p != null) photos.add(p);
                            }
                        }
                        
                        try {
                            photos.sort((p1, p2) -> {
                                String c1 = p1.getCreatedAt();
                                String c2 = p2.getCreatedAt();
                                if (c1 == null && c2 == null) return 0;
                                if (c1 == null) return 1;
                                if (c2 == null) return -1;
                                return c2.compareTo(c1);
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error sorting author photos: " + e.getMessage());
                        }

                        listener.onSuccess(photos);
                    } else {
                        Log.e(TAG, "Error getting photos by author: ", task.getException());
                        listener.onError(task.getException());
                    }
                });
    }

    public void getUser(String userId, OnDataLoadedListener<Author> listener) {
        if (userId == null) {
            listener.onError(new Exception("User ID is null"));
            return;
        }
        db.collection(COLLECTION_USERS).document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot doc = task.getResult();
                try {
                    Author author = new Author(
                            doc.getId(),
                            doc.getString("name") != null ? doc.getString("name") : "Utilisateur Inconnu",
                            doc.getString("avatar") != null ? doc.getString("avatar") : ""
                    );
                    listener.onSuccess(author);
                } catch (Exception e) {
                    Log.e(TAG, "Error mapping user: " + e.getMessage());
                    listener.onError(e);
                }
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public void createUser(Author author, OnDataLoadedListener<Void> listener) {
        if (author == null || author.getId() == null) {
            listener.onError(new Exception("Invalid author data"));
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("name", author.getName() != null ? author.getName() : "Voyageur");
        map.put("avatar", author.getAvatar() != null ? author.getAvatar() : "");
        
        db.collection(COLLECTION_USERS).document(author.getId())
                .set(map)
                .addOnSuccessListener(v -> listener.onSuccess(null))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user: " + e.getMessage());
                    listener.onError(e);
                });
    }

    public void getPhoto(String photoId, OnDataLoadedListener<Photo> listener) {
        if (photoId == null) {
            listener.onError(new Exception("Photo ID is null"));
            return;
        }
        db.collection(COLLECTION_PHOTOS).document(photoId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Photo p = mapDocToPhoto(task.getResult());
                if (p != null) listener.onSuccess(p);
                else listener.onError(new Exception("Failed to map photo"));
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public void likePhoto(String photoId, boolean like, OnDataLoadedListener<Void> listener) {
        if (photoId == null) return;
        db.collection(COLLECTION_PHOTOS).document(photoId)
                .update("likes", com.google.firebase.firestore.FieldValue.increment(like ? 1 : -1))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) listener.onSuccess(null);
                    else {
                        Log.e(TAG, "Error liking photo: " + task.getException());
                        listener.onError(task.getException());
                    }
                });
    }

    public void addComment(String photoId, String comment, OnDataLoadedListener<Void> listener) {
        if (photoId == null || comment == null) return;
        db.collection(COLLECTION_PHOTOS).document(photoId)
                .update("comments", com.google.firebase.firestore.FieldValue.arrayUnion(comment))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) listener.onSuccess(null);
                    else {
                        Log.e(TAG, "Error adding comment: " + task.getException());
                        listener.onError(task.getException());
                    }
                });
    }

    public void addPhoto(Photo photo, OnDataLoadedListener<Void> listener) {
        if (photo == null || photo.getId() == null) {
            listener.onError(new Exception("Invalid photo object"));
            return;
        }
        try {
            Map<String, Object> data = mapPhotoToMap(photo);
            db.collection(COLLECTION_PHOTOS).document(photo.getId()).set(data)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Photo published successfully: " + photo.getId());
                        listener.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error publishing photo: " + e.getMessage());
                        listener.onError(e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception in addPhoto: " + e.getMessage());
            listener.onError(e);
        }
    }


    public void getAllGroups(OnDataLoadedListener<List<Group>> listener) {
        db.collection(COLLECTION_GROUPS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Group> groups = new ArrayList<>();
                if (task.getResult() != null) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        Group g = mapDocToGroup(doc);
                        if (g != null) groups.add(g);
                    }
                }
                listener.onSuccess(groups);
            } else {
                Log.e(TAG, "Error getting groups: ", task.getException());
                listener.onError(task.getException());
            }
        });
    }

    public void joinGroup(String groupId, OnDataLoadedListener<Void> listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || groupId == null) return;

        db.collection(COLLECTION_GROUPS).document(groupId)
                .update("memberIds", FieldValue.arrayUnion(user.getUid()))
                .addOnSuccessListener(v -> listener.onSuccess(null))
                .addOnFailureListener(listener::onError);
    }

    public void leaveGroup(String groupId, OnDataLoadedListener<Void> listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || groupId == null) return;

        db.collection(COLLECTION_GROUPS).document(groupId)
                .update("memberIds", FieldValue.arrayRemove(user.getUid()))
                .addOnSuccessListener(v -> listener.onSuccess(null))
                .addOnFailureListener(listener::onError);
    }

    public void addGroup(Group group, OnDataLoadedListener<Void> listener) {
        if (group == null || group.getId() == null) return;
        Map<String, Object> map = new HashMap<>();
        map.put("name", group.getName() != null ? group.getName() : "Nouveau Groupe");
        map.put("description", group.getDescription() != null ? group.getDescription() : "");
        map.put("imageUrl", group.getImageUrl() != null ? group.getImageUrl() : "");
        map.put("memberIds", group.getMemberIds() != null ? group.getMemberIds() : new ArrayList<>());

        db.collection(COLLECTION_GROUPS).document(group.getId())
                .set(map)
                .addOnSuccessListener(v -> listener.onSuccess(null))
                .addOnFailureListener(listener::onError);
    }


    @SuppressWarnings("unchecked")
    private Photo mapDocToPhoto(DocumentSnapshot doc) {
        try {
            Map<String, Object> locMap = (Map<String, Object>) doc.get("location");
            PhotoLocation loc;
            if (locMap != null) {
                loc = new PhotoLocation(
                        (String) locMap.getOrDefault("name", "Lieu inconnu"),
                        locMap.get("lat") != null ? ((Number) locMap.get("lat")).doubleValue() : 0.0,
                        locMap.get("lng") != null ? ((Number) locMap.get("lng")).doubleValue() : 0.0,
                        locMap.get("approximate") != null ? (Boolean) locMap.get("approximate") : false
                );
            } else {
                loc = new PhotoLocation("Lieu inconnu", 0.0, 0.0, false);
            }

            Map<String, Object> authorMap = (Map<String, Object>) doc.get("author");
            Author author;
            if (authorMap != null) {
                author = new Author(
                        (String) authorMap.getOrDefault("id", "unknown"),
                        (String) authorMap.getOrDefault("name", "Voyageur"),
                        (String) authorMap.getOrDefault("avatar", "")
                );
            } else {
                author = new Author("unknown", "Voyageur", "");
            }

            List<String> comments = (List<String>) doc.get("comments");
            if (comments == null) comments = new ArrayList<>();

            List<String> tags = (List<String>) doc.get("tags");
            if (tags == null) tags = new ArrayList<>();

            return new Photo(
                    doc.getId(),
                    doc.getString("imageUrl") != null ? doc.getString("imageUrl") : "",
                    doc.getString("title") != null ? doc.getString("title") : "Sans titre",
                    doc.getString("description") != null ? doc.getString("description") : "",
                    loc,
                    doc.getString("date") != null ? doc.getString("date") : "",
                    doc.getString("period") != null ? doc.getString("period") : "Matin",
                    comments,
                    doc.getString("directions") != null ? doc.getString("directions") : "",
                    author,
                    tags,
                    doc.getString("locationType") != null ? doc.getString("locationType") : "other",
                    doc.get("likes") != null ? ((Number) doc.get("likes")).intValue() : 0,
                    false,
                    doc.get("isPublic") != null ? doc.getBoolean("isPublic") : true,
                    doc.getString("createdAt") != null ? doc.getString("createdAt") : "",
                    doc.getString("groupId") != null ? doc.getString("groupId") : "all"
            );
        } catch (Exception e) {
            Log.e(TAG, "Error mapping document " + doc.getId() + " to Photo: " + e.getMessage());
            return null; // On ignore ce document malformé
        }
    }

    private Map<String, Object> mapPhotoToMap(Photo p) {
        Map<String, Object> map = new HashMap<>();
        map.put("imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "");
        map.put("title", p.getTitle() != null ? p.getTitle() : "");
        map.put("description", p.getDescription() != null ? p.getDescription() : "");
        map.put("date", p.getDate() != null ? p.getDate() : "");
        map.put("period", p.getPeriod() != null ? p.getPeriod() : "");
        map.put("comments", p.getComments() != null ? p.getComments() : new ArrayList<>());
        map.put("tags", p.getTags() != null ? p.getTags() : new ArrayList<>());
        map.put("directions", p.getDirections() != null ? p.getDirections() : "");
        map.put("locationType", p.getLocationType() != null ? p.getLocationType() : "other");
        map.put("likes", p.getLikes());
        map.put("isPublic", p.isPublic());
        map.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt() : String.valueOf(System.currentTimeMillis()));
        map.put("groupId", p.getGroupId() != null ? p.getGroupId() : "all");

        Map<String, Object> loc = new HashMap<>();
        if (p.getLocation() != null) {
            loc.put("name", p.getLocation().getName() != null ? p.getLocation().getName() : "Lieu inconnu");
            loc.put("lat", p.getLocation().getLat());
            loc.put("lng", p.getLocation().getLng());
            loc.put("approximate", p.getLocation().isApproximate());
        } else {
            loc.put("name", "Lieu inconnu");
            loc.put("lat", 0.0);
            loc.put("lng", 0.0);
            loc.put("approximate", false);
        }
        map.put("location", loc);

        Map<String, Object> author = new HashMap<>();
        if (p.getAuthor() != null) {
            author.put("id", p.getAuthor().getId() != null ? p.getAuthor().getId() : "unknown");
            author.put("name", p.getAuthor().getName() != null ? p.getAuthor().getName() : "Voyageur");
            author.put("avatar", p.getAuthor().getAvatar() != null ? p.getAuthor().getAvatar() : "");
        } else {
            author.put("id", "unknown");
            author.put("name", "Voyageur");
            author.put("avatar", "");
        }
        map.put("author", author);

        return map;
    }

    @SuppressWarnings("unchecked")
    private Group mapDocToGroup(DocumentSnapshot doc) {
        try {
            Group g = new Group(
                    doc.getId(),
                    doc.getString("name") != null ? doc.getString("name") : "Groupe sans nom",
                    doc.getString("description") != null ? doc.getString("description") : "",
                    doc.getString("imageUrl") != null ? doc.getString("imageUrl") : ""
            );
            List<String> members = (List<String>) doc.get("memberIds");
            if (members != null) {
                for (String m : members) if (m != null) g.getMemberIds().add(m);
            }
            return g;
        } catch (Exception e) {
            Log.e(TAG, "Error mapping group " + doc.getId() + ": " + e.getMessage());
            return null;
        }
    }

    public interface OnDataLoadedListener<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }
}
