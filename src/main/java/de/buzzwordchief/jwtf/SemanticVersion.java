package de.buzzwordchief.jwtf;

public record SemanticVersion(int major, int minor, int patch) {
    @Override
    public String toString() {
        return String.join(".", String.valueOf(major), String.valueOf(minor), String.valueOf(patch));
    }
}
