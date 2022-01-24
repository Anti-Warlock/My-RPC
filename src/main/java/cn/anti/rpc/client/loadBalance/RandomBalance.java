package cn.anti.rpc.client.loadBalance;

import cn.anti.rpc.annotation.LoadBalanceAno;
import cn.anti.rpc.constant.RpcConstant;
import cn.anti.rpc.model.ServiceUrl;

import java.util.List;
import java.util.Random;

/**
 * 随机算法
 * @author zhuyusheng
 * @date 2021/12/30
 */
@LoadBalanceAno(RpcConstant.BALANCE_RANDOM)
public class RandomBalance implements LoadBalance{

    private static Random random = new Random();

    @Override
    public ServiceUrl getOne(List<ServiceUrl> serviceUrlList) {
        return serviceUrlList.get(random.nextInt(serviceUrlList.size()));
    }
}
