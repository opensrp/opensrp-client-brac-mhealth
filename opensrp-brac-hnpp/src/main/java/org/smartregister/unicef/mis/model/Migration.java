package org.smartregister.unicef.mis.model;

import org.smartregister.clientandeventmodel.Address;

import java.util.ArrayList;

public class Migration {
    public String baseEntityId;
    public String firstName;
    public String birthdate;
    public String gender;
    public ArrayList<Address> addresses;
    public String cityVillage;
    public HHAttribute attributes;

    public class HHAttribute{
        public String BLOCK_NAME;
        public String Number_of_HH_Member;
        public String Mobile_Number;
        public String HOH_Phone_Number;
        public String nationalId;
        public String birthRegistrationID;

    }

}
