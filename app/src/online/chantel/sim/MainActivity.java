package online.chantel.sim;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Widescreen 1920x1080 Automotive Application for Changan (SPM8675).
 * Styled in Changan Native Cockpit Theme (Deep Obsidian & Champagne Gold).
 *
 * 3-Column Cockpit Architecture:
 *  - Column 1 (33%): Clear Human-Friendly Network Status
 *  - Column 2 (35%): Operator Presets (KG, RU, KZ, UZ, BY) & 1-Click Fix
 *  - Column 3 (32%): Chantel PRO Smartphone Telematics Promo & High-Res QR
 */
public class MainActivity extends Activity {

    // Native Changan Automotive Color Palette
    private static final String COLOR_BG = "#0E1117";
    private static final String COLOR_CARD = "#151A23";
    private static final String COLOR_CARD_STROKE = "#222B3A";
    private static final String COLOR_PILL_BG = "#1B222F";
    private static final String COLOR_PILL_STROKE = "#2C374A";
    private static final String COLOR_CHAMPAGNE = "#E5B887";
    private static final String COLOR_CHAMPAGNE_DARK = "#11131A";
    private static final String COLOR_CHAMPAGNE_PRESSED = "#CBA071";
    private static final String COLOR_EMERALD = "#10B981";
    private static final String COLOR_EMERALD_BG = "#064E3B";
    private static final String COLOR_RUBY = "#EF4444";
    private static final String COLOR_RUBY_BG = "#450A0A";
    private static final String COLOR_AMBER = "#F59E0B";
    private static final String COLOR_AMBER_BG = "#451A03";
    private static final String COLOR_TEXT_PRIMARY = "#FFFFFF";
    private static final String COLOR_TEXT_SECONDARY = "#94A3B8";

    private Handler mHandler;
    private SharedPreferences mPrefs;

    // UI Elements
    private TextView mTvStatusPill;
    private TextView mTvSimVal;
    private TextView mTvQualityVal;
    private TextView mTvAccessVal;
    private TextView mTvWatchdogVal;
    private TextView mTvLog;

    private Spinner mSpCountry;
    private Spinner mSpOperator;
    private EditText mEtApn;
    private EditText mEtUser;
    private EditText mEtPass;
    private Button mBtnActivate;
    private Button mBtnCheckStatus;
    private Button mBtnWatchdogToggle;

    private boolean mWatchdogEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        mPrefs = getSharedPreferences(CellularWatchdogService.PREFS_NAME, Context.MODE_PRIVATE);
        mWatchdogEnabled = mPrefs.getBoolean(CellularWatchdogService.KEY_WATCHDOG_ENABLED, true);

        View root = createWidescreenLayout();
        setContentView(root);

        refreshStatusAsync();

