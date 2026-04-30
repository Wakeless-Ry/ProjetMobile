package com.example.traveling.travelshare.data;


import com.example.traveling.travelshare.model.Author;
import com.example.traveling.travelshare.model.Group;
import com.example.traveling.travelshare.model.Photo;
import com.example.traveling.travelshare.model.PhotoLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SampleData {

    private static List<Photo> cachedPhotos = null;
    private static List<Group> cachedGroups = null;
    private static List<String> joinedGroupIds = new ArrayList<>();

    public static List<Group> getJoinedGroups() {
        List<Group> joined = new ArrayList<>();
        if (cachedGroups == null) getGroups();
        for (Group g : cachedGroups) {
            if (joinedGroupIds.contains(g.getId())) joined.add(g);
        }
        return joined;
    }

    public static List<Group> getDiscoverGroups() {
        List<Group> discover = new ArrayList<>();
        if (cachedGroups == null) getGroups();
        for (Group g : cachedGroups) {
            if (!joinedGroupIds.contains(g.getId())) discover.add(g);
        }
        return discover;
    }

    public static void joinGroup(String groupId) {
        if (!joinedGroupIds.contains(groupId)) {
            joinedGroupIds.add(groupId);
        }
    }

    public static void leaveGroup(String groupId) {
        joinedGroupIds.remove(groupId);
    }

    public static boolean isJoined(String groupId) {
        return joinedGroupIds.contains(groupId);
    }

    public static List<Group> getGroups() {
        if (cachedGroups != null) return cachedGroups;

        cachedGroups = new ArrayList<>();
        cachedGroups.add(new Group("g1", "Randonneurs du Pic St-Loup", "Pour ceux qui aiment l'Hérault", "https://images.unsplash.com/photo-1551632811-561732d1e306?w=400"));
        cachedGroups.add(new Group("g2", "Montpellier Photo Club", "Capturer la surdouée", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=400"));
        cachedGroups.add(new Group("g3", "Foodies du Clapas", "Les meilleures tables de Montpellier", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400"));
        
        return cachedGroups;
    }

    public static void addGroup(Group group) {
        if (cachedGroups == null) {
            getGroups();
        }
        cachedGroups.add(0, group);
    }

    public static List<Photo> getAllPhotos() {
        if (cachedPhotos != null) return cachedPhotos;

        List<Photo> photos = new ArrayList<>();

        Author marie   = new Author("user_1", "Marie Dubois",   "https://api.dicebear.com/7.x/avataaars/svg?seed=Marie");
        Author thomas  = new Author("user_2", "Thomas Martin",  "https://api.dicebear.com/7.x/avataaars/svg?seed=Thomas");
        Author sophie  = new Author("user_3", "Sophie Laurent", "https://api.dicebear.com/7.x/avataaars/svg?seed=Sophie");
        Author pierre  = new Author("user_4", "Pierre Rousseau","https://api.dicebear.com/7.x/avataaars/svg?seed=Pierre");
        Author julien  = new Author("user_5", "Julien Moreau",  "https://api.dicebear.com/7.x/avataaars/svg?seed=Julien");
        Author emma    = new Author("user_6", "Emma Bernard",   "https://api.dicebear.com/7.x/avataaars/svg?seed=Emma");

        photos.add(new Photo(
                "photo_1",
                "https://images.unsplash.com/photo-1563905317-d736775aa713?w=800",
                "Place de la Comédie au crépuscule",
                "L'Opéra Comédie illuminé sous un ciel rose magnifique.",
                new PhotoLocation("Place de la Comédie, Montpellier", 43.6085, 3.8794, false),
                "2024-06-15", "Été 2024",
                Arrays.asList("L'endroit emblématique !", "J'y bois mon café tous les matins"),
                "Tram ligne 1 ou 2 - Arrêt Comédie",
                marie,
                Arrays.asList("comedie", "montpellier", "centre-ville", "opera"),
                "monument", 234, false, true, "2024-06-15T18:30:00Z"
        ));

        photos.add(new Photo(
                "photo_2",
                "https://images.unsplash.com/photo-1627918803730-8025e1730d1d?w=800",
                "Balade au Peyrou",
                "Vue sur l'aqueduc des Arceaux depuis la promenade du Peyrou.",
                new PhotoLocation("Promenade du Peyrou, Montpellier", 43.6111, 3.8703, false),
                "2024-04-20", "Printemps 2024",
                Arrays.asList("Le plus beau coucher de soleil"),
                "Tram ligne 4 - Arrêt Peyrou - Arc de Triomphe",
                thomas,
                Arrays.asList("peyrou", "nature", "panorama", "histoire"),
                "nature", 189, true, true, "2024-04-20T14:00:00Z"
        ));

        photos.add(new Photo(
                "photo_3",
                "https://images.unsplash.com/photo-1516738901171-8eb4fc13bd20?w=800",
                "Quartier Antigone - Architecture",
                "Les courbes néoclassiques du quartier Ricardo Bofill.",
                new PhotoLocation("Antigone, Montpellier", 43.6078, 3.8895, false),
                "2024-05-10", "Printemps 2024",
                Arrays.asList("On se croirait en Grèce !"),
                "Tram ligne 1 - Arrêt Antigone ou Place de l'Europe",
                sophie,
                Arrays.asList("antigone", "architecture", "bofill"),
                "monument", 456, false, true, "2024-05-10T11:30:00Z"
        ));

        photos.add(new Photo(
                "photo_4",
                "https://images.unsplash.com/photo-1549413289-53e7784f1f50?w=800",
                "Terrasses St-Roch",
                "Petite ruelle pleine de charme près de l'église Saint-Roch.",
                new PhotoLocation("Quartier Saint-Roch, Montpellier", 43.6074, 3.8767, true),
                "2024-07-01", "Été 2024",
                Arrays.asList("Ambiance authentique", "Les restos sont top ici"),
                "Centre piétonnier",
                pierre,
                Arrays.asList("saint-roch", "ruelles", "terrasse", "culture"),
                "restaurant", 167, true, true, "2024-07-01T09:00:00Z"
        ));

        photos.add(new Photo(
                "photo_5",
                "https://images.unsplash.com/photo-1518173946687-a4c8892bbd9f?w=800",
                "Zoo de Lunaret",
                "Un flamant rose au milieu de la végétation luxuriante.",
                new PhotoLocation("Zoo de Montpellier", 43.6395, 3.8736, false),
                "2024-06-20", "Été 2024",
                Arrays.asList("Sortie parfaite en famille", "Gratuit pour les Montpelliérains !"),
                "Bus ligne 13 - Arrêt Zoo",
                marie,
                Arrays.asList("zoo", "nature", "lunaret", "animaux"),
                "nature", 301, false, true, "2024-06-20T11:00:00Z"
        ));

        photos.add(new Photo(
                "photo_6",
                "https://images.unsplash.com/photo-1627918803730-8025e1730d1d?w=800",
                "Arc de Triomphe du Peyrou",
                "La majestueuse porte d'entrée de la ville ancienne.",
                new PhotoLocation("Arc de Triomphe, Montpellier", 43.6110, 3.8715, false),
                "2024-08-15", "Été 2024",
                Arrays.asList("Un monument historique magnifique", "La vue depuis le sommet est dingue"),
                "Tram ligne 4 - Arrêt Peyrou",
                julien,
                Arrays.asList("peyrou", "montpellier", "histoire", "monument"),
                "monument", 521, true, true, "2024-08-15T17:00:00Z"
        ));

        photos.add(new Photo(
                "photo_7",
                "https://images.unsplash.com/photo-1549413289-53e7784f1f50?w=800",
                "Faculté de Médecine",
                "La plus ancienne école de médecine du monde occidental encore en activité.",
                new PhotoLocation("Faculté de Médecine, Montpellier", 43.6128, 3.8732, false),
                "2024-09-10", "Automne 2024",
                Arrays.asList("Quelle architecture !", "Un lieu chargé d'histoire"),
                "À côté de la Cathédrale St-Pierre",
                emma,
                Arrays.asList("medecine", "histoire", "patrimoine"),
                "monument", 278, false, true, "2024-09-10T10:30:00Z"
        ));

        photos.add(new Photo(
                "photo_8",
                "https://images.unsplash.com/photo-1563905317-d736775aa713?w=800",
                "Rives du Lez",
                "Le nouveau Montpellier avec l'Arbre Blanc en arrière-plan.",
                new PhotoLocation("Rives du Lez, Montpellier", 43.6030, 3.8965, false),
                "2024-03-15", "Printemps 2024",
                Arrays.asList("Génial pour courir", "L'architecture de l'Arbre Blanc est folle"),
                "Tram ligne 1 ou 3 - Arrêt Port Marianne",
                thomas,
                Arrays.asList("lez", "architecture", "moderne", "rivière"),
                "nature", 892, true, true, "2024-03-15T15:00:00Z"
        ));

        // Photos liées à des groupes
        photos.add(new Photo(
                "photo_g1",
                "https://images.unsplash.com/photo-1551632811-561732d1e306?w=800",
                "Rando dans les Alpes",
                "Une superbe journée de marche au grand air.",
                new PhotoLocation("Chamonix", 45.9237, 6.8694, false),
                "2024-07-20", "Été 2024",
                new ArrayList<>(), "Se garer au parking des Bossons", emma,
                Arrays.asList("rando", "montagne", "nature"), "nature", 45, false, false, "2024-07-20T10:00:00Z", "g1"
        ));

        photos.add(new Photo(
                "photo_g2",
                "https://images.unsplash.com/photo-1508833319223-f8004d4ba3b6?w=800",
                "Paris By Night",
                "Essai de pose longue sur les quais.",
                new PhotoLocation("Pont Neuf, Paris", 48.8580, 2.3414, false),
                "2024-08-05", "Été 2024",
                Arrays.asList("Beau travail sur la lumière"), "Prendre le métro Pont Neuf", thomas,
                Arrays.asList("paris", "nuit", "photo"), "street", 112, false, false, "2024-08-05T22:00:00Z", "g2"
        ));

        cachedPhotos = photos;
        return photos;
    }

    public static void addPhoto(Photo photo) {
        if (cachedPhotos == null) {
            getAllPhotos();
        }
        cachedPhotos.add(0, photo); // Ajouter au début
    }

    public static List<Photo> filterPhotos(List<Photo> photos,
                                           String query,
                                           String locationType,
                                           String groupId,
                                           boolean random) {
        List<Photo> result = new ArrayList<>();

        for (Photo p : photos) {
            boolean matches = true;

            // Filtre par groupe
            if (groupId != null && !groupId.equals("all")) {
                if (p.getGroupId() == null || !p.getGroupId().equals(groupId)) {
                    matches = false;
                }
            } else if (p.getGroupId() != null) {
                // Par défaut (si groupId == all), on ne montre que les photos publiques (pas de groupe)
                matches = false;
            }

            if (matches) result.add(p);
        }

        // Appliquer les autres filtres sur le résultat partiel
        if (query != null && !query.isEmpty()) {
            String q = query.toLowerCase();
            List<Photo> filtered = new ArrayList<>();
            for (Photo p : result) {
                if (p.getTitle().toLowerCase().contains(q)
                        || p.getDescription().toLowerCase().contains(q)
                        || p.getLocation().getName().toLowerCase().contains(q)
                        || p.getAuthor().getName().toLowerCase().contains(q)
                        || tagsContain(p.getTags(), q)) {
                    filtered.add(p);
                }
            }
            result = filtered;
        }

        if (locationType != null && !locationType.equals("all")) {
            List<Photo> filtered = new ArrayList<>();
            for (Photo p : result) {
                if (p.getLocationType().equals(locationType)) {
                    filtered.add(p);
                }
            }
            result = filtered;
        }

        if (random) {
            Collections.shuffle(result);
        }

        return result;
    }

    private static boolean tagsContain(List<String> tags, String query) {
        for (String tag : tags) {
            if (tag.toLowerCase().contains(query)) return true;
        }
        return false;
    }
}