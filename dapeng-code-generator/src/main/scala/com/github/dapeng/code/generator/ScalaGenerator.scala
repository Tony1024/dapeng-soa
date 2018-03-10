package com.github.dapeng.code.generator

import java.io._
import java.util

import com.github.dapeng.core.metadata.DataType.KIND
import com.github.dapeng.core.metadata.TEnum.EnumItem
import com.github.dapeng.core.metadata._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.xml.Elem
import collection.JavaConverters._

/**
  * Scala生成器
  *
  * @author tangliu
  */
class ScalaGenerator extends CodeGenerator {

  override def generate(services: util.List[Service], outDir: String): Unit = {}

  private def rootDir(rootDir: String, packageName: String): File = {
    val dir = rootDir + "/scala/" + packageName.replaceAll("[.]", "/")

    val file = new File(dir)

    if(!file.exists())
      file.mkdirs()

    return file
  }

  protected def toFieldArrayBufferWithOptionBack(array: util.List[Field]): ArrayBuffer[Field] = {
    val newArray, optionalArray: ArrayBuffer[Field] = ArrayBuffer()

    for (index <- (0 until array.size())) {

      val field = array.get(index)
      if(field.isOptional)
        optionalArray += field
      else
        newArray += field
    }
    newArray.appendAll(optionalArray)
    newArray
  }

  private def resourceDir(rootDir: String, packageName: String): String = {
    val dir = rootDir + "/resources/"

    val file = new File(dir)

    if(!file.exists()){}
    file.mkdirs()

    dir
  }

  val notice: String = " * Autogenerated by Dapeng-Code-Generator (2.0.0)\n *\n * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING\n *  @generated\n"


