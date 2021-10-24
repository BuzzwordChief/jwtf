package de.buzzwordchief.jwtf;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AcronymService {
    private static final Path DATA_PATH            = Paths.get("data.json");
    private static final URI  ACRONYM_DATABASE_URI = URI.create("http://www.mirbsd.org/acronyms");

    private final HttpClient  client  = HttpClient.newHttpClient();
    private final HttpRequest request = HttpRequest.newBuilder(ACRONYM_DATABASE_URI).build();

    private List<Pair<String, String>> data = new ArrayList<>();


    public AcronymService() {
        if (Files.exists(DATA_PATH)) {
            loadData();
        } else {
            updateData();
        }
    }

    private static double diceCoefficient(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        Set<String> nx = new HashSet<>();
        Set<String> ny = new HashSet<>();

        getDiceCoefficientTupleSet(s1, nx);
        getDiceCoefficientTupleSet(s2, ny);

        Set<String> intersection = new HashSet<>(nx);
        intersection.retainAll(ny);
        double totcombigrams = intersection.size();

        return (2 * totcombigrams) / (nx.size() + ny.size());
    }

    private static void getDiceCoefficientTupleSet(String s1, Set<String> nx) {
        for (int i = 0; i < s1.length() - 1; i++) {
            char   x1  = s1.charAt(i);
            char   x2  = s1.charAt(i + 1);
            String tmp = "" + x1 + x2;
            nx.add(tmp);
        }
    }

    void updateData() {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                setData(Arrays.stream(response.body().split("\n"))
                              .skip(13)
                              .parallel()
                              .map(s -> s.split("\t"))
                              .filter(a -> a.length == 2)
                              .map(a -> new Pair<>(a[0], a[1]))
                              .collect(Collectors.toList()));
                storeData();
            } else {
                throw new IOException();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Failed to update database");
                alert.setContentText("""
                                             While updating the acronym database an error occurred.
                                             Please check your internet connection.""");
                alert.show();
            });
        }
    }

    synchronized List<String> searchFor(String searchTerm) {
        return data.stream()
                   .parallel()
                   .map(p -> new Pair<>(diceCoefficient(p.left(), searchTerm), p.right()))
                   .filter(p -> p.left() > 0.9)
                   .sorted(Comparator.comparing(Pair::left))
                   .map(Pair::right)
                   .toList();
    }

    private synchronized void setData(List<Pair<String, String>> data) {
        this.data = data;
    }

    private void loadData() {
        try {
            String        rawData = Files.readString(DATA_PATH);
            AtomicBoolean error   = new AtomicBoolean(false);
            class PairDeserializer implements JsonDeserializer<Pair<?, ?>> {
                @Override
                public Pair<?, ?> deserialize(JsonElement json, Type typeOfT,
                                              JsonDeserializationContext context) throws JsonParseException {
                    JsonObject ob = json.getAsJsonObject();
                    try {
                        String leftSerialName = Pair.class.getDeclaredField("left").getAnnotation(SerializedName.class)
                                                          .value();
                        String rightSerialName = Pair.class.getDeclaredField("right")
                                                           .getAnnotation(SerializedName.class)
                                                           .value();
                        return new Pair<>(ob.get(leftSerialName).getAsString(), ob.get(rightSerialName).getAsString());
                    } catch (NoSuchFieldException e) {
                        error.set(true);
                    }
                    return null;
                }
            }

            Gson gson = new GsonBuilder().registerTypeAdapter(Pair.class, new PairDeserializer()).create();
            List<Pair<String, String>> data =
                    gson.fromJson(rawData, TypeToken.getParameterized(List.class, Pair.class).getType());

            if (error.get() || data.stream().anyMatch((p) -> p.left() == null || p.right() == null)) {
                throw new IOException();
            }

            setData(data);
        } catch (IOException | JsonSyntaxException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Failed to load local data");
                alert.setContentText("""
                                             While loading the local data an error occurred.
                                             We will try to update via internet.""");
                alert.show();
            });
            updateData();
        }
    }

    private synchronized void storeData() {
        String jsonString = new Gson().toJson(data);

        try {
            Files.writeString(DATA_PATH, jsonString);
        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText(
                        """
                                Could not save acronym database to disk.
                                                                
                                Make sure the folder in which the executable is can be written to.
                                You can still use the program without being able to write to disk""");
                alert.show();
            });
        }
    }
}
