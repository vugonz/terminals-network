package prr.app.clients;

import prr.Network;
import prr.app.exceptions.UnknownClientKeyException;
import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;

/**
 * Show specific client: also show previous notifications.
 */
class DoShowClient extends Command<Network> {

	DoShowClient(Network receiver) {
		super(Label.SHOW_CLIENT, receiver);
        addStringField("key", Prompt.key());
	}

	@Override
	protected final void execute() throws CommandException {
        try {
            _display.popup(_receiver.getClientByKey(stringField("key")));
			_display.popup(_receiver.getClientByKey(stringField("key")).getUnhandledNotification());
        } catch (prr.exceptions.UnknownClientKeyException e) {
            throw new UnknownClientKeyException(e.getKey());
        }
	}
}
