package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.stomp.StompEncoderDecoder;
import bgu.spl.net.impl.stomp.StompProtocol;

import java.io.Closeable;
import java.util.function.Supplier;

public interface Server<T> extends Closeable {

    /**
     * The main loop of the server, Starts listening and handling new clients.
     */
    void serve();

    /**
     *This function returns a new instance of a thread per client pattern server
     * @param port The port for the server socket
     * @param supplier A factory that creats new MessagingProtocols
     * @param supplier2 A factory that creats new MessageEncoderDecoder
     * @param <T> The Message Object for the protocol
     * @return A new Thread per client server
     */
    public static <T> Server< T>  threadPerClient(
            int port,
            Supplier<StompProtocol<T>> supplier,
            Supplier<StompEncoderDecoder> supplier2,ConnectionsImpl<T> connections) {

        return new BaseServer<T>(port, supplier, supplier2,connections) {
            @Override
            protected void execute(BlockingConnectionHandler<T>  handler) {
                new Thread(handler).start();
            }
        };

    }

    /**
     * This function returns a new instance of a reactor pattern server
     * @param nthreads Number of threads available for protocol processing
     * @param port The port for the server socket
     * @param protocolFactory A factory that creats new MessagingProtocols
     * @param encoderDecoderFactory A factory that creats new MessageEncoderDecoder
     * @param <T> The Message Object for the protocol
     * @return A new reactor server
     */
    public static <T> Server<T> reactor(
            int nthreads,
            int port,
            Supplier<StompProtocol<T>> protocolFactory,
            Supplier<StompEncoderDecoder> encoderDecoderFactory,ConnectionsImpl<T> connections) {
        return new Reactor<T>(nthreads, port, protocolFactory, encoderDecoderFactory,connections);
    }

}
