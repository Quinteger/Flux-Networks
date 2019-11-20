package fluxnetworks.api.utils;

import net.minecraft.nbt.CompoundNBT;

public interface INetworkNBT {

    void readNetworkNBT(CompoundNBT nbt, NBTType type);

    CompoundNBT writeNetworkNBT(CompoundNBT nbt, NBTType type);
}