  override def generate(services: util.List[Service], outDir: String, generateAll:Boolean , structs: util.List[Struct], enums:util.List[TEnum]): Unit = {

    val namespaces:util.Set[String] = new util.HashSet[String]()
    val structNamespaces:util.Set[String] = new util.HashSet[String]()

    val oriNamespaces = replaceNamespace(services,namespaces,structNamespaces)

    if(generateAll){
      println("=========================================================")
      toStructArrayBuffer(structs).map{(struct: Struct)=>{
        println(s"生成struct:${struct.namespace}.${struct.name}.scala")
        val domainTemplate = new StringTemplate(toDomainTemplate(struct))
        val domainWriter = new PrintWriter(new File(rootDir(outDir, struct.getNamespace), s"${struct.name}.scala"), "UTF-8")
        domainWriter.write(domainTemplate.toString)
        domainWriter.close()
        println(s"生成struct:${struct.namespace}.${struct.name}.scala 完成")

        println(s"生成serializer: ${struct.namespace+".serializer."}${struct.name}Serializer.scala")
        val structSerializerTemplate = new StringTemplate(new ScalaCodecGenerator().toStructSerializerTemplate(struct,structNamespaces))
        val structSerializerWriter = new PrintWriter(new File(rootDir(outDir, struct.namespace+".serializer."),s"${struct.name}Serializer.scala"), "UTF-8")
        structSerializerWriter.write(structSerializerTemplate.toString)
        structSerializerWriter.close()
        println(s"生成serializer:${struct.namespace+".serializer."}${struct.name}Serializer.scala 完成")
      }
      }

      toTEnumArrayBuffer(enums).map{(enum: TEnum)=>{

        println(s"生成Enum:${enum.name}.scala")
        val enumTemplate = new StringTemplate(toEnumTemplate(enum))
        val enumWriter = new PrintWriter(new File(rootDir(outDir, enum.getNamespace), s"${enum.name}.scala"), "UTF-8")
        enumWriter.write(enumTemplate.toString)
        enumWriter.close()
        println(s"生成Enum:${enum.name}.scala 完成")
      }
      }
      println("=========================================================")
    }

    for (index <- (0 until services.size())) {

      val service = services.get(index)
      val oriMethods = service.methods
      val nonVirtualMethods = service.methods.asScala.filter(i => {
        val annotations = i.annotations
        if (annotations != null) {
          val virtualAnnotations = annotations.asScala.filter(a => a.key.equals("virtual") && a.value.equals("true"))
          virtualAnnotations.isEmpty
        } else {
          true
        }
      })
      service.setMethods(nonVirtualMethods.asJava)

      val t1 = System.currentTimeMillis();
      println("=========================================================")
      println(s"服务名称:${service.name}")

      println(s"生成service:${service.name}.scala")
      val serviceTemplate = new StringTemplate(toServiceTemplate(service,oriNamespaces.get(service).getOrElse("")))
      val writer = new PrintWriter(new File(rootDir(outDir, service.getNamespace), s"${service.name}.scala"), "UTF-8")
      writer.write(serviceTemplate.toString())
      writer.close()
      println(s"生成service:${service.name}.scala 完成")

      println(s"生成AsyncService:${service.name}Async.scala")
      val asyncServiceTemplate = new StringTemplate(toAsyncServiceTemplate(service,oriNamespaces.get(service).getOrElse("")))
      val writer2 = new PrintWriter(new File(rootDir(outDir, service.getNamespace), s"${service.name}Async.scala"), "UTF-8")
      writer2.write(asyncServiceTemplate.toString())
      writer2.close()
      println(s"生成AsyncService:${service.name}Async.scala 完成")

      if(!generateAll){
        {
          toStructArrayBuffer(service.structDefinitions).map{(struct: Struct)=>{

            println(s"生成struct:${struct.namespace}.${struct.name}.scala")
            val domainTemplate = new StringTemplate(toDomainTemplate(struct))
            val domainWriter = new PrintWriter(new File(rootDir(outDir, struct.getNamespace), s"${struct.name}.scala"), "UTF-8")
            domainWriter.write(domainTemplate.toString)
            domainWriter.close()
            println(s"生成struct:${struct.namespace}${struct.name}.scala 完成")
          }
          }
        }

        {
          toTEnumArrayBuffer(service.enumDefinitions).map{(enum: TEnum)=>{

            println(s"生成Enum:${enum.name}.scala")
            val enumTemplate = new StringTemplate(toEnumTemplate(enum))
            val enumWriter = new PrintWriter(new File(rootDir(outDir, enum.getNamespace), s"${enum.name}.scala"), "UTF-8")
            enumWriter.write(enumTemplate.toString)
            enumWriter.close()
            println(s"生成Enum:${enum.name}.scala 完成")
          }
          }
        }
      }

      println(s"生成client:${service.name}Client.scala")
      val clientTemplate = new StringTemplate(toClientTemplate(service,oriNamespaces.get(service).getOrElse("")))
      val clientWriter = new PrintWriter(new File(rootDir(outDir, service.namespace.substring(0, service.namespace.lastIndexOf("."))), s"${service.name}Client.scala"), "UTF-8")
      clientWriter.write(clientTemplate.toString())
      clientWriter.close()
      println(s"生成client:${service.name}Client.scala 完成")

      println(s"生成client:${service.name}AsyncClient.scala")
      val asyncClientTemplate = new StringTemplate(toAsyncClientTemplate(service, oriNamespaces.get(service).getOrElse("")))
      val asyncClientWriter = new PrintWriter(new File(rootDir(outDir, service.namespace.substring(0, service.namespace.lastIndexOf("."))), s"${service.name}AsyncClient.scala"), "UTF-8")
      asyncClientWriter.write(asyncClientTemplate.toString())
      asyncClientWriter.close()
      println(s"生成client:${service.name}AsyncClient.scala 完成")

      println(s"生成Codec:${service.name}Codec.scala")
      val codecTemplate = new StringTemplate(new ScalaCodecGenerator().toCodecTemplate(service,structNamespaces, oriNamespaces.get(service).getOrElse("")))
      val codecWriter = new PrintWriter(new File(rootDir(outDir, service.namespace.substring(0, service.namespace.lastIndexOf("."))), s"${service.name}Codec.scala"), "UTF-8")
      codecWriter.write(codecTemplate.toString())
      codecWriter.close()
      println(s"生成Codec:${service.name}Codec.scala 完成")

      println(s"生成Codec:${service.name}AsyncCodec.scala")
      val asyncCodecTemplate = new StringTemplate(new ScalaCodecGenerator().toAsyncCodecTemplate(service,structNamespaces, oriNamespaces.get(service).getOrElse("")))
      val asyncCodecWriter = new PrintWriter(new File(rootDir(outDir, service.namespace.substring(0, service.namespace.lastIndexOf("."))), s"${service.name}AsyncCodec.scala"), "UTF-8")
      asyncCodecWriter.write(asyncCodecTemplate.toString())
      asyncCodecWriter.close()
      println(s"生成Codec:${service.name}AsyncCodec.scala 完成")


      //scala & java client should use the same xml
      service.setMethods(oriMethods)
      if (!service.namespace.contains("scala")) {
        println(s"生成metadata:${service.namespace}.${service.name}.xml")
        new MetadataGenerator().generateXmlFile(service, resourceDir(outDir, service.namespace.substring(0, service.namespace.lastIndexOf("."))));
        println(s"生成metadata:${service.namespace}.${service.name}.xml 完成")
      } else {
        val nonScalaNameSpace = service.namespace.replace(".scala","")
        val xmlFile = new File(resourceDir(outDir, nonScalaNameSpace.substring(0, nonScalaNameSpace.lastIndexOf("."))) + s"${nonScalaNameSpace}.${service.name}.xml")
        if (!xmlFile.exists()) {
          service.setNamespace(nonScalaNameSpace)
          new MetadataGenerator().generateXmlFile(service, resourceDir(outDir, nonScalaNameSpace.substring(0, nonScalaNameSpace.lastIndexOf("."))));
        } else {
          println(" skip *.scala.metadata.xml generate....")
        }
      }

      println("==========================================================")
      val t2 = System.currentTimeMillis();
      println(s"生成耗时:${t2 - t1}ms")
      println(s"生成状态:完成")

    }

  }

