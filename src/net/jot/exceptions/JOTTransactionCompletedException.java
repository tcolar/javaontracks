/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.exceptions;

/**
 *
 * @author thibautc
 */
public class JOTTransactionCompletedException extends Exception
{

    public JOTTransactionCompletedException()
    {
        super("This transaction was already completed (commited or rolled back)");
    }
}
