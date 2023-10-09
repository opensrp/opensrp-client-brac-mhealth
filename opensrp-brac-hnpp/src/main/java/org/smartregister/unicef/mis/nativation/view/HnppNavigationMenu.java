package org.smartregister.unicef.mis.nativation.view;


public class HnppNavigationMenu  implements NavigationMenu.Flavour {

    @Override
    public String[] getSupportedLanguages() {
        return new String[]{"Bangla", "English"};
    }
}
