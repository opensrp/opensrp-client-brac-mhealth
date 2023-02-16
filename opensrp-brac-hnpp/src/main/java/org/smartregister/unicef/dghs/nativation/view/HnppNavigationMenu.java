package org.smartregister.unicef.dghs.nativation.view;


public class HnppNavigationMenu  implements NavigationMenu.Flavour {

    @Override
    public String[] getSupportedLanguages() {
        return new String[]{"Bangla", "English"};
    }
}