  private def replaceNamespace(services: util.List[Service],namespaces:util.Set[String],structNamespaces:util.Set[String]): Map[Service, String] ={
    val oriNameSpaces = mutable.HashMap[Service,String]()
    for (index <- (0 until services.size())) {
      val service = services.get(index)
      oriNameSpaces.put(service, service.namespace.replace(".scala",""))
      //like: com.dapeng.hello.service => com.dapeng.hello.scala.service
      service.setNamespace(service.getNamespace)
      namespaces.add(service.getNamespace)

      //設置method中字段的namespace
      for(methodIndex <-(0 until service.getMethods.size())){
        val methodDefinition = service.getMethods.get(methodIndex)
        for(reqFieldIndex <- (0 until methodDefinition.request.fields.size()) ){
          val fieldDefinition=methodDefinition.request.fields.get(reqFieldIndex)
          if (fieldDefinition.dataType != null) fieldDefinition.dataType.setQualifiedName(fieldDefinition.dataType.qualifiedName)

          if (fieldDefinition.dataType != null && fieldDefinition.dataType.valueType != null) {
            fieldDefinition.dataType.valueType.setQualifiedName(fieldDefinition.dataType.valueType.qualifiedName)
          }
        }

        for(respFieldIndex <- (0 until methodDefinition.response.fields.size()) ){
          val field2Definition=methodDefinition.response.fields.get(respFieldIndex)

          if (field2Definition.dataType != null) field2Definition.dataType.setQualifiedName(field2Definition.dataType.qualifiedName)

          if (field2Definition.dataType != null && field2Definition.dataType.valueType != null) {
            field2Definition.dataType.valueType.setQualifiedName(field2Definition.dataType.valueType.qualifiedName)
          }
        }

      }

      for(enumIndex <- (0 until service.getEnumDefinitions.size())) {
        val enumDefinition = service.getEnumDefinitions.get(enumIndex)
        enumDefinition.setNamespace(enumDefinition.namespace)
        namespaces.add(enumDefinition.getNamespace)
      }
      for(structIndex <- (0 until service.getStructDefinitions.size())) {
        val structDefinition = service.getStructDefinitions.get(structIndex)
        structDefinition.setNamespace(structDefinition.getNamespace)
        namespaces.add(structDefinition.getNamespace)
        structNamespaces.add(structDefinition.getNamespace)


        for(fieldIndex <- (0 until structDefinition.getFields.size())){
          val fieldDefinition = structDefinition.getFields.get(fieldIndex)

          if (fieldDefinition.dataType != null) fieldDefinition.dataType.setQualifiedName(fieldDefinition.dataType.qualifiedName)

          if (fieldDefinition.dataType != null && fieldDefinition.dataType.valueType != null) {
            fieldDefinition.dataType.valueType.setQualifiedName(fieldDefinition.dataType.valueType.qualifiedName)
          }
        }
      }
    }
    oriNameSpaces.toMap
  }
  private def toClientTemplate(service: Service, oriNamespace: String): Elem = {
    return {
      <div>package {service.namespace.substring(0, service.namespace.lastIndexOf("."))}

        import com.github.dapeng.core._;
        import com.github.dapeng.org.apache.thrift._;
        import java.util.ServiceLoader;
        import {service.namespace.substring(0, service.namespace.lastIndexOf(".")) + "." + service.name + "Codec._"};
        import {service.namespace.substring(0, service.namespace.lastIndexOf(".")) + ".service." + service.name };

        /**
        {notice}
        **/
        class {service.name}Client extends {service.name} <block>

        import java.util.function.<block> Function ⇒ JFunction, Predicate ⇒ JPredicate, BiPredicate </block>
        implicit def toJavaFunction[A, B](f: Function1[A, B]) = new JFunction[A, B] <block>
          override def apply(a: A): B = f(a)
        </block>

        val serviceName = "{oriNamespace + "." + service.name }"
        val version = "{service.meta.version}"
        val pool = <block>
          val serviceLoader = ServiceLoader.load(classOf[SoaConnectionPoolFactory])
          if (serviceLoader.iterator().hasNext) <block> serviceLoader.iterator().next().getPool </block> else null
        </block>
        val clientInfo = if (pool != null) pool.registerClientInfo(serviceName,version) else null

        def getServiceMetadata: String = <block>
          pool.send(
          serviceName,
          version,
          "getServiceMetadata",
          new getServiceMetadata_args,
          new GetServiceMetadata_argsSerializer,
          new GetServiceMetadata_resultSerializer
          ).success
        </block>


        {
        toMethodArrayBuffer(service.methods).map{(method:Method)=>{
          <div>
            /**
            * {method.doc}
            **/
            def {method.name}({toFieldArrayBuffer(method.getRequest.getFields).map{ (field: Field) =>{
            <div>{nameAsId(field.name)}:{toDataTypeTemplate(field.getDataType())} {if(field != method.getRequest.fields.get(method.getRequest.fields.size() - 1)) <span>,</span>}</div>}}}) : {toDataTypeTemplate(method.getResponse.getFields().get(0).getDataType)} = <block>

            val response = pool.send(
            serviceName,
            version,
            "{method.name}",
            {method.request.name}({
            toFieldArrayBuffer(method.getRequest.getFields).map{(field: Field)=>{
              <div>{nameAsId(field.name)}{if(field != method.getRequest.fields.get(method.getRequest.fields.size() - 1)) <span>,</span>}</div>
            }
            }}),
            new {method.request.name.charAt(0).toUpper + method.request.name.substring(1)}Serializer(),
            new {method.response.name.charAt(0).toUpper + method.response.name.substring(1)}Serializer())

            {if(method.getResponse.getFields.get(0).getDataType.kind != DataType.KIND.VOID) <div>response.success</div>}

          </block>
          </div>
        }
        }
        }
      </block>
      </div>
    }
  }


