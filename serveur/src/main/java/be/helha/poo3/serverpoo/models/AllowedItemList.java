package be.helha.poo3.serverpoo.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllowedItemList {
    @JsonProperty("AllowedItems")
    protected List<String> allowedItems;

    public List<String> getAllowedItems() {
        return allowedItems;
    }

    public void setAllowedItems(List<String> allowedItems) {
        this.allowedItems = allowedItems;
    }
}
