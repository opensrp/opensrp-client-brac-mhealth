package org.smartregister.unicef.mis.model;

import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;

import java.util.ArrayList;

public class GlobalSearchResult {
    public ArrayList<Client> clients = new ArrayList<>();
    public int no_of_events;
    public ArrayList<Event> events = new ArrayList<>();
}
