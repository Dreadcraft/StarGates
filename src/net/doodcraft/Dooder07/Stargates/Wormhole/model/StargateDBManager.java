package net.doodcraft.Dooder07.Stargates.Wormhole.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import net.doodcraft.Dooder07.Stargates.Wormhole.StarGates;
import net.doodcraft.Dooder07.Stargates.Wormhole.config.ConfigManager;
import net.doodcraft.Dooder07.Stargates.Wormhole.logic.StargateHelper;
import net.doodcraft.Dooder07.Stargates.Wormhole.permissions.PermissionsManager.PermissionLevel;
import net.doodcraft.Dooder07.Stargates.Wormhole.utils.SGLogger;

import org.omg.CORBA.Environment;

public class StargateDBManager {

    private static Connection wormholeSQLConnection = null;
    private static volatile PreparedStatement storeStatement;
    private static volatile PreparedStatement updateGateStatement;
    private static volatile PreparedStatement getGateStatement;
    private static volatile PreparedStatement removeStatement;
    private static volatile PreparedStatement updateIndvPermStatement = null;
    private static volatile PreparedStatement storeIndvPermStatement = null;
    private static volatile PreparedStatement getIndvPermStatement = null;
    private static volatile PreparedStatement getAllIndvPermStatement = null;

    private static void connectDB() {
    	
    	String driver = null;
    	
    	ConfigurationSection dbConfig = StarGates.getThisPlugin().getConfig().getConfigurationSection("database");
    	
    	String dbType = dbConfig.getString("type").toLowerCase();
    	String url = dbConfig.getString("url");
    	String user = dbConfig.getString("user");
    	String password = dbConfig.getString("password");
    	
    	if(dbType.equals("sqlite")) {
    		driver = "org.sqlite.JDBC";
    	}
    	else if(dbType.equals("mysql")) {
    		driver = "com.mysql.jdbc.Driver";
    	}
    	else {
    		SGLogger.prettyLog(Level.WARNING, false, "Invalid database type: " + dbType + ". Defaulting to SQLite");
    		
    		dbType = "sqlite";
    		driver = "org.sqlite.JDBC";
    	}
    	
    	try {
    		Class.forName(driver);
    		
    		url = String.format("jdbc:%s:%s", dbType, url);
    		
    		StargateDBManager.setWormholeSQLConnection(DriverManager.getConnection(url, user, password));
    		StargateDBManager.wormholeSQLConnection.setAutoCommit(true);
    	}
    	catch(ClassNotFoundException cnfe) {
    		SGLogger.prettyLog(Level.SEVERE, false, "Missing driver class: " + driver);
    		cnfe.printStackTrace();
    	}
    	catch (SQLException e) {
			SGLogger.prettyLog(Level.SEVERE, false, "Error connecting to database");
			e.printStackTrace();
		}
    }
    
    public static ConcurrentHashMap<String, PermissionLevel> getAllIndividualPermissions() {
        final ConcurrentHashMap<String, PermissionLevel> perms = new ConcurrentHashMap<String, PermissionLevel>();
        if (!isConnected()) {
            connectDB();
        }

        ResultSet perm = null;
        try {
            if (wormholeSQLConnection.isClosed()) {
                connectDB();
            }

            if (getAllIndvPermStatement == null) {
                getAllIndvPermStatement = wormholeSQLConnection.prepareStatement("SELECT PlayerName, Permission FROM StargateIndividualPermissions;");
            }

            perm = getAllIndvPermStatement.executeQuery();
            while (perm.next()) {
                perms.put(perm.getString("PlayerName"), PermissionLevel.valueOf(perm.getString("Permission")));
            }
        } catch (final SQLException e) {
            SGLogger.prettyLog(Level.SEVERE, false, "Error GetAllIndividualPermissions: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                perm.close();
            } catch (final SQLException e) {
                SGLogger.prettyLog(Level.FINE, false, e.getMessage());
            }
        }
        return perms;
    }

