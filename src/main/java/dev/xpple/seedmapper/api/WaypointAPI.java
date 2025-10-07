package dev.xpple.seedmapper.api;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.path.XaeroPath;

/**
 * Simple waypoint API for SMIntegrated that provides basic waypoint creation functionality.
 * Uses Xaero's Minimap API directly.
 */
public class WaypointAPI {
    
    /**
     * Add a waypoint to a specific dimension with given coordinates and title
     * @param dimension the dimension resource key (Level.OVERWORLD, Level.NETHER, Level.END)
     * @param x the X coordinate
     * @param y the Y coordinate  
     * @param z the Z coordinate
     * @param title the waypoint title/name
     * @throws CommandSyntaxException if the waypoint cannot be added
     */
    public static void addWaypoint(ResourceKey<Level> dimension, int x, int y, int z, String title) throws CommandSyntaxException {
        try {
            MinimapWorld minimapWorld = getMinimapWorld(dimension);
            if (minimapWorld == null) {
                throw new CommandSyntaxException(null, () -> "No valid world available for dimension. Make sure Xaero's Minimap is installed.");
            }
            
            WaypointSet waypointSet = getOrCreateWaypointSet(minimapWorld, "gui.xaero_default");
            
            // Create waypoint with first letter of title as initials
            String initials = title.isEmpty() ? "W" : title.substring(0, Math.min(2, title.length()));
            
            Waypoint waypoint = new Waypoint(
                x, y, z,
                title,
                initials,
                WaypointColor.RED, // Default color
                WaypointPurpose.NORMAL,
                false, // not temporary
                true   // y included
            );
            
            waypointSet.add(waypoint);
            
            // Request waypoint refresh if WorldMap is available
            // This uses reflection to avoid hard dependency on WorldMap mod
            try {
                Class<?> supportModsClass = Class.forName("xaero.map.mods.SupportMods");
                Object xaeroMinimapSupport = supportModsClass.getField("xaeroMinimap").get(null);
                xaeroMinimapSupport.getClass().getMethod("requestWaypointsRefresh").invoke(xaeroMinimapSupport);
            } catch (Exception e) {
                // WorldMap not installed, waypoints will still work but might not refresh immediately
            }
        } catch (NoClassDefFoundError e) {
            throw new CommandSyntaxException(null, () -> "Xaero's Minimap is not installed. Please install it to use waypoint features.");
        }
    }
    
    private static MinimapWorld getMinimapWorld(ResourceKey<Level> dim) {
        MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (minimapSession == null) return null;
        MinimapWorld currentWorld = minimapSession.getWorldManager().getCurrentWorld();
        if (currentWorld == null) return null;
        if (currentWorld.getDimId() == dim) {
            return currentWorld;
        }
        var rootContainer = minimapSession.getWorldManager().getCurrentRootContainer();
        for (MinimapWorld world : rootContainer.getWorlds()) {
            if (world.getDimId() == dim) {
                return world;
            }
        }
        String dimensionDirectoryName = minimapSession.getDimensionHelper().getDimensionDirectoryName(dim);
        String worldNode = minimapSession.getWorldStateUpdater().getPotentialWorldNode(dim, true);
        XaeroPath containerPath = minimapSession.getWorldState()
            .getAutoRootContainerPath()
            .resolve(dimensionDirectoryName)
            .resolve(worldNode);
        return minimapSession.getWorldManager().getWorld(containerPath);
    }
    
    private static WaypointSet getOrCreateWaypointSet(MinimapWorld minimapWorld, String setName) {
        WaypointSet waypointSet = minimapWorld.getWaypointSet(setName);
        if (waypointSet == null) {
            minimapWorld.addWaypointSet(setName);
            waypointSet = minimapWorld.getWaypointSet(setName);
        }
        return waypointSet;
    }
}