package com.gettyimages.spray.swagger

import scala.reflect.runtime.universe._
import scala.collection.immutable.ListMap
import scala.Option.option2Iterable

object ReflectionUtils {

  private def getSymbolAnnotation[K: TypeTag](baseTypeSymbol: Symbol): Option[Annotation] = {
    val annotations = baseTypeSymbol.annotations
    val annotationType = typeOf[K]
    annotations.find(_.tpe =:= annotationType)
  }
  
  def getClassAnnotation[T <: AnyRef: TypeTag, K: TypeTag](): Option[Annotation] = {
    getSymbolAnnotation[K](typeOf[T].typeSymbol.asClass)
  }
  
  def getClassAnnotation[K: TypeTag](objectType: Type): Option[Annotation] = {
    getSymbolAnnotation[K](objectType.typeSymbol.asClass)
  }
  
  def getAllMethodAnnotations[K: TypeTag](objectType: Type): Seq[(Annotation, TermSymbol)] = 
    getAllDeclarationAnnotations[K](objectType, _.asTerm.isMethod, _.asTerm)
  
  def getAllFieldAnnotations[K: TypeTag](objectType: Type): Seq[(Annotation, TermSymbol)] = 
    getAllDeclarationAnnotations[K](objectType, _.asTerm.isAccessor, _.asTerm.accessed.asTerm)
  
  private def getAllDeclarationAnnotations[K: TypeTag]
  (objectType: Type, filterFunc: Symbol => Boolean, mapFunc: Symbol => TermSymbol): 
  Seq[(Annotation, TermSymbol)] = {
    objectType.declarations.filter(filterFunc).map(mapFunc).flatMap(symbol => {
      getSymbolAnnotation[K](symbol).map(a => (a, symbol))
    }).toSeq
  }
  
  def getMethodAnnotation[K: TypeTag](objectType: Type)(name: String): Option[Annotation] = {
    getSymbolAnnotation[K](objectType.declaration(name: TermName).asTerm)
  }
  
  def getFieldAnnotation[T: TypeTag, K: TypeTag](name: String): Option[Annotation] = {
    val declarationType = typeOf[T].declaration(name: TermName).asTerm.accessed.asTerm
    getSymbolAnnotation[K](declarationType)
  }
 
  def getFieldAnnotation[K: TypeTag](objectType: Type)(name: String): Option[Annotation] = {
    val declarationType = objectType.declaration(name: TermName).asTerm.accessed.asTerm
    getSymbolAnnotation[K](declarationType)
  }
  
  def getType[T: TypeTag](obj: T) = typeOf[T]
  
  def getType[T: TypeTag](clazz: Class[T]) = typeOf[T]
  
  def getTypeTag[T: TypeTag](obj: T) = typeTag[T]
  
  private def getLiteralJavaAnnotation[T: TypeTag](
    name: String, annotationValues: ListMap[Name, JavaArgument]
  ): Option[T] = annotationValues(name: TermName) match {
    case (LiteralArgument(Constant(x))) => Some(x.asInstanceOf[T])
    case x                              => None
  }
  private def getArrayJavaAnnotation(
    name: String, annotationValues: ListMap[Name, JavaArgument]
  ): Option[Array[Annotation]] = annotationValues(name: TermName) match {
    case (ArrayArgument(arr)) => Some(arr.map( _ match {
      case NestedArgument(ann) => ann
    }))
    case x 					  => None	
  }
  
  def getStringJavaAnnotation(name: String, annotation: Annotation): Option[String] = {
    getLiteralJavaAnnotation(name, annotation.javaArgs)
  } 
  def getIntJavaAnnotation(name: String, annotation: Annotation): Option[Int] = 
    getLiteralJavaAnnotation(name, annotation.javaArgs)
    
  def getBooleanJavaAnnotation(name: String, annotation: Annotation): Option[Boolean] = 
    getLiteralJavaAnnotation(name, annotation.javaArgs)
    
  def getArrayJavaAnnotation(name: String, annotation: Annotation): Option[Array[Annotation]] =
    getArrayJavaAnnotation(name, annotation.javaArgs)
}