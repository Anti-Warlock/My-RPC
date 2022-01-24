package cn.anti.rpc.client.serializeProtocol;

import cn.anti.rpc.annotation.SerializeProtocolAno;
import cn.anti.rpc.constant.RpcConstant;
import cn.anti.rpc.domain.RpcRequest;
import cn.anti.rpc.domain.RpcResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Java序列化实现方式
 * @author zhuyusheng
 * @date 2021/12/24
 */
@SerializeProtocolAno(RpcConstant.PROTOCOL_JAVA)
public class JavaSerializeProtocol implements SerializeProtocol{

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(obj);
        return bout.toByteArray();
    }

    @Override
    public byte[] marshallingRequest(RpcRequest request) throws Exception {
        return this.serialize(request);
    }

    @Override
    public RpcRequest unmarshallRequest(byte[] data) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        return (RpcRequest) in.readObject();
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) throws Exception {
        return this.serialize(response);
    }

    @Override
    public RpcResponse unmarshallResponse(byte[] data) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        return (RpcResponse) in.readObject();
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final int objectNum = 1000000;
        List<RpcRequest> list = new ArrayList<>(objectNum);
        for (int i=0;i<objectNum;i++){
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setServiceName("a"+i);
            rpcRequest.setParameterTypes(new Class<?>[]{List.class,JavaSerializeProtocol.class});
            rpcRequest.setParameters(new Object[]{1,"xx"});
            rpcRequest.setMethodName("A method");
            rpcRequest.setHeaders(new HashMap<>());
            list.add(rpcRequest);
        }
        JavaSerializeProtocol java = new JavaSerializeProtocol();
        ProtoBufSerializeProtocol protoBuf = new ProtoBufSerializeProtocol();
        HessianSerializeProtocol hessian = new HessianSerializeProtocol();

        long t1 = System.currentTimeMillis();
        int size = 0;
        for(int j=0;j<list.size();j++){
            byte[] bytes = java.marshallingRequest(list.get(j));
            size += bytes.length;
        }
        long t2 = System.currentTimeMillis();
        // java序列化耗时：7926ms,byte数组大小为：568888890
        System.out.println("java序列化耗时："+(t2-t1)+"ms,byte数组大小为："+size);

        long t3 = System.currentTimeMillis();
        int sum = 0;
        for(int j=0;j<list.size();j++){
            RpcRequest rpcRequest = list.get(j);
            byte[] bytes = protoBuf.marshallingRequest(rpcRequest);
            sum += bytes.length;
        }
        long t4 = System.currentTimeMillis();
        // protobuf序列化耗时：1532ms,byte数组大小为：155888890
        System.out.println("protobuf序列化耗时："+(t4-t3)+"ms,byte数组大小为："+sum);

        long t5 = System.currentTimeMillis();
        int len = 0;
        for(int j=0;j<list.size();j++){
            RpcRequest rpcRequest = list.get(j);
            byte[] bytes = protoBuf.marshallingRequest(rpcRequest);
            len += bytes.length;
        }
        long t6 = System.currentTimeMillis();
        // protobuf序列化耗时：1532ms,byte数组大小为：155888890
        System.out.println("hessian序列化耗时："+(t6-t5)+"ms,byte数组大小为："+len);

    }
}
