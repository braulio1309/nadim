package com.mycompany.nadim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final Path PROFILES_DIR = Paths.get("profiles");
    private static UserProfile current = null;

    public static class UserProfile {
        public final String userName;
        public final String displayName;
        public final String gender;

        public UserProfile(String userName, String displayName, String gender) {
            this.userName = userName;
            this.displayName = displayName;
            this.gender = gender;
        }
    }

    public static void ensureProfilesDir() {
        try {
            Files.createDirectories(PROFILES_DIR);
        } catch (IOException ignored) {
        }
    }

    public static List<UserProfile> listProfiles() {
        ensureProfilesDir();
        List<UserProfile> out = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(PROFILES_DIR, "*.json")) {
            for (Path p : ds) {
                UserProfile up = readProfileFile(p);
                if (up != null) {
                    out.add(up);
                }
            }
        } catch (IOException ignored) {
        }
        return out;
    }

    private static UserProfile readProfileFile(Path p) {
        try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line.trim());
            }
            String s = sb.toString();
            if (s.isEmpty())
                return null;
            // ultra simple parse for keys we need
            String user = extractString(s, "userName");
            String display = extractString(s, "displayName");
            String gender = extractString(s, "gender");
            if (user == null)
                return null;
            if (display == null)
                display = user;
            if (gender == null)
                gender = "Chico";
            return new UserProfile(user, display, gender);
        } catch (IOException e) {
            return null;
        }
    }

    private static String extractString(String json, String key) {
        int k = json.indexOf('"' + key + '"');
        if (k < 0)
            return null;
        int colon = json.indexOf(':', k);
        if (colon < 0)
            return null;
        int q1 = json.indexOf('"', colon);
        if (q1 < 0)
            return null;
        int q2 = json.indexOf('"', q1 + 1);
        if (q2 < 0)
            return null;
        return json.substring(q1 + 1, q2);
    }

    public static boolean saveProfile(UserProfile p) {
        ensureProfilesDir();
        Path out = PROFILES_DIR.resolve(p.userName + ".json");
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"userName\":\"").append(escape(p.userName)).append("\",");
        sb.append("\"displayName\":\"").append(escape(p.displayName)).append("\",");
        sb.append("\"gender\":\"").append(escape(p.gender)).append("\"");
        sb.append('}');
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write(sb.toString());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static boolean createProfile(String userName, String displayName, String gender) {
        if (userName == null || userName.isBlank())
            return false;
        // sanitize
        String cleaned = userName.replaceAll("[^a-zA-Z0-9_-]", "_");
        UserProfile up = new UserProfile(cleaned, displayName == null || displayName.isBlank() ? cleaned : displayName,
                gender == null ? "Chico" : gender);
        if (!saveProfile(up))
            return false;
        setCurrentUser(up.userName);
        return true;
    }

    public static void setCurrentUser(String userName) {
        if (userName == null)
            return;
        UserProfile found = null;
        for (UserProfile p : listProfiles()) {
            if (p.userName.equalsIgnoreCase(userName)) {
                found = p;
                break;
            }
        }
        if (found == null) {
            // attempt to read direct file
            Path p = PROFILES_DIR.resolve(userName + ".json");
            if (Files.exists(p)) {
                found = readProfileFile(p);
            }
        }
        current = found;
    }

    public static UserProfile getCurrentUser() {
        return current;
    }

}
