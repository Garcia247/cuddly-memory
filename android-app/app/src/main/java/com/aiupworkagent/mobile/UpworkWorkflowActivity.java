package com.aiupworkagent.mobile;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class UpworkWorkflowActivity extends V12Activity {
    private static final int FOREST = Color.rgb(12, 77, 55);
    private static final int FOREST_DARK = Color.rgb(8, 48, 37);
    private static final int MINT = Color.rgb(229, 245, 237);
    private static final int MINT_2 = Color.rgb(215, 238, 226);
    private static final int WARM = Color.rgb(252, 250, 246);
    private static final int TEXT = Color.rgb(19, 31, 27);
    private static final int SOFT_TEXT = Color.rgb(98, 111, 105);
    private static final int SOFT_BORDER = Color.rgb(222, 229, 225);
    private static final int GOLD = Color.rgb(156, 105, 18);

    @Override
    void shell() {
        getWindow().setStatusBarColor(FOREST_DARK);
        getWindow().setNavigationBarColor(Color.WHITE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(WARM);

        LinearLayout head = row();
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(18), dp(14), dp(18), dp(12));
        head.setBackgroundColor(Color.WHITE);
        head.setElevation(dp(2));

        LinearLayout brand = col();
        TextView eyebrow = txt("UPWORK WORKFLOW", 9, true);
        eyebrow.setTextColor(FOREST);
        eyebrow.setLetterSpacing(0.12f);
        brand.addView(eyebrow);
        TextView name = txt("Work smarter. Apply better.", 19, true);
        name.setTextColor(TEXT);
        name.setPadding(0, dp(2), 0, 0);
        brand.addView(name);
        TextView sub = txt("Discover  •  Qualify  •  Propose  •  Track", 10, false);
        sub.setTextColor(SOFT_TEXT);
        sub.setPadding(0, dp(2), 0, 0);
        brand.addView(sub);
        head.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));

        String remembered = p.getString("lastCloudState", "");
        String initial = "online".equals(remembered) ? "Online" : ("protected".equals(remembered) ? "Blocked" : "Checking…");
        int initialBg = "online".equals(remembered) ? MINT : ("protected".equals(remembered) ? Color.rgb(253, 239, 237) : MINT);
        int initialFg = "protected".equals(remembered) ? RED : FOREST;
        cloudChip = chip(initial, initialBg, initialFg);
        head.addView(cloudChip);
        root.addView(head);

        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        page = col();
        page.setPadding(dp(16), dp(16), dp(16), dp(28));
        sv.addView(page);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));

        nav = row();
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(7), dp(8), dp(9));
        nav.setBackgroundColor(Color.WHITE);
        nav.setElevation(dp(8));
        root.addView(nav);

        setContentView(root);
        drawNav();
    }

    @Override
    void drawNav() {
        if (nav == null) return;
        nav.removeAllViews();
        String[] names = {"Home", "Brain", "Opportunities", "Tracker", "Cloud"};
        String[] marks = {"●", "✦", "◎", "▤", "☁"};
        Runnable[] actions = {this::home, this::brain, this::analyze, this::tracker, this::cloud};

        for (int i = 0; i < names.length; i++) {
            final int x = i;
            LinearLayout item = col();
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(3), dp(6), dp(3), dp(6));
            item.setClickable(true);
            item.setFocusable(true);
            item.setBackground(bg(x == tab ? MINT : Color.TRANSPARENT, 14, x == tab ? MINT_2 : Color.TRANSPARENT, x == tab ? 1 : 0));

            TextView icon = txt(marks[i], 15, true);
            icon.setGravity(Gravity.CENTER);
            icon.setTextColor(x == tab ? FOREST : SOFT_TEXT);
            item.addView(icon);

            TextView label = txt(names[i], 8, x == tab);
            label.setGravity(Gravity.CENTER);
            label.setTextColor(x == tab ? FOREST : SOFT_TEXT);
            label.setPadding(0, dp(2), 0, 0);
            item.addView(label);

            item.setOnClickListener(v -> actions[x].run());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(57), 1);
            lp.setMargins(dp(2), 0, dp(2), 0);
            nav.addView(item, lp);
        }
    }

    @Override
    void home() {
        select(0);
        clear();

        TextView hello = txt("YOUR APPLICATION COMMAND CENTRE", 9, true);
        hello.setTextColor(FOREST);
        hello.setLetterSpacing(0.10f);
        page.addView(hello);

        TextView h1 = txt("One workflow. Better applications.", 27, true);
        h1.setTextColor(TEXT);
        h1.setPadding(0, dp(4), 0, 0);
        page.addView(h1);

        TextView lead = txt("Move from opportunity to proposal to outcome without losing the thread.", 12, false);
        lead.setTextColor(SOFT_TEXT);
        lead.setPadding(0, dp(5), 0, dp(11));
        page.addView(lead);

        LinearLayout hero = col();
        hero.setPadding(dp(19), dp(18), dp(19), dp(18));
        GradientDrawable heroBg = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{FOREST_DARK, FOREST, Color.rgb(24, 112, 80)}
        );
        heroBg.setCornerRadius(dp(24));
        hero.setBackground(heroBg);
        hero.setElevation(dp(2));
        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(-1, -2);
        heroLp.setMargins(0, 0, 0, dp(8));
        hero.setLayoutParams(heroLp);

        TextView next = txt("NEXT BEST ACTION", 9, true);
        next.setTextColor(Color.rgb(176, 225, 201));
        next.setLetterSpacing(0.10f);
        hero.addView(next);

        TextView heroTitle = txt("Qualify the job before you spend Connects.", 21, true);
        heroTitle.setTextColor(Color.WHITE);
        heroTitle.setPadding(0, dp(5), 0, 0);
        hero.addView(heroTitle);

        TextView heroSub = txt("Check fit, client quality, pricing, evidence gaps and your proposal angle in one pass.", 12, false);
        heroSub.setTextColor(Color.rgb(223, 239, 231));
        heroSub.setPadding(0, dp(5), 0, dp(6));
        hero.addView(heroSub);

        Button go = button("Open Opportunities  →", Color.WHITE, FOREST, Color.WHITE);
        go.setOnClickListener(v -> analyze());
        hero.addView(go);
        page.addView(hero);

        page.addView(label("YOUR WORKFLOW"));
        LinearLayout flow = card();
        flow.setPadding(dp(10), dp(12), dp(10), dp(12));
        String[] steps = {"1\nQualify", "2\nPosition", "3\nPropose", "4\nTrack"};
        Runnable[] stepActions = {this::analyze, this::brain, this::analyze, this::tracker};
        LinearLayout sr = row();
        for (int i = 0; i < steps.length; i++) {
            final int x = i;
            TextView s = txt(steps[i], 10, true);
            s.setGravity(Gravity.CENTER);
            s.setTextColor(FOREST);
            s.setBackground(bg(MINT, 14, MINT_2, 1));
            s.setPadding(dp(3), dp(10), dp(3), dp(10));
            s.setClickable(true);
            s.setFocusable(true);
            s.setOnClickListener(v -> stepActions[x].run());
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, -2, 1);
            slp.setMargins(dp(3), 0, dp(3), 0);
            sr.addView(s, slp);
        }
        flow.addView(sr);
        TextView flowHint = txt("Tap a stage to continue the workflow.", 9, false);
        flowHint.setTextColor(SOFT_TEXT);
        flowHint.setGravity(Gravity.CENTER);
        flowHint.setPadding(0, dp(9), 0, 0);
        flow.addView(flowHint);
        page.addView(flow);

        List<String[]> xs = items();
        if (!xs.isEmpty()) {
            String[] recent = xs.get(0);
            page.addView(label("RECENT OPPORTUNITY"));
            LinearLayout recentCard = card();
            LinearLayout top = row();
            TextView rt = txt(recent[0], 15, true);
            rt.setTextColor(TEXT);
            top.addView(rt, new LinearLayout.LayoutParams(0, -2, 1));
            top.addView(chip(recent[1] + "/100", MINT, FOREST));
            recentCard.addView(top);
            TextView rm = txt("$" + recent[2] + " budget  •  " + recent[3], 10, false);
            rm.setTextColor(SOFT_TEXT);
            rm.setPadding(0, dp(7), 0, dp(3));
            recentCard.addView(rm);
            Button continueButton = secondary("Continue Application  →");
            continueButton.setOnClickListener(v -> tracker());
            recentCard.addView(continueButton);
            page.addView(recentCard);
        }

        page.addView(label("PERFORMANCE SNAPSHOT"));
        LinearLayout r1 = row();
        r1.addView(metric("Applications", "" + items().size(), "in your tracker"), weight());
        r1.addView(metric("Replies", "" + atLeast("Reply"), "client responses"), weight());
        page.addView(r1);
        LinearLayout r2 = row();
        r2.addView(metric("Interviews", "" + atLeast("Interview"), "qualified conversations"), weight());
        r2.addView(metric("Wins", "" + exact("Won"), "contracts won"), weight());
        page.addView(r2);

        page.addView(label("SYSTEM READINESS"));
        LinearLayout statusCard = card();
        statusCard.addView(statusRow("Cloud API", cloud ? "Online" : "Refreshing", cloud ? GREEN2 : GOLD));
        statusCard.addView(statusRow("Opportunity intelligence", ai ? "GPT-5.6 Terra live" : "Safe fallback", ai ? GREEN2 : GOLD));
        statusCard.addView(statusRow("Cloud account sync", persistence.equals("database-not-connected") ? "Pending" : "Active", persistence.equals("database-not-connected") ? GOLD : GREEN2));
        page.addView(statusCard);

        page.addView(label("YOUR FREELANCER BRAIN"));
        LinearLayout brainCard = card();
        TextView bTitle = txt(p.getString("headline", ""), 16, true);
        bTitle.setTextColor(TEXT);
        brainCard.addView(bTitle);
        TextView bMeta = txt("$" + p.getInt("hourlyRate", 15) + "/hr reference  •  $" + p.getInt("minBudget", 100) + " minimum project", 11, false);
        bMeta.setTextColor(SOFT_TEXT);
        bMeta.setPadding(0, dp(7), 0, dp(6));
        brainCard.addView(bMeta);
        TextView bHint = txt("Keep this evidence current so every recommendation and proposal stays grounded in what you can actually defend.", 11, false);
        bHint.setTextColor(SOFT_TEXT);
        brainCard.addView(bHint);
        Button edit = secondary("Review Freelancer Brain");
        edit.setOnClickListener(v -> brain());
        brainCard.addView(edit);
        page.addView(brainCard);
    }

    @Override
    LinearLayout title(String h, String s) {
        LinearLayout c = col();
        c.setPadding(0, dp(1), 0, dp(14));
        TextView eyebrow = txt("UPWORK WORKFLOW", 9, true);
        eyebrow.setTextColor(FOREST);
        eyebrow.setLetterSpacing(0.10f);
        c.addView(eyebrow);
        TextView head = txt(h, 27, true);
        head.setTextColor(TEXT);
        head.setPadding(0, dp(5), 0, 0);
        c.addView(head);
        TextView p = txt(s, 12, false);
        p.setTextColor(SOFT_TEXT);
        p.setPadding(0, dp(5), 0, 0);
        c.addView(p);
        return c;
    }

    @Override
    LinearLayout card() {
        LinearLayout c = col();
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        c.setBackground(bg(Color.WHITE, 20, SOFT_BORDER, 1));
        c.setElevation(dp(1));
        LinearLayout.LayoutParams q = new LinearLayout.LayoutParams(-1, -2);
        q.setMargins(0, dp(5), 0, dp(8));
        c.setLayoutParams(q);
        return c;
    }

    @Override
    LinearLayout metric(String a, String b, String c) {
        LinearLayout m = card();
        m.setPadding(dp(15), dp(14), dp(15), dp(14));
        TextView value = txt(b, 28, true);
        value.setTextColor(TEXT);
        m.addView(value);
        TextView name = txt(a, 11, true);
        name.setTextColor(FOREST);
        name.setPadding(0, dp(2), 0, 0);
        m.addView(name);
        TextView note = txt(c, 9, false);
        note.setTextColor(SOFT_TEXT);
        note.setPadding(0, dp(2), 0, 0);
        m.addView(note);
        return m;
    }

    @Override
    TextView label(String s) {
        TextView t = txt(s, 9, true);
        t.setTextColor(SOFT_TEXT);
        t.setLetterSpacing(0.10f);
        t.setPadding(dp(2), dp(15), 0, dp(7));
        return t;
    }

    @Override
    EditText input(String v) {
        EditText e = new EditText(this);
        e.setText(v);
        e.setTextSize(13);
        e.setTextColor(TEXT);
        e.setHintTextColor(Color.rgb(145, 154, 149));
        e.setPadding(dp(14), dp(13), dp(14), dp(13));
        e.setBackground(bg(Color.WHITE, 15, SOFT_BORDER, 1));
        LinearLayout.LayoutParams q = new LinearLayout.LayoutParams(-1, -2);
        q.setMargins(0, 0, 0, dp(8));
        e.setLayoutParams(q);
        return e;
    }

    @Override
    Button primary(String s) {
        return button(s, FOREST, Color.WHITE, FOREST);
    }

    @Override
    Button secondary(String s) {
        return button(s, Color.WHITE, FOREST, SOFT_BORDER);
    }

    @Override
    Button button(String s, int fill, int fg, int stroke) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(12);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(fg);
        b.setStateListAnimator(null);
        b.setBackground(bg(fill, 15, stroke, stroke == fill ? 0 : 1));
        LinearLayout.LayoutParams q = new LinearLayout.LayoutParams(-1, dp(52));
        q.setMargins(0, dp(7), 0, dp(4));
        b.setLayoutParams(q);
        return b;
    }

    @Override
    LinearLayout statusRow(String a, String b, int color) {
        LinearLayout r = row();
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0, dp(7), 0, dp(7));
        TextView left = txt(a, 12, true);
        left.setTextColor(TEXT);
        r.addView(left, new LinearLayout.LayoutParams(0, -2, 1));
        TextView right = txt(b, 10, true);
        right.setTextColor(color);
        right.setPadding(dp(9), dp(5), dp(9), dp(5));
        right.setBackground(bg(color == RED ? Color.rgb(253, 239, 237) : (color == GREEN2 ? MINT : Color.rgb(251, 245, 229)), 999, Color.TRANSPARENT, 0));
        r.addView(right);
        return r;
    }
}
