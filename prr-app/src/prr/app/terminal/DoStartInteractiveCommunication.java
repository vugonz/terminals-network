package prr.app.terminal;

import prr.Network;
import prr.app.exceptions.UnknownTerminalKeyException;
import prr.terminals.BusyTerminalState;
import prr.terminals.OffTerminalState;
import prr.terminals.Terminal;
import pt.tecnico.uilib.forms.Form;
import pt.tecnico.uilib.menus.CommandException;
//FIXME add more imports if needed

/**
 * Command for starting communication.
 */
class DoStartInteractiveCommunication extends TerminalCommand {

	DoStartInteractiveCommunication(Network context, Terminal terminal) {
		super(Label.START_INTERACTIVE_COMMUNICATION, context, terminal, receiver -> receiver.canStartCommunication());
		addStringField("key", Prompt.terminalKey());
		addOptionField("commType", Prompt.commType(), "VOICE", "VIDEO");
	}

	@Override
	protected final void execute() throws CommandException {
		try {
			_receiver.sendInteractiveCommunication(stringField("key"), stringField("commType"), _network);
		} catch (prr.exceptions.UnknownTerminalKeyException utke) {
			throw new UnknownTerminalKeyException(utke.getKey());
		} catch (prr.exceptions.UnavailableTerminalException ute) {
			if(ute.getState().isBusy()) 
				_display.popup(Message.destinationIsBusy(ute.getKey()));
			else if(ute.getState().isSilent()) 
				_display.popup(Message.destinationIsSilent(ute.getKey()));
			else 
				_display.popup(Message.destinationIsOff(ute.getKey()));
		} catch (prr.exceptions.UnsupportedOperationException uoe) {
			if(stringField("key").equals(uoe.getKey())) {
				_display.popup(Message.unsupportedAtDestination(uoe.getKey(), stringField("commType")));
			} else
				_display.popup(Message.unsupportedAtOrigin(uoe.getKey(), stringField("commType")));
		}
	}
}
