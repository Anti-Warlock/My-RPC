package cn.anti.rpc.client.net;

import java.util.concurrent.*;

/**
 *  RpcFuture(响应结果)
 * @param <T>
 * @author zhuyusheng
 * @date 2022/1/3
 */
public class RpcFuture<T> implements Future<T> {

    private T response;

    /**
     * 请求和响应,一一对应
     */
    private CountDownLatch latch = new CountDownLatch(1);

    /**
     * Future请求时间
     */
    private Long startTime = System.currentTimeMillis();

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        if(null != response){
            return true;
        }
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        //如果连接未建立,state!=0,则线程等待
        //等待收到结果后才能返回响应
        latch.await();
        return response;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        //如果等待一定时长后,没有超时,则返回响应
        if(latch.await(timeout,unit)){
            return response;
        }
        return null;
    }

    public void setResponse(T response){
        this.response = response;
        //响应结果处理完成后,latch计数器减一
        latch.countDown();
    }

    public long getStartTime(){
        return startTime;
    }
}
