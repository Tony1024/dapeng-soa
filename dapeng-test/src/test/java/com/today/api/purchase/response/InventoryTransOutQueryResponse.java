/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.today.api.purchase.response;

        import java.util.Optional;
        import com.github.dapeng.org.apache.thrift.TException;
        import com.github.dapeng.org.apache.thrift.protocol.TCompactProtocol;
        import com.github.dapeng.util.TCommonTransport;

        /**
         * Autogenerated by Dapeng-Code-Generator (2.2.0-SNAPSHOT)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING

        *

 盘点查询请求返回结果
 转闭店前财务店号
 转闭类型
 关闭店账表日
 转店后财务店号

        **/
        public class InventoryTransOutQueryResponse{
        
            /**
            *
            **/
            public java.util.List<StoreTransInfo> storeTransInfoList = new java.util.ArrayList();
            public java.util.List<StoreTransInfo> getStoreTransInfoList(){ return this.storeTransInfoList; }
            public void setStoreTransInfoList(java.util.List<StoreTransInfo> storeTransInfoList){ this.storeTransInfoList = storeTransInfoList; }

            public java.util.List<StoreTransInfo> storeTransInfoList(){ return this.storeTransInfoList; }
            public InventoryTransOutQueryResponse storeTransInfoList(java.util.List<StoreTransInfo> storeTransInfoList){ this.storeTransInfoList = storeTransInfoList; return this; }
          

        public static byte[] getBytesFromBean(InventoryTransOutQueryResponse bean) throws TException {
          byte[] bytes = new byte[]{};
          TCommonTransport transport = new TCommonTransport(bytes, TCommonTransport.Type.Write);
          TCompactProtocol protocol = new TCompactProtocol(transport);

          new com.today.api.purchase.response.serializer.InventoryTransOutQueryResponseSerializer().write(bean, protocol);
          transport.flush();
          return transport.getByteBuf();
        }

        public static InventoryTransOutQueryResponse getBeanFromBytes(byte[] bytes) throws TException {
          TCommonTransport transport = new TCommonTransport(bytes, TCommonTransport.Type.Read);
          TCompactProtocol protocol = new TCompactProtocol(transport);
          return new com.today.api.purchase.response.serializer.InventoryTransOutQueryResponseSerializer().read(protocol);
        }

        public String toString(){
          StringBuilder stringBuilder = new StringBuilder("{");
            stringBuilder.append("\"").append("storeTransInfoList").append("\":").append(this.storeTransInfoList).append(",");
    
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
            stringBuilder.append("}");

          return stringBuilder.toString();
        }
      }
      