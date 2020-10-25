package sonar.fluxnetworks.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import sonar.fluxnetworks.api.misc.FeedbackInfo;
import sonar.fluxnetworks.api.misc.FluxConstants;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import sonar.fluxnetworks.common.connection.BasicFluxNetwork;
import sonar.fluxnetworks.common.connection.FluxNetworkInvalid;
import sonar.fluxnetworks.common.misc.FluxUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class FluxClientCache {

    private static final Int2ObjectMap<IFluxNetwork> NETWORKS = new Int2ObjectOpenHashMap<>();

    public static boolean superAdmin = false;
    public static boolean detailedNetworkView = false;

    public static IFluxNetwork adminViewingNetwork = FluxNetworkInvalid.INSTANCE;

    private static FeedbackInfo feedback = FeedbackInfo.NONE; // Text message.
    private static FeedbackInfo feedbackAction = FeedbackInfo.NONE; // Special operation.

    private static int feedbackTimer = 0;

    public static void release() {
        NETWORKS.clear();
    }

    public static void updateNetworks(@Nonnull Int2ObjectMap<CompoundNBT> serverSideNetworks, int type) {
        for (Int2ObjectMap.Entry<CompoundNBT> entry : serverSideNetworks.int2ObjectEntrySet()) {
            int id = entry.getIntKey();
            CompoundNBT nbt = entry.getValue();
            IFluxNetwork network = NETWORKS.get(id);
            if (type == FluxConstants.TYPE_NET_DELETE) {
                if (network != null) {
                    NETWORKS.remove(id);
                }
            } else {
                if (network == null) {
                    network = new BasicFluxNetwork();
                    network.readCustomNBT(nbt, type);
                    NETWORKS.put(id, network);
                } else {
                    network.readCustomNBT(nbt, type);
                }
            }
        }
    }

    public static void updateConnections(int networkID, List<CompoundNBT> tags) {
        IFluxNetwork network = NETWORKS.get(networkID);
        if (network != null) {
            for (CompoundNBT tag : tags) {
                GlobalPos globalPos = FluxUtils.readGlobalPos(tag);
                network.getConnectionByPos(globalPos).ifPresent(c -> c.readCustomNBT(tag, FluxConstants.TYPE_CONNECTION_UPDATE));
            }
        }
    }

    @Nonnull
    public static IFluxNetwork getNetwork(int id) {
        return NETWORKS.getOrDefault(id, FluxNetworkInvalid.INSTANCE);
    }

    public static String getDisplayName(int networkID) {
        IFluxNetwork network = getNetwork(networkID);
        if (network.isValid()) {
            return network.getNetworkName();
        }
        return "NONE";
    }

    public static FeedbackInfo getFeedback(boolean action) {
        return action ? feedbackAction : feedback;
    }

    public static void setFeedback(FeedbackInfo info, boolean action) {
        if (action) {
            feedbackAction = info;
        } else {
            feedback = info;
        }
        feedbackTimer = 0;
    }

    public static void tick() {
        if (feedback.isValid()) {
            feedbackTimer++;
            if (feedbackTimer >= 60) {
                feedbackTimer = 0;
                setFeedback(FeedbackInfo.NONE, false);
            }
        }
    }

    @Nonnull
    public static List<IFluxNetwork> getAllNetworks() {
        return new ArrayList<>(NETWORKS.values());
    }
}
