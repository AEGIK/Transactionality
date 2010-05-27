package transactionality;

/** @author Christoffer Lerno */
public class ValidationException extends RuntimeException
{
	public ValidationException()
	{
	}

	public ValidationException(Throwable cause)
	{
		super(cause);
	}

	public ValidationException(String message)
	{
		super(message);
	}

	public ValidationException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
