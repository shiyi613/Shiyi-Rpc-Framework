package com.shiyi.remoting.transport.netty.client;

import com.shiyi.constants.RpcTimeoutConstant;
import com.shiyi.enums.RpcResponseCodeEnum;
import com.shiyi.exception.RpcException;
import com.shiyi.exception.TimeOutException;
import com.shiyi.remoting.constants.RpcConstants;
import com.shiyi.remoting.dto.RpcRequest;
import com.shiyi.remoting.dto.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class ResultFuture extends CompletableFuture<RpcResponse<Object>> {

    private static final Map<String, ResultFuture> FUTURES = new ConcurrentHashMap<>();

    private final String id;

    private final RpcRequest rpcRequest;

    private final String timeout;

    private final long start = System.currentTimeMillis();

    private volatile long sent;

    private TimeoutCheckTask timeoutCheckTask;

    private ExecutorService executor;

    private static ResultFuture lastFuture;


    public ResultFuture() {
        this.id = null;
        this.rpcRequest = null;
        this.timeout = null;
    }

    public ResultFuture(RpcRequest _rpcRequest, String _timeout) {
        this.id = _rpcRequest.getRequestId();
        this.rpcRequest = _rpcRequest;
        this.timeout = _timeout;
        this.timeoutCheckTask = new TimeoutCheckTask(id, _timeout);
        this.executor = new ThreadPoolExecutor(10, 20, 60, TimeUnit.MINUTES, new ArrayBlockingQueue<>(50));
    }

    public String getId(){
        return this.id;
    }

    public ExecutorService getExecutor() {
        return this.executor;
    }

    public long getStart() {
        return this.start;
    }

    public static ResultFuture getFuture() {
        if (lastFuture == null) {
            return new ResultFuture();
        }
        return lastFuture;
    }

    public void put(String requestId, ResultFuture future) {
        lastFuture = future;
        FUTURES.put(requestId, future);
    }

    public static void received(RpcResponse<Object> rpcResponse) {
        ResultFuture future = FUTURES.remove(rpcResponse.getRequestId());
        if (future != null) {
            TimeoutCheckTask task = future.timeoutCheckTask;
            task.cancel();
            future.doReceived(rpcResponse);
        } else {
            log.warn("The timeout response finally returned at "
                    + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()))
                    + ", response status is " + rpcResponse.getCode());
        }

    }

    private void doReceived(RpcResponse<Object> res) {
        if (res == null) {
            throw new IllegalStateException("response cannot be null");
        }
        if (res.getCode() == RpcResponseCodeEnum.SUCCESS.getCode()) {
            this.complete(res);
        } else if (res.getCode() == RpcResponseCodeEnum.CLIENT_TIMEOUT.getCode() || res.getCode() == RpcResponseCodeEnum.SERVER_TIMEOUT.getCode()) {
            this.completeExceptionally(new TimeOutException(res.getMessage()));
        } else {
            this.completeExceptionally(new RpcException(res.getMessage()));
        }
    }


    public static void sent(RpcRequest rpcRequest) {
        ResultFuture future = FUTURES.get(rpcRequest.getRequestId());
        if (future != null) {
            future.doSent();
            // 启动超时扫描线程
            Thread th = new Thread(future.timeoutCheckTask, "ResponseTimeoutScanTimer");
            th.setDaemon(true);
            th.start();
        }
    }

    private void doSent() {
        sent = System.currentTimeMillis();
    }

    private boolean isSent() {
        return sent > 0;
    }

    private String getTimeoutMessage(boolean scan) {
        long nowTimestamp = System.currentTimeMillis();
        return (sent > 0 ? "Waiting server-side response timeout" : "Sending request timeout in client-side")
                + (scan ? " by scan timer" : "") + ". start time: "
                + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(start))) + ", end time: "
                + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(nowTimestamp))) + ","
                + (sent > 0 ? " client elapsed: " + (sent - start)
                + " ms, server elapsed: " + (nowTimestamp - sent)
                : " elapsed: " + (nowTimestamp - start)) + " ms, timeout: "
                + timeout + " ms, request: " + rpcRequest;
    }


    private static class TimeoutCheckTask implements Runnable {

        private final String requestID;

        private final String timeout;

        private Thread thread;

        public TimeoutCheckTask(String requestId, String _timeout) {
            this.requestID = requestId;
            this.timeout = _timeout;
        }

        @Override
        public void run() {
            thread = Thread.currentThread();
            ResultFuture future = FUTURES.get(requestID);
            if (future == null || future.isDone()) {
                return;
            }
            try {
                while (!future.isDone()) {
                    // 超时
                    if (System.currentTimeMillis() - future.getStart() > Integer.parseInt(timeout)) {
                        if (future.getExecutor() != null) {
                            future.getExecutor().execute(() -> notifyTimeout(future));
                        } else {
                            notifyTimeout(future);
                        }
                        break;
                    }else if(future.isCancelled()){        // future已经被取消
                        if (future.getExecutor() != null) {
                            future.getExecutor().execute(() -> notifyCancelled(future));
                        } else {
                            notifyCancelled(future);
                        }
                        break;
                    }else if(Thread.currentThread().isInterrupted()){      // 线程被中断
                        if (future.getExecutor() != null) {
                            future.getExecutor().execute(() -> notifyInterrupted(future));
                        } else {
                            notifyInterrupted(future);
                        }
                        break;
                    }
                    Thread.sleep(3);
                }
            }catch (InterruptedException ignored){

            }catch (Throwable e){
                log.error("Exception when scan the timeout invocation of remoting.", e);
            }
        }

        private void notifyTimeout(ResultFuture future) {
            // create exception response.
            RpcResponse<Object> timeoutResponse = new RpcResponse<>(future.getId());
            // set timeout status.
            timeoutResponse.setCode(future.isSent() ? RpcResponseCodeEnum.SERVER_TIMEOUT.getCode() : RpcResponseCodeEnum.CLIENT_TIMEOUT.getCode());
            timeoutResponse.setMessage(future.getTimeoutMessage(true));
            // handle response.
            ResultFuture.received(timeoutResponse);
        }

        private void notifyCancelled(ResultFuture future) {
            RpcResponse<Object> cancelledResponse = RpcResponse.fail(future.getId(), RpcResponseCodeEnum.FUTURE_CANCELLED);
            // handle response.
            ResultFuture.received(cancelledResponse);
        }

        private void notifyInterrupted(ResultFuture future) {
            RpcResponse<Object> interruptedResponse = RpcResponse.fail(future.getId(), RpcResponseCodeEnum.THREAD_INTERRUPTED);
            // handle response.
            ResultFuture.received(interruptedResponse);
        }

        public void cancel() {
            if(thread != null && thread.isAlive()){
                thread.interrupt();
            }
        }

    }



}
