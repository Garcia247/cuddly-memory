package com.aiupworkagent.mobile;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class V12Activity extends V111Activity {
    private SecureStore secure;
    private static final String TOKEN_KEY = "cloud_session_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        secure = new SecureStore(this);
    }

    private SecureStore secure() {
        if (secure == null) secure = new SecureStore(this);
        return secure;
    }

    private String token() {
        return secure().get(TOKEN_KEY);
    }

    private boolean signedIn() {
        return !token().isEmpty();
    }

    @Override
    void cloud() {
        super.cloud();
        page.addView(label("CLOUD ACCOUNT"));

        LinearLayout account = card();
        if (signedIn()) {
            TextView h = txt("Signed in", 16, true);
            h.setTextColor(GREEN2);
            account.addView(h);
            TextView e = txt(p.getString("accountEmail", "Cloud account"), 12, false);
            e.setTextColor(MUTED);
            e.setPadding(0, dp(3), 0, dp(10));
            account.addView(e);

            Button sync = primary("Sync Brain & Tracker Now");
            sync.setOnClickListener(v -> syncNow(sync, true));
            account.addView(sync);

            Button refresh = secondary("Pull Latest Cloud Data");
            refresh.setOnClickListener(v -> pullAccount(refresh));
            account.addView(refresh);

            Button logout = secondary("Sign Out");
            logout.setOnClickListener(v -> logout(logout));
            account.addView(logout);
        } else {
            TextView h = txt("Create your private cloud account", 16, true);
            account.addView(h);
            TextView sub = txt("Your session token is encrypted with Android Keystore. Passwords are sent only over HTTPS and stored server-side as scrypt hashes.", 12, false);
            sub.setTextColor(MUTED);
            sub.setPadding(0, dp(4), 0, dp(10));
            account.addView(sub);

            EditText email = input(p.getString("accountEmail", ""));
            EditText password = input("");
            password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            account.addView(wrap("Email", email));
            account.addView(wrap("Password (8+ characters)", password));

            Button create = primary("Create Account & Sync This Phone");
            create.setOnClickListener(v -> register(email, password, create));
            account.addView(create);

            Button login = secondary("Sign In to Existing Account");
            login.setOnClickListener(v -> login(email, password, login));
            account.addView(login);
        }
        page.addView(account);

        LinearLayout privacy = card();
        privacy.setBackground(bg(Color.rgb(244, 247, 255), 16, Color.rgb(213, 222, 244), 1));
        TextView ph = txt("What syncs", 14, true);
        ph.setTextColor(Color.rgb(52, 74, 131));
        privacy.addView(ph);
        TextView pb = txt("Freelancer Brain, application tracker status, opportunity scores and future learning signals. Your account password is never stored on the phone.", 12, false);
        pb.setTextColor(Color.rgb(68, 82, 120));
        pb.setPadding(0, dp(5), 0, 0);
        privacy.addView(pb);
        page.addView(privacy);
    }

    @Override
    void brain() {
        super.brain();
        if (signedIn()) {
            Button sync = secondary("Sync Saved Brain to Cloud");
            sync.setOnClickListener(v -> syncNow(sync, false));
            page.addView(sync);
        }
    }

    private void register(EditText emailField, EditText passwordField, Button button) {
        final String email = emailField.getText().toString().trim();
        final String password = passwordField.getText().toString();
        if (!email.contains("@") || password.length() < 8) {
            Toast.makeText(this, "Use a valid email and a password of at least 8 characters", Toast.LENGTH_LONG).show();
            return;
        }
        button.setEnabled(false);
        button.setText("Creating account…");
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject().put("email", email).put("password", password).put("brain", brainJson());
                JSONObject result = request("POST", API + "/api/auth/register", body, "");
                saveSession(result, email);
                JSONObject syncResult = request("POST", API + "/api/sync/bootstrap", localSyncPayload(), token());
                applyCloudData(syncResult);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Cloud account created and this phone is synced", Toast.LENGTH_LONG).show();
                    cloud();
                });
            } catch (Exception e) {
                showError("Account setup failed", e);
            } finally {
                runOnUiThread(() -> { button.setEnabled(true); button.setText("Create Account & Sync This Phone"); });
            }
        }).start();
    }

    private void login(EditText emailField, EditText passwordField, Button button) {
        final String email = emailField.getText().toString().trim();
        final String password = passwordField.getText().toString();
        if (!email.contains("@") || password.isEmpty()) {
            Toast.makeText(this, "Enter your account email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        button.setEnabled(false);
        button.setText("Signing in…");
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject().put("email", email).put("password", password);
                JSONObject result = request("POST", API + "/api/auth/login", body, "");
                saveSession(result, email);
                applyCloudData(result);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Signed in and cloud data restored", Toast.LENGTH_LONG).show();
                    cloud();
                });
            } catch (Exception e) {
                showError("Sign in failed", e);
            } finally {
                runOnUiThread(() -> { button.setEnabled(true); button.setText("Sign In to Existing Account"); });
            }
        }).start();
    }

    private void logout(Button button) {
        button.setEnabled(false);
        new Thread(() -> {
            try {
                if (signedIn()) request("POST", API + "/api/auth/logout", new JSONObject(), token());
            } catch (Exception ignored) {
            } finally {
                secure().remove(TOKEN_KEY);
                p.edit().remove("accountEmail").apply();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Signed out. Local data remains on this phone.", Toast.LENGTH_LONG).show();
                    cloud();
                });
            }
        }).start();
    }

    private void pullAccount(Button button) {
        button.setEnabled(false);
        button.setText("Pulling cloud data…");
        new Thread(() -> {
            try {
                JSONObject result = request("GET", API + "/api/me", null, token());
                applyCloudData(result);
                runOnUiThread(() -> Toast.makeText(this, "Latest cloud data restored", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                showError("Cloud refresh failed", e);
            } finally {
                runOnUiThread(() -> { button.setEnabled(true); button.setText("Pull Latest Cloud Data"); });
            }
        }).start();
    }

    private void syncNow(Button button, boolean showCloudAfter) {
        if (!signedIn()) {
            Toast.makeText(this, "Sign in first", Toast.LENGTH_SHORT).show();
            return;
        }
        button.setEnabled(false);
        button.setText("Syncing…");
        new Thread(() -> {
            try {
                JSONObject result = request("POST", API + "/api/sync/bootstrap", localSyncPayload(), token());
                applyCloudData(result);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Cloud sync complete", Toast.LENGTH_SHORT).show();
                    if (showCloudAfter) cloud();
                });
            } catch (Exception e) {
                showError("Sync failed", e);
            } finally {
                runOnUiThread(() -> { button.setEnabled(true); button.setText(showCloudAfter ? "Sync Brain & Tracker Now" : "Sync Saved Brain to Cloud"); });
            }
        }).start();
    }

    private void saveSession(JSONObject result, String email) throws Exception {
        String sessionToken = result.optString("token", "");
        if (sessionToken.isEmpty()) throw new Exception("The server did not return a session token");
        secure().put(TOKEN_KEY, sessionToken);
        p.edit().putString("accountEmail", email).apply();
    }

    private JSONObject localSyncPayload() throws Exception {
        JSONObject out = new JSONObject().put("brain", brainJson());
        JSONArray apps = new JSONArray();
        for (String[] x : items()) {
            String identity = x[0] + "|" + x[1] + "|" + x[2];
            String clientRef = UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)).toString();
            apps.put(new JSONObject()
                .put("clientRef", clientRef)
                .put("title", x[0])
                .put("opportunityScore", Integer.parseInt(x[1]))
                .put("budget", Integer.parseInt(x[2]))
                .put("status", x[3]));
        }
        return out.put("applications", apps);
    }

    private void applyCloudData(JSONObject result) throws Exception {
        JSONObject b = result.optJSONObject("brain");
        if (b != null) {
            p.edit()
                .putString("headline", b.optString("headline", p.getString("headline", "")))
                .putString("services", b.optString("services", p.getString("services", "")))
                .putString("evidence", b.optString("evidence", p.getString("evidence", "")))
                .putString("preferred", b.optString("preferredJobs", p.getString("preferred", "")))
                .putString("avoid", b.optString("avoid", p.getString("avoid", "")))
                .putInt("minBudget", b.optInt("minBudget", p.getInt("minBudget", 100)))
                .putInt("hourlyRate", b.optInt("hourlyRate", p.getInt("hourlyRate", 15)))
                .apply();
        }
        JSONArray applications = result.optJSONArray("applications");
        if (applications != null) {
            List<String[]> list = new ArrayList<>();
            for (int i = 0; i < applications.length(); i++) {
                JSONObject a = applications.optJSONObject(i);
                if (a == null) continue;
                list.add(new String[]{
                    a.optString("title", "Untitled"),
                    String.valueOf(a.optInt("opportunityScore", 0)),
                    String.valueOf(a.optInt("budget", 0)),
                    a.optString("status", "Saved")
                });
            }
            saveItems(list);
        }
    }

    @Override
    void runAnalysis(String t, String d, int b, int c, String cl, Button btn) {
        if (!signedIn()) {
            super.runAnalysis(t, d, b, c, cl, btn);
            return;
        }
        new Thread(() -> {
            try {
                JSONObject job = new JSONObject()
                    .put("title", t).put("description", d).put("budget", b).put("connects", c).put("clientDetails", cl);
                JSONObject body = new JSONObject().put("job", job).put("brain", brainJson());
                JSONObject res = request("POST", API + "/api/analyze", body, token());
                JSONObject analysis = res.getJSONObject("analysis");
                String mode = res.optString("mode", "fallback");
                runOnUiThread(() -> report(t, b, mode, analysis));
            } catch (Exception e) {
                JSONObject local = local(t, d, b, c, cl);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Authenticated AI unavailable — using safe local analysis", Toast.LENGTH_LONG).show();
                    report(t, b, "device-fallback", local);
                });
            } finally {
                runOnUiThread(() -> { btn.setEnabled(true); btn.setText("Build Opportunity Report"); });
            }
        }).start();
    }

    private JSONObject request(String method, String url, JSONObject body, String bearer) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(12000);
        c.setReadTimeout(65000);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        c.setRequestProperty("User-Agent", "AI-Upwork-Agent-Android/1.2.0");
        if (bearer != null && !bearer.isEmpty()) c.setRequestProperty("Authorization", "Bearer " + bearer);
        if (body != null && !"GET".equals(method)) {
            c.setDoOutput(true);
            try (OutputStream out = c.getOutputStream()) {
                out.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        int code = c.getResponseCode();
        InputStream stream = code >= 400 ? c.getErrorStream() : c.getInputStream();
        String raw = readAll(stream);
        JSONObject result = raw.isEmpty() ? new JSONObject() : new JSONObject(raw);
        if (code >= 400) {
            String error = result.optString("error", "HTTP " + code);
            throw new Exception(error.replace('_', ' '));
        }
        return result;
    }

    private String readAll(InputStream stream) throws Exception {
        if (stream == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) out.append(line);
        reader.close();
        return out.toString();
    }

    private void showError(String title, Exception e) {
        String detail = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        runOnUiThread(() -> Toast.makeText(this, title + ": " + detail, Toast.LENGTH_LONG).show());
    }
}
