package prr.exceptions;

import java.io.Serial;

public class InvalidCommunicationPayment extends Exception {
	/** Serial number for serialization. */
    @Serial
	private static final long serialVersionUID = 202208091753L;

	private Integer _id;

	/** @param id the duplicated id */
	public InvalidCommunicationPayment(Integer id) {
        _id = id;
	}

    public Integer getId() {
        return _id;
    }

}