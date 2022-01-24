package cn.anti.rpc.client.serializeProtocol;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import cn.anti.rpc.annotation.SerializeProtocolAno;
import cn.anti.rpc.constant.RpcConstant;
import cn.anti.rpc.domain.RpcRequest;
import cn.anti.rpc.domain.RpcResponse;

/**
 * ProtoBuf序列化实现
 * @author zhuyusheng
 * @date 2021/12/29
 */
@SerializeProtocolAno(RpcConstant.PROTOCOL_PROTOBUF)
public class ProtoBufSerializeProtocol implements SerializeProtocol{

    /**
     * 将目标类序列化为byte数组
     *
     * @param source
     * @param <T>
     * @return
     */
    private static <T> byte[] serialize(T source) {
        Schema<T> schema = RuntimeSchema.getSchema((Class<T>) source.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        final byte[] result;
        try {
            result = ProtobufIOUtil.toByteArray(source, schema, buffer);
        } finally {
            buffer.clear();
        }
        return result;
    }

    /**
     * 将byte数组序列化为目标类
     *
     * @param source
     * @param clazz
     * @param <T>
     * @return
     */
    private static <T> T deserialize(byte[] source, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T t = schema.newMessage();
        ProtobufIOUtil.mergeFrom(source, t, schema);
        return t;
    }

    @Override
    public byte[] marshallingRequest(RpcRequest request) throws Exception {
        return serialize(request);
    }

    @Override
    public RpcRequest unmarshallRequest(byte[] reqData) throws Exception {
        return deserialize(reqData,RpcRequest.class);
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) throws Exception {
        return serialize(response);
    }

    @Override
    public RpcResponse unmarshallResponse(byte[] resData) throws Exception {
        return deserialize(resData,RpcResponse.class);
    }
}
