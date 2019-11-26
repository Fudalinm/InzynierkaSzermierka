package model;

import javafx.beans.property.SimpleStringProperty;

public class ParticipantResult {

    private SimpleStringProperty triathlonOpen = new SimpleStringProperty("Uninitialized");
    private SimpleStringProperty triathlonWomen = new SimpleStringProperty("Uninitialized");

    public String getTriathlonOpen() {
        return triathlonOpen.get();
    }

    public SimpleStringProperty triathlonOpenProperty() {
        return triathlonOpen;
    }

    public String getTriathlonWomen() {
        return triathlonWomen.get();
    }

    public SimpleStringProperty triathlonWomenProperty() {
        return triathlonWomen;
    }

}
