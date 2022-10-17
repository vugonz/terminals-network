package prr.clients;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import prr.terminals.Terminal;

public class Client implements Serializable {

    @Serial
    /** Serial number for serialization. */
	private static final long serialVersionUID = 202208091753L;

    /** Client's key */
    String _key;

    /** Client's name */
    String _name;

    /** Client's tax ID */
    int _taxId;

    /** Client's type */
    ClientType _type;

    Map<String, Terminal> _terminals = new TreeMap<>();

    /** Client's notifications not yet delivered */
    ArrayList<Notification> _notificationsLog = new ArrayList<>();

    /** Flag to determine if Client should be notified */
    boolean _notificationsOn = true;

    /** Notification method, initialized with default NotificationMethod */
    NotificationMethod _notificationMethod = new NotificationMethod() {

        @Serial
        /** Serial number for serialization. */
	    private static final long serialVersionUID = 202208091753L;

        /** The default Notification Method currently does not perform any relevant action as Notifications 
         * are delivered through the Client's Menu 
         */
        public void deliverNotifications() {
            // EMPTY (for now)
        }
    }; 

    public class Notification {
        public Notification() {}
        // TODO
    }
    
    public Client(String key, String name, int taxId) {
        _key = key;
        _name = name;
        _taxId = taxId;
        _type = new NormalType();
    }

    public String getKey() {
        return _key;
    }

    public String getName() {
        return _name;
    }

    public int getTaxId() {
        return _taxId;
    }

    public boolean notificationsOn() {
        return _notificationsOn;
    }

    public ClientType getClientType() {
        return _type;
    }
    
    public Integer getNumberOfTerminals() {
        return _terminals.size();
    }

    public Integer getClientDebt() {
        return 0;
        // TODO
    }

    public Integer getClientPaid() {
        return 0;
        // TODO
    }

    public void setNotificationsOn(boolean notificationsOn) {
        _notificationsOn = notificationsOn;
    }

    public void setClientType(ClientType type) {
        _type = type;
    }

    /** 
     * Deliver Client notification using current Client's Notification Method
     */
    public void doNotify() {
        _notificationMethod.notify();
    }

    /**
     * Adds given Terminal to the Client's Terminals 
     * @param t
     */
    public void addTerminal(Terminal t) {
        _terminals.put(t.getKey(), t);
    }

    @Override
    public String toString() {
        return "CLIENT|" + 
                getKey() + "|" + 
                getName() + "|" +
                getTaxId() + "|" + 
                getClientType().toString() + "|" + 
                (notificationsOn() ? "YES" : "NO") + "|" +
                getNumberOfTerminals() + "|" + 
                getClientDebt() + "|" +
                getClientPaid(); 
    }
}
