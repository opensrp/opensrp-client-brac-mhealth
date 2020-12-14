package org.smartregister.brac.hnpp.model;

public class Migration {
    int baseEntityId;
    String name;
    String age;
    String gender;

    public int getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(int baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
