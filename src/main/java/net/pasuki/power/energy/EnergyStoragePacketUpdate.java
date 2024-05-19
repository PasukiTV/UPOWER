package net.pasuki.power.energy;

/**
 * Used for EnergySyncS2CPacket
 */
public interface EnergyStoragePacketUpdate {
    void setEnergy(int energy);
    void setCapacity(int capacity);
}