package cn.anti.rpc.client.loadBalance;

import cn.anti.rpc.annotation.LoadBalanceAno;
import cn.anti.rpc.constant.RpcConstant;
import cn.anti.rpc.model.ServiceUrl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平滑加权轮询
 * @author zhuyusheng
 * @date 2021/12/30
 */
@LoadBalanceAno(RpcConstant.BALANCE_SMOOTH_WEIGHT_ROUND)
public class SmoothWeightRoundBalance implements LoadBalance{

    private static final Map<String,Integer> map = new HashMap<>();

    @Override
    public synchronized ServiceUrl getOne(List<ServiceUrl> serviceUrlList) {
        /**
         * A B C [4,3,2] totalWeight=9
         * 次数   请求前的currentWeight(请求前的currentWeight+原始比重)   选中节点maxWeight   请求后的currentWeight(请求前的maxWeight-totalWeight)
         * 1        [4+4,3+3,2+2]                                           A                [8-9,6,4]
         * 2        [-1+4,6+3,4+2]                                          B                [3,9-9,6]
         * 3        [3+4,0+3,6+2]                                           C                [7,3,8-9]
         * 4        [7+4,3+3,-1+2]                                          A                [2,6,1]
         */
        //map初始化,存入原始比重
        serviceUrlList.forEach(serviceUrl ->
                map.computeIfAbsent(serviceUrl.toString(), key -> serviceUrl.getWeight())
        );
        //总权重
        int totalWeight = serviceUrlList.stream().mapToInt(ServiceUrl::getWeight).sum();
        ServiceUrl maxWeight = null;
        //设置当前轮次的最大权重
        for(ServiceUrl serviceUrl:serviceUrlList){
            Integer currentWeight = map.get(serviceUrl.toString());
            if(null == maxWeight || currentWeight > map.get(maxWeight.toString())){
                maxWeight = serviceUrl;
            }
        }
        //削弱最大权重,重新赋值
        if(null != maxWeight){
            map.put(maxWeight.toString(), map.get(maxWeight.toString())-totalWeight);
        }
        //权重累加
        for(ServiceUrl serviceUrl:serviceUrlList){
            Integer currentWeight = map.get(serviceUrl.toString());
            map.put(serviceUrl.toString(),currentWeight+serviceUrl.getWeight());
        }
        return maxWeight;
    }
}
