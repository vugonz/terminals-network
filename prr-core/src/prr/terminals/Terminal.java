package prr.terminals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import prr.Network;
import prr.clients.Client;
import prr.communications.Communication;
import prr.communications.InteractiveCommunication;
import prr.exceptions.InvalidCommunicationPayment;
import prr.exceptions.NoActiveCommunicationException;
import prr.exceptions.SameTerminalStateException;
import prr.exceptions.UnavailableTerminalException;
import prr.exceptions.UnsupportedOperationException;

/**
* Abstract terminal.
*/
abstract public class Terminal implements Serializable {
    
    /** Serial number for serialization. */
    private static final long serialVersionUID = 202208091753L;
    
    /** Terminal identifying key */
    protected String _key;
    
    /** Client that owns this Terminal */
    protected Client _owner;
    
    protected Double _paidBalance = 0.0;
    
    protected Double _debtBalance = 0.0;
    
    /** Current ongoing communication */
    protected InteractiveCommunication _activeCommunication;
    
    /** List of Clients that are awaiting this Terminal State update (clients who needs to be notificated) */
    protected List<Client> _clientObservers = new ArrayList<>();
    
    /** List of communications started by this Terminal */
    protected List<Communication> _receivedCommunications = new ArrayList<>();
    
    /** List of communications recieved by this Terminal */
    protected List<Communication> _sentCommunications = new ArrayList<>();
    
    /** The current State of this Terminal */
    protected TerminalState _stateBeforeBusy;
    
    /** The current State of this Terminal */
    protected TerminalState _state;
    
    /** Terminal friends of this Terminal */
    protected Map<String, Terminal> _friends = new TreeMap<>();
    
    /**
    *
    * @param key Terinal identifying key
    * @param owner Terminal's Client owner
    */
    public Terminal(String key, Client owner) {
        _key = key;
        _owner = owner;
        _state = new OnTerminalState();
    }
    
    public Terminal(String key, Client owner, TerminalState state) {
        _key = key;
        _owner = owner;
        _state = state;
    }
    
    /**
    * Returns Terminal's identifying key
    *
    * @return Terminal key
    */
    public String getKey() { return _key; }
    
    /**
    * Returns Terminal's owner
    * @return
    */
    public Client getOwner() { return _owner; }
    
    /**
    * Returns Terminal's total paid balance in Communication's prices
    *
    * @return paid balance
    */
    public Double getPaidBalance() { return _paidBalance; }
    
    /**
    * Returns Terminal's total debt balance in Communication's prices
    *
    * @return debt balance
    */
    public Double getDebtBalance() { return _debtBalance; }
    
    public TerminalState getTerminalState() { return _state; }
    
    public Map<String, Terminal> getFriends() { return _friends; }
    
    /**
    * Returns current Terminal's state
    *
    * @return Terminal's state
    */
    public TerminalState getState() { return _state; }
    
    public TerminalState getStateBeforeBusy() { return _stateBeforeBusy; }
    
    public void setActiveCommunication(InteractiveCommunication c) {
        _activeCommunication = c;
    }
    
    public void setTerminalStateBeforeBusy(TerminalState state) {
        _stateBeforeBusy = state;
    }
    
    public void setTerminalState(TerminalState state) { _state = state; }
    
    /**
    * Returns a List of all Communications started by Terminal
    *
    * @return List of Communications started by this Terminal
    */
    public List<Communication> getStartedCommunications() { return _sentCommunications; }
    
    /**
    * Returns a List of all Communications received by Terminal
    *
    * @return List of Communications received by Terminal
    */
    public List<Communication> getReceivedCommunications() { return _receivedCommunications; }
    
    public List<Client> getClientsObserver() { return _clientObservers; }
    
