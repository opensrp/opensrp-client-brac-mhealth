package org.smartregister.unicef.mis.imci;

public class Utility {
    public enum ASSESSMENT_RESULT_TYPE_SEVERE {
        TWO("সম্ভাব্য মারাত্মক ব্যাকটেরিয়াল সংক্রমণ অথবা খুব মারাত্মক রোগ - খুব মারাত্মক সংক্রমণ (VSD-PSBI)"),
        THREE("সম্ভাব্য মারাত্মক ব্যাকটেরিয়াল সংক্রমণ অথবা খুব মারাত্মক রোগ - খুব মারাত্মক সংক্রমণ (VSD-PSBI)"),
        FOUR("সম্ভাব্য মারাত্মক ব্যাকটেরিয়াল সংক্রমণ অথবা খুব মারাত্মক রোগ- দ্রুত শ্বাস নিউমোনিয়া (০-৬ দিন বয়সের জন্য)"),
        FIVE("খুব মারাত্মক রোগ- দ্রুত শ্বাস নিউমোনিয়া (৭-৫৯ দিন বয়সের জন্য)"),
        SIX("স্থানীয় ব্যাকটেরিয়াল সংক্রমণ"),
        SEVEN("মারাত্মক রোগ অথবা স্থানীয় সংক্রমণ নেই");
        String value;
        ASSESSMENT_RESULT_TYPE_SEVERE(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_DIARRHEA {
        ONE("পানি স্বল্পতা নাই"),
        TWO("কিছু পানি স্বল্পতা"),
        THREE("চরম পানি স্বল্পতা");
        String value;
        ASSESSMENT_RESULT_TYPE_DIARRHEA(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_FEEDING {
        ONE("খাওয়ানোর সমস্যা নাই"),
        TWO("খাওয়ানোর সমস্যা অথবা কম ওজন"),
        THREE("বয়সের অনুপাতে খুব কম ওজন");
        String value;
        ASSESSMENT_RESULT_TYPE_FEEDING(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_DANGER_SIGN {
        ONE("খুব মারাত্মক রোগ নেই"),
        TWO("খুব মারাত্মক রোগ");
        String value;
        ASSESSMENT_RESULT_TYPE_DANGER_SIGN(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_PNEUMONIA {
        ONE("স্বাভাবিক"),
        TWO("কাশি অথবা সর্দি"),
        THREE("নিউমোনিয়া"),
        FOUR("মারাত্মক নিউমোনিয়া");
        String value;
        ASSESSMENT_RESULT_TYPE_PNEUMONIA(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59 {
        ONE("পানি স্বল্পতা নাই"),
        TWO("আমাশয়"),
        THREE("দীর্ঘ মেয়াদী ডায়রিয়া"),
        FOUR("কিছু পানি স্বল্পতা"),
        FIVE("চরম পানি স্বল্পতা"),
        SIX("মারাত্মক দীর্ঘ মেয়াদী ডায়রিয়া");
        String value;
        ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_FEVER {
        ONE("জ্বর নেই"),
        TWO("জ্বর"),
        THREE("জ্বর-ম্যালেরিয়া নয়"),
        FOUR("ম্যালেরিয়া"),
        FIVE("খুব মারাত্মক জ্বর জনিত রোগ");
        String value;
        ASSESSMENT_RESULT_TYPE_FEVER(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_MALNUTRITION {
        ONE("তীব্র অপুষ্টি নেই"),
        TWO("মাঝারী তীব্র অপুষ্টি"),
        THREE("জটিলতাবিহীন মারাত্মক তীব্র অপুষ্টি"),
        FOUR("জটিল মারাত্মক তীব্র অপুষ্টি");
        String value;
        ASSESSMENT_RESULT_TYPE_MALNUTRITION(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_ANEMIA {
        ONE("রক্ত স্বল্পতা নেই"),
        TWO("রক্ত স্বল্পতা"),
        THREE("মারাত্মক রক্ত স্বল্পতা");
        String value;
        ASSESSMENT_RESULT_TYPE_ANEMIA(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
}
