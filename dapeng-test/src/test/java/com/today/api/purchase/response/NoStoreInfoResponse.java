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
         * Autogenerated by Dapeng-Code-Generator (2.2.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING

        *

 售罄信息返回体
 记录SKU在某个时刻库存为0或负数

        **/
        public class NoStoreInfoResponse{
        
            /**
            *
            **/
            public java.util.List<NoStoreInfo> noStoreInfoList = new java.util.ArrayList();
            public java.util.List<NoStoreInfo> getNoStoreInfoList(){ return this.noStoreInfoList; }
            public void setNoStoreInfoList(java.util.List<NoStoreInfo> noStoreInfoList){ this.noStoreInfoList = noStoreInfoList; }

            public java.util.List<NoStoreInfo> noStoreInfoList(){ return this.noStoreInfoList; }
            public NoStoreInfoResponse noStoreInfoList(java.util.List<NoStoreInfo> noStoreInfoList){ this.noStoreInfoList = noStoreInfoList; return this; }
          

        public static byte[] getBytesFromBean(NoStoreInfoResponse bean) throws TException {
          byte[] bytes = new byte[]{};
          TCommonTransport transport = new TCommonTransport(bytes, TCommonTransport.Type.Write);
          TCompactProtocol protocol = new TCompactProtocol(transport);

          new com.today.api.purchase.response.serializer.NoStoreInfoResponseSerializer().write(bean, protocol);
          transport.flush();
          return transport.getByteBuf();
        }

        public static NoStoreInfoResponse getBeanFromBytes(byte[] bytes) throws TException {
          TCommonTransport transport = new TCommonTransport(bytes, TCommonTransport.Type.Read);
          TCompactProtocol protocol = new TCompactProtocol(transport);
          return new com.today.api.purchase.response.serializer.NoStoreInfoResponseSerializer().read(protocol);
        }

        public String toString(){
          StringBuilder stringBuilder = new StringBuilder("{");
            stringBuilder.append("\"").append("noStoreInfoList").append("\":").append(this.noStoreInfoList).append(",");
    
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
            stringBuilder.append("}");

          return stringBuilder.toString();
        }
      }
      