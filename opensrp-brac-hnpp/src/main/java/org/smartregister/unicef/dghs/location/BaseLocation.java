package org.smartregister.unicef.dghs.location;

import java.io.Serializable;

public class BaseLocation implements Serializable {
    public String name = "";
    public int id;
    public String code;

    @Override
    public String toString() {
        return "BaseLocation{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", code='" + code + '\'' +
                '}';
    }
}
