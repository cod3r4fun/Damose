package utility;

//documentation written with the help of chatGPT


/**
 * Defines the contract for a Subject in the Observer design pattern.
 * 
 * <p>A Subject maintains a list of observers and provides mechanisms
 * to attach or detach observers dynamically. When the subject's state changes,
 * it is responsible for notifying all registered observers of these changes.
 * 
 * <p>This interface abstracts the core behaviors required to manage and notify observers.
 * Concrete implementations should internally manage the collection of observers and 
 * ensure thread safety if needed.
 * 
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Attach new observers to receive updates.</li>
 *   <li>Detach existing observers to stop receiving updates.</li>
 *   <li>Notify all attached observers of changes or events.</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong>
 * <pre>
 * Subject subject = new ConcreteSubject();
 * Observer observer = new ConcreteObserver();
 * subject.attach(observer);
 * 
 * // When subject state changes
 * subject.notifyObservers();
 * </pre>
 * 
 * @author Franco Della Negra
 * @version 1.0
 * @since 1.0
 * 
 * @see Observer
 */
public interface Subject {

    /**
     * Registers an observer so that it will receive notifications
     * when the subject's state changes.
     * 
     * @param o the observer to attach
     */
    public abstract void attach(Observer o);

    /**
     * Removes a previously registered observer. The observer will no longer
     * receive notifications from this subject.
     * 
     * @param o the observer to detach
     */
    public abstract void detach(Observer o);

    /**
     * Notifies all currently attached observers of a change in the subject's state.
     * The manner and content of the notification depend on the concrete implementation.
     */
    public abstract void notifyObservers();
}
