package br.com.gridreactive.exception;

import org.apache.commons.httpclient.HttpException;

/**
 * Base GridReactive Exception Class. basic HttpException handle
 * 
 * @author Wilson M da Penha Jr
 *
 */
public class GridReactiveException extends HttpException {
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -104831425731568578L;
	private String code;
	public static final String HTTP_400 = "400";
	public static final String HTTP_401 = "401";
	public static final String HTTP_403 = "403";
	public static final String HTTP_500 = "500";

	/**
	 * Constructs a new P2W exception with <code>null</code> as its detail
	 * message.
	 */
	public GridReactiveException() {
		super();
	}
	
	/**
	 * Constructs a new P2W exception with the specified detail message.
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later
	 *            retrieval by the {@link #getMessage()} method.
	 */
	public GridReactiveException(String message, String code) {
		super(message);
		this.code = code;
	}
	
	/**
	 * Constructs a new GridReactiveException with the specified detail message and
	 * cause.
	 * <p>
	 * Note that the detail message associated with <code>cause</code> is
	 * <i>not</i> automatically incorporated in this exception's detail
	 * message.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 */
	public GridReactiveException(String message, Throwable cause, String code) {
		super(message, cause);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