  private def toAsyncClientTemplate(service: Service, oriNamespace: String): Elem = {
    return {
      <div>package {service.namespace.substring(0, service.namespace.lastIndexOf("."))}

        import com.github.dapeng.core._;
        import com.github.dapeng.org.apache.thrift._;
        import java.util.ServiceLoader;
        import java.util.concurrent.CompletableFuture;
        import {service.namespace.substring(0, service.namespace.lastIndexOf(".")) + "." + service.name + "AsyncCodec._"};
        import {service.namespace.substring(0, service.namespace.lastIndexOf(".")) + ".service." + service.name }Async;
        import scala.concurrent.duration._
        import scala.concurrent.<block>Future, Promise</block>
        import scala.concurrent.ExecutionContext.Implicits.global

        /**
        {notice}
        **/
        class {service.name}AsyncClient extends {service.name}Async <block>

        val serviceName = "{oriNamespace + "." + service.name }"
        val version = "{service.meta.version}"
        val pool = <block>
          val serviceLoader = ServiceLoader.load(classOf[SoaConnectionPoolFactory])
          if (serviceLoader.iterator().hasNext) <block> serviceLoader.iterator().next().getPool </block> else null
        </block>
        val clientInfo = if (pool != null) pool.registerClientInfo(serviceName,version) else null

        def getServiceMetadata: String = <block>
          pool.send(
          serviceName,
          version,
          "getServiceMetadata",
          new getServiceMetadata_args,
          new GetServiceMetadata_argsSerializer,
          new GetServiceMetadata_resultSerializer
          ).success
        </block>

        /**
        *  java CompletableFuture => scala Future common function
        */
        def toScala[T,R](response: CompletableFuture[T])(extractor: T => R): Future[R] = <block>

          val promise = Promise[R]()
          response.whenComplete((res: T, ex) => <block>
            if (ex != null) promise.failure(ex)
            else promise.success(extractor(res))
          </block>)
          promise.future
        </block>


        {
        toMethodArrayBuffer(service.methods).map{(method:Method)=>{
          <div>

            /**
            * {method.doc}
            **/
            def {method.name}({toFieldArrayBuffer(method.getRequest.getFields).map{ (field: Field) =>{
            <div>{nameAsId(field.name)}:{toDataTypeTemplate(field.getDataType())} {if(field != method.getRequest.fields.get(method.getRequest.fields.size() - 1)) <span>,</span>}</div>}}}) : Future[{toDataTypeTemplate(method.getResponse.getFields().get(0).getDataType)}] = <block>

            val response = pool.sendAsync(
            serviceName,
            version,
            "{method.name}",
            {method.request.name}({
            toFieldArrayBuffer(method.getRequest.getFields).map{(field: Field)=>{
              <div>{nameAsId(field.name)}{if(field != method.getRequest.fields.get(method.getRequest.fields.size() - 1)) <span>,</span>}</div>
            }
            }}),
            new {method.request.name.charAt(0).toUpper + method.request.name.substring(1)}Serializer(),
            new {method.response.name.charAt(0).toUpper + method.response.name.substring(1)}Serializer() ).asInstanceOf[CompletableFuture[{method.response.name}]]

            {if(method.getResponse.getFields.get(0).getDataType.kind != DataType.KIND.VOID) <div>toScala(response)(_.success)</div> else <div>toScala(response)(null)</div>}

          </block>

          </div>
        }
        }
        }
      </block>
      </div>
    }
  }

