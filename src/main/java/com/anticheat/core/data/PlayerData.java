package com.anticheat.core.data;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Bir oyuncunun anti-cheat kontrolleri icin tuttugu gecici veri.
 * Oyuncu ayrilinca PlayerDataManager tarafindan silinir.
 */
public class PlayerData {

    // --- Hareket (Speed/Fly/NoFall) ---
    private Location lastLocation;
    private double lastY;
    private int airTicks;
    private double fallDistance;
    private int moveTickCounter;

    // --- AutoClicker (CPS) ---
    private int clickCount;

    // --- Killaura ---
    private UUID lastTarget;
    private long lastHitTime;

    // --- NoSwing (ProtocolLib) ---
    private long lastSwingTime;

    // --- Timer (ProtocolLib) ---
    private int packetCount;

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public double getLastY() {
        return lastY;
    }

    public void setLastY(double lastY) {
        this.lastY = lastY;
    }

    public int getAirTicks() {
        return airTicks;
    }

    public void setAirTicks(int airTicks) {
        this.airTicks = airTicks;
    }

    public void incrementAirTicks() {
        this.airTicks++;
    }

    public double getFallDistance() {
        return fallDistance;
    }

    public void setFallDistance(double fallDistance) {
        this.fallDistance = fallDistance;
    }

    public void addFallDistance(double amount) {
        this.fallDistance += amount;
    }

    public int incrementAndGetMoveTickCounter() {
        return ++this.moveTickCounter;
    }

    public void setMoveTickCounter(int moveTickCounter) {
        this.moveTickCounter = moveTickCounter;
    }

    public void incrementClickCount() {
        this.clickCount++;
    }

    public int getAndResetClickCount() {
        int c = this.clickCount;
        this.clickCount = 0;
        return c;
    }

    public UUID getLastTarget() {
        return lastTarget;
    }

    public void setLastTarget(UUID lastTarget) {
        this.lastTarget = lastTarget;
    }

    public long getLastHitTime() {
        return lastHitTime;
    }

    public void setLastHitTime(long lastHitTime) {
        this.lastHitTime = lastHitTime;
    }

    public long getLastSwingTime() {
        return lastSwingTime;
    }

    public void setLastSwingTime(long lastSwingTime) {
        this.lastSwingTime = lastSwingTime;
    }

    public void incrementPacketCount() {
        this.packetCount++;
    }

    public int getAndResetPacketCount() {
        int c = this.packetCount;
        this.packetCount = 0;
        return c;
    }
}
