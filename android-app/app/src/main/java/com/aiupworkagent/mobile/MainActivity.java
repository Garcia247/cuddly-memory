package com.aiupworkagent.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(16, 55, 42);
    private static final int BG = Color.rgb(245, 247, 246);
    private static final int BORDER = Color.rgb(220, 229, 223);
    private static final String[] STAGES = {"Saved", "Applied", "Reply", "Interview", "Offer", "Won", "Lost"};

    private SharedPreferences prefs;
    private LinearLayout page;
    private LinearLayout nav;
    private String lastProposal = "";
    private String lastJobTitle = "";
    private int lastScore = 0;
    private int lastBudget = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("ai_upwork_agent", MODE_PRIVATE);
        seedBrain();
        buildShell();
        showDashboard();
    }

    private void seedBrain() {
        if (!prefs.contains("headline")) {
            prefs.edit()
                .putString("headline", "PhD Researcher & Academic Manuscript Editor | Journal-Ready Papers")
                .putString("services", "Academic research, manuscript editing, literature reviews, dissertation/thesis support, methodology, SPSS/data analysis, business research")
                .putString("evidence", "Postgraduate research support; manuscript editing; structured literature reviews; methodology support; quantitative analysis; journal-readiness")
                .putString("preferred", "Academic manuscript editing, dissertation support, literature reviews, research methodology, SPSS/data analysis")
                .putString("avoid", "Unrelated general writing, unpaid samples, suspicious off-platform requests")
                .putInt("minBudget", 100)
                .putInt("hourlyRate", 15)
                .apply();
        }
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(14), dp(14), dp(14), dp(10));

        TextView title = text("AI Upwork Agent", 25, true);
        title.setTextColor(GREEN);
        root.addView(title);

        TextView subtitle = text("Your private Upwork application intelligence assistant", 12, false);
        subtitle.setTextColor(Color.DKGRAY);
        root.addView(subtitle);

        nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(0, dp(12), 0, dp(8));
        addNav("Home", this::showDashboard);
        addNav("Brain", this::showBrain);
        addNav("Analyze", this::showAnalyze);
        addNav("Tracker", this::showTracker);
        root.addView(nav);

        ScrollView scroll = new ScrollView(this);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(0, dp(4), 0, dp(40));
        scroll.addView(page);
        root.addView(scroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
    }

    private void addNav(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(42), 1f);
        p.setMargins(dp(2), 0, dp(2), 0);
        nav.addView(b, p);
    }

    private void showDashboard() {
        clearPage();
        page.addView(sectionTitle("Dashboard"));
        page.addView(text("Good to see you. Focus your Connects on opportunities with strong evidence fit.", 14, false));

        int total = tracker().size();
        int replies = countStageAtLeast("Reply");
        int interviews = countStageAtLeast("Interview");
        int wins = countExact("Won");

        LinearLayout metrics = new LinearLayout(this);
        metrics.setOrientation(LinearLayout.HORIZONTAL);
        metrics.addView(metric("Applications", String.valueOf(total)), weight());
        metrics.addView(metric("Replies", String.valueOf(replies)), weight());
        page.addView(metrics);

        LinearLayout metrics2 = new LinearLayout(this);
        metrics2.setOrientation(LinearLayout.HORIZONTAL);
        metrics2.addView(metric("Interviews", String.valueOf(interviews)), weight());
        metrics2.addView(metric("Wins", String.valueOf(wins)), weight());
        page.addView(metrics2);

        Button analyze = primary("Analyze a New Job");
        analyze.setOnClickListener(v -> showAnalyze());
        page.addView(analyze);

        page.addView(cardText("Freelancer Brain", prefs.getString("headline", "") + "\n\nReference rate: $" + prefs.getInt("hourlyRate", 15) + "/hr"));
        page.addView(cardText("Truth Guard", "The app should only use claims stored in your Freelancer Brain. Unsupported credentials should be added by you before use."));
    }

    private void showBrain() {
        clearPage();
        page.addView(sectionTitle("Freelancer Brain"));
        page.addView(text("This is the evidence base the proposal engine is allowed to use.", 13, false));

        EditText headline = field("Headline", prefs.getString("headline", ""), false);
        EditText services = field("Services", prefs.getString("services", ""), true);
        EditText evidence = field("Evidence / proof", prefs.getString("evidence", ""), true);
        EditText preferred = field("Preferred jobs", prefs.getString("preferred", ""), true);
        EditText avoid = field("Jobs to avoid", prefs.getString("avoid", ""), true);
        EditText minBudget = field("Minimum budget ($)", String.valueOf(prefs.getInt("minBudget", 100)), false);
        EditText rate = field("Reference hourly rate ($)", String.valueOf(prefs.getInt("hourlyRate", 15)), false);

        Button save = primary("Save Freelancer Brain");
        save.setOnClickListener(v -> {
            prefs.edit()
                .putString("headline", headline.getText().toString().trim())
                .putString("services", services.getText().toString().trim())
                .putString("evidence", evidence.getText().toString().trim())
                .putString("preferred", preferred.getText().toString().trim())
                .putString("avoid", avoid.getText().toString().trim())
                .putInt("minBudget", parseInt(minBudget.getText().toString(), 100))
                .putInt("hourlyRate", parseInt(rate.getText().toString(), 15))
                .apply();
            Toast.makeText(this, "Freelancer Brain saved", Toast.LENGTH_SHORT).show();
        });
        page.addView(save);
    }

    private void showAnalyze() {
        clearPage();
        page.addView(sectionTitle("Analyze Upwork Job"));
        page.addView(text("Paste a real job. This offline APK uses the first deterministic scoring engine; cloud AI comes after the installable app is proven.", 12, false));

        EditText title = field("Job title", "Academic Manuscript Editor for Journal Submission", false);
        EditText description = field("Job description", "We need an experienced academic editor to improve two research manuscripts for peer-reviewed journal submission. Strengthen clarity, structure, references and overall journal readiness.", true);
        EditText budget = field("Budget ($)", "500", false);
        EditText connects = field("Connects required", "12", false);
        EditText client = field("Client details", "Payment verified, 76% hire rate, $26k spend, good reviews", true);

        Button run = primary("Analyze Job & Build Proposal");
        run.setOnClickListener(v -> {
            String t = title.getText().toString().trim();
            String d = description.getText().toString().trim();
            int b = parseInt(budget.getText().toString(), 0);
            int c = parseInt(connects.getText().toString(), 0);
            String cl = client.getText().toString().trim();
            renderAnalysis(t, d, b, c, cl);
        });
        page.addView(run);
    }

    private void renderAnalysis(String title, String description, int budget, int connects, String client) {
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Add a job title and description", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = (title + " " + description).toLowerCase(Locale.ROOT);
        String evidence = prefs.getString("evidence", "").toLowerCase(Locale.ROOT);
        int score = 46;
        String[] fitWords = {"research", "academic", "manuscript", "journal", "thesis", "dissertation", "literature", "spss", "methodology", "analysis"};
        for (String word : fitWords) {
            if (text.contains(word)) score += 4;
        }
        if (budget >= prefs.getInt("minBudget", 100)) score += 8; else score -= 8;
        if (client.toLowerCase(Locale.ROOT).contains("verified")) score += 5;
        if (client.toLowerCase(Locale.ROOT).contains("hire")) score += 3;
        if (connects > 20) score -= 8;
        if (connects <= 12) score += 3;
        score = Math.max(0, Math.min(96, score));

        String recommendation = score >= 88 ? "STRONG APPLY" : score >= 74 ? "APPLY" : score >= 60 ? "CONSIDER" : "SKIP";
        int suggestedBid = Math.max(prefs.getInt("minBudget", 100), budget > 0 ? Math.round(budget * 0.9f) : 150);
        String headline = prefs.getString("headline", "Academic research specialist");
        String proof = prefs.getString("evidence", "relevant research support");
        String proposal = "I read your brief with the submission outcome in mind: work that is clearer, better structured and genuinely ready for the next review stage.\n\n" +
            "My relevant positioning is " + headline + ". My saved evidence includes " + proof + ".\n\n" +
            "For this project, I would first diagnose the highest-impact gaps, strengthen the structure and academic language, review the evidence/references, and finish with a focused quality-assurance pass rather than simply line-editing the document.\n\n" +
            "If you share the current manuscript and target journal requirements, I can begin by identifying the sections most likely to need substantive attention.";

        lastProposal = proposal;
        lastJobTitle = title;
        lastScore = score;
        lastBudget = budget;

        page.removeAllViews();
        page.addView(sectionTitle("Opportunity Report"));

        TextView scoreView = text(score + "/100", 44, true);
        scoreView.setTextColor(score >= 74 ? Color.rgb(8, 117, 77) : score >= 60 ? Color.rgb(154, 104, 0) : Color.rgb(163, 51, 51));
        page.addView(scoreView);
        page.addView(text(recommendation, 20, true));

        int minBudget = prefs.getInt("minBudget", 100);
        String connectsDecision = score >= 74 && connects <= 16 ? "Worth spending" : score >= 60 ? "Borderline" : "Save Connects";
        page.addView(cardText("Decision Intelligence", "Budget fit: " + (budget >= minBudget ? "Good" : "Low") + "\nConnects: " + connectsDecision + "\nSuggested bid: $" + suggestedBid));
        page.addView(cardText("What the client needs", "A reliable specialist who can turn the current work into a stronger final deliverable—not just perform surface-level editing."));
        page.addView(cardText("Proposal", proposal));
        page.addView(cardText("Truth Guard", "Proposal generated only from the Freelancer Brain stored on this device. Review every factual claim before sending."));

        Button copy = primary("Copy Proposal");
        copy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("proposal", lastProposal));
            Toast.makeText(this, "Proposal copied", Toast.LENGTH_SHORT).show();
        });
        page.addView(copy);

        Button save = secondary("Add to Application Tracker");
        save.setOnClickListener(v -> {
            addTracker(lastJobTitle, lastScore, lastBudget, "Saved");
            Toast.makeText(this, "Added to tracker", Toast.LENGTH_SHORT).show();
        });
        page.addView(save);

        Button back = secondary("Analyze Another Job");
        back.setOnClickListener(v -> showAnalyze());
        page.addView(back);
    }

    private void showTracker() {
        clearPage();
        page.addView(sectionTitle("Application Tracker"));
        List<String[]> items = tracker();
        if (items.isEmpty()) {
            page.addView(cardText("No applications yet", "Analyze a job and add it to your tracker."));
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            String[] item = items.get(i);
            LinearLayout card = card();
            card.addView(text(item[0], 16, true));
            card.addView(text("Score " + item[1] + "/100 · Budget $" + item[2], 12, false));
            TextView stage = text("Status: " + item[3], 14, true);
            stage.setTextColor(GREEN);
            card.addView(stage);
            Button next = secondary("Move to Next Stage");
            next.setOnClickListener(v -> {
                List<String[]> current = tracker();
                if (index >= current.size()) return;
                String now = current.get(index)[3];
                int s = stageIndex(now);
                if (s < STAGES.length - 1) current.get(index)[3] = STAGES[s + 1];
                saveTracker(current);
                showTracker();
            });
            card.addView(next);
            page.addView(card);
        }
    }

    private void addTracker(String title, int score, int budget, String status) {
        List<String[]> items = tracker();
        items.add(0, new String[]{sanitize(title), String.valueOf(score), String.valueOf(budget), status});
        saveTracker(items);
    }

    private List<String[]> tracker() {
        List<String[]> out = new ArrayList<>();
        String raw = prefs.getString("tracker", "");
        if (raw == null || raw.isEmpty()) return out;
        String[] rows = raw.split("\\u001e");
        for (String row : rows) {
            String[] cols = row.split("\\u001f", -1);
            if (cols.length == 4) out.add(cols);
        }
        return out;
    }

    private void saveTracker(List<String[]> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append('\u001e');
            String[] x = items.get(i);
            sb.append(sanitize(x[0])).append('\u001f').append(x[1]).append('\u001f').append(x[2]).append('\u001f').append(x[3]);
        }
        prefs.edit().putString("tracker", sb.toString()).apply();
    }

    private String sanitize(String s) {
        return s.replace("\u001e", " ").replace("\u001f", " ");
    }

    private int countExact(String stage) {
        int n = 0;
        for (String[] x : tracker()) if (x[3].equals(stage)) n++;
        return n;
    }

    private int countStageAtLeast(String stage) {
        int threshold = stageIndex(stage), n = 0;
        for (String[] x : tracker()) {
            int i = stageIndex(x[3]);
            if (i >= threshold && !x[3].equals("Lost")) n++;
        }
        return n;
    }

    private int stageIndex(String s) {
        for (int i = 0; i < STAGES.length; i++) if (STAGES[i].equals(s)) return i;
        return 0;
    }

    private void clearPage() { page.removeAllViews(); }

    private TextView sectionTitle(String s) {
        TextView t = text(s, 23, true);
        t.setTextColor(GREEN);
        t.setPadding(0, dp(6), 0, dp(10));
        return t;
    }

    private TextView text(String s, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(Color.rgb(30, 43, 36));
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, dp(4), 0, dp(5));
        return t;
    }

    private LinearLayout metric(String label, String value) {
        LinearLayout box = card();
        box.addView(text(value, 28, true));
        TextView l = text(label, 11, false);
        l.setTextColor(Color.DKGRAY);
        box.addView(l);
        return box;
    }

    private LinearLayout.LayoutParams weight() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        p.setMargins(dp(3), dp(3), dp(3), dp(3));
        return p;
    }

    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(12));
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(14));
        bg.setStroke(dp(1), BORDER);
        box.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(5), 0, dp(5));
        box.setLayoutParams(p);
        return box;
    }

    private LinearLayout cardText(String heading, String body) {
        LinearLayout c = card();
        c.addView(text(heading, 15, true));
        c.addView(text(body, 13, false));
        return c;
    }

    private EditText field(String label, String value, boolean multiline) {
        page.addView(text(label, 12, true));
        EditText e = new EditText(this);
        e.setText(value);
        e.setTextSize(14);
        e.setPadding(dp(10), dp(9), dp(10), dp(9));
        if (multiline) {
            e.setMinLines(3);
            e.setGravity(Gravity.TOP);
        } else {
            e.setSingleLine(true);
        }
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1), BORDER);
        e.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, dp(8));
        page.addView(e, p);
        return e;
    }

    private Button primary(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(GREEN);
        bg.setCornerRadius(dp(10));
        b.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(50));
        p.setMargins(0, dp(8), 0, dp(6));
        b.setLayoutParams(p);
        return b;
    }

    private Button secondary(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        p.setMargins(0, dp(6), 0, dp(4));
        b.setLayoutParams(p);
        return b;
    }

    private int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }

    private int dp(int n) {
        return Math.round(n * getResources().getDisplayMetrics().density);
    }
}
