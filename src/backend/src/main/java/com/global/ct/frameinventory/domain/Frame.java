package com.global.ct.frameinventory.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "frames")
public class Frame {

    @Id
    private String id;

    @Indexed(unique = true)
    private String frameReference;

    private Location location;
    private Site site;
    private FrameFormat format;
    private Commercials commercials;
    private Lifecycle lifecycle;
    private ExternalRefs externalRefs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrameReference() {
        return frameReference;
    }

    public void setFrameReference(String frameReference) {
        this.frameReference = frameReference;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public FrameFormat getFormat() {
        return format;
    }

    public void setFormat(FrameFormat format) {
        this.format = format;
    }

    public Commercials getCommercials() {
        return commercials;
    }

    public void setCommercials(Commercials commercials) {
        this.commercials = commercials;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public ExternalRefs getExternalRefs() {
        return externalRefs;
    }

    public void setExternalRefs(ExternalRefs externalRefs) {
        this.externalRefs = externalRefs;
    }
}
