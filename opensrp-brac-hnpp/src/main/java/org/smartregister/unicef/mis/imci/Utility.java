package org.smartregister.unicef.mis.imci;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;

public class Utility {

    public enum ASSESSMENT_RESULT_TYPE_SEVERE {
        TWO(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_SEVERE_TWO)),
        THREE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_SEVERE_THREE)),
        FOUR(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_SEVERE_FOUR)),
        FIVE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_SEVERE_FIVE)),
        SIX(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_SEVERE_SIX)),
        SEVEN(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_SEVERE_SEVEN));
        String value;
        ASSESSMENT_RESULT_TYPE_SEVERE(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_DIARRHEA {
        ONE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_DIARRHEA_ONE)),
        TWO(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_DIARRHEA_TWO)),
        THREE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_DIARRHEA_THREE));
        String value;
        ASSESSMENT_RESULT_TYPE_DIARRHEA(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_FEEDING {
        ONE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_FEEDING_ONE)),
        TWO(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_FEEDING_TWO)),
        THREE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_FEEDING_THREE));
        String value;
        ASSESSMENT_RESULT_TYPE_FEEDING(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_DANGER_SIGN {
        ONE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_DANGER_SIGN_ONE)),
        TWO(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_DANGER_SIGN_TWO));
        String value;
        ASSESSMENT_RESULT_TYPE_DANGER_SIGN(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_PNEUMONIA {
        ONE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_PNEUMONIA_ONE)),
        TWO(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_PNEUMONIA_TWO)),
        THREE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_PNEUMONIA_THREE)),
        FOUR(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_PNEUMONIA_FOUR));
        String value;
        ASSESSMENT_RESULT_TYPE_PNEUMONIA(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59 {
        ONE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59_ONE)),
        TWO(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59_TWO)),
        THREE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59_THREE)),
        FOUR(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59_FOUR)),
        FIVE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59_FIVE)),
        SIX(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59_SIX));
        String value;
        ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_FEVER {
        ONE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_FEVER_ONE)),
        TWO(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_FEVER_TWO)),
        THREE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_FEVER_THREE)),
        FOUR(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_FEVER_FOUR)),
        FIVE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_FEVER_FIVE));
        String value;
        ASSESSMENT_RESULT_TYPE_FEVER(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_MALNUTRITION {
        ONE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_MALNUTRITION_ONE)),
        TWO(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_MALNUTRITION_TWO)),
        THREE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_MALNUTRITION_THREE)),
        FOUR(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_MALNUTRITION_FOUR));
        String value;
        ASSESSMENT_RESULT_TYPE_MALNUTRITION(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    public enum ASSESSMENT_RESULT_TYPE_ANEMIA {
        ONE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_ANEMIA_ONE)),
        TWO(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_ANEMIA_TWO)),
        THREE(HnppApplication.appContext.getString(R.string.ASSESSMENT_RESULT_TYPE_ANEMIA_THREE));
        String value;
        ASSESSMENT_RESULT_TYPE_ANEMIA(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
}
