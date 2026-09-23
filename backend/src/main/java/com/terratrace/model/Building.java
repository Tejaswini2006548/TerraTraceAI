package com.terratrace.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "buildings")
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String featureType;

    @Column(nullable = false)
    private Double areaPixels;

    @Column(nullable = false)
    private Double mapArea;

    @Column(nullable = false)
    private Double perimeter;

    @Column(columnDefinition = "geometry(Geometry,4326)")
private Geometry geometry;

    @Column(nullable = false)
    private String verificationStatus = "PENDING";

    @Column
    private String verificationNote;

    public Building() {
    }

    public Long getId() {
        return id;
    }

    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(
        String featureType
    ) {
        this.featureType = featureType;
    }

    public Double getAreaPixels() {
        return areaPixels;
    }

    public void setAreaPixels(
        Double areaPixels
    ) {
        this.areaPixels = areaPixels;
    }

    public Double getMapArea() {
        return mapArea;
    }

    public void setMapArea(
        Double mapArea
    ) {
        this.mapArea = mapArea;
    }

    public Double getPerimeter() {
        return perimeter;
    }

    public void setPerimeter(
        Double perimeter
    ) {
        this.perimeter = perimeter;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(
        Geometry geometry
    ) {
        this.geometry = geometry;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(
        String verificationStatus
    ) {
        this.verificationStatus =
            verificationStatus;
    }

    public String getVerificationNote() {
        return verificationNote;
    }

    public void setVerificationNote(
        String verificationNote
    ) {
        this.verificationNote =
            verificationNote;
    }
}