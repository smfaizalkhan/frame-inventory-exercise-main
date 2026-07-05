package com.global.ct.frameinventory.domain;

import java.util.List;

public class ExternalRefs {

    private String broadsignId;
    private String pricingRef;
    private List<String> linkedFrameIds;

    public String getBroadsignId() {
        return broadsignId;
    }

    public void setBroadsignId(String broadsignId) {
        this.broadsignId = broadsignId;
    }

    public String getPricingRef() {
        return pricingRef;
    }

    public void setPricingRef(String pricingRef) {
        this.pricingRef = pricingRef;
    }

    public List<String> getLinkedFrameIds() {
        return linkedFrameIds;
    }

    public void setLinkedFrameIds(List<String> linkedFrameIds) {
        this.linkedFrameIds = linkedFrameIds;
    }
}
