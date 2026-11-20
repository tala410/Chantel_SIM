package online.chantel.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OperatorPresets {

    public static class Operator {
        public final String name;
        public final String apn;
        public final String user;
        public final String pass;
        public final int pdpType; // 0 = IPV4, 1 = IPV6, 2 = IPV4V6
        public final String hint;

        public Operator(String name, String apn, String user, String pass, int pdpType, String hint) {
            this.name = name;
            this.apn = apn;
            this.user = user;
            this.pass = pass;
            this.pdpType = pdpType;
            this.hint = hint;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final Map<String, List<Operator>> PRESETS = new LinkedHashMap<>();

    static {
        // --- Кыргызстан ---
        List<Operator> kg = new ArrayList<>();
        kg.add(new Operator("MEGA (MegaCom)", "internet", "", "", 0, "Точка доступа: internet (IPv4)"));
        kg.add(new Operator("O! (NUR Telecom)", "internet", "", "", 0, "Точка доступа: internet (IPv4)"));
        kg.add(new Operator("Beeline KG", "internet.beeline.kg", "", "", 0, "Точка доступа: internet.beeline.kg"));
        PRESETS.put("🇰🇬 Кыргызстан", kg);

        // --- Россия ---
        List<Operator> ru = new ArrayList<>();
        ru.add(new Operator("МТС (MTS)", "internet.mts.ru", "mts", "mts", 0, "Точка доступа: internet.mts.ru"));
        ru.add(new Operator("МегаФон (MegaFon)", "internet", "gdata", "gdata", 0, "Точка доступа: internet"));
        ru.add(new Operator("Билайн (Beeline)", "internet.beeline.ru", "beeline", "beeline", 0, "Точка доступа: internet.beeline.ru"));
        ru.add(new Operator("Tele2 Россия", "internet.tele2.ru", "", "", 0, "Точка доступа: internet.tele2.ru"));
        ru.add(new Operator("Т-Мобайл (Tinkoff)", "m.tinkoff", "", "", 0, "Точка доступа: m.tinkoff"));
        ru.add(new Operator("СберМобайл", "sberbank.ru", "", "", 0, "Точка доступа: sberbank.ru"));
        ru.add(new Operator("Yota", "internet.yota", "", "", 0, "Точка доступа: internet.yota"));
        ru.add(new Operator("Мотив", "inet.ycc.ru", "motiv", "motiv", 0, "Точка доступа: inet.ycc.ru"));
        PRESETS.put("🇷🇺 Россия", ru);

        // --- Казахстан ---
        List<Operator> kz = new ArrayList<>();
        kz.add(new Operator("Beeline KZ", "internet.beeline.kz", "", "", 0, "Точка доступа: internet.beeline.kz"));
        kz.add(new Operator("Kcell / Activ", "internet", "", "", 0, "Точка доступа: internet"));
        kz.add(new Operator("Tele2 KZ", "internet.tele2.kz", "", "", 0, "Точка доступа: internet.tele2.kz"));
        kz.add(new Operator("Altel 4G", "internet.altel.kz", "", "", 0, "Точка доступа: internet.altel.kz"));
        kz.add(new Operator("Izi Mobile", "internet.izi.me", "", "", 0, "Точка доступа: internet.izi.me"));
        PRESETS.put("🇰🇿 Казахстан", kz);

        // --- Узбекистан ---
        List<Operator> uz = new ArrayList<>();
        uz.add(new Operator("Ucell", "internet", "", "", 0, "Точка доступа: internet"));
        uz.add(new Operator("Beeline UZ", "internet.beeline.uz", "", "", 0, "Точка доступа: internet.beeline.uz"));
        uz.add(new Operator("Mobiuz (UMS)", "internet", "", "", 0, "Точка доступа: internet"));
        uz.add(new Operator("Uztelecom (Uzmobile)", "internet.uzmobile.uz", "", "", 0, "Точка доступа: internet.uzmobile.uz"));
        PRESETS.put("🇺🇿 Узбекистан", uz);

        // --- Беларусь ---
        List<Operator> by = new ArrayList<>();
        by.add(new Operator("A1 Беларусь", "vmi.velcom.by", "", "", 0, "Точка доступа: vmi.velcom.by"));
        by.add(new Operator("МТС Беларусь", "mts", "mts", "mts", 0, "Точка доступа: mts"));
        by.add(new Operator("life:) Беларусь", "internet", "", "", 0, "Точка доступа: internet"));
        PRESETS.put("🇧🇾 Беларусь", by);

        // --- Пользовательский ---
        List<Operator> custom = new ArrayList<>();
        custom.add(new Operator("Свой APN (Ручной ввод)", "internet", "", "", 0, "Ручной ввод точки доступа"));
        PRESETS.put("⚙️ Другой оператор", custom);
    }

    public static List<String> getCountries() {
        return new ArrayList<>(PRESETS.keySet());
    }

    public static List<Operator> getOperators(String country) {
        List<Operator> list = PRESETS.get(country);
        return list != null ? list : Collections.emptyList();
    }
}
