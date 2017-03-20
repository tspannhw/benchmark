package org.ward


import java.io.{BufferedWriter, FileWriter}

import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.broadcast.Broadcast
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

import util.Random
import java.lang.System.{currentTimeMillis => _time}

/**
 * Hello world!
 *
 */
object Benchmark {
  def main(args: Array[String]){
    val conf = new SparkConf().setAppName("Benchmark").set("spark.hadoop.dfs.replication", "1")
    val sc = new SparkContext(conf)

    sc.hadoopConfiguration.set("mapred.output.compress", "false")

    def profile[R](code: => R, t: Long = _time) = (code, _time - t)

    // This is the output file for statistics
    val statFile = new BufferedWriter(new FileWriter("Benchmark_write.stat"))

    val fs = FileSystem.get(new Configuration(true))

    fs.delete(new Path("foo"), true)

    val a = sc.parallelize(1 until 121, 120)

    val b = a.map( i => {
      "0" * 50000000
    })

    // force calculation
    b.count()

    val (junk, timeW) = profile {
      b.saveAsObjectFile("foo")
    }

    fs.delete(new Path("foo"), true)

    // Write statistics
    statFile.write("\nMilliseconds for writing: " + timeW)
    statFile.close()


  }
}