  private def toEnumTemplate(enum: TEnum): Elem = return {
    <div>package {enum.namespace};

      class {enum.name} private(val id: Int, val name: String) extends com.github.dapeng.core.enums.TEnum(id,name) <block>{}</block>

      /**
      {notice}
      * {enum.doc}
      **/
      object {enum.name} <block>

      {
      toEnumItemArrayBuffer(enum.enumItems).map{(enumItem: EnumItem)=>{
        if(enumItem.doc != null)
          <div>
            val {enumItem.label} = new {enum.name}({enumItem.value}, "{enumItem.doc.trim.replace("*","")}")
          </div>
        else
          <div>
            val {enumItem.label} = new {enum.name}({enumItem.value},"")
          </div>
      }
      }
      }
      <div>val UNDEFINED = new {enum.name}(-1,"UNDEFINED") // undefined enum
      </div>

      def findByValue(v: Int): {enum.name} = <block>
        v match <block>
          {toEnumItemArrayBuffer(enum.enumItems).map { (enumItem: EnumItem) => {
            <div>case {enumItem.value} => {enumItem.label}
            </div>
          }
          }}
          case _ => new {enum.name}(v,"#"+ v)
        </block>
      </block>

      def apply(v: Int) = findByValue(v)
      def unapply(v: {enum.name}): Option[Int] = Some(v.id)

    </block>
    </div>
  }

