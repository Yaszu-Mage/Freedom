package xyz.yaszu.freedom.Subsystems.NPCS;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Subsystems.FakePlayerHandle;
import xyz.yaszu.freedom.Util.Util;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;

/**
 * NPC Manager for handling fake players and ML-based behavior.
 */
public class NpcManager extends Util implements Listener {

    private boolean isLoggingEnabled = false;

    public void setLoggingEnabled(boolean enabled) {
        this.isLoggingEnabled = enabled;
    }

    public boolean isLoggingEnabled() {
        return isLoggingEnabled;
    }

    public void loadData(Npc npc, File modelFile) throws IOException {
        npc.setModel(ModelSerializer.restoreMultiLayerNetwork(modelFile));
    }

    public class Npc {

        FakePlayerHandle handler;
        private MultiLayerNetwork model;

        public Npc(String name, Location location) {
            handler = new FakePlayerHandle(name,location);
        }
        public Npc(String name) {
            handler = new FakePlayerHandle(name,new Location(Bukkit.getWorld("world"),0,0,0));
        }

        public void setModel(MultiLayerNetwork model) {
            this.model = model;
        }

        public MultiLayerNetwork getModel() {
            return model;
        }
    }
}