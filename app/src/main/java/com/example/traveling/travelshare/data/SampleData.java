package com.example.traveling.travelshare.data;


import com.example.traveling.travelshare.model.Author;
import com.example.traveling.travelshare.model.Photo;
import com.example.traveling.travelshare.model.PhotoLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SampleData {

    public static List<Photo> getAllPhotos() {
        List<Photo> photos = new ArrayList<>();

        Author marie   = new Author("user_1", "Marie Dubois",   "https://api.dicebear.com/7.x/avataaars/svg?seed=Marie");
        Author thomas  = new Author("user_2", "Thomas Martin",  "https://api.dicebear.com/7.x/avataaars/svg?seed=Thomas");
        Author sophie  = new Author("user_3", "Sophie Laurent", "https://api.dicebear.com/7.x/avataaars/svg?seed=Sophie");
        Author pierre  = new Author("user_4", "Pierre Rousseau","https://api.dicebear.com/7.x/avataaars/svg?seed=Pierre");
        Author julien  = new Author("user_5", "Julien Moreau",  "https://api.dicebear.com/7.x/avataaars/svg?seed=Julien");
        Author emma    = new Author("user_6", "Emma Bernard",   "https://api.dicebear.com/7.x/avataaars/svg?seed=Emma");

        photos.add(new Photo(
                "photo_1",
                "https://images.unsplash.com/photo-1431274172761-fca41d930114?w=800",
                "Tour Eiffel au coucher du soleil",
                "Vue magnifique de la Tour Eiffel illuminée pendant le coucher du soleil depuis le Trocadéro",
                new PhotoLocation("Tour Eiffel, Paris", 48.8584, 2.2945, false),
                "2024-06-15", "Été 2024",
                Arrays.asList("Quelle vue incroyable !", "J'y étais la semaine dernière"),
                "Métro ligne 6 - Station Trocadéro, puis 5 minutes à pied",
                marie,
                Arrays.asList("tour eiffel", "paris", "coucher de soleil", "romantique"),
                "monument", 234, false, true, "2024-06-15T18:30:00Z"
        ));

        photos.add(new Photo(
                "photo_2",
                "https://images.unsplash.com/photo-1696079196661-a5cbfb884255?w=800",
                "Jardin du Luxembourg en fleurs",
                "Les magnifiques parterres de fleurs du Jardin du Luxembourg au printemps",
                new PhotoLocation("Jardin du Luxembourg, Paris", 48.8462, 2.3372, false),
                "2024-04-20", "Printemps 2024",
                Arrays.asList("Les couleurs sont superbes", "Mon parc préféré à Paris"),
                "RER B - Station Luxembourg",
                thomas,
                Arrays.asList("jardin", "nature", "fleurs", "printemps"),
                "nature", 189, true, true, "2024-04-20T14:00:00Z"
        ));

        photos.add(new Photo(
                "photo_3",
                "https://images.unsplash.com/photo-1729722189474-2c021a2cf80c?w=800",
                "Musée du Louvre - Pyramide",
                "La célèbre pyramide de verre du Louvre avec reflets",
                new PhotoLocation("Musée du Louvre, Paris", 48.8606, 2.3376, false),
                "2024-05-10", "Printemps 2024",
                Arrays.asList("Architecture moderne et classique", "Moins de monde que d'habitude"),
                "Métro ligne 1 - Station Palais Royal - Musée du Louvre",
                sophie,
                Arrays.asList("louvre", "musée", "architecture", "pyramide"),
                "monument", 456, false, true, "2024-05-10T11:30:00Z"
        ));

        photos.add(new Photo(
                "photo_4",
                "https://images.unsplash.com/photo-1762922425226-8cfe6987e7b0?w=800",
                "Café parisien typique",
                "Terrasse d'un café traditionnel dans le Marais",
                new PhotoLocation("Le Marais, Paris", 48.8566, 2.3622, true),
                "2024-07-01", "Été 2024",
                Arrays.asList("Ambiance authentique", "Excellent café"),
                "Métro ligne 1 - Station Saint-Paul",
                pierre,
                Arrays.asList("café", "marais", "terrasse", "culture"),
                "restaurant", 167, true, true, "2024-07-01T09:00:00Z"
        ));

        photos.add(new Photo(
                "photo_5",
                "https://images.unsplash.com/photo-1759299983432-e0097fad9b15?w=800",
                "Boutiques des Champs-Élysées",
                "Avenue des Champs-Élysées illuminée le soir",
                new PhotoLocation("Champs-Élysées, Paris", 48.8698, 2.3078, false),
                "2024-12-20", "Hiver 2024",
                Arrays.asList("Décorations de Noël magnifiques", "Shopping de luxe"),
                "Métro ligne 1 - Station George V",
                marie,
                Arrays.asList("shopping", "luxe", "champs-élysées", "nuit"),
                "shop", 301, false, true, "2024-12-20T19:00:00Z"
        ));

        photos.add(new Photo(
                "photo_6",
                "https://images.unsplash.com/photo-1762698860407-13383c010b13?w=800",
                "Sacré-Cœur depuis Montmartre",
                "Vue panoramique sur Paris depuis les marches du Sacré-Cœur",
                new PhotoLocation("Sacré-Cœur, Montmartre", 48.8867, 2.3431, false),
                "2024-08-15", "Été 2024",
                Arrays.asList("Vue à 360° sur Paris", "Montée sportive mais ça vaut le coup"),
                "Métro ligne 12 - Station Abbesses, puis funiculaire",
                julien,
                Arrays.asList("sacré-coeur", "montmartre", "panorama", "église"),
                "monument", 521, true, true, "2024-08-15T17:00:00Z"
        ));

        photos.add(new Photo(
                "photo_7",
                "https://images.unsplash.com/photo-1721596461283-0adb7e00fc60?w=800",
                "Rue pavée de Montmartre",
                "Rue typique de Montmartre avec ses pavés et ses façades colorées",
                new PhotoLocation("Montmartre, Paris", 48.8867, 2.3400, true),
                "2024-09-10", "Automne 2024",
                Arrays.asList("Charme parisien authentique", "Parfait pour une balade"),
                "Métro ligne 12 - Station Abbesses",
                emma,
                Arrays.asList("montmartre", "rue", "authenticité", "charme"),
                "street", 278, false, true, "2024-09-10T10:30:00Z"
        ));

        photos.add(new Photo(
                "photo_8",
                "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800",
                "Notre-Dame avant restauration",
                "Photo de Notre-Dame de Paris avant l'incendie, vue depuis la Seine",
                new PhotoLocation("Notre-Dame de Paris", 48.8530, 2.3499, false),
                "2019-03-15", "Printemps 2019",
                Arrays.asList("Souvenir précieux", "Hâte de la voir restaurée"),
                "Métro ligne 4 - Station Cité",
                thomas,
                Arrays.asList("notre-dame", "histoire", "architecture", "seine"),
                "monument", 892, true, true, "2019-03-15T15:00:00Z"
        ));

        return photos;
    }

    public static List<Photo> filterPhotos(List<Photo> photos,
                                                 String query,
                                                 String locationType,
                                                 boolean random) {
        List<Photo> result = new ArrayList<>(photos);

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