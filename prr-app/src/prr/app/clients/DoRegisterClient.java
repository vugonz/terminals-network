package prr.app.clients;

import prr.Network;
import prr.app.exceptions.DuplicateClientKeyException;
import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
//FIXME add more imports if needed

/**
 * Register new client.
 */
class DoRegisterClient extends Command<Network> {

	DoRegisterClient(Network receiver) {
		super(Label.REGISTER_CLIENT, receiver);
		addStringField("key", Prompt.key());
		addStringField("name", Prompt.name());
		addIntegerField("taxId", Prompt.taxId());
	}

	@Override
	protected final void execute() throws CommandException {
		try {
			_receiver.registerClient(
						    stringField("key"), 
							    stringField("name"), 
							        integerField("taxId"));
		} catch(prr.exceptions.DuplicateClientKeyException e) {
			throw new DuplicateClientKeyException(e.getKey());
		}
	}

}
