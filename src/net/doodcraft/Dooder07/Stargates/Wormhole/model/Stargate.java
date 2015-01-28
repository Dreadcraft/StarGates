package net.doodcraft.Dooder07.Stargates.Wormhole.model;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.logic.StargateUpdateRunnable;
import net.doodcraft.Dooder07.Stargates.Wormhole.logic.StargateUpdateRunnable.ActionToTake;
import net.doodcraft.Dooder07.Stargates.Wormhole.player.WormholePlayer;
import net.doodcraft.Dooder07.Stargates.Wormhole.player.WormholePlayerManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.WorldUtils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class Stargate {

    private byte loadedVersion = -1;
    private long gateId = -1;
    private String gateName = "";
    private String gateSourceName = null;
    private String gateOwner = null;
    private String lastUsedBy = null;
    private StargateNetwork gateNetwork;
    private StargateShape gateShape;
    private World gateWorld;
    private boolean gateActive = false;
    private boolean gateRecentlyActive = false;
    private BlockFace gateFacing;
    private boolean gateLightsActive = false;
    private boolean gateSignPowered;
    private boolean gateRedstonePowered;
    private Stargate gateTarget = null;
    private Stargate gateDialSignTarget;
    private long gateTempSignTarget = -1;
    private int gateDialSignIndex = 0;
    private long gateTempTargetId = -1;
    private String gateIrisDeactivationCode = "";
    private boolean gateIrisActive = false;
    private boolean gateIrisDefaultActive = false;
    private Sign gateDialSign;
    private Location gatePlayerTeleportLocation;
    private Location gateMinecartTeleportLocation;
    private Block gateDialLeverBlock;
    private Block gateIrisLeverBlock;
    private Block gateDialSignBlock;
    private Block gateRedstoneDialActivationBlock;
    private Block gateRedstoneSignActivationBlock;
    private Block gateRedstoneGateActivatedBlock;
    private Block gateNameBlockHolder;
    private int gateActivateTaskId;
    private int gateEstablishWormholeTaskId;
    private int gateShutdownTaskId;
    private int gateAfterShutdownTaskId;
    private int gateAnimationStep3D = 1;
    private int gateAnimationStep2D = 0;
    private boolean gateAnimationRemoving = false;
    private int gateLightingCurrentIteration = 0;
    private final ArrayList<Location> gateStructureBlocks = new ArrayList<Location>();
    private final ArrayList<Location> gatePortalBlocks = new ArrayList<Location>();
    private final ArrayList<ArrayList<Location>> gateLightBlocks = new ArrayList<ArrayList<Location>>();
    private final ArrayList<ArrayList<Location>> gateWooshBlocks = new ArrayList<ArrayList<Location>>();
    private final ArrayList<Block> gateAnimatedBlocks = new ArrayList<Block>();
    private final HashMap<Integer, Stargate> gateSignOrder = new HashMap<Integer, Stargate>();
    private boolean gateCustom = false;
    private Material gateCustomStructureMaterial = null;
    private Material gateCustomPortalMaterial = null;
    private Material gateCustomLightMaterial = null;
    private Material gateCustomIrisMaterial = null;
    private int gateCustomWooshTicks = -1;
    private int gateCustomLightTicks = -1;
    private int gateCustomWooshDepth = -1;
    private int gateCustomWooshDepthSquared = -1;
    private boolean gateChevronsLocked = false;
    private boolean gateEstablishedWormhole = false;
    
    public Stargate() {
    }

    public void animateOpening() {
        final Material wooshMaterial = isGateCustom()
                ? getGateCustomPortalMaterial()
                : getGateShape() != null
                ? getGateShape().getShapePortalMaterial()
                : Material.STATIONARY_WATER;
        final int wooshDepth = isGateCustom()
                ? getGateCustomWooshDepth()
                : getGateShape() != null
                ? getGateShape().getShapeWooshDepth()
                : 0;

        if ((getGateWooshBlocks() != null) && (getGateWooshBlocks().size() > 0)) {
            final ArrayList<Location> wooshBlockStep = getGateWooshBlocks().get(getGateAnimationStep3D());
            if (!isGateAnimationRemoving()) {
                if (wooshBlockStep != null) {
                    for (final Location l : wooshBlockStep) {
                        final Block b = getGateWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                        getGateAnimatedBlocks().add(b);
                        StargateManager.getOpeningAnimationBlocks().put(l, b);
                        b.setType(wooshMaterial);
                    }

                    SGLogger.prettyLog(Level.FINER, false, getGateName() + " Woosh Adding: " + getGateAnimationStep3D() + " Woosh Block Size: " + wooshBlockStep.size());
                }

                if (getGateWooshBlocks().size() == getGateAnimationStep3D() + 1) {
                    setGateAnimationRemoving(true);
                } else {
                    setGateAnimationStep3D(getGateAnimationStep3D() + 1);
                }
                StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH), isGateCustom()
                        ? getGateCustomWooshTicks()
                        : getGateShape() != null
                        ? getGateShape().getShapeWooshTicks()
                        : 2);
            } else {

                if (wooshBlockStep != null) {
                    for (final Location loc : wooshBlockStep) {
                        final Block b = getGateWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

                        StargateManager.getOpeningAnimationBlocks().remove(loc);
                        getGateAnimatedBlocks().remove(b);
                        if (!StargateManager.isBlockInGate(b)) {
                            b.setType(Material.AIR);
                        }
                    }
                    SGLogger.prettyLog(Level.FINER, false, getGateName() + " Woosh Removing: " + getGateAnimationStep3D() + " Woosh Block Size: " + wooshBlockStep.size());
                }

                if (getGateAnimationStep3D() == 1) {
                    setGateAnimationRemoving(false);
                    if (isGateLightsActive() && isGateActive()) {
                        fillGateInterior(wooshMaterial);
                    }
                } else {
                    setGateAnimationStep3D(getGateAnimationStep3D() - 1);
                    StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH), isGateCustom()
                            ? getGateCustomWooshTicks()
                            : getGateShape() != null
                            ? getGateShape().getShapeWooshTicks()
                            : 2);
                }
            }
        } else {
            if ((getGateAnimationStep2D() == 0) && (wooshDepth > 0)) {
                for (final Location block : getGatePortalBlocks()) {
                    final Block r = getGateWorld().getBlockAt(block.getBlockX(), block.getBlockY(), block.getBlockZ()).getRelative(getGateFacing());
                    r.setType(wooshMaterial);
                    getGateAnimatedBlocks().add(r);
                    StargateManager.getOpeningAnimationBlocks().put(r.getLocation(), r);
                }
                setGateAnimationStep2D(getGateAnimationStep2D() + 1);
                StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH), 4);
            } else if (getGateAnimationStep2D() < wooshDepth) {
                final int size = getGateAnimatedBlocks().size();
                final int start = getGatePortalBlocks().size();
                for (int i = (size - start); i < size; i++) {
                    final Block b = getGateAnimatedBlocks().get(i);
                    final Block r = b.getRelative(getGateFacing());
                    r.setType(wooshMaterial);
                    getGateAnimatedBlocks().add(r);
                    StargateManager.getOpeningAnimationBlocks().put(r.getLocation(), r);
                }
                setGateAnimationStep2D(getGateAnimationStep2D() + 1);
                if (getGateAnimationStep2D() == wooshDepth) {
                    StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH), 8);
                } else {
                    StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH), 4);
                }
            } else if (getGateAnimationStep2D() >= wooshDepth) {
                for (int i = 0; i < getGatePortalBlocks().size(); i++) {
                    final int index = getGateAnimatedBlocks().size() - 1;
                    if (index >= 0) {
                        final Block b = getGateAnimatedBlocks().get(index);
                        b.setType(Material.AIR);
                        getGateAnimatedBlocks().remove(index);
                        StargateManager.getOpeningAnimationBlocks().remove(b.getLocation());
                    }
                }
                if (getGateAnimationStep2D() < ((wooshDepth * 2) - 1)) {
                    setGateAnimationStep2D(getGateAnimationStep2D() + 1);
                    StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH), 3);
                } else {
                    setGateAnimationStep2D(0);
                    if (isGateActive()) {
                        fillGateInterior(wooshMaterial);
                    }
                }
            }
        }
    }

    void completeGate(final String name, final String idc) {
        setGateName(name);

        if (getGateNameBlockHolder() != null) {
            setupGateSign(true);
        }

        setIrisDeactivationCode(idc);

        if (isGateRedstonePowered()) {
            setupRedstoneGateActivatedLever(true);
            if (isGateSignPowered()) {
                setupRedstoneDialWire(true);
                setupRedstoneSignDialWire(true);
            }
        }
    }

    public void deleteGateBlocks() {
        for (final Location bc : getGateStructureBlocks()) {
            final Block b = getGateWorld().getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
            b.setType(Material.AIR);
        }
    }

    public void deletePortalBlocks() {
        for (final Location bc : getGatePortalBlocks()) {
            final Block b = getGateWorld().getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
            b.setType(Material.AIR);
        }
    }

    public void deleteTeleportSign() {
        if ((getGateDialSignBlock() != null) && (getGateDialSign() != null)) {
            final Block teleportSign = getGateDialSignBlock().getRelative(getGateFacing());
            teleportSign.setType(Material.AIR);
        }
    }

    private boolean dialStargate() {
        WorldUtils.scheduleChunkLoad(getGatePlayerTeleportLocation().getBlock());
        if (getGateShutdownTaskId() > 0) {
            StarGates.getScheduler().cancelTask(getGateShutdownTaskId());
        }
        
        if (getGateAfterShutdownTaskId() > 0) {
            StarGates.getScheduler().cancelTask(getGateAfterShutdownTaskId());
        }

        final int timeout = ConfigManager.getTimeoutShutdown() * 20;
        if (timeout > 0) {
            setGateShutdownTaskId(StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.SHUTDOWN), timeout));
            SGLogger.prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" ShutdownTaskID \"" + getGateShutdownTaskId() + "\" created.");
            if (getGateShutdownTaskId() == -1) {
                shutdownStargate(true);
                SGLogger.prettyLog(Level.SEVERE, false, "Failed to schedule wormhole shutdown timeout: " + timeout + " Received task id of -1. Wormhole forced closed NOW.");
            }
        }

        if ((getGateShutdownTaskId() > 0) || (timeout == 0)) {
            if (!isGateActive()) {
                setGateActive(true);
                toggleDialLeverState(false);
                toggleRedstoneGateActivatedPower();
                setGateRecentlyActive(false);
            }
            
            if (!isGateLightsActive()) {
                lightStargate(true);
            } else {
                SGLogger.prettyLog(Level.FINE, false, "Chevrons locked at gate: '" + this.getGateName() + "'");
                setGateChevronsLocked(true);
            }
            
            return true;
        } else {
            SGLogger.prettyLog(Level.WARNING, false, "No wormhole. No visual events.");
        }
        
        return false;
    }

    public boolean dialStargate(final Stargate target, final boolean force) {
        SGLogger.prettyLog(Level.FINER, false, "Dialing Stargate: '" + target.getGateName() + "'; force:='" + force + "'");
        if (getGateActivateTaskId() > 0) {
            SGLogger.prettyLog(Level.FINER, false, "Cancelling ActivateTaskID: " + getGateActivateTaskId() + " for gate '" + target.getGateName() + "'");
            StarGates.getScheduler().cancelTask(getGateActivateTaskId());
        }

        if (!target.isGateLightsActive() || force) {
            setGateTarget(target);

            if (getGateTarget() == null) {
                SGLogger.prettyLog(Level.WARNING, false, "Target lost! Closing local wormhole for safety percussions.");
                shutdownStargate(true);
                return false;
            }

            this.dialStargate();
            this.getGateTarget().dialStargate();

            this.getGateTarget().setSourceGateName(this.getGateName());

            this.establishWormhole();

            if ((isGateActive()) && (getGateTarget().isGateActive())) {
                return true;
            } else if ((isGateActive()) && (!getGateTarget().isGateActive())) {
                shutdownStargate(true);
                SGLogger.prettyLog(Level.WARNING, false, "Far wormhole failed to open. Closing local wormhole for safety sake.");
            } else if ((!isGateActive()) && (getGateTarget().isGateActive())) {
                target.shutdownStargate(true);
                SGLogger.prettyLog(Level.WARNING, false, "Local wormhole failed to open. Closing far end wormhole for safety sake.");
            }
        }

        return false;
    }
    
    @SuppressWarnings("deprecation")
	public void establishWormhole() {
        if (getGateEstablishWormholeTaskId() > 0) {
            SGLogger.prettyLog(Level.FINER, false, "Wormhole \"" + getGateName() + "\" EstablishWormholeTaskIdID \"" + getGateEstablishWormholeTaskId() + "\" cancelled.");
            StarGates.getScheduler().cancelTask(getGateEstablishWormholeTaskId());
        }

        if ((this.getGateTarget() == null) || (!this.isGateActive()))
            return;

        SGLogger.prettyLog(Level.FINER, false, "Trying to establish link between '" + this.getGateName() + "' and '" + this.getGateTarget().getGateName() +"'");
        if (this.getGateTarget().getGateChevronsLocked()) {
            this.setWormholeEstablished(true);
            this.getGateTarget().setWormholeEstablished(true);

            SGLogger.prettyLog(Level.FINER, false, "Chevrons locked on both sides. Starting thread ANIMATE_WOOSH.");

            StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ANIMATE_WOOSH));
            StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this.getGateTarget(), ActionToTake.ANIMATE_WOOSH));
        } else {
            SGLogger.prettyLog(Level.FINER, false, "Chevrons where not locked on both sides. Restarting thread.");
            setGateEstablishWormholeTaskId(StarGates.getScheduler().scheduleAsyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.ESTABLISH_WORMHOLE)));
        }
    }

    @SuppressWarnings("deprecation")
	public void fillGateInterior(Material mat) {
        fillGateInterior(mat.getId());
    }

    @SuppressWarnings("deprecation")
	public void fillGateInterior(int typeId) {
        for (Location loc : getGatePortalBlocks()) {
            final Block blk = getGateWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            blk.setTypeId(typeId, false);
            blk.getState().update();
        }
    }
    
    private int getGateEstablishWormholeTaskId() {
        return gateEstablishWormholeTaskId;
    }
    
    private int getGateActivateTaskId() {
        return gateActivateTaskId;
    }

    private int getGateAfterShutdownTaskId() {
        return gateAfterShutdownTaskId;
    }

    private ArrayList<Block> getGateAnimatedBlocks() {
        return gateAnimatedBlocks;
    }

    public int getGateAnimationStep2D() {
        return gateAnimationStep2D;
    }

    private int getGateAnimationStep3D() {
        return gateAnimationStep3D;
    }

    public Material getGateCustomIrisMaterial() {
        return gateCustomIrisMaterial;
    }

    public Material getGateCustomLightMaterial() {
        return gateCustomLightMaterial;
    }

    public int getGateCustomLightTicks() {
        return gateCustomLightTicks;
    }

    public Material getGateCustomPortalMaterial() {
        return gateCustomPortalMaterial;
    }

    public Material getGateCustomStructureMaterial() {
        return gateCustomStructureMaterial;
    }

    public int getGateCustomWooshDepth() {
        return gateCustomWooshDepth;
    }

    public int getGateCustomWooshDepthSquared() {
        return gateCustomWooshDepthSquared;
    }

    public int getGateCustomWooshTicks() {
        return gateCustomWooshTicks;
    }

    public Block getGateDialLeverBlock() {
        return gateDialLeverBlock;
    }

    public synchronized Sign getGateDialSign() {
        return gateDialSign;
    }

    public synchronized Block getGateDialSignBlock() {
        return gateDialSignBlock;
    }

    public synchronized int getGateDialSignIndex() {
        return gateDialSignIndex;
    }

    public Stargate getGateDialSignTarget() {
        return gateDialSignTarget;
    }

    public BlockFace getGateFacing() {
        return gateFacing;
    }

    public long getGateId() {
        return gateId;
    }

    public String getGateIrisDeactivationCode() {
        return gateIrisDeactivationCode;
    }

    public Block getGateIrisLeverBlock() {
        return gateIrisLeverBlock;
    }

    public ArrayList<ArrayList<Location>> getGateLightBlocks() {
        return gateLightBlocks;
    }

    private int getGateLightingCurrentIteration() {
        return gateLightingCurrentIteration;
    }

    public Location getGateMinecartTeleportLocation() {
        return gateMinecartTeleportLocation;
    }

    public String getGateName() {
        return gateName;
    }

    public Block getGateNameBlockHolder() {
        return gateNameBlockHolder;
    }

    public StargateNetwork getGateNetwork() {
        return gateNetwork;
    }

    public String getGateOwner() {
        return gateOwner;
    }

    public Location getGatePlayerTeleportLocation() {
        return gatePlayerTeleportLocation;
    }

    public ArrayList<Location> getGatePortalBlocks() {
        return gatePortalBlocks;
    }

    public Block getGateRedstoneDialActivationBlock() {
        return gateRedstoneDialActivationBlock;
    }

    public Block getGateRedstoneGateActivatedBlock() {
        return gateRedstoneGateActivatedBlock;
    }

    public Block getGateRedstoneSignActivationBlock() {
        return gateRedstoneSignActivationBlock;
    }

    public StargateShape getGateShape() {
        return gateShape;
    }

    private int getGateShutdownTaskId() {
        return gateShutdownTaskId;
    }

    private HashMap<Integer, Stargate> getGateSignOrder() {
        return gateSignOrder;
    }

    public ArrayList<Location> getGateStructureBlocks() {
        return gateStructureBlocks;
    }

    public Stargate getGateTarget() {
        return gateTarget;
    }

    long getGateTempSignTarget() {
        return gateTempSignTarget;
    }

    long getGateTempTargetId() {
        return gateTempTargetId;
    }

    public ArrayList<ArrayList<Location>> getGateWooshBlocks() {
        return gateWooshBlocks;
    }

    public World getGateWorld() {
        return gateWorld;
    }

    public byte getLoadedVersion() {
        return loadedVersion;
    }

    public boolean isGateActive() {
        return this.gateActive;
    }
    
    public boolean getGateChevronsLocked() {
        return this.gateChevronsLocked;
    }

    private boolean isGateAnimationRemoving() {
        return gateAnimationRemoving;
    }

    public boolean isGateCustom() {
        return gateCustom;
    }

    public boolean isGateIrisActive() {
        return gateIrisActive;
    }

    private boolean isGateIrisDefaultActive() {
        return gateIrisDefaultActive;
    }

    public boolean isGateLightsActive() {
        return gateLightsActive;
    }

    public boolean isGateRecentlyActive() {
        return gateRecentlyActive;
    }

    public boolean isGateRedstonePowered() {
        return gateRedstonePowered;
    }

    public boolean isGateSignPowered() {
        return gateSignPowered;
    }
    
    public boolean isWormholeEstablished() {
        return gateEstablishedWormhole;
    }
    
    public void setWormholeEstablished(boolean established) {
        this.gateEstablishedWormhole = established;
    }

    public void lightStargate(boolean on) {
        SGLogger.prettyLog(Level.FINE, false, "Lighting up '" + this.getGateName() + "'");

        if (on) {
            SGLogger.prettyLog(Level.FINER, false, "Lighting up Order: " + getGateLightingCurrentIteration());
            if (getGateLightingCurrentIteration() == 0) {
                this.setGateLightsActive(true);
                this.setGateChevronsLocked(false);
            } else if (!isGateLightsActive()) {
                this.lightStargate(false);
                this.setGateLightingCurrentIteration(0);
                return;
            }
            
            this.setGateLightingCurrentIteration(getGateLightingCurrentIteration() + 1);

            if (this.getGateLightBlocks() != null) {
                if ((this.getGateLightBlocks().size() > 0) && (this.getGateLightBlocks().get(this.getGateLightingCurrentIteration()) != null)) {
                    for (final Location l : this.getGateLightBlocks().get(this.getGateLightingCurrentIteration())) {
                        final Block b = this.getGateWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                        b.setType(this.isGateCustom()
                                ? this.getGateCustomLightMaterial()
                                : this.getGateShape() != null
                                ? this.getGateShape().getShapeLightMaterial()
                                : Material.GLOWSTONE);
                    }
                }

                if (this.getGateLightingCurrentIteration() >= this.getGateLightBlocks().size() - 1) {

                    this.setGateLightingCurrentIteration(0);
                    this.setGateChevronsLocked(true);

                    SGLogger.prettyLog(Level.FINE, false, "Locked Chevrons for gate '" + this.getGateName() + "'");
                } else {

                    StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.LIGHTUP), this.isGateCustom()
                            ? this.getGateCustomLightTicks()
                            : this.getGateShape() != null
                            ? this.getGateShape().getShapeLightTicks()
                            : 2);
                }
            }
        } else {
            SGLogger.prettyLog(Level.FINE, false, "Cleanup lighting process for gate: '" + this.getGateName() + "'");

            this.setGateLightsActive(false);
            this.setGateChevronsLocked(false);
            
            if (this.getGateLightBlocks() != null) {
                for (int i = 0; i < this.getGateLightBlocks().size(); i++) {
                    if (this.getGateLightBlocks().get(i) != null) {
                        for (final Location l : this.getGateLightBlocks().get(i)) {
                            final Block b = this.getGateWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                            b.setType(this.isGateCustom()
                                    ? this.getGateCustomStructureMaterial()
                                    : this.getGateShape() != null
                                    ? this.getGateShape().getShapeStructureMaterial()
                                    : Material.OBSIDIAN);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
	public void resetSign(final boolean teleportSign) {
        if (!teleportSign)
            return;

        getGateDialSignBlock().setTypeIdAndData(Material.WALL_SIGN.getId(), WorldUtils.getSignFacingByteFromBlockFace(getGateFacing()), false);
        setGateDialSign((Sign) getGateDialSignBlock().getState());
        getGateDialSign().setLine(0, getGateName());
        if (getGateNetwork() != null) {
            getGateDialSign().setLine(1, getGateNetwork().getNetworkName());
        } else {
            getGateDialSign().setLine(1, "");
        }
        getGateDialSign().setLine(2, "");
        getGateDialSign().setLine(3, "");
        getGateDialSign().update(true);
    }

    @SuppressWarnings("deprecation")
	public void resetTeleportSign() {
        if ((getGateDialSignBlock() != null) && (getGateDialSign() != null)) {
            getGateDialSignBlock().setTypeId(0);
            StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.DIAL_SIGN_RESET), 2);
        }
    }

    private void setGateActivateTaskId(final int gateActivateTaskId) {
        this.gateActivateTaskId = gateActivateTaskId;
    }
    
    private void setGateEstablishWormholeTaskId(int gateEstablishWormholeTaskId) {
        this.gateEstablishWormholeTaskId = gateEstablishWormholeTaskId;
    }

    public void setGateActive(final boolean gateActive) {
        this.gateActive = gateActive;
    }

    private void setGateAfterShutdownTaskId(final int gateAfterShutdownTaskId) {
        this.gateAfterShutdownTaskId = gateAfterShutdownTaskId;
    }

    private void setGateAnimationRemoving(final boolean gateAnimationRemoving) {
        this.gateAnimationRemoving = gateAnimationRemoving;
    }

    public void setGateAnimationStep2D(final int gateAnimationStep2D) {
        this.gateAnimationStep2D = gateAnimationStep2D;
    }

    private void setGateAnimationStep3D(final int gateAnimationStep3D) {
        this.gateAnimationStep3D = gateAnimationStep3D;
    }

    public void setGateCustom(final boolean gateCustom) {
        this.gateCustom = gateCustom;
    }

    public void setGateCustomIrisMaterial(final Material gateCustomIrisMaterial) {
        this.gateCustomIrisMaterial = gateCustomIrisMaterial;
    }

    public void setGateCustomLightMaterial(final Material gateCustomLightMaterial) {
        this.gateCustomLightMaterial = gateCustomLightMaterial;
    }

    public void setGateCustomLightTicks(final int gateCustomLightTicks) {
        this.gateCustomLightTicks = gateCustomLightTicks;
    }

    public void setGateCustomPortalMaterial(final Material gateCustomPortalMaterial) {
        this.gateCustomPortalMaterial = gateCustomPortalMaterial;
    }

    public void setGateCustomStructureMaterial(final Material gateCustomStructureMaterial) {
        this.gateCustomStructureMaterial = gateCustomStructureMaterial;
    }

    public void setGateCustomWooshDepth(final int gateCustomWooshDepth) {
        this.gateCustomWooshDepth = gateCustomWooshDepth;
    }

    public void setGateCustomWooshDepthSquared(final int gateCustomWooshDepthSquared) {
        this.gateCustomWooshDepthSquared = gateCustomWooshDepthSquared;
    }

    public void setGateCustomWooshTicks(final int gateCustomWooshTicks) {
        this.gateCustomWooshTicks = gateCustomWooshTicks;
    }

    public void setGateDialLeverBlock(final Block gateDialLeverBlock) {
        this.gateDialLeverBlock = gateDialLeverBlock;
    }

    public synchronized void setGateDialSign(final Sign gateDialSign) {
        this.gateDialSign = gateDialSign;
    }

    public synchronized void setGateDialSignBlock(final Block gateDialSignBlock) {
        this.gateDialSignBlock = gateDialSignBlock;
    }

    public synchronized void setGateDialSignIndex(final int gateDialSignIndex) {
        this.gateDialSignIndex = gateDialSignIndex;
    }

    protected void setGateDialSignTarget(final Stargate gateDialSignTarget) {
        this.gateDialSignTarget = gateDialSignTarget;
    }

    public void setGateFacing(final BlockFace gateFacing) {
        this.gateFacing = gateFacing;
    }

    void setGateId(final long gateId) {
        this.gateId = gateId;
    }

    public void setGateIrisActive(final boolean gateIrisActive) {
        this.gateIrisActive = gateIrisActive;
    }

    public void setGateIrisDeactivationCode(final String gateIrisDeactivationCode) {
        this.gateIrisDeactivationCode = gateIrisDeactivationCode;
    }

    public void setGateIrisDefaultActive(final boolean gateIrisDefaultActive) {
        this.gateIrisDefaultActive = gateIrisDefaultActive;
    }

    public void setGateIrisLeverBlock(final Block gateIrisLeverBlock) {
        this.gateIrisLeverBlock = gateIrisLeverBlock;
    }

    private void setGateLightingCurrentIteration(final int gateLightingCurrentIteration) {
        this.gateLightingCurrentIteration = gateLightingCurrentIteration;
    }

    public void setGateLightsActive(final boolean gateLightsActive) {
        this.gateLightsActive = gateLightsActive;
    }

    public void setGateMinecartTeleportLocation(final Location gateMinecartTeleportLocation) {
        this.gateMinecartTeleportLocation = gateMinecartTeleportLocation;
    }

    public void setGateName(final String gateName) {
        this.gateName = gateName;
    }

    public void setGateNameBlockHolder(final Block gateNameBlockHolder) {
        this.gateNameBlockHolder = gateNameBlockHolder;
    }

    public void setGateNetwork(final StargateNetwork gateNetwork) {
        this.gateNetwork = gateNetwork;
    }

    public void setGateOwner(final String gateOwner) {
        this.gateOwner = gateOwner;
    }

    public void setGatePlayerTeleportLocation(final Location gatePlayerTeleportLocation) {
        this.gatePlayerTeleportLocation = gatePlayerTeleportLocation;
    }

    private void setGateRecentlyActive(final boolean gateRecentlyActive) {
        this.gateRecentlyActive = gateRecentlyActive;
    }

    public void setGateRedstoneDialActivationBlock(final Block gateRedstoneDialActivationBlock) {
        this.gateRedstoneDialActivationBlock = gateRedstoneDialActivationBlock;
    }

    public void setGateRedstoneGateActivatedBlock(final Block gateRedstoneGateActivatedBlock) {
        this.gateRedstoneGateActivatedBlock = gateRedstoneGateActivatedBlock;
    }

    public void setGateRedstonePowered(final boolean gateRedstonePowered) {
        this.gateRedstonePowered = gateRedstonePowered;
    }

    public void setGateRedstoneSignActivationBlock(final Block gateRedstoneSignActivationBlock) {
        this.gateRedstoneSignActivationBlock = gateRedstoneSignActivationBlock;
    }

    public void setGateShape(final StargateShape gateShape) {
        this.gateShape = gateShape;
    }

    private void setGateShutdownTaskId(final int gateShutdownTaskId) {
        this.gateShutdownTaskId = gateShutdownTaskId;
    }

    public void setGateSignPowered(final boolean gateSignPowered) {
        this.gateSignPowered = gateSignPowered;
    }

    private void setGateTarget(final Stargate gateTarget) {
        this.gateTarget = gateTarget;
    }

    public void setGateTempSignTarget(final long gateTempSignTarget) {
        this.gateTempSignTarget = gateTempSignTarget;
    }

    public void setGateTempTargetId(final long gateTempTargetId) {
        this.gateTempTargetId = gateTempTargetId;
    }

    public void setGateWorld(final World gateWorld) {
        this.gateWorld = gateWorld;
    }
    
    public String getLastUsedBy() {
        return this.lastUsedBy;
    }
    
    public void setLastUsedBy(Player player) {
        this.setLastUsedBy(player.getName());
    }
    
    public void setLastUsedBy(String playerName) {
        this.lastUsedBy = playerName;
    }
    
    public String getSourceGateName() {
        return this.gateSourceName;
    }
    
    public void setSourceGateName(String gateName) {
        this.gateSourceName = gateName;
    }
    
    public void setGateChevronsLocked(boolean locked) {
        this.gateChevronsLocked = locked;
    }
    
    public boolean gateChevronsLocked() {
        return this.gateChevronsLocked;
    }    

    public void setIrisDeactivationCode(final String idc) {

        if ((idc != null) && !idc.equals("")) {
            setGateIrisDeactivationCode(idc);
            setupIrisLever(true);
        } else {
            setIrisState(false);
            setupIrisLever(false);
            setGateIrisDeactivationCode("");
        }
    }

    @SuppressWarnings("deprecation")
	private void setIrisState(final boolean irisactive) {
        setGateIrisActive(irisactive);
        fillGateInterior(isGateIrisActive()
                ? isGateCustom()
                ? getGateCustomIrisMaterial()
                : getGateShape() != null
                ? getGateShape().getShapeIrisMaterial()
                : Material.STONE
                : isGateActive()
                ? isGateCustom()
                ? getGateCustomPortalMaterial()
                : getGateShape() != null
                ? getGateShape().getShapePortalMaterial()
                : Material.STATIONARY_WATER
                : Material.AIR);
        if ((getGateIrisLeverBlock() != null) && (getGateIrisLeverBlock().getTypeId() == 69)) {
            getGateIrisLeverBlock().setData(WorldUtils.getLeverToggleByte(getGateIrisLeverBlock().getData(), isGateIrisActive()));
        }
    }

    public void setLoadedVersion(final byte loadedVersion) {
        this.loadedVersion = loadedVersion;
    }

    @SuppressWarnings("deprecation")
	public void setupGateSign(final boolean create) {
        if (getGateNameBlockHolder() != null) {
            if (create) {
                final Block nameSign = getGateNameBlockHolder().getRelative(getGateFacing());
                getGateStructureBlocks().add(nameSign.getLocation());
                nameSign.setTypeIdAndData(Material.WALL_SIGN.getId(), WorldUtils.getSignFacingByteFromBlockFace(getGateFacing()), false);
                final Sign sign = (Sign) nameSign.getState();
                sign.setLine(0, "-" + getGateName() + "-");

                if (getGateNetwork() != null) {
                    sign.setLine(1, "N:" + getGateNetwork().getNetworkName());
                }

                if (getGateOwner() != null) {
                    sign.setLine(2, "O:" + getGateOwner());
                }
                sign.update(true);

            } else {
                final Block nameSign = getGateNameBlockHolder().getRelative(getGateFacing());
                if (nameSign.getType().equals(Material.WALL_SIGN)) {
                    getGateStructureBlocks().remove(nameSign.getLocation());
                    nameSign.setTypeId(0);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
	public void setupIrisLever(final boolean create) {
        if ((getGateIrisLeverBlock() == null) && (getGateShape() != null) && !(getGateShape() instanceof Stargate3DShape)) {
            setGateIrisLeverBlock(getGateDialLeverBlock().getRelative(BlockFace.DOWN));
        }
        if (getGateIrisLeverBlock() != null) {
            if (create) {
                getGateStructureBlocks().add(getGateIrisLeverBlock().getLocation());
                getGateIrisLeverBlock().setTypeIdAndData(Material.LEVER.getId(), WorldUtils.getLeverFacingByteFromBlockFace(getGateFacing()), false);
            } else {
                if (getGateIrisLeverBlock().getType().equals(Material.LEVER)) {
                    getGateStructureBlocks().remove(getGateIrisLeverBlock().getLocation());
                    getGateIrisLeverBlock().setTypeId(0);
                }
            }
        }
    }

    public void setupRedstone(final boolean create) {
        if (isGateSignPowered()) {
            setupRedstoneDialWire(create);
            setupRedstoneSignDialWire(create);
        }
        setupRedstoneGateActivatedLever(create);
    }

    @SuppressWarnings("deprecation")
	private void setupRedstoneDialWire(final boolean create) {
        if (getGateRedstoneDialActivationBlock() != null) {
            if (create) {
                getGateStructureBlocks().add(getGateRedstoneDialActivationBlock().getLocation());
                getGateRedstoneDialActivationBlock().setTypeId(55);
            } else {
                if (getGateRedstoneGateActivatedBlock().getTypeId() == 55) {
                    getGateStructureBlocks().remove(getGateRedstoneDialActivationBlock().getLocation());
                    getGateRedstoneDialActivationBlock().setTypeId(0);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
	private void setupRedstoneGateActivatedLever(final boolean create) {
        if (getGateRedstoneGateActivatedBlock() != null) {
            if (create) {
                getGateStructureBlocks().add(getGateRedstoneGateActivatedBlock().getLocation());
                getGateRedstoneGateActivatedBlock().setTypeIdAndData(Material.LEVER.getId(), (byte) 0x5, false);
            } else {
                if (getGateRedstoneGateActivatedBlock().getType().equals(Material.LEVER)) {
                    getGateStructureBlocks().remove(getGateRedstoneGateActivatedBlock().getLocation());
                    getGateRedstoneGateActivatedBlock().setTypeId(0);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
	private void setupRedstoneSignDialWire(final boolean create) {
        if (getGateRedstoneSignActivationBlock() != null) {
            if (create) {
                getGateStructureBlocks().add(getGateRedstoneSignActivationBlock().getLocation());
                getGateRedstoneSignActivationBlock().setTypeId(55);
            } else {
                if (getGateRedstoneGateActivatedBlock().getTypeId() == 55) {
                    getGateStructureBlocks().remove(getGateRedstoneSignActivationBlock().getLocation());
                    getGateRedstoneSignActivationBlock().setTypeId(0);
                }
            }
        }
    }

    public void shutdownStargate(final boolean timer) {
        if (this.getGateShutdownTaskId() > 0) {
            SGLogger.prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" ShutdownTaskID \"" + getGateShutdownTaskId() + "\" cancelled.");
            StarGates.getScheduler().cancelTask(getGateShutdownTaskId());
            this.setGateShutdownTaskId(-1);
        }

        if (this.getGateTarget() != null) {
            this.getGateTarget().shutdownStargate(true);
            this.getGateTarget().setSourceGateName(null);
        }

        this.setGateTarget(null);
        if (timer) {
            this.setGateRecentlyActive(true);
        }
        
        this.setGateActive(false);
        this.lightStargate(false);
        this.setWormholeEstablished(false);
        this.setSourceGateName(null);
        
        this.toggleDialLeverState(false);
        this.toggleRedstoneGateActivatedPower();
        
        if (this.isGateIrisDefaultActive()) {
            this.setIrisState(isGateIrisDefaultActive());
        } else if (!this.isGateIrisActive()) {
            this.fillGateInterior(Material.AIR);
        }

        if (timer) {
            this.startAfterShutdownTimer();
        }

        WorldUtils.scheduleChunkUnload(getGatePlayerTeleportLocation().getBlock());

        // remove activated Stargate
        StargateManager.removeActivatedStargate(this.getGateName());
        if (WormholePlayerManager.findPlayerByGateName(this.getGateName()) != null)
            WormholePlayerManager.findPlayerByGateName(this.getGateName()).removeStargate(this.getGateName());
    }

    public void startActivationTimer(final Player p) {
        if (getGateActivateTaskId() > 0) {
            StarGates.getScheduler().cancelTask(getGateActivateTaskId());
        }

        final int timeout = ConfigManager.getTimeoutActivate() * 20;
        setGateActivateTaskId(StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.DEACTIVATE), timeout));
        SGLogger.prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" ActivateTaskID \"" + getGateActivateTaskId() + "\" created.");
    }

    private void startAfterShutdownTimer() {
        if (getGateAfterShutdownTaskId() > 0) {
            StarGates.getScheduler().cancelTask(getGateAfterShutdownTaskId());
        }
        final int timeout = 60;
        setGateAfterShutdownTaskId(StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.AFTERSHUTDOWN), timeout));
        SGLogger.prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" AfterShutdownTaskID \"" + getGateAfterShutdownTaskId() + "\" created.");
        if (getGateAfterShutdownTaskId() == -1) {
            SGLogger.prettyLog(Level.SEVERE, false, "Failed to schdule wormhole after shutdown, received task id of -1.");
            setGateRecentlyActive(false);
        }
    }

    public void stopActivationTimer() {
        if (getGateActivateTaskId() > 0) {
            SGLogger.prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" ActivateTaskID \"" + getGateActivateTaskId() + "\" cancelled.");
            StarGates.getScheduler().cancelTask(getGateActivateTaskId());
            setGateActivateTaskId(-1);
        }
    }

    public void stopAfterShutdownTimer() {
        if (getGateAfterShutdownTaskId() > 0) {
            SGLogger.prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" AfterShutdownTaskID \"" + getGateAfterShutdownTaskId() + "\" cancelled.");
            StarGates.getScheduler().cancelTask(getGateAfterShutdownTaskId());
            setGateAfterShutdownTaskId(-1);
        }
        setGateRecentlyActive(false);
    }

    public void dialSignClicked() {
        this.dialSignClicked(null);
    }

    @SuppressWarnings("deprecation")
	public void dialSignClicked(Action eventAction) {
        synchronized (getGateNetwork().getNetworkGateLock()) {
            //@TODO check if this is still needed
            getGateDialSignBlock().setTypeIdAndData(Material.WALL_SIGN.getId(), WorldUtils.getSignFacingByteFromBlockFace(getGateFacing()), false);
            setGateDialSign((Sign) getGateDialSignBlock().getState());
            getGateDialSign().setLine(0, "-" + getGateName() + "-");
            
            String lineMarkerS = ">" + ChatColor.GREEN;
            String lineMarkerE = ChatColor.BLACK + "<";

            if (getGateNetwork().getNetworkSignGateList().size() <= 1) {
                getGateDialSign().setLine(1, "");
                getGateDialSign().setLine(2, ChatColor.DARK_RED + "No Other Gates" + ChatColor.BLACK);
                getGateDialSign().setLine(3, "");
                getGateDialSign().update();
                setGateDialSignTarget(null);
                return;
            }

            if (getGateDialSignIndex() == -1) {
                setGateDialSignIndex(0);
            }

            int direction = 1;
            if ((eventAction != null) && (eventAction.equals(Action.RIGHT_CLICK_BLOCK))) {
                direction = -1;
            }
            
            getGateSignOrder().clear();
            int orderIndex = 1;

            if (getGateDialSignIndex() > getGateNetwork().getNetworkSignGateList().size()) {
                setGateDialSignIndex(0);
            }

            for (int i = 0; i < 4; i++) {
                if (getGateDialSignIndex() == getGateNetwork().getNetworkSignGateList().size()) {
                    setGateDialSignIndex(0);
                }

                if (getGateDialSignIndex() < 0) {
                    setGateDialSignIndex(getGateNetwork().getNetworkSignGateList().size() - 1);
                }

                if (getGateNetwork().getNetworkSignGateList().get(getGateDialSignIndex()).getGateName().equals(getGateName())) {
                    setGateDialSignIndex(getGateDialSignIndex() + direction);

                    if (getGateDialSignIndex() == getGateNetwork().getNetworkSignGateList().size()) {
                        setGateDialSignIndex(0);
                    }
                }

                if (getGateDialSignIndex() >= 0) {
                    getGateSignOrder().put(orderIndex, getGateNetwork().getNetworkSignGateList().get(getGateDialSignIndex()));
                    orderIndex++;

                    setGateDialSignIndex(getGateDialSignIndex() + direction);
                }
            }

            //@TODO: remove debug
            //System.out.println(getGateNetwork().getNetworkName() + " size: " + getGateNetwork().getNetworkSignGateList().size());
            //for (Stargate s: getGateNetwork().getNetworkSignGateList()) {
            //    System.out.println(s.getGateDialSignIndex() + ": " + s.getGateName());
            //}

            String line1 = "";
            String line2 = "";
            String line3 = "";
            String lineTemp = "";

            if (getGateNetwork().getNetworkSignGateList().size() >= 2) {
                line2 = lineMarkerS + getGateSignOrder().get(2).getGateName() + lineMarkerE;
            } else {
                line2 = lineMarkerS + getGateSignOrder().get(1).getGateName() + lineMarkerE;
            }

            if (getGateNetwork().getNetworkSignGateList().size() > 2) {
                line1 = getGateSignOrder().get(1).getGateName();
                line3 = getGateSignOrder().get(3).getGateName();
            }

            setGateDialSignTarget(getGateSignOrder().get(2));
            setGateDialSignIndex(getGateNetwork().getNetworkSignGateList().indexOf(getGateSignOrder().get(2)));

            if (direction == -1) {
                lineTemp = line1;
                line1 = line3;
                line3 = lineTemp;
            }

            getGateDialSign().setLine(1, line1);
            getGateDialSign().setLine(2, line2);
            getGateDialSign().setLine(3, line3);
            getGateDialSign().update(true);
        }
    }

    public void timeoutStargate() {
        if (getGateActivateTaskId() > 0) {
            SGLogger.prettyLog(Level.FINE, false, "Wormhole \"" + getGateName() + "\" ActivateTaskID \"" + getGateActivateTaskId() + "\" timed out.");
            setGateActivateTaskId(-1);
        }

        WormholePlayer wormholePlayer = WormholePlayerManager.getRegisteredWormholePlayer(this.getLastUsedBy());
        
        if (isGateIrisDefaultActive()) {
            this.setIrisState(isGateIrisDefaultActive());
        }

        if (this.isGateLightsActive()) {
            this.lightStargate(false);
        }

        if (wormholePlayer != null) {
            wormholePlayer.sendMessage("Gate: " + getGateName() + " timed out and deactivated.");
            wormholePlayer.removeStargate(this.getGateName());
        }

        StargateManager.removeActivatedStargate(this.getGateName());
    }

    @SuppressWarnings("deprecation")
	public void toggleDialLeverState(final boolean regenerate) {
        if (getGateDialLeverBlock() != null) {
            if (isGateActive()) {
                WorldUtils.scheduleChunkLoad(getGateDialLeverBlock());
            }
            
            Material material = getGateDialLeverBlock().getType();
            if (regenerate) {
                getGateDialLeverBlock().setTypeIdAndData(Material.LEVER.getId(), WorldUtils.getLeverFacingByteFromBlockFace(getGateFacing()), false);
                material = getGateDialLeverBlock().getType();
            }
            
            final byte leverState = getGateDialLeverBlock().getData();
            switch (material) {
                case STONE_BUTTON:
                    getGateDialLeverBlock().setType(Material.LEVER);
                    getGateDialLeverBlock().setData(leverState);
                    SGLogger.prettyLog(Level.FINE, false, "Automaticially replaced Button on gate \"" + getGateName() + "\" with Lever.");
                    getGateDialLeverBlock().setData(WorldUtils.getLeverToggleByte(leverState, isGateActive()));
                    break;
                case LEVER:
                    getGateDialLeverBlock().setData(WorldUtils.getLeverToggleByte(leverState, isGateActive()));
                    break;
                default:
                    break;
            }
            
            if (!isGateActive()) {
                WorldUtils.scheduleChunkUnload(getGateDialLeverBlock());
            }
            
            SGLogger.prettyLog(Level.FINE, false, "Dial Button Lever Gate: \"" + getGateName() + "\" Material: \"" + material.toString() + "\" State: \"" + leverState + "\"");
        }
    }

    public void toggleIrisActive(final boolean setDefault) {
        setGateIrisActive(!isGateIrisActive());
        setIrisState(isGateIrisActive());
        if (setDefault) {
            setGateIrisDefaultActive(isGateIrisActive());
        }
    }

    @SuppressWarnings("deprecation")
	private void toggleRedstoneGateActivatedPower() {
        if (isGateRedstonePowered() && (getGateRedstoneGateActivatedBlock() != null) && (getGateRedstoneGateActivatedBlock().getTypeId() == 69)) {
            final byte leverState = getGateRedstoneGateActivatedBlock().getData();
            getGateRedstoneGateActivatedBlock().setData(WorldUtils.getLeverToggleByte(leverState, isGateActive()));
        }
    }

    public boolean tryClickTeleportSign(Block clickedBlock) {
        return this.tryClickTeleportSign(clickedBlock, Action.LEFT_CLICK_BLOCK, null);
    }
    
    public boolean tryClickTeleportSign(Block clickedBlock, Action eventAction) {
        return this.tryClickTeleportSign(clickedBlock, eventAction, null);
    }
    
    @SuppressWarnings("deprecation")
	public boolean tryClickTeleportSign(Block clickedBlock, Action eventAction, String triggeredByPlayer) {
        if ((getGateDialSign() == null) && (getGateDialSignBlock() != null)) {
            if (getGateDialSignBlock().getType().equals(Material.WALL_SIGN)) {
                setGateDialSignIndex(-1);

                //@TODO can be removed after long term test, only nuke sign on load
                if (eventAction == null) {
                    getGateDialSignBlock().setTypeId(0); //nuke sign
                }

                StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.DIAL_SIGN_CLICK, eventAction));
            }
        } else if (WorldUtils.isSameBlock(clickedBlock, getGateDialSignBlock())) {
            //@TODO can be removed after long term test, only nuke sign on load
            if (eventAction == null) {
                getGateDialSignBlock().setTypeId(0); //nuke sign
            }

            StarGates.getScheduler().scheduleSyncDelayedTask(StarGates.getThisPlugin(), new StargateUpdateRunnable(this, ActionToTake.DIAL_SIGN_CLICK, eventAction));
            return true;
        }

        return false;        
    }
}