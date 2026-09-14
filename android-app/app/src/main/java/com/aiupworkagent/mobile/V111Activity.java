package com.aiupworkagent.mobile;

import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class V111Activity extends V11Activity {
    private volatile String cloudState = "checking";
    private volatile String cloudReason = "Checking production API…";

    @Override
    void status() {
        new Thread(() -> {
            String state = "offline";
            String reason = "Cloud API could not be reached.";
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(API + "/api/status").openConnection();
                c.setRequestMethod("GET");
                c.setInstanceFollowRedirects(false);
                c.setConnectTimeout(8000);
                c.setReadTimeout(12000);
                c.setRequestProperty("Accept", "application/json");
                c.setRequestProperty("User-Agent", "AI-Upwork-Agent-Android/1.1.1");

                int code = c.getResponseCode();
                String location = c.getHeaderField("Location");

                if (code == 401 || code == 403 || code == 302 || code == 307 || code == 308) {
                    state = "protected";
                    reason = "Vercel Deployment Protection is blocking anonymous mobile access (HTTP " + code + ").";
                    if (location != null && location.toLowerCase().contains("vercel")) {
                        reason += " The API is online, but Vercel requires authentication before the request can reach it.";
                    }
                    cloud = false;
                    ai = false;
                } else {
                    InputStream stream = code >= 400 ? c.getErrorStream() : c.getInputStream();
                    String body = readBody(stream);
                    String lower = body.toLowerCase();
                    String type = c.getContentType() == null ? "" : c.getContentType().toLowerCase();

                    if (type.contains("text/html") || lower.contains("vercel authentication") || lower.contains("_vercel_sso") || body.trim().startsWith("<")) {
                        state = "protected";
                        reason = "Vercel Deployment Protection returned an authentication page instead of the JSON API.";
                        cloud = false;
                        ai = false;
                    } else if (code >= 200 && code < 300) {
                        JSONObject s = new JSONObject(body);
                        cloud = s.optBoolean("ok");
                        ai = s.optBoolean("ai");
                        persistence = s.optString("persistence", "database-not-connected");
                        state = cloud ? "online" : "offline";
                        reason = cloud ? "Production API responded successfully." : "The API responded, but did not report a healthy state.";
                    } else {
                        cloud = false;
                        ai = false;
                        reason = "Cloud returned HTTP " + code + ".";
                    }
                }
            } catch (Exception e) {
                cloud = false;
                ai = false;
                String msg = e.getMessage();
                reason = "Network check failed: " + e.getClass().getSimpleName() + (msg == null ? "" : " — " + msg);
            }

            cloudState = state;
            cloudReason = reason;
            p.edit().putString("lastCloudState", state).putString("lastCloudReason", reason).apply();

            runOnUiThread(() -> {
                if ("protected".equals(cloudState)) {
                    cloudChip.setText("Vercel blocked");
                    cloudChip.setTextColor(RED);
                    cloudChip.setBackground(bg(Color.rgb(253, 237, 234), 999, Color.rgb(242, 205, 198), 1));
                } else {
                    cloudChip.setText(cloud ? (ai ? "AI live" : "Cloud online") : "Offline");
                    cloudChip.setTextColor(cloud ? GREEN : RED);
                    cloudChip.setBackground(bg(cloud ? PALE : Color.rgb(253, 237, 234), 999, cloud ? Color.rgb(207, 230, 218) : Color.rgb(242, 205, 198), 1));
                }
                if (tab == 0) home();
                if (tab == 4) cloud();
            });
        }).start();
    }

    @Override
    void home() {
        super.home();
        if ("protected".equals(cloudState) || "protected".equals(p.getString("lastCloudState", ""))) {
            page.addView(protectionCard());
        }
    }

    @Override
    void cloud() {
        super.cloud();
        String state = "checking".equals(cloudState) ? p.getString("lastCloudState", "checking") : cloudState;
        String reason = "Checking production API…".equals(cloudReason) ? p.getString("lastCloudReason", cloudReason) : cloudReason;

        LinearLayout diag = card();
        TextView h = txt("Connection diagnosis", 14, true);
        h.setTextColor("protected".equals(state) ? RED : (cloud ? GREEN2 : AMBER));
        diag.addView(h);
        TextView r = txt(reason, 12, false);
        r.setTextColor(MUTED);
        r.setPadding(0, dp(6), 0, 0);
        diag.addView(r);
        page.addView(diag);

        if ("protected".equals(state)) {
            page.addView(protectionCard());
        }
    }

    private LinearLayout protectionCard() {
        LinearLayout n = card();
        n.setBackground(bg(Color.rgb(253, 244, 242), 16, Color.rgb(240, 204, 198), 1));
        TextView h = txt("Cloud is online — Vercel is blocking this phone", 14, true);
        h.setTextColor(RED);
        n.addView(h);
        TextView q = txt(
            "Fix in Vercel: ai-upwork-agent-cloud → Settings → Security → Deployment Protection → change Vercel Authentication from All Deployments to Standard Protection, then return here and tap Refresh Cloud Status.",
            12,
            false
        );
        q.setTextColor(Color.rgb(110, 61, 54));
        q.setPadding(0, dp(6), 0, 0);
        n.addView(q);
        return n;
    }

    private String readBody(InputStream stream) throws Exception {
        if (stream == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) out.append(line);
        reader.close();
        return out.toString();
    }
}
