package com.global.ct.frameinventory.domain;

public class Commercials {

    private Double impactWeight;
    private String pricingGrade;
    private String productionRateCard;
    private Boolean premium = false;

    public Double getImpactWeight() {
        return impactWeight;
    }

    public void setImpactWeight(Double impactWeight) {
        this.impactWeight = impactWeight;
    }

    public String getPricingGrade() {
        return pricingGrade;
    }

    public void setPricingGrade(String pricingGrade) {
        this.pricingGrade = pricingGrade;
    }

    public String getProductionRateCard() {
        return productionRateCard;
    }

    public void setProductionRateCard(String productionRateCard) {
        this.productionRateCard = productionRateCard;
    }

    public Boolean getPremium() {
        return premium;
    }

    public void setPremium(Boolean premium) {
        this.premium = premium;
    }
}