    public Communication getUnpaidCommunicationById(Integer id)
    throws InvalidCommunicationPayment {
        for(Communication c : _sentCommunications) {
            if(c.getNumber().equals(id)) {
                if(c.isFinished() && !c.isPaid()) {
                    return c;
                } else {
                    break;
                }
            }
        }
        throw new InvalidCommunicationPayment(id);
    }
    /**
    * Returns Terminal's currently active Communication
    *
    * @return Current active Communication
    *
    * @throws NoActiveCommunication if there is not active communication
    */
    public Communication getActiveCommunication() throws NoActiveCommunicationException {
        if(_activeCommunication == null) {
            throw new NoActiveCommunicationException();
        }
        return _activeCommunication;
    }
    
    
    public void addFriend(String key, Network context)
                                throws prr.exceptions.UnknownTerminalKeyException {
        Terminal t = context.getTerminalByKey(key);
        
        // if trying to add Terminal to its own friends
        if(key.equals(_key)) {
            return;
        }
        // if terminal is already a friend
        if(isFriend(t)) {
            return;
        }
        
        // add to friends list
        _friends.put(t.getKey(), t);
        // add this Terminal to other Terminal's friend list
        // t.getFriends().put(_key, this);
    }
    
    /**
    *
    * @param key Key of Terminal to be removed from Terminals Friends
    * @param context The network context
    * @throws prr.exceptions.UnknownTerminalKeyException If Terminal with
    *                                                    specified key doesn't exist
    */
    public void removeFriend(String key, Network context)
                                throws prr.exceptions.UnknownTerminalKeyException {
        Terminal t = context.getTerminalByKey(key);
        
        // if trying to remove same Terminal from its friends list
        if(key.equals(_key)) {
            return;
        }
        
        // if Terminal is not a friend
        if(!isFriend(t)) {
            return;
        }
        
        // remove Terminal from friends
        _friends.remove(t.getKey());
        // remove this Terminal from other Terminal friends
        // t.getFriends().remove(_key);
    }
    
    /**
    * Returns True if Terminal with given key is a
    * friend
    *
    * @param key Terminal's key
    *
    * @return true if Terminal with given key is friends with
    *         given Terminal
    */
    public boolean isFriend(Terminal terminal) {
        return _friends.containsKey(terminal.getKey());
    }
    
    public void doNotify(String notificationType, String terminalKey){
        for(Client c : _clientObservers){
            // delivers a Notification to the Client given the Clients defined Notification method
            c.notify(c.new Notification(terminalKey, notificationType));
        }
    }
    
    
    public void changeTerminalState(TerminalState state, Network context) 
                                throws SameTerminalStateException {
        // check for same Terminal Type and throw exception if same
        if(_state.isSameType(state)) {
            throw new SameTerminalStateException();
        }
        
        // set Network data as dirty
        context.setDirty();
        
        _state.changeTerminalState(this, state);
    }
    
    /**
    * Checks if this terminal can end the current interactive communication.
    *
    * @return True if this terminal is Busy (i.e., it has an active Interactive Communication) and
    *          it was the originator of this communication.
    **/
    public boolean canEndCurrentCommunication() {
        return _state.canEndCurrentCommunication(this);
    }
    
    /**
    * Checks if this terminal can start a new communication.
    *
    * @return True if this terminal is neither Off or Busy, False otherwise.
    **/
    public boolean canStartCommunication() {
        return _state.canStartCommunication();
    }
    
    /**
    * Returns True if Terminal can receive a Text Communication, that is
    * if it isn't Off or Busy
    *
    * @return True or False 
    */
    public boolean canReceiveTextCommunication() {
        return _state.canReceiveTextCommunication();
    }
    
    /**
     * Returns True if the Terminal can receive the specified type of Interactive Communication
     * 
     * @param commType String identifying the type of Interactive Communication (VOICE or VIDEO)
     * @return True or False
     * @throws prr.exceptions.UnsupportedOperationException if either this Terminal or the destination Terminal
     *                                                      do not support the specified type of Communication
     */
    public abstract boolean canReceiveInteractiveCommunication(String commType)
    throws UnsupportedOperationException;
    
