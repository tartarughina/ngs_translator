package com.ngs_translator

import NetGraphAlgebraDefs.{NetGraph, NodeObject}
import com.google.common.graph.EndpointPair

import scala.jdk.CollectionConverters.*
import java.io.IOException
import org.json4s.*
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

import java.nio.file.{Files, Paths, StandardOpenOption}

object Main:
  private def endsWithSlash(s: String): String =
    if (s.endsWith("/")) s else s + "/"

  private def load(dir: String, filename: String): (List[MyNode], List[MyEdge]) =
    val graph: NetGraph = NetGraph.load(filename, endsWithSlash(dir)) match {
      case Some(value) =>
        value
      case None =>
        throw new IOException("Error reading graph")
    }

    (translateNode(graph.sm.nodes.asScala.toList), translateEdge(graph.sm.edges.asScala.toList, graph))

  private def translateEdge(edges: List[EndpointPair[NodeObject]], graph: NetGraph): List[MyEdge] =
    edges.map(edge => {
      val action = graph.sm.edgeValue(edge.source, edge.target).get

      MyEdge(edge.source.id, edge.target.id, action.cost, action.actionType, action.resultingValue.getOrElse(0))
    })

  private def translateNode(nodes: List[NodeObject]): List[MyNode] =
    nodes.map(node => MyNode(node.id, node.children, node.props, node.currentDepth, node.propValueRange, node.maxDepth, node.maxBranchingFactor, node.maxProperties, node.storedValue, node.valuableData))

  private def writeStringToFile(content: String, filepath: String): Unit =
    Files.write(Paths.get(filepath), content.getBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)

  private def getJsonName(filename: String): String =
    if (filename.contains("perturbed"))
      filename.substring(0, filename.indexOf('.')) + ".perturbed.json"
    else filename.substring(0, filename.indexOf('.')) + ".json"

  private def getDir(filename: String): String =
    try {
      filename.substring(0, filename.lastIndexOf('/'))
    } catch {
      case _: Throwable => {
        println("Error: filename must contain a directory, a relative or absolute path")
        sys.exit(2)}
    }

  private def getName(filename: String): String =
    filename.substring(filename.lastIndexOf('/') + 1, filename.length)

  private def persistJson(graph: MyGraph, filename: String): Unit =
    implicit val formats: Formats = DefaultFormats
    writeStringToFile(write(graph), s"${endsWithSlash(getDir(filename))}${getJsonName(getName(filename))}")

  def main(args: Array[String]): Unit =
    val (base, perturbed) = if (args.length == 2) {
      (args(0), args(1))
    } else {
      println("Example: ngs-translator /path/to/base.graph /path/to/perturbed.graph")
      sys.exit(1)
    }

    val (base_nodes, base_edges) = load(getDir(base), getName(base))
    val (perturbed_nodes, perturbed_edges) = load(getDir(perturbed), getName(perturbed))

    val base_graph = MyGraph(base_nodes, base_edges)
    val perturbed_graph = MyGraph(perturbed_nodes, perturbed_edges)

    persistJson(base_graph, base)
    persistJson(perturbed_graph, perturbed)