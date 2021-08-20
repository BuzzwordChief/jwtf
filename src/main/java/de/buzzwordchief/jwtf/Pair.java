package de.buzzwordchief.jwtf;

import com.google.gson.annotations.SerializedName;

public record Pair<T, E>(@SerializedName("abbr") T left, @SerializedName("full") E right) {
    @Override
    public String toString() {
        return "Pair{" +
               "left=" + left +
               ", right=" + right +
               '}';
    }
}