  private def toDomainTemplate(struct: Struct): Elem = {
    return {

      var index = 0

      <div>package {struct.namespace}

        import com.github.dapeng.core.BeanSerializer
        object {struct.name} <block>
          implicit val x: BeanSerializer[{struct.namespace}.{struct.name}] = new {struct.namespace}.serializer.{struct.name}Serializer
      </block>

        /**
        {notice}
        *{struct.doc}
        **/
        case class {struct.name}(

        {toFieldArrayBufferWithOptionBack(struct.getFields).map{(field : Field) =>{<div> /**
        *{field.doc}
        **/
        {index = index + 1}
        {nameAsId(field.name)} : {if(field.isOptional) <div>Option[</div>}{toDataTypeTemplate(field.getDataType)}{if(field.isOptional) <div>] = None</div>}{if(index < struct.getFields.size) <span>,</span>}</div>}}}
        )
      </div>
    }
  }

  val keywords = Set("type") // TODO is there any other keyword need to be escape
  def nameAsId(name: String) = if(keywords contains name) s"`$name`" else name

  private def toServiceTemplate(service:Service, oriNamespace: String): Elem = {
    return {
      <div>
        package {service.namespace}

        import com.github.dapeng.core.<block>Processor, Service</block>
        import com.github.dapeng.core.SoaGlobalTransactional

        /**
        {notice}
        * {service.doc}
        **/
        @Service(name ="{oriNamespace+"."+service.name}" , version = "{service.meta.version}")
        @Processor(className = "{service.namespace.substring(0, service.namespace.lastIndexOf("service"))}{service.name}Codec$Processor")
        trait {service.name} <block>
        {
        toMethodArrayBuffer(service.methods).map { (method: Method) =>
        {
          <div>
            /**
            * {method.doc}
            **/
            {if(method.doc != null && method.doc.contains("@SoaGlobalTransactional")) <div>@SoaGlobalTransactional</div>}
            <div>@throws[com.github.dapeng.core.SoaException]</div>
            def {method.name}(
            {toFieldArrayBuffer(method.getRequest.getFields).map{ (field: Field) =>{
            <div>{nameAsId(field.name)}: {toDataTypeTemplate(field.getDataType())} {if(field != method.getRequest.fields.get(method.getRequest.fields.size() - 1)) <span>,</span>}</div>}
          }
            }): {toDataTypeTemplate(method.getResponse.getFields().get(0).getDataType)}

          </div>

        }
        }
        }
      </block>
      </div>
    }
  }


