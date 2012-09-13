package the.umbautologin;

/**
 * An exception that is thrown when there is a problem parsing form data out of
 * an HTML page.
 * 
 * @author michael
 */
@SuppressWarnings("serial")
class InvalidFormException extends Exception
{
    public InvalidFormException(String message)
    {
        super(message);
    }
}