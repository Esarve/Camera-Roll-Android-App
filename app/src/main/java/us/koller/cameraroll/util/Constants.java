package us.koller.cameraroll.util;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public enum THEMES {
        LIGHT("L"),
        DARK("D"),
        SYSTEM("S");

        private final String code;
        private static final Map<String, THEMES> valuesByCode;

        static {
            valuesByCode = new HashMap<>(values().length);
            for(THEMES value : values()) {
                valuesByCode.put(value.code, value);
            }
        }

        THEMES(String code) {
            this.code = code;
        }

        public static THEMES lookupByCode(String code) {
            return valuesByCode.get(code);
        }

        public String getCode() {
            return code;
        }
    }
}