  private def toAsyncServiceTemplate(service:Service, oriNamespace: String): Elem = return {
    <div>
      package {service.namespace}

      import com.github.dapeng.core.<block>Processor, Service</block>
      import com.github.dapeng.core.SoaGlobalTransactional
      import scala.concurrent.Future

      /**
      {notice}
      * {service.doc}
      **/
      @Service(name ="{oriNamespace+"."+service.name}" , version = "{service.meta.version}")
      @Processor(className = "{service.namespace.substring(0, service.namespace.lastIndexOf("service"))}{service.name}AsyncCodec$Processor")
      trait {service.name}Async extends com.github.dapeng.core.definition.AsyncService <block>
      {
      toMethodArrayBuffer(service.methods).map { (method: Method) =>
      {
        <div>
          /**
          * {method.doc}
          **/
          {if(method.doc != null && method.doc.contains("@SoaGlobalTransactional")) <div>@SoaGlobalTransactional</div>}
          <div>@throws[com.github.dapeng.core.SoaException]</div>
          def {method.name}(
          {toFieldArrayBuffer(method.getRequest.getFields).map{ (field: Field) =>{
          <div>{nameAsId(field.name)}: {toDataTypeTemplate(field.getDataType())} {if(field != method.getRequest.fields.get(method.getRequest.fields.size() - 1)) <span>,</span>}</div>}
        }}): Future[{if(method.getResponse.getFields().get(0).getDataType.kind.equals(KIND.VOID)) <div>Unit</div> else toDataTypeTemplate(method.getResponse.getFields().get(0).getDataType)}]

        </div>
      }
      }
      }
    </block>
    </div>
  }


  /**
    * idl解析类型转为scala对应类型
    *
    * @param dataType
    * @return
    */
  def toDataTypeTemplate(dataType:DataType): Elem = {
    dataType.kind match {
      case KIND.VOID => <div>Unit</div>
      case KIND.BOOLEAN => <div>Boolean</div>
      case KIND.BYTE => <div>Byte</div>
      case KIND.SHORT => <div>Short</div>
      case KIND.INTEGER => <div>Int</div>
      case KIND.LONG => <div>Long</div>
      case KIND.DOUBLE => <div>Double</div>
      case KIND.STRING => <div>String</div>
      case KIND.BINARY => <div>java.nio.ByteBuffer</div>
      case KIND.DATE => <div>java.util.Date</div>
      case KIND.BIGDECIMAL => <div>BigDecimal</div>
      case KIND.MAP =>
        return {<div>Map[{toDataTypeTemplate(dataType.getKeyType())}, {toDataTypeTemplate(dataType.getValueType())}]</div>}
      case KIND.LIST =>
        return {<div>List[{toDataTypeTemplate(dataType.getValueType())}]</div>}
      case KIND.SET =>
        return {<div>Set[{toDataTypeTemplate(dataType.getValueType())}]</div>}
      case KIND.ENUM =>
        return {<div>{dataType.getQualifiedName}</div>}
      case KIND.STRUCT =>
        return {<div>{dataType.getQualifiedName}</div>}
    }
  }

  def getToStringElement(field: Field): Elem = {
    <div>stringBuilder.append("\"").append("{nameAsId(field.name)}").append("\":{if(field.dataType.kind == DataType.KIND.STRING) <div>\"</div>}").append({getToStringByDataType(field)}).append("{if(field.dataType.kind == DataType.KIND.STRING) <div>\"</div>},");
    </div>
  }

  def getToStringByDataType(field: Field):Elem = {

    if(field.getDoc != null && field.getDoc.toLowerCase.contains("@logger(level=\"off\")"))
      <div>"LOGGER_LEVEL_OFF"</div>
    else if(field.isOptional)
      <div>this.{nameAsId(field.name)}.isPresent()?this.{nameAsId(field.name)}.get(){if(field.dataType.kind == KIND.STRUCT) <div>.toString()</div>}:null</div>
    else
      <div>this.{nameAsId(field.name)}{if(field.dataType.kind == KIND.STRUCT) <div>.toString()</div>}</div>
  }
}