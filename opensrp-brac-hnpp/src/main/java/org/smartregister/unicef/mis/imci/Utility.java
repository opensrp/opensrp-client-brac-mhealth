package org.smartregister.unicef.mis.imci;

public class Utility {
    public enum ASSESSMENT_RESULT_TYPE_SEVERE {
        ONE("স্বাভাবিক"),
        TWO("সম্ভাব্য মারাত্মক ব্যাকটেরিয়াল সংক্রমণ অথবা খুব মারাত্মক রোগসঙ্কটাপন্ন অসুস্থতা (VSD-CI)"),
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
}
