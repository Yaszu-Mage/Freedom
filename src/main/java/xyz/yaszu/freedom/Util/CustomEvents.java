package xyz.yaszu.freedom.Util;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;

public class CustomEvents {

    public static class TimeChangeEvent extends Event {
        private static final HandlerList HANDLER_LIST = new HandlerList();
        private long Time;

        public TimeChangeEvent(long time) {
            this.Time = time;
        }

        public long getMessage() {
            return this.Time;
        }

        public void setMessage(long message) {
            this.Time = message;
        }

        public static HandlerList getHandlerList() {
            return HANDLER_LIST;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLER_LIST;
        }
    }

}
