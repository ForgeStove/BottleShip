package io.github.forgestove.bottle_ship.config;
import java.io.Serial;
@SuppressWarnings("unused")
public class SerializationException extends Exception {
	@Serial private static final long serialVersionUID = 1L;
	public SerializationException(Throwable cause) {
		super(cause);
	}
	public SerializationException(String message) {
		super(message);
	}
}

