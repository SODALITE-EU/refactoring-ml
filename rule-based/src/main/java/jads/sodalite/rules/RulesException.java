package jads.sodalite.rules;

/**
 * Exception class for rules related exception.
 *
 * @author The ROAD team, Swinburne University of Technology
 */
@SuppressWarnings("serial")
public class RulesException extends Exception {

    /**
     * Constructor for a custom exception message to be placed
     *
     * @param exceptionStr
     */
    public RulesException(String exceptionStr) {
        super(exceptionStr);
    }

    /**
     * Default constructor
     */
    public RulesException() {
        super();
    }
}
