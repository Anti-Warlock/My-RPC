package cn.anti.rpc.client.serializeProtocol;

import cn.anti.rpc.annotation.SerializeProtocolAno;
import cn.anti.rpc.constant.RpcConstant;
import cn.anti.rpc.domain.RpcRequest;
import cn.anti.rpc.domain.RpcResponse;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian序列化实现
 * @author zhuyusheng
 * @date 2021/12/30
 */
@SerializeProtocolAno(RpcConstant.PROTOCOL_HESSIAN)
public class HessianSerializeProtocol implements SerializeProtocol{

    /**
     * Hessian序列化对象
     * @param object
     * @return
     * @throws IOException
     */
    private static byte[] hessianSerialize(Object object) throws IOException {
        ByteArrayOutputStream bos = null;
        Hessian2Output oo = null;
        byte[] result = null;
        try {
            bos = new ByteArrayOutputStream();
            oo = new Hessian2Output(bos);
            oo.writeObject(object);
            oo.flush();
            result = bos.toByteArray();
        } finally {
            oo.close();
            bos.close();
        }
        return result;
    }

    /**
     * Hessian反序列化
     * @param bytes
     * @return
     */
    public static <T> T hessianDeserialize(byte[] bytes, Class<T> clazz) throws IOException{
        ByteArrayInputStream is = null;
        Hessian2Input input = null;
        Object result = null;
        try{
            is = new ByteArrayInputStream(bytes);
            input = new Hessian2Input(is);
            result = input.readObject();
        }finally {
            is.close();
            input.close();
        }
        return (T) result;
    }

    @Override
    public byte[] marshallingRequest(RpcRequest request) throws Exception {
        return hessianSerialize(request);
    }

    @Override
    public RpcRequest unmarshallRequest(byte[] reqData) throws Exception {
        return hessianDeserialize(reqData,RpcRequest.class);
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) throws Exception {
        return hessianSerialize(response);
    }

    @Override
    public RpcResponse unmarshallResponse(byte[] resData) throws Exception {
        return hessianDeserialize(resData,RpcResponse.class);
    }
}