    public static Connection getConnection() {
    	if(!StargateDBManager.isConnected()) {
    		StargateDBManager.connectDB();
    	}
    	
    	return StargateDBManager.wormholeSQLConnection;
    }

    public static boolean isConnected() {
        if (wormholeSQLConnection != null) {
            try {
                if (wormholeSQLConnection.isClosed())
                    return false;
            } catch (SQLException e) {
                SGLogger.prettyLog(Level.FINE, false, "DBLink not available.");
                return false;
            }

            return true;
        }

        return false;
    }

    public static void loadStargates(final Server server) {
        if (!isConnected()) {
            connectDB();
        }

        //@TODO: re-implement world id search
        PreparedStatement stmt = null;
        ResultSet gatesData = null;
        try {
            if (wormholeSQLConnection.isClosed()) {
                connectDB();
            }
            stmt = wormholeSQLConnection.prepareStatement("SELECT * FROM Stargates;");

            gatesData = stmt.executeQuery();
            while (gatesData.next()) {
                final String networkName = gatesData.getString("Network");
                StargateNetwork sn = null;
                if (networkName != null) {
                    sn = StargateManager.getStargateNetwork(networkName);
                    if ((sn == null) && !networkName.equals("")) {
                        sn = StargateManager.addStargateNetwork(networkName);
                    }
                }

                final String worldName = gatesData.getString("WorldName");
                final String worldEnvironment = gatesData.getString("WorldEnvironment");
                
                World w = null;
                
                if (ConfigManager.isWormholeWorldsSupportEnabled()) {
                    //@TODO: should throw an exception instead
                    if ((StarGates.getWorldHandler() != null) && !StarGates.getWorldHandler().loadWorld(worldName)) {
                        SGLogger.prettyLog(Level.WARNING, true, "World: " + worldName + " is not a Wormhole World, the suggested action is to add it as one. Otherwise disregard this warning.");
                    }
                } else {
                    server.createWorld(new WorldCreator(worldName).environment(Environment.valueOf(worldEnvironment)));
                }
                w = server.getWorld(worldName);

                final Stargate s = StargateHelper.parseVersionedData(gatesData.getBytes("GateData"), w, gatesData.getString("Name"), sn);
                if (s != null) {
                    s.setGateId(gatesData.getInt("Id"));
                    s.setGateOwner(gatesData.getString("Owner"));
                    String gateShapeName = gatesData.getString("GateShape");
                    if (gateShapeName == null) {
                        gateShapeName = "Standard";
                    }

                    s.setGateShape(StargateHelper.getStargateShape(gateShapeName));
                    if (sn != null) {
                        sn.getNetworkGateList().add(s);
                        if (s.isGateSignPowered()) {
                            sn.getNetworkSignGateList().add(s);
                            if ((s.getGateDialSign() != null) && (s.getGateDialSignBlock() != null)) {
                                s.tryClickTeleportSign(s.getGateDialSignBlock());
                            }
                        }
                    }
                    
                    if(StargateManager.addStargate(s)) {
                    	SGLogger.prettyLog(Level.FINE, false, "Loading Stargate: '" + s.getGateName() + "', GateFace: '" + s.getGateFacing().name() +"' from DB");
                    }
                    else {
                    	SGLogger.prettyLog(Level.WARNING, false, "Failed to load Stargate: '" + s.getGateName() + "'. Removing gate from database.");
                    	StargateDBManager.removeStargateFromSQL(s);
                    }
                } else {
                    SGLogger.prettyLog(Level.WARNING, true, "Failed to load Stargate '" + sn + "' from DB.");
                }
            }
            gatesData.close();
            stmt.close();

            //@TODO optimize this code section (gate initialization)
            final ArrayList<Stargate> gateList = StargateManager.getAllGates();
            for (final Stargate s : gateList) {
                if (s.isGateLightsActive() && !s.isGateActive()) {
                    s.lightStargate(false);
                }

                if (s.getGateTempTargetId() >= 0) {
                    for (final Stargate t : gateList) {
                        if (t.getGateId() == s.getGateTempTargetId()) {
                            s.dialStargate(t, true);
                            break;
                        }
                    }
                }

                if (s.getGateTempSignTarget() >= 0) {
                    for (final Stargate t : gateList) {
                        if (t.getGateId() == s.getGateTempSignTarget()) {
                            s.setGateDialSignTarget(t);
                            break;
                        }
                    }
                }
            }

            SGLogger.prettyLog(Level.INFO, false, gateList.size() + " Wormholes loaded from WormholeDB.");

        } catch (final SQLException e) {
            SGLogger.prettyLog(Level.SEVERE, false, "Error loading stargates from DB: " + e.getMessage());
        } finally {
            try {
                gatesData.close();
            } catch (final SQLException e) {
                SGLogger.prettyLog(Level.FINE, false, e.getMessage());
            }
            try {
                stmt.close();
            } catch (final SQLException e) {
                SGLogger.prettyLog(Level.FINE, false, e.getMessage());
            }
        }
    }