    /**
     * Sends a Text Communication to another Terminal
     * 
     * @param key Identifying key of the destination Terminal
     * @param commType String of the type of Interactive Comminucation (VOICE or VIDEO)
     * @param context The Network
     * @throws UnavailableTerminalException if this Terminal cannot receive an Interactive Communication
     *                                      i.e is currently Busy, Off or Silent
     * @throws prr.exceptions.UnknownTerminalKeyException if the Terminal with given key doesn't exist
     */
    public abstract void sendTextCommunication(String key, String text, Network context)
    throws UnavailableTerminalException, prr.exceptions.UnknownTerminalKeyException;
    
    /**
     * Starts an Interactive Communication with another Terminal
     * 
     * @param key Identifying key of the destination Terminal
     * @param commType String of the type of Interactive Comminucation (VOICE or VIDEO)
     * @param context The Network
     * @throws UnavailableTerminalException if this Terminal cannot receive an Interactive Communication
     *                                      i.e is currently Busy, Off or Silent
     * @throws prr.exceptions.UnknownTerminalKeyException if the Terminal with given key doesn't exist
     * @throws prr.exceptions.UnsupportedOperationException if either this Terminal or the destination Terminal
     *                                                      do not support the specified type of Communication
     */
    public abstract void sendInteractiveCommunication(String key, String commType, Network context)
    throws UnavailableTerminalException, prr.exceptions.UnknownTerminalKeyException,
                prr.exceptions.UnsupportedOperationException;
    
    /**
     * Ends this Terminal's current ongoing Interactive Communication 
     * 
     * @param duration Duration of the Interactive Communication
     * @param context The Network
     * @return
     */
    public Integer endInteractiveCommunication(Integer duration, Network context) {
        // define units of interactive communication (duration)
        _activeCommunication.setUnits(duration);
        
        // calculate and set communication price
        _activeCommunication.determinePrice(_owner.getClientType().getTariffTable());
        
        // get price to return
        Double price = _activeCommunication.getPrice();
        
        // set communication as finished and remove references in sender and receiver terminal
        _activeCommunication.setFinished();
        
        // add to Terminal's debt
        _debtBalance += price;
        
        // flag Network data as dirty
        context.setDirty();
        
        return (int) Math.round(price);
    }
    
    /**
     * Pays a Communication started by this Terminal
     * 
     * @param idComm ID of the Communication to be paid
     * @param context The Network
     * @throws InvalidCommunicationPayment if the Communication doesn't belong to this Terminal's
     *                                     sent Communications or is already paid
     */
    public void payCommunication(Integer idComm, Network context)
                                throws InvalidCommunicationPayment {
        Communication c = getUnpaidCommunicationById(idComm);
        _debtBalance -= c.getPrice();
        _paidBalance += c.getPrice();
        c.setPaid();

        _owner.pay();
        
        // flag Network data as dirty
        context.setDirty();
    }
    
    /**
    * Returns String representation of the Terminal
    *
    * Formats:
    * <p>
    * {@code type|terminal-key|owner-key|state|debt|paid}
    * <p>
    * {@code type|terminal-key|owner-key|state|debt|paid|friend1,friend2,...,friendN}
    *
    * @see java.lang.Object#toString()
    */
    @Override
    public String toString() {
        Set<String> friendSet;
        String friendsString = "";
        // if Terminal has friends, compose a string with friend's keys
        if(!_friends.isEmpty()) {
            friendSet = _friends.keySet();
            friendsString = String.join(",", friendSet);
        }
        
        return
            _key + "|" +
            _owner.getKey() + "|" +
            _state + "|" +
            (int) Math.round(_paidBalance) + "|" +
            (int) Math.round(_debtBalance) +
            (friendsString.isEmpty() ? "" : "|" + friendsString);
    }
}
