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

  private def load(dir: String, filename: String): (List[NodeObject], List[EndpointPair[NodeObject]]) =
    val graph: NetGraph = NetGraph.load(filename, endsWithSlash(dir)) match {
      case Some(value) =>
        value
      case None =>
        throw new IOException("Error reading graph")
    }

    (graph.sm.nodes.asScala.toList, graph.sm.edges.asScala.toList)

  private def translateEdge(edges: List[EndpointPair[NodeObject]]): List[MyEdge] =
    edges.map(edge => MyEdge(edge.source.id, edge.target.id, edge.target.storedValue))

  private def translateNode(nodes: List[NodeObject]): List[MyNode] =
    nodes.map(node => MyNode(node.id, node.children, node.props, node.currentDepth, node.propValueRange, node.maxDepth, node.maxBranchingFactor, node.maxProperties, node.storedValue, node.valuableData))

  private def writeStringToFile(content: String, filepath: String): Unit =
    Files.write(Paths.get(filepath), content.getBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)

  def main(args: Array[String]): Unit =
    val (dir, base, perturbed) = if (args.length == 3) {
      println("Usage: ngs-translator <dir> <base graph> <perturbed graph>")
      (args(0), args(1), args(2))
    }
    else {
      ("./input", "honeypot.ngs", "honeypot.ngs.perturbed")
    }

    val (base_nodes, base_edges) = load(dir, base)
    val (perturbed_nodes, perturbed_edges) = load(dir, perturbed)

    implicit val formats: Formats = DefaultFormats

    val base_graph = MyGraph(translateNode(base_nodes), translateEdge(base_edges))
    val perturbed_graph = MyGraph(translateNode(perturbed_nodes), translateEdge(perturbed_edges))

    val dir_out = "./output"
    val base_out = "honeypot.json"
    val perturbed_out = "honeypot.perturbed.json"

    println(write(perturbed_graph))

    writeStringToFile(write(base_graph), s"${endsWithSlash(dir_out)}$base_out")
    writeStringToFile(write(perturbed_graph), s"${endsWithSlash(dir_out)}$perturbed_out")