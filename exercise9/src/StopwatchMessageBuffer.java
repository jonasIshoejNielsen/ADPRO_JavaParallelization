import io.reactivex.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StopwatchMessageBuffer<T> implements MessageBuffer {

    ConcurrentLinkedQueue<T> messages = new ConcurrentLinkedQueue<T>();

    @Override
    public void sendMessage(Object elem) {
        messages.add((T)elem);
    }

    @Override
    public Object receiveMessage() {
        T temp;

        while ((temp = messages.poll()) == null){ }

        return temp;
    }
}
