/*-
 * #%L
 * com.oceanbase:obkv-table-client
 * %%
 * Copyright (C) 2021 - 2024 OceanBase
 * %%
 * OBKV Table Client Framework is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * #L%
 */

package com.alipay.oceanbase.rpc.protocol.payload.impl.execute;

import com.alipay.oceanbase.rpc.protocol.payload.AbstractPayload;
import com.alipay.oceanbase.rpc.protocol.payload.impl.execute.query.ObNewRange;
import com.alipay.oceanbase.rpc.util.Serialization;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class ObTableSingleOp extends AbstractPayload {
    private ObTableOperationType   singleOpType;
    private ObTableSingleOpFlag singleOpFlag = new ObTableSingleOpFlag();
    private ObTableSingleOpQuery query = new ObTableSingleOpQuery();
    private List<ObTableSingleOpEntity> entities = new ArrayList<>();

    /*
     * Encode.
     */
    @Override
    public byte[] encode() {
        byte[] bytes = new byte[(int) getPayloadSize()];
        int idx = 0;

        // 0. encode header
        idx = encodeHeader(bytes, idx);

        // 1. encode op type
        byte opTypeVal = singleOpType.getByteValue();
        System.arraycopy(Serialization.encodeI8(opTypeVal), 0, bytes, idx, 1);
        idx += 1;

        long flag = singleOpFlag.getValue();
        int len = Serialization.getNeedBytes(flag);
        System.arraycopy(Serialization.encodeVi64(flag), 0, bytes, idx, len);
        idx += len;

        // 2. encode single op query
        len = (int) query.getPayloadSize();
        System.arraycopy(query.encode(), 0, bytes, idx, len);
        idx += len;

        // 3. encode entities
        len = Serialization.getNeedBytes(entities.size());
        System.arraycopy(Serialization.encodeVi64(entities.size()), 0, bytes, idx, len);
        idx += len;
        for (ObTableSingleOpEntity entity : entities) {
            len = (int) entity.getPayloadSize();
            System.arraycopy(entity.encode(), 0, bytes, idx, len);
            idx += len;
        }

        return bytes;
    }

    /*
     * Decode.
     */
    @Override
    public Object decode(ByteBuf buf) {
        super.decode(buf);

        this.singleOpType = ObTableOperationType.valueOf(Serialization.decodeI8(buf.readByte()));
        this.singleOpFlag.setValue(Serialization.decodeVi64(buf));
        this.query.decode(buf);
        int len = (int) Serialization.decodeVi64(buf);
        for (int i = 0; i < len; i++) {
            ObTableSingleOpEntity entity = new ObTableSingleOpEntity();
            entity.decode(buf);
            entities.add(entity);
        }

        return this;
    }

    /*
     * Get payload content size.
     */
    @Override
    public long getPayloadContentSize() {

        long payloadContentSize = Serialization.getNeedBytes(singleOpType.getByteValue());
        payloadContentSize += Serialization.getNeedBytes(singleOpFlag.getValue());
        payloadContentSize += query.getPayloadSize();
        payloadContentSize += Serialization.getNeedBytes(entities.size());
        for (ObTableSingleOpEntity entity : entities) {
            payloadContentSize += entity.getPayloadSize();
        }
        return payloadContentSize;
    }

    public List<ObNewRange> getScanRange() {
        return query.getScanRanges();
    }

    public void addScanRange(ObNewRange range)
    {
       this.addScanRange(range);
    }

//    public void setMutations(ObTableBatchOperation mutations) {
//        this.queryAndMutate.setMutations(mutations);
//    }

    public void setIsCheckNoExists(boolean isCheckNoExists) {
        singleOpFlag.setIsCheckNotExists(isCheckNoExists);
    }

    public ObTableOperationType getSingleOpType() {
        return singleOpType;
    }

    public void setSingleOpType(ObTableOperationType singleOpType) {
        this.singleOpType = singleOpType;
    }

    public ObTableSingleOpQuery getQuery() {
        return query;
    }

    public void setQuery(ObTableSingleOpQuery query) {
        this.query = query;
    }

    public List<ObTableSingleOpEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<ObTableSingleOpEntity> entities) {
        this.entities = entities;
    }

    public void addEntity(ObTableSingleOpEntity entity) {
       this.entities.add(entity);
    }
}
