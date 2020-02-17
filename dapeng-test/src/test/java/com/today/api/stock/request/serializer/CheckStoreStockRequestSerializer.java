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
package com.today.api.stock.request.serializer;
        import com.today.api.purchase.request.serializer.*;import com.today.api.common.serializer.*;import com.today.api.purchase.response.serializer.*;import com.today.api.stock.response.serializer.*;import com.today.api.stock.request.serializer.*;import com.today.api.stock.events.serializer.*;import com.today.api.stock.vo.serializer.*;

        import com.github.dapeng.core.*;
        import com.github.dapeng.org.apache.thrift.*;
        import com.github.dapeng.org.apache.thrift.protocol.*;

        import java.util.Optional;
        import java.util.concurrent.CompletableFuture;
        import java.util.concurrent.Future;

        /**
        * Autogenerated by Dapeng-Code-Generator (2.2.0-SNAPSHOT)
        *
        * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
        *
        **/
        public class CheckStoreStockRequestSerializer implements BeanSerializer<com.today.api.stock.request.CheckStoreStockRequest>{
        
      @Override
      public com.today.api.stock.request.CheckStoreStockRequest read(TProtocol iprot) throws TException{

      com.today.api.stock.request.CheckStoreStockRequest bean = new com.today.api.stock.request.CheckStoreStockRequest();
      TField schemeField;
      iprot.readStructBegin();

      while(true){
        schemeField = iprot.readFieldBegin();
        if(schemeField.type == TType.STOP){ break;}

        switch(schemeField.id){
          
              case 1:
              if(schemeField.type == TType.STRING){
              String elem0 = iprot.readString();
       bean.setStoreId(elem0);
            }else{
              TProtocolUtil.skip(iprot, schemeField.type);
            }
              break;
            
              case 2:
              if(schemeField.type == TType.LIST){
               TList _list0 = iprot.readListBegin();
        java.util.List<String> elem0 = new java.util.ArrayList<>(_list0.size);
        for(int _i0 = 0; _i0 < _list0.size; ++ _i0){
          String elem1 = iprot.readString();
          elem0.add(elem1);
        }
        iprot.readListEnd();
       bean.setSkuNos(elem0);
            }else{
              TProtocolUtil.skip(iprot, schemeField.type);
            }
              break;
            
          
            default:
            TProtocolUtil.skip(iprot, schemeField.type);
          
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      validate(bean);
      return bean;
    }
    
      @Override
      public void write(com.today.api.stock.request.CheckStoreStockRequest bean, TProtocol oprot) throws TException{

      validate(bean);
      oprot.writeStructBegin(new TStruct("CheckStoreStockRequest"));

      
            oprot.writeFieldBegin(new TField("storeId", TType.STRING, (short) 1));
            String elem0 = bean.getStoreId();
            oprot.writeString(elem0);
            
            oprot.writeFieldEnd();
          
            oprot.writeFieldBegin(new TField("skuNos", TType.LIST, (short) 2));
            java.util.List<String> elem1 = bean.getSkuNos();
            
          oprot.writeListBegin(new TList(TType.STRING, elem1.size()));
          for(String elem2 : elem1){
          oprot.writeString(elem2);
        }
          oprot.writeListEnd();
        
            
            oprot.writeFieldEnd();
          
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    
      public void validate(com.today.api.stock.request.CheckStoreStockRequest bean) throws TException{
      
              if(bean.getStoreId() == null)
              throw new SoaException(SoaCode.StructFieldNull, "storeId字段不允许为空");
            
              if(bean.getSkuNos() == null)
              throw new SoaException(SoaCode.StructFieldNull, "skuNos字段不允许为空");
            
    }
    
        @Override
        public String toString(com.today.api.stock.request.CheckStoreStockRequest bean)
        {return bean == null ? "null" : bean.toString();}
      }
      

      