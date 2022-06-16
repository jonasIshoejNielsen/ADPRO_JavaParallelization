interface MessageBuffer<T> {
    void sendMessage(T elem);
    T receiveMessage();
}