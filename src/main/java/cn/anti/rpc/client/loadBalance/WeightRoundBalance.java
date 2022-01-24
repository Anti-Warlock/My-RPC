package cn.anti.rpc.client.loadBalance;

import cn.anti.rpc.annotation.LoadBalanceAno;
import cn.anti.rpc.constant.RpcConstant;
import cn.anti.rpc.model.ServiceUrl;

import java.util.List;

/**
 * 加权轮询
 * @author zhuyusheng
 * @date 2021/12/30
 */
@LoadBalanceAno(RpcConstant.BALANCE_WEIGHT_ROUND)
public class WeightRoundBalance implements LoadBalance{

    private static int index;

    @Override
    public synchronized ServiceUrl getOne(List<ServiceUrl> serviceUrlList) {
        int totalWeight = serviceUrlList.stream().mapToInt(ServiceUrl::getWeight).sum();
        int offset = (index++) % totalWeight;
        for (ServiceUrl serviceUrl:serviceUrlList){
            if(serviceUrl.getWeight() > offset){
                return serviceUrl;
            }
            offset -= serviceUrl.getWeight();
        }
        return null;
    }
}
