package prr.app.clients;

import prr.Network;
import prr.app.exceptions.UnknownClientKeyException;
import prr.clients.Client;
import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
//FIXME add more imports if needed

/**
 * Show the payments and debts of a client.
 */
class DoShowClientPaymentsAndDebts extends Command<Network> {

	DoShowClientPaymentsAndDebts(Network receiver) {
		super(Label.SHOW_CLIENT_BALANCE, receiver);
		addStringField("key", Prompt.key());
	}

	@Override
	protected final void execute() throws CommandException {
		try {
			Client c = _receiver.getClientByKey(stringField("key"));
			_display.popup(Message.clientPaymentsAndDebts(
				stringField("key"), 
					(int) Math.round(c.getClientPaidBalance()), 
						(int) Math.round(c.getClientDebtBalance())));
		} catch (prr.exceptions.UnknownClientKeyException e) {
			throw new UnknownClientKeyException(stringField("key"));
		}	
	}
}