    protected static void removeStargateFromSQL(final Stargate s) {
        if (!isConnected()) {
            connectDB();
        }

        try {
            if (wormholeSQLConnection.isClosed()) {
                connectDB();
            }
            if (removeStatement == null) {
                removeStatement = wormholeSQLConnection.prepareStatement("DELETE FROM Stargates WHERE name = ?;");
            }

            removeStatement.setString(1, s.getGateName());
            removeStatement.executeUpdate();
        } catch (final SQLException e) {
            SGLogger.prettyLog(Level.SEVERE, false, "Error storing stargate to DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void setWormholeSQLConnection(final Connection connection) {
        StargateDBManager.wormholeSQLConnection = connection;
    }

    public static void shutdown() {
        try {
            if ((wormholeSQLConnection != null) && (!wormholeSQLConnection.isClosed())) {
                wormholeSQLConnection.close();
                wormholeSQLConnection = null;
                SGLogger.prettyLog(Level.INFO, false, "WormholeDB shutdown successful.");
            }
        } catch (final SQLException e) {
            SGLogger.prettyLog(Level.SEVERE, false, " Failed to shutdown:" + e.getMessage());
        } finally {
            if (wormholeSQLConnection == null) {
                wormholeSQLConnection = null;
                storeStatement = null;
                updateGateStatement = null;
                getGateStatement = null;
                removeStatement = null;
                updateIndvPermStatement = null;
                storeIndvPermStatement = null;
                getIndvPermStatement = null;
                getAllIndvPermStatement = null;                
            }
        }
    }

    public static void stargateToSQL(final Stargate s) {
        if (!isConnected()) {
            connectDB();
        }
        ResultSet gatesData = null;
        try {
            if (wormholeSQLConnection.isClosed()) {
                connectDB();
            }
            if (getGateStatement == null) {
                getGateStatement = wormholeSQLConnection.prepareStatement("SELECT * FROM Stargates WHERE Name = ?");
            }
            getGateStatement.setString(1, s.getGateName());

            gatesData = getGateStatement.executeQuery();
            if (gatesData.next()) {
                gatesData.close();

                if (updateGateStatement == null) {
                    updateGateStatement = wormholeSQLConnection.prepareStatement("UPDATE Stargates SET GateData = ?, Network = ?, World = ?, WorldName = ?, WorldEnvironment = ?, Owner = ?, GateShape = ? WHERE Name = ?");
                }

                final byte[] data = StargateHelper.stargatetoBinary(s);
                updateGateStatement.setBytes(1, data);
                if (s.getGateNetwork() != null) {
                    updateGateStatement.setString(2, s.getGateNetwork().getNetworkName());
                } else {
                    updateGateStatement.setString(2, "");
                }
                updateGateStatement.setLong(3, s.getGateWorld().getUID().getMostSignificantBits());
                updateGateStatement.setString(4, s.getGateWorld().getName());
                updateGateStatement.setString(5, s.getGateWorld().getEnvironment().toString());
                updateGateStatement.setString(6, s.getGateOwner());
                if (s.getGateShape() == null) {
                    updateGateStatement.setString(7, "Standard");
                } else {
                    updateGateStatement.setString(7, s.getGateShape().getShapeName());
                }

                updateGateStatement.setString(8, s.getGateName());
                updateGateStatement.executeUpdate();

                SGLogger.prettyLog(Level.FINE,  false, "Saved gate '" + s.getGateName() + "', GateFace: '" + s.getGateFacing().name() + "' to DB");
            } else {
                gatesData.close();

                if (storeStatement == null) {
                    storeStatement = wormholeSQLConnection.prepareStatement("INSERT INTO Stargates(Name, GateData, Network, World, WorldName, WorldEnvironment, Owner, GateShape) VALUES ( ? , ? , ? , ? , ? , ?, ?, ? );");
                }

                storeStatement.setString(1, s.getGateName());
                final byte[] data = StargateHelper.stargatetoBinary(s);
                storeStatement.setBytes(2, data);
                if (s.getGateNetwork() != null) {
                    storeStatement.setString(3, s.getGateNetwork().getNetworkName());
                } else {
                    storeStatement.setString(3, "");
                }

                storeStatement.setLong(4, s.getGateWorld().getUID().getMostSignificantBits());
                storeStatement.setString(5, s.getGateWorld().getName());
                storeStatement.setString(6, s.getGateWorld().getEnvironment().toString());
                storeStatement.setString(7, s.getGateOwner());
                storeStatement.setString(8, s.getGateShape().getShapeName());

                storeStatement.executeUpdate();

                //@TODO Id is currently unused. When rewriting import with id and change WHERE clause in update stmt.
                getGateStatement.setString(1, s.getGateName());
                gatesData = getGateStatement.executeQuery();
                if (gatesData.next()) {
                    s.setGateId(gatesData.getInt("Id"));
                }
            }
        } catch (final SQLException e) {
            SGLogger.prettyLog(Level.SEVERE, false, "Error storing stargate to DB: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                gatesData.close();
            } catch (final SQLException e) {
                SGLogger.prettyLog(Level.FINE, false, e.getMessage());
            }
        }
    }

    public static void storeIndividualPermissionInDB(final String player, final PermissionLevel pl) {
        if (!isConnected()) {
            connectDB();
        }

        ResultSet perm = null;
        try {
            if (wormholeSQLConnection.isClosed()) {
                connectDB();
            }

            if (getIndvPermStatement == null) {
                getIndvPermStatement = wormholeSQLConnection.prepareStatement("SELECT Permission FROM StargateIndividualPermissions WHERE PlayerName = ?;");
            }

            getIndvPermStatement.setString(1, player);
            perm = getIndvPermStatement.executeQuery();
            if (!perm.next()) {
                if (storeIndvPermStatement == null) {
                    storeIndvPermStatement = wormholeSQLConnection.prepareStatement("INSERT INTO StargateIndividualPermissions ( PlayerName, Permission ) VALUES ( ? , ? );");
                }

                storeIndvPermStatement.setString(1, player);
                storeIndvPermStatement.setString(2, pl.toString());
                storeIndvPermStatement.executeUpdate();
            } else {
                if (updateIndvPermStatement == null) {
                    updateIndvPermStatement = wormholeSQLConnection.prepareStatement("UPDATE StargateIndividualPermissions SET Permission = ? WHERE PlayerName = ?;");
                }

                updateIndvPermStatement.setString(2, player);
                updateIndvPermStatement.setString(1, pl.toString());
                final int modified = updateIndvPermStatement.executeUpdate();

                if (modified != 1) {
                    SGLogger.prettyLog(Level.SEVERE, false, "Failed to update " + player + " permissions in DB.");
                }
            }
        } catch (final SQLException e) {
            SGLogger.prettyLog(Level.SEVERE, false, "Error StoreIndividualPermissionInDB : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                perm.close();
            } catch (final SQLException e) {
                SGLogger.prettyLog(Level.FINE, false, e.getMessage());
            }
        }
    }
}
