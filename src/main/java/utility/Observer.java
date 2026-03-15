package utility;

//documentation written with the help of chatGPT


/**
 * Represents an observer in the Observer design pattern.
 * 
 * <p>This interface defines a contract for objects that wish to be notified
 * about changes or events occurring in a {@link Subject}. An observer registers
 * itself with a subject and implements the {@code update} method, which the subject
 * calls to notify the observer of state changes.
 * 
 * <p>Being a functional interface, it can be implemented using lambda expressions
 * or method references for concise observer definitions.
 * 
 * <h3>Usage Example:</h3>
 * <pre>
 * Observer observer = () -> System.out.println("Subject state changed!");
 * subject.attach(observer);
 * </pre>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 * 
 * @see Subject
 */
@FunctionalInterface
public interface Observer {

    /**
     * Called by the subject to notify this observer that its state has changed.
     * Implementations should define what action to take upon receiving this notification.
     */
    public abstract void update();
}

