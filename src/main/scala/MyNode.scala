package com.ngs_translator

case class MyNode(id: Int, children: Int, props: Int, currentDepth: Int = 1, propValueRange:Int, maxDepth:Int,
                  maxBranchingFactor:Int, maxProperties:Int, storedValue: Double, valuableData: Boolean = false)