        if (mWatchdogEnabled) {
            startWatchdogService();
        }
    }

    private View createWidescreenLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor(COLOR_BG));
        root.setPadding(dp(26), dp(18), dp(26), dp(20));

        // =====================================================================
        // TOP AUTOMOTIVE HEADER BAR
        // =====================================================================
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(14), dp(20), dp(14));
        header.setBackground(makeRoundedRect(COLOR_CARD, COLOR_CARD_STROKE, dp(16), dp(1)));
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerLp.bottomMargin = dp(16);
        header.setLayoutParams(headerLp);

        // App Icon
        ImageView logoBadge = new ImageView(this);
        logoBadge.setImageResource(R.mipmap.ic_launcher);
        logoBadge.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(50), dp(50));
        logoLp.rightMargin = dp(16);
        logoBadge.setLayoutParams(logoLp);
        header.addView(logoBadge);

        // Title and Subtitle
        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        LinearLayout rowTitle = new LinearLayout(this);
        rowTitle.setOrientation(LinearLayout.HORIZONTAL);
        rowTitle.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("CHANTEL SIM • ИНТЕРНЕТ ДЛЯ CHANGAN");
        tvTitle.setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        rowTitle.addView(tvTitle);

        TextView tvBadge = new TextView(this);
        tvBadge.setText(" FREE EDITION ");
        tvBadge.setTextColor(Color.parseColor(COLOR_CHAMPAGNE));
        tvBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvBadge.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tvBadge.setBackground(makeRoundedRect(COLOR_PILL_BG, COLOR_CHAMPAGNE, dp(4), dp(1)));
        tvBadge.setPadding(dp(8), dp(2), dp(8), dp(2));
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bLp.leftMargin = dp(12);
        rowTitle.addView(tvBadge, bLp);

        titleCol.addView(rowTitle);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText("Подключение местной SIM-карты, восстановление интернета и авто-защита от сброса настроек");
        tvSubtitle.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        tvSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvSubtitle.setPadding(0, dp(3), 0, 0);
        titleCol.addView(tvSubtitle);

        header.addView(titleCol);

        // Watchdog Toggle on Top Right
        mBtnWatchdogToggle = new Button(this);
        updateWatchdogButtonUi();
        mBtnWatchdogToggle.setOnClickListener(v -> toggleWatchdog());
        header.addView(mBtnWatchdogToggle);

        root.addView(header);

        // =====================================================================
        // MAIN 3-COLUMN WIDESCREEN BODY (Spans 100% of 1920x1080)
        // =====================================================================
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        body.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        // ---------------------------------------------------------------------
        // COLUMN 1 (33%): HUMAN-FRIENDLY INTERNET STATUS
        // ---------------------------------------------------------------------
        View colStatus = createStatusColumn();
        LinearLayout.LayoutParams col1Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.33f);
        col1Lp.rightMargin = dp(16);
        body.addView(colStatus, col1Lp);

        // ---------------------------------------------------------------------
        // COLUMN 2 (35%): OPERATOR SELECTION & 1-CLICK FIX
        // ---------------------------------------------------------------------
        View colSettings = createSettingsColumn();
        LinearLayout.LayoutParams col2Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.35f);
        col2Lp.rightMargin = dp(16);
        body.addView(colSettings, col2Lp);

        // ---------------------------------------------------------------------
        // COLUMN 3 (32%): CHANTEL PRO TELEMATICS PROMO & QR
        // ---------------------------------------------------------------------
        View colPromo = createPromoColumn();
        LinearLayout.LayoutParams col3Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.32f);
        body.addView(colPromo, col3Lp);

        root.addView(body);

        return root;
    }

    private View createStatusColumn() {
        LinearLayout card = createBaseCard();
        card.setPadding(dp(22), dp(20), dp(22), dp(20));

        TextView tvTitle = new TextView(this);
        tvTitle.setText("1. СТАТУС ПОДКЛЮЧЕНИЯ");
        tvTitle.setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(tvTitle);

        TextView tvHint = new TextView(this);
        tvHint.setText("Текущее состояние интернета и SIM-карты в автомобиле");
        tvHint.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        tvHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvHint.setPadding(0, dp(2), 0, dp(12));
        card.addView(tvHint);

        // Large Status Pill
        mTvStatusPill = new TextView(this);
        mTvStatusPill.setText("● ПРОВЕРКА СВЯЗИ...");
        mTvStatusPill.setTextColor(Color.parseColor(COLOR_AMBER));
        mTvStatusPill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        mTvStatusPill.setTypeface(Typeface.DEFAULT_BOLD);
        mTvStatusPill.setGravity(Gravity.CENTER);
        mTvStatusPill.setBackground(makeRoundedRect(COLOR_AMBER_BG, COLOR_AMBER, dp(12), dp(2)));
        mTvStatusPill.setPadding(dp(16), dp(12), dp(16), dp(12));
        LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pLp.bottomMargin = dp(16);
        card.addView(mTvStatusPill, pLp);

        // Human-friendly Metrics Table
        addMetricRow(card, "Сотовый модем авто", "Встроенный 4G LTE");
        mTvSimVal = addMetricRow(card, "SIM-карта в машине", "Проверка...");
        mTvAccessVal = addMetricRow(card, "Интернет в приложениях", "Проверка...");
        mTvQualityVal = addMetricRow(card, "Качество соединения", "Замер...");
        mTvWatchdogVal = addMetricRow(card, "Защита после сна авто", mWatchdogEnabled ? "Включена (авто)" : "Выключена");

        // Spacer to push button down
        View spacer = new View(this);
        LinearLayout.LayoutParams spLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        card.addView(spacer, spLp);

        // Check Status Button
        mBtnCheckStatus = new Button(this);
        mBtnCheckStatus.setText("🔄  ПРОВЕРИТЬ ИНТЕРНЕТ СЕЙЧАС");
        mBtnCheckStatus.setTextColor(Color.WHITE);
        mBtnCheckStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mBtnCheckStatus.setTypeface(Typeface.DEFAULT_BOLD);
        mBtnCheckStatus.setAllCaps(false);
        mBtnCheckStatus.setBackground(makeRoundedRect(COLOR_PILL_BG, COLOR_PILL_STROKE, dp(10), dp(1)));
        mBtnCheckStatus.setOnClickListener(v -> refreshStatusAsync());
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        card.addView(mBtnCheckStatus, btnLp);

        return card;
    }

    private View createSettingsColumn() {
        LinearLayout card = createBaseCard();
        card.setPadding(dp(22), dp(20), dp(22), dp(20));

        TextView tvTitle = new TextView(this);
        tvTitle.setText("2. ВЫБОР СИМ-КАРТЫ");
        tvTitle.setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(tvTitle);

        TextView tvHint = new TextView(this);
        tvHint.setText("Выберите вашего оператора связи для авто-настройки");
        tvHint.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        tvHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvHint.setPadding(0, dp(2), 0, dp(10));
        card.addView(tvHint);

        // Country Selector
        TextView tvLblCountry = new TextView(this);
        tvLblCountry.setText("Страна вашей SIM-карты:");
        tvLblCountry.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        tvLblCountry.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        card.addView(tvLblCountry);

        mSpCountry = new Spinner(this);
        mSpCountry.setBackground(makeRoundedRect(COLOR_PILL_BG, COLOR_PILL_STROKE, dp(8), dp(1)));
        List<String> countries = OperatorPresets.getCountries();
        ArrayAdapter<String> cAdapter = new StyledSpinnerAdapter<>(this, countries);
        mSpCountry.setAdapter(cAdapter);
        LinearLayout.LayoutParams spLp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        spLp1.topMargin = dp(4);
        spLp1.bottomMargin = dp(10);
        card.addView(mSpCountry, spLp1);

        // Operator Selector
        TextView tvLblOp = new TextView(this);
        tvLblOp.setText("Оператор связи:");
        tvLblOp.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        tvLblOp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        card.addView(tvLblOp);

        mSpOperator = new Spinner(this);
        mSpOperator.setBackground(makeRoundedRect(COLOR_PILL_BG, COLOR_PILL_STROKE, dp(8), dp(1)));
        LinearLayout.LayoutParams spLp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        spLp2.topMargin = dp(4);
        spLp2.bottomMargin = dp(10);
        card.addView(mSpOperator, spLp2);

        // APN Input field
        TextView tvLblApn = new TextView(this);
        tvLblApn.setText("Точка доступа (APN):");
        tvLblApn.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        tvLblApn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        card.addView(tvLblApn);

        mEtApn = new EditText(this);
        mEtApn.setText(mPrefs.getString(CellularWatchdogService.KEY_APN, "internet"));
        mEtApn.setTextColor(Color.parseColor(COLOR_CHAMPAGNE));
        mEtApn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        mEtApn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        mEtApn.setBackground(makeRoundedRect(COLOR_PILL_BG, COLOR_CHAMPAGNE, dp(8), dp(1)));
        mEtApn.setPadding(dp(14), dp(10), dp(14), dp(10));
        LinearLayout.LayoutParams etLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        etLp.topMargin = dp(4);
        etLp.bottomMargin = dp(14);
        card.addView(mEtApn, etLp);

        mEtUser = new EditText(this);
        mEtUser.setVisibility(View.GONE);
        mEtPass = new EditText(this);
        mEtPass.setVisibility(View.GONE);
        card.addView(mEtUser);
        card.addView(mEtPass);

        // Spinner Listeners
        mSpCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = countries.get(position);
                List<OperatorPresets.Operator> ops = OperatorPresets.getOperators(selectedCountry);
                ArrayAdapter<OperatorPresets.Operator> oAdapter = new StyledSpinnerAdapter<>(MainActivity.this, ops);
                mSpOperator.setAdapter(oAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mSpOperator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                OperatorPresets.Operator op = (OperatorPresets.Operator) mSpOperator.getSelectedItem();
                if (op != null) {
                    mEtApn.setText(op.apn);
                    mEtUser.setText(op.user);
                    mEtPass.setText(op.pass);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Big Primary Action Button
        mBtnActivate = new Button(this);
        mBtnActivate.setText("⚡️ ВКЛЮЧИТЬ И ПОЧИНИТЬ ИНТЕРНЕТ");
        mBtnActivate.setTextColor(Color.parseColor(COLOR_CHAMPAGNE_DARK));
        mBtnActivate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        mBtnActivate.setTypeface(Typeface.DEFAULT_BOLD);
        mBtnActivate.setAllCaps(false);
        mBtnActivate.setBackground(makeRoundedRect(COLOR_CHAMPAGNE, COLOR_CHAMPAGNE_PRESSED, dp(10), dp(1)));
        mBtnActivate.setOnClickListener(v -> runActivationFlow());
        LinearLayout.LayoutParams actLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        actLp.bottomMargin = dp(10);
        card.addView(mBtnActivate, actLp);

        // Terminal Log Console
        mTvLog = new TextView(this);
        mTvLog.setText("Готов к работе. Выберите вашего оператора и нажмите кнопку выше.");
        mTvLog.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        mTvLog.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mTvLog.setBackground(makeRoundedRect("#090C10", COLOR_CARD_STROKE, dp(8), dp(1)));
        mTvLog.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams logLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64));
        card.addView(mTvLog, logLp);

        return card;
    }

    private View createPromoColumn() {
        LinearLayout card = createBaseCard();
        card.setPadding(dp(22), dp(20), dp(22), dp(20));
        card.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("3. ТЕЛЕМАТИКА CHANTEL PRO");
        tvTitle.setTextColor(Color.parseColor(COLOR_CHAMPAGNE));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(tvTitle);

        TextView tvHint = new TextView(this);
        tvHint.setText("Управляйте автомобилем прямо со смартфона");
        tvHint.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        tvHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvHint.setPadding(0, dp(2), 0, dp(10));
        card.addView(tvHint);

        // Features List
        TextView tvFeatures = new TextView(this);
        tvFeatures.setText("• Автозапуск двигателя и прогрев климата\n• Открытие / закрытие дверей с телефона\n• Онлайн GPS-мониторинг и охрана 24/7\n• Без разбора авто и без потери гарантии");
        tvFeatures.setTextColor(Color.parseColor("#E2E8F0"));
        tvFeatures.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvFeatures.setLineSpacing(dp(5), 1.0f);
        LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fLp.bottomMargin = dp(12);
        card.addView(tvFeatures, fLp);

        // High-Res Large QR Code (230x230 px)
        FrameLayout qrFrame = new FrameLayout(this);
        LinearLayout qrBox = new LinearLayout(this);
        qrBox.setPadding(dp(10), dp(10), dp(10), dp(10));
        qrBox.setBackground(makeRoundedRect("#FFFFFF", COLOR_CHAMPAGNE, dp(16), dp(2)));

        ImageView ivQr = new ImageView(this);
        ivQr.setScaleType(ImageView.ScaleType.FIT_XY);
        Bitmap qrBmp = QrGenerator.generateQr("https://chantel.online", dp(230));
        ivQr.setImageBitmap(qrBmp);
        qrBox.addView(ivQr, new LinearLayout.LayoutParams(dp(230), dp(230)));

        qrFrame.addView(qrBox, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        card.addView(qrFrame);

        // Website Pill
        TextView tvWebLink = new TextView(this);
        tvWebLink.setText("  chantel.online  ");
        tvWebLink.setTextColor(Color.parseColor(COLOR_CHAMPAGNE));
        tvWebLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvWebLink.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tvWebLink.setBackground(makeRoundedRect(COLOR_PILL_BG, COLOR_CHAMPAGNE, dp(6), dp(1)));
        tvWebLink.setPadding(dp(14), dp(4), dp(14), dp(4));
        LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wLp.topMargin = dp(10);
        card.addView(tvWebLink, wLp);

        TextView tvScanHint = new TextView(this);
        tvScanHint.setText("Наведите камеру смартфона для подключения");
        tvScanHint.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        tvScanHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvScanHint.setGravity(Gravity.CENTER_HORIZONTAL);
        tvScanHint.setPadding(0, dp(4), 0, 0);
        card.addView(tvScanHint);

        return card;
    }

    private LinearLayout createBaseCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(makeRoundedRect(COLOR_CARD, COLOR_CARD_STROKE, dp(20), dp(1)));
        return card;
    }

    private TextView addMetricRow(LinearLayout parent, String label, String initialValue) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(makeRoundedRect(COLOR_PILL_BG, COLOR_PILL_STROKE, dp(8), dp(1)));
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rLp.bottomMargin = dp(8);
        row.setLayoutParams(rLp);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        row.addView(tvLabel, lLp);

        TextView tvVal = new TextView(this);
        tvVal.setText(initialValue);
        tvVal.setTextColor(Color.WHITE);
        tvVal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvVal.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(tvVal);

        parent.addView(row);
        return tvVal;
    }

    private void refreshStatusAsync() {
        mTvStatusPill.setText("● ПРОВЕРКА СВЯЗИ...");
        mTvStatusPill.setTextColor(Color.parseColor(COLOR_AMBER));
        mTvStatusPill.setBackground(makeRoundedRect(COLOR_AMBER_BG, COLOR_AMBER, dp(12), dp(2)));
        mBtnCheckStatus.setEnabled(false);

        new Thread(() -> {
            CellularController.NetStatus status = CellularController.checkLiveStatus();
            mHandler.post(() -> {
                mBtnCheckStatus.setEnabled(true);

                if (status.isConnected) {
                    mTvStatusPill.setText("● ИНТЕРНЕТ РАБОТАЕТ");
                    mTvStatusPill.setTextColor(Color.parseColor(COLOR_EMERALD));
                    mTvStatusPill.setBackground(makeRoundedRect(COLOR_EMERALD_BG, COLOR_EMERALD, dp(12), dp(2)));

                    mTvSimVal.setText("Активна (" + status.ipAddress + ")");
                    mTvSimVal.setTextColor(Color.parseColor(COLOR_EMERALD));

                    mTvAccessVal.setText("Все сервисы доступны");
                    mTvAccessVal.setTextColor(Color.parseColor(COLOR_EMERALD));

                    if (status.pingMs >= 0 && status.pingMs < 100) {
                        mTvQualityVal.setText("Отличное (" + status.pingMs + " мс)");
                        mTvQualityVal.setTextColor(Color.parseColor(COLOR_EMERALD));
                    } else if (status.pingMs >= 100) {
                        mTvQualityVal.setText("Хорошее (" + status.pingMs + " мс)");
                        mTvQualityVal.setTextColor(Color.WHITE);
                    } else {
                        mTvQualityVal.setText("Соединение стабильно");
                        mTvQualityVal.setTextColor(Color.WHITE);
                    }
                } else {
                    mTvStatusPill.setText("● НЕТ ПОДКЛЮЧЕНИЯ К СЕТИ");
                    mTvStatusPill.setTextColor(Color.parseColor(COLOR_RUBY));
                    mTvStatusPill.setBackground(makeRoundedRect(COLOR_RUBY_BG, COLOR_RUBY, dp(12), dp(2)));

                    if (status.ipAddress != null && !status.ipAddress.equals("Не назначен")) {
                        mTvSimVal.setText("Вставлена (требует APN)");
                        mTvSimVal.setTextColor(Color.parseColor(COLOR_AMBER));
                    } else {
                        mTvSimVal.setText("Нет сигнала / не вставлена");
                        mTvSimVal.setTextColor(Color.parseColor(COLOR_RUBY));
                    }

                    mTvAccessVal.setText("Нет выхода в интернет");
                    mTvAccessVal.setTextColor(Color.parseColor(COLOR_RUBY));

                    mTvQualityVal.setText("Связь отсутствует");
                    mTvQualityVal.setTextColor(Color.parseColor(COLOR_RUBY));
                }

                mTvWatchdogVal.setText(mWatchdogEnabled ? "Включена (авто)" : "Выключена");
                mTvWatchdogVal.setTextColor(mWatchdogEnabled ? Color.parseColor(COLOR_EMERALD) : Color.parseColor(COLOR_TEXT_SECONDARY));
            });
        }).start();
    }

    private void runActivationFlow() {
        final String apn = mEtApn.getText().toString().trim();
        final String user = mEtUser.getText().toString().trim();
        final String pass = mEtPass.getText().toString().trim();

        if (apn.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, укажите точку доступа APN!", Toast.LENGTH_SHORT).show();
            return;
        }

        mPrefs.edit()
                .putString(CellularWatchdogService.KEY_APN, apn)
                .putString(CellularWatchdogService.KEY_USER, user)
                .putString(CellularWatchdogService.KEY_PASS, pass)
                .apply();

        mBtnActivate.setEnabled(false);
        mTvLog.setText("Настраиваем сотовый модем автомобиля...");
        mTvStatusPill.setText("● НАСТРОЙКА ИНТЕРНЕТА...");
        mTvStatusPill.setTextColor(Color.parseColor(COLOR_AMBER));
        mTvStatusPill.setBackground(makeRoundedRect(COLOR_AMBER_BG, COLOR_AMBER, dp(12), dp(2)));

        CellularController.activateAsync(apn, user, pass, 0, new CellularController.StatusCallback() {
            @Override
            public void onProgress(String message) {
                mTvLog.setText(message);
            }

            @Override
            public void onFinished(boolean success, String message, String ip, long pingMs) {
                mBtnActivate.setEnabled(true);
                mTvLog.setText(message);
                refreshStatusAsync();
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                if (success && mWatchdogEnabled) {
                    startWatchdogService();
                }
            }
        });
    }

    private void toggleWatchdog() {
        mWatchdogEnabled = !mWatchdogEnabled;
        mPrefs.edit().putBoolean(CellularWatchdogService.KEY_WATCHDOG_ENABLED, mWatchdogEnabled).apply();
        updateWatchdogButtonUi();

        if (mWatchdogEnabled) {
            startWatchdogService();
            Toast.makeText(this, "Авто-удержание интернета включено", Toast.LENGTH_SHORT).show();
        } else {
            stopWatchdogService();
            Toast.makeText(this, "Авто-удержание интернета выключено", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWatchdogButtonUi() {
        if (mWatchdogEnabled) {
            mBtnWatchdogToggle.setText("🛡 АВТО-УДЕРЖАНИЕ: ВКЛ");
            mBtnWatchdogToggle.setTextColor(Color.parseColor(COLOR_EMERALD));
            mBtnWatchdogToggle.setBackground(makeRoundedRect(COLOR_EMERALD_BG, COLOR_EMERALD, dp(14), dp(2)));
        } else {
            mBtnWatchdogToggle.setText("⚪️ АВТО-УДЕРЖАНИЕ: ВЫКЛ");
            mBtnWatchdogToggle.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
            mBtnWatchdogToggle.setBackground(makeRoundedRect(COLOR_PILL_BG, COLOR_PILL_STROKE, dp(14), dp(1)));
        }
        mBtnWatchdogToggle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mBtnWatchdogToggle.setTypeface(Typeface.DEFAULT_BOLD);
        mBtnWatchdogToggle.setPadding(dp(18), dp(10), dp(18), dp(10));
    }

    private void startWatchdogService() {
        try {
            Intent intent = new Intent(this, CellularWatchdogService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } catch (Throwable t) {
            // Ignore
        }
    }

    private void stopWatchdogService() {
        try {
            Intent intent = new Intent(this, CellularWatchdogService.class);
            intent.putExtra("stop", true);
            startService(intent);
        } catch (Throwable t) {
            // Ignore
        }
    }

    private GradientDrawable makeRoundedRect(String bgColor, String strokeColor, int radiusPx, int strokeWidthPx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(bgColor));
        gd.setCornerRadius(radiusPx);
        gd.setStroke(strokeWidthPx, Color.parseColor(strokeColor));
        return gd;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    /**
     * Custom Spinner Adapter ensuring high-contrast white text on dark background
     */
    private static class StyledSpinnerAdapter<T> extends ArrayAdapter<T> {
        public StyledSpinnerAdapter(Context context, List<T> objects) {
            super(context, android.R.layout.simple_spinner_item, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setPadding(14, 10, 14, 10);
            }
            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View v = super.getDropDownView(position, convertView, parent);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.parseColor("#151A23"));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tv.setPadding(18, 14, 18, 14);
            }
            return v;
        }
    }
}
