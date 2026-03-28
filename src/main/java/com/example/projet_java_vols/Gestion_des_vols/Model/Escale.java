package com.example.projet_java_vols.Gestion_des_vols.Model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public class Escale {
    private int idEscale ;
    private int ordre;



    private String numVol;
    private Aeroport aeroportArrivee;
    private Aeroport aeroportDepart;
    private Date dateArrivee;
    private Date dateDepart;
    private LocalTime heureArrivee;
    private LocalTime heureDepart;

    public Escale(int idEscale, int ordre, Aeroport aeroportArrivee, Aeroport aeroportDepart, Date dateArrivee, Date dateDepart, LocalTime heureArrivee, LocalTime heureDepart, String numVol) {
        this.idEscale = idEscale;
        this.ordre = ordre;
        this.aeroportArrivee = aeroportArrivee;
        this.aeroportDepart = aeroportDepart;
        this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
        this.heureArrivee = heureArrivee;
        this.heureDepart = heureDepart;
        this.numVol = numVol;
    }
    public void setNumVol(String numVol) {
        this.numVol = numVol;
    }
    public int getIdEscale() {
        return idEscale;
    }

    public int getOrdre() {
        return ordre;
    }

    public Aeroport getAeroportArrivee() {
        return aeroportArrivee;
    }

    public Aeroport getAeroportDepart() {
        return aeroportDepart;
    }

    public Date getDateArrivee() {
        return dateArrivee;
    }

    public Date getDateDepart() {
        return dateDepart;
    }

    public LocalTime getHeureArrivee() {
        return heureArrivee;
    }

    public LocalTime getHeureDepart() {
        return heureDepart;
    }

    public long calculerDureeMinutes() {
            return java.time.Duration.between(heureDepart, heureArrivee).toMinutes();}

    public void setIdEscale(int idEscale) {
        this.idEscale = idEscale;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public void setAeroportArrivee(Aeroport aeroportArrivee) {
        this.aeroportArrivee = aeroportArrivee;
    }

    public void setAeroportDepart(Aeroport aeroportDepart) {
        this.aeroportDepart = aeroportDepart;
    }

    public void setDateArrivee(Date dateArrivee) {
        this.dateArrivee = dateArrivee;
    }

    public void setDateDepart(Date dateDepart) {
        this.dateDepart = dateDepart;
    }

    public void setHeureArrivee(LocalTime heureArrivee) {
        this.heureArrivee = heureArrivee;
    }

    public void setHeureDepart(LocalTime heureDepart) {
        this.heureDepart = heureDepart;
    }

    public String getNumVol() {
        return numVol;
    }
    public static String formaterDuree(LocalDate dDep, LocalTime hDep, LocalDate dArr, LocalTime hArr) {
        if (dDep == null || hDep == null || dArr == null || hArr == null) return "--";
        java.time.LocalDateTime depart = java.time.LocalDateTime.of(dDep, hDep);
        java.time.LocalDateTime arrivee = java.time.LocalDateTime.of(dArr, hArr);
        java.time.Duration duration = java.time.Duration.between(depart, arrivee);

        if (duration.isNegative() || duration.isZero()) return "--";

        long totalMinutes = duration.toMinutes();
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" jour").append(days > 1 ? "s " : " ");
        if (hours > 0) sb.append(hours).append(" h ");
        if (minutes > 0 || (days == 0 && hours == 0)) sb.append(minutes).append(" min");
        return sb.toString().trim();
    }

}
