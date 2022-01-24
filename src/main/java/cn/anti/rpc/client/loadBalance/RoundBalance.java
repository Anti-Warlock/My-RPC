package cn.anti.rpc.client.loadBalance;

import cn.anti.rpc.annotation.LoadBalanceAno;
import cn.anti.rpc.constant.RpcConstant;
import cn.anti.rpc.model.ServiceUrl;

import java.util.List;

/**
 * 轮询算法
 * @author zhuyusheng
 * @date 2021/12/30
 */
@LoadBalanceAno(RpcConstant.BALANCE_ROUND)
public class RoundBalance implements LoadBalance{

    private int index;

    @Override
    public synchronized ServiceUrl getOne(List<ServiceUrl> serviceUrlList) {
        //设计成同步是为了防止多线程情况下index超过list.size
        if(index == serviceUrlList.size()){
            index = 0;
        }
        return serviceUrlList.get(index++);
    }
}
